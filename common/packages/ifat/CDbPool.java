/************************************************************
 *                                                          *
 *  Contents of file Copyright (c) Moogsoft Inc 2010        *
 *                                                          *
 *----------------------------------------------------------*
 *                                                          *
 *  WARNING:                                                *
 *  THIS FILE CONTAINS UNPUBLISHED PROPRIETARY              *
 *  SOURCE CODE WHICH IS THE PROPERTY OF MOOGSOFT INC AND   *
 *  WHOLLY OWNED SUBSIDIARY COMPANIES.                      *
 *  PLEASE READ THE FOLLOWING AND TAKE CAREFUL NOTE:        *
 *                                                          *
 *  This source code is confidential and any person who     *
 *  receives a copy of it, or believes that they are viewing*
 *  it without permission is asked to notify Phil Tee       *
 *  on 07734 591962 or email to phil@moogsoft.com.          *
 *  All intellectual property rights in this source code    *
 *  are owned by Moogsoft Inc.  No part of this source      *
 *  code may be reproduced, adapted or transmitted in any   *
 *  form or by any means, electronic, mechanical,           *
 *  photocopying, recording or otherwise.                   *
 *                                                          *
 *  You have been warned....so be good for goodness sake... *
 *                                                          *
 ************************************************************/
package com.moogsoft.ifat;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

//
// The connection pool class
//
public class CDbPool
{
    //
    // The SQL state used for an exception when a connection
    // cannot be obtained is the same as the SQL state of
    // an invalid connection.
    //
    // e.g. Communication link failure (SQL State: 08S01).
    // For MySQL these are the relevant SQL state error codes:
    //
    // Error: 1042 SQLSTATE: 08S01 (ER_BAD_HOST_ERROR)
    // Message: Can't get hostname for your address
    //
    // Error: 1043 SQLSTATE: 08S01 (ER_HANDSHAKE_ERROR)
    // Message: Bad handshake
    //
    // This is public as it is also used by CHttpResponseUtils.
    //
    public static final String MYSQL_DOWN_SQL_STATE_CODE = "08S01";

    private static final int RECONNECT_DELAY_MS = 0;
    private static final int MAX_RECONNECT_TRY = 1;
    private static final int IS_VALID_TIMEOUT = 1;
    
    private static final boolean DEFAULT_AUTO_COMMIT = true;

    //
    // Map of pooled connections to a boolean.  The boolean indicates whether
    // connection has been checked out or not.
    // 
    private HashMap<Connection,Boolean> mConnections = new HashMap<Connection,Boolean>();
    private ReentrantLock               mLock = new ReentrantLock();
    private Condition                   mCond = mLock.newCondition();
    private IDbFactory                  mDbFactory = null;
    private int                         mSize = 0;

    // Indicates that the pool is being shutdown.
    private boolean                     mIsClosingDown = false;
    
    //
    // Default constructor, use default factory.
    //
    public CDbPool(int size)
            throws SQLException,ClassNotFoundException
    {
        this(size, null);
    }
    
    //
    // Constructor with custom factory
    //
    public CDbPool(int size, IDbFactory factory)
            throws SQLException,ClassNotFoundException
    {
        if(factory == null)
        {
            factory = new CDbFactory();
        }
        mDbFactory = factory;

        if(size<1)
        {
            CLogger.logger().warning("Pool size can not be set to [%d], please set it to 1 or more. Pool size was set to 1.", size);
            size = 1;
        }
        
        //
        // Initialize connections
        //
        mSize = size;
        for(int i = 0;i < mSize; i++)
        {
            Connection con = this.addConnection(false);

            //
            // If a connection cannot be created then break from the for loop as
            // the connections will be created on checkOut if need be.
            //
            if (con == null)
            {
                break;
            }
        }

        //
        // If the pool of connections is empty then throw a SQLException.
        // Some classes (like CExternalDbManager) expect an exception to be
        // thrown on construction of the pool if there are connection problems.
        // In the old version of this class if a single connection failed to be
        // created at initialization then it would fail.  This new version will
        // only fail if the connection pool is empty after construction.
        //
        if (mConnections.size() == 0)
        {
            //
            // The SQL state is needed so that CHttpResponseUtils handles
            // it correctly.
            //
            throw new SQLException("Db pool could not be initialized", MYSQL_DOWN_SQL_STATE_CODE);
        }

        CLogger.logger().info("Db pool initiliased with %s/%s connections", mConnections.size(), mSize); 
    }

    //
    // Pull a connection from the pool
    //
    public Connection checkOut() 
            throws SQLException
    {
        Connection result = null;
        
        //
        // loop until success
        //
        boolean done = false;
        int retry = 0;

        //
        // Don't do this if we're closing down the pool or
        // we have tried to reconnect MAX_RECONNECT_TRY times.
        //
        while(!mIsClosingDown &&
              !done &&
              (retry < MAX_RECONNECT_TRY))
        {
            //
            // Grab a lock to prevent overwriting
            // access to shared instance variables
            //
            try
            {
                //
                // Grab the lock
                // 
                mLock.lock();

                //
                // And the next free connection
                //
                result = this.nextFreeConnection();
                
                //
                // No available connection in the pool
                //
                if(result == null)
                {
                    //
                    // If we're missing connections, try to build one now
                    //
                    if(mSize > mConnections.size())
                    {
                        //
                        // Increment the retry counter as we are attempting
                        // to create a new connection.
                        //
                        retry++;

                        result = addConnection(true);
                    }
                }
                
                //
                // Did I get a connection
                //
                if(result == null)
                {
                    //
                    // Wait to be signaled by a checkout, only if we actually have any connections
                    //
                    if(!mConnections.isEmpty())
                    {
                        mCond.await();
                    }
                }
                else
                {
                    //
                    // Now test this connection for validity
                    //
                    if(result.isValid(IS_VALID_TIMEOUT) != true)
                    {
                        //
                        // Replace this connection
                        //
                        mConnections.remove(result);

                        //
                        // Increment the retry counter as we are attempting
                        // to create a new connection.
                        //
                        retry++;

                        //
                        // Create a new connection
                        //
                        result = this.addConnection(true);
                    }

                    //
                    // If we have a connection at this stage then all is good
                    //
                    if (result != null)
                    {
                        done = true;
                    }
                }
            }
            catch(InterruptedException e)
            {
                //
                // NOP, mutex is unlocked in finally
                // code, and we re-enter the loop
                //
            }
            finally
            {
                mLock.unlock();
            }
            
            //
            // If we couldn't get a connection, sleep outside the lock, and retry
            // if we have not reached the retry limit.
            //
            if (!done)
            {
                try
                {
                    //
                    // If the retry is still under the MAX_RECONNECT_TRY
                    // then sleep before retrying.
                    //
                    if (retry < MAX_RECONNECT_TRY)
                    {
                        CLogger.logger().info("Waiting %d before reconnect db attempt", RECONNECT_DELAY_MS);
                        Thread.sleep(RECONNECT_DELAY_MS);
                    }
                }
                catch (InterruptedException e)
                {
                    // NOP, just re-enter the loop
                }
            }
        }

        if (result == null)
        {
            //
            // The SQL state is needed so that CHttpResponseUtils handles
            // it correctly.
            //
            throw new SQLException("Unable to obtain DB connection from pool", MYSQL_DOWN_SQL_STATE_CODE);
        }

        return result;  
    }

    //
    // Pull a connection from the pool, and set the autocommit state to false
    // Call rollbackAndCheckIn to reset the autocommit flag and check the
    // connection back.
    //
    public Connection checkOutForTransaction() 
            throws SQLException
    {
        //
        // Get the connection
        //
        Connection connection = this.checkOut();
        
        try
        {
            //
            // Set the connection autocommit state
            //
            connection.setAutoCommit(false);
        }
        catch (SQLException e)
        {
            //
            // Error while setting autocommit, check in the connection and
            // throw the error
            //
            CLogger.logger().logThrowable("SQL Exception: %s ", e);
            this.checkIn(connection);
            connection = null;
            throw e;
        }
        return connection;
    }

    //
    // Check in a used connection
    //
    public void checkIn(Connection cn)
    {
        if (cn != null)
        {
            try
            {
                //
                // Grab the lock
                //
                mLock.lock();
    
                //
                // Pop in the checked out state
                //
                mConnections.put(cn,false);
        
                //
                // And go home
                //
                mCond.signal();
            }
            finally
            {
                //
                // Make sure we release the lock
                //
                mLock.unlock();
            }
        }
    }

    //
    // Roll back any uncommited changes, set the autocommit mode back to the
    // default mode and check in a used connection.
    //
    public void rollbackAndCheckIn(Connection connection)
    {
        if(connection != null)
        {
            try
            {
                //
                // Check the autocommit state
                //
                boolean autoCommitState = connection.getAutoCommit();
                
                //
                // Roll back any uncommited changes
                //
                if(!autoCommitState)
                {
                    connection.rollback();
                }
                
                //
                // Reset the autocommit state
                //
                if(autoCommitState != DEFAULT_AUTO_COMMIT)
                {
                    connection.setAutoCommit(DEFAULT_AUTO_COMMIT);
                }
            }
            catch (SQLException e)
            {
                CLogger.logger().logThrowable("SQL Exception: %s ", e);
            }
        }
        
        //
        // Check in the connection
        //
        this.checkIn(connection);
    }
    
    //
    // Close the connections associated with a DB pool.
    // This is intended to be called at shutdown time, and for thorough clean-up
    // only.  It is not intended to be called mid-execution.
    // The primary motivation is for classes where repeated instantiation as
    // part of testcases can create "too many connection errors"
    //
    public void close()
    {
        // Set flag to indicate that we're closing down - no further connections
        // Can be checked out after this is set.
        mIsClosingDown = true;

        boolean done = mConnections.isEmpty();

        while (!done)
        {
            //
            // Grab a lock to prevent overwriting
            // access to shared instance variables
            //
            Connection connection = null;
            try
            {
                mLock.lock();

                while (connection == null)
                {
                    // Get the next free connection
                    connection = this.nextFreeConnection();

                    // Did I get a connection
                    if (connection == null)
                    {
                        if(!mConnections.isEmpty())
                        {
                            // Wait until one is put back in the pool
                            mCond.await();
                        }
                        else
                        {
                            break;
                        }
                    }
                }
            }
            catch(InterruptedException e)
            {
                // NOP, just re-enter the loop
            }
            finally
            {
                if (connection != null)
                {
                    // Close the connection
                    try
                    {
                        connection.close();
                    }
                    catch (SQLException e)
                    {
                        // Ignore close exception
                    }

                    // Remove it from the pool...
                    mConnections.remove(connection);
                }
                
                // Check whether we are finished.
                done = mConnections.isEmpty();

                mLock.unlock();
            }
        }
    }

    //
    // Convenience getter for the deadlock max retries.
    // Obtained from the underlying factory.
    //
    public int getDeadlockMaxRetries()
    {
        return mDbFactory.getDeadlockMaxRetries();
    }

    //
    // Convenience getter for the deadlock retry wait.
    // Obtained from the underlying factory.
    //
    public int getDeadlockRetryWait()
    {
        return mDbFactory.getDeadlockRetryWait();
    }
    
    //
    // Private non-thread safe method which scans for
    // next available connection.  Returns null if no
    // connection is available.
    //
    private Connection nextFreeConnection()
    {
        Connection result = null;
        
        for (Map.Entry<Connection, Boolean> entry : mConnections.entrySet())
        {
            if (entry.getValue() == false)
            {
                result = entry.getKey();
                
                mConnections.put(result, true);
                
                break;
            }
        }

        return result;
    }
    
    //
    // Add a connection to the pool.  This method is not thread safe and
    // should be called with a method that has the appropriate locks.
    //
    private Connection addConnection(boolean checkout)
    {
        CLogger.logger().info("Adding connection %d to db pool", mConnections.size());
        
        Connection con = null;
        try
        {
            //
            // Build new connection 
            //
            con = mDbFactory.getConnection();
            
            if (con != null)
            {
                //
                // Add the new connection to the pool
                //
                mConnections.put(con, checkout);
            }
            else
            {
                CLogger.logger().warning("Could not add SQL connection to db pool due to: null connection");
            }
        }
        catch (SQLException ex)
        {
            CLogger.logger().warning("Could not add SQL connection to db pool due to: %s", ex.getMessage());
        }

        return con;
    }
    
}
