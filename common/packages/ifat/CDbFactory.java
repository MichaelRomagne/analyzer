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
//
// Package definition
//
package com.moogsoft.ifat;

//
// Import statements
//
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class CDbFactory implements IDbFactory
{
	private static final String    DEFAULT_HOST="localhost";
	private static final int       DEFAULT_PORT=3306;
	private static final String    DEFAULT_DATABASE="moogdb";
    private static final String    DEFAULT_USER="ermintrude";
    private static final String    DEFAULT_PASSWORD="m00";
    private static final int       DEFAULT_DEADLOCK_RETRIES=5;
    private static final int       DEFAULT_DEADLOCK_RETRY_WAIT=10;

    private static final String    DRIVER="com.mysql.jdbc.Driver";
    private static final String    URL_START="jdbc:mysql://";
    private static final String URL_OPTS="?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&connectTimeout=5000";
    
    private CSystemParams mSystemParams = null;
    private String mHost = null;
    private int mPort = -1;
    private String mDb = null;
    private String mUser = null;
    private String mPwd = null;
    private String mUrl = null;
    private int mDeadlockMaxRetries = DEFAULT_DEADLOCK_RETRIES;
    private int mDeadlockRetryWait = DEFAULT_DEADLOCK_RETRY_WAIT;

    //
    // Constructors
    //
    public CDbFactory( ) throws ClassNotFoundException
    {
    	mSystemParams = new CSystemParams();
    	
    	//
        // Load the JDBC driver
        //
        Class.forName( DRIVER );
    	
        //
        // Get connection details
        //
        Map<String, Object> mysqlData = mSystemParams.getMysqlData();
        if (mysqlData != null)
        {
        	//
        	// Get Host
        	//
            try
            {
                mHost = (String) mysqlData.get("host");
            }
            catch (ClassCastException e)
            {
                CLogger.logger().warning(e.getMessage());
            }

        	//
        	// Get Port
        	//
            try
            {
                Long longPort = (Long) mysqlData.get("port");
                if (longPort != null)
                {
                	mPort = longPort.intValue();
                }
            }
            catch (ClassCastException e)
            {
                CLogger.logger().warning(e.getMessage());
            }
            
        	//
        	// Get Database
        	//
            try
            {
                mDb = (String) mysqlData.get("database");
            }
            catch (ClassCastException e)
            {
                CLogger.logger().warning(e.getMessage());
            }
            
        	//
        	// Get Username
        	//
            try
            {
                mUser = (String) mysqlData.get("username");
            }
            catch (ClassCastException e)
            {
                CLogger.logger().warning(e.getMessage());
            }
            
        	//
        	// Get Password
        	//
            try
            {
                mPwd = (String) mysqlData.get("password");
            }
            catch (ClassCastException e)
            {
                CLogger.logger().warning(e.getMessage());
            }

            // MOOG-551 While we're at it get the max retries and retry wait for dealing
            // with deadlocks.
            
            //
        	// Get max retries
        	//
            try
            {
                Long retries = (Long) mysqlData.get("maxRetries");
                if (retries != null)
                {
                	mDeadlockMaxRetries = retries.intValue();
                }
            }
            catch (ClassCastException e)
            {
                CLogger.logger().warning(e.getMessage());
            }
            CLogger.logger().info("Max deadlock retries set to %d.", mDeadlockMaxRetries);

            //
        	// Get retry wait
        	//
            try
            {
                Long wait = (Long) mysqlData.get("retryWait");
                if (wait != null)
                {
                	mDeadlockRetryWait = wait.intValue();
                }
            }
            catch (ClassCastException e)
            {
                CLogger.logger().warning(e.getMessage());
            }
            CLogger.logger().info("Wait between deadlock retries set to %d milliseconds.", mDeadlockRetryWait);

        }
        
        //
        // Set defaults if any values have not been picked up
        //
        if (mHost == null || mHost.length()==0)
        {
			//Use hardcoded default
            mHost = DEFAULT_HOST;
        }
        if (mPort == -1)
        {
			//Use hardcoded default
			mPort = DEFAULT_PORT;
		}
        if (mDb == null || mDb.length()==0)
		{
			//Use hardcoded default
			mDb = DEFAULT_DATABASE;
		}
        if (mUser == null || mUser.length()==0)
        {
			//Use hardcoded default
			mUser = DEFAULT_USER;
		}
		if (mPwd == null || mPwd.length()==0)
		{
			//Use hardcoded default
			mPwd = DEFAULT_PASSWORD;
		}
		
		mUrl = this.buildUrl( mDb );
		CLogger.logger().info( "Database url: [%s]", mUrl);
    }
    
    private String buildUrl( String dbName )
    {
		return URL_START + mHost + ":" + mPort + "/" + dbName + URL_OPTS;
    }

    //
    // get a connection for the default config
    //
    public Connection getConnection()
        throws SQLException
    {
        return DriverManager.getConnection( mUrl, mUser, mPwd );
    }

    //
    // get a connection for a different DB
    //
    public Connection getConnection( String dbName )
        throws SQLException
    {
        String  url=this.buildUrl( dbName );

        return DriverManager.getConnection( url, mUser, mPwd );
    }

    //
    // close the incoming connection
    //
    static public void closeConnection( Connection cnxn ) 
            throws SQLException
    {
        cnxn.close();
    }


    public int getDeadlockMaxRetries()
    {
        return mDeadlockMaxRetries;
    }

    public int getDeadlockRetryWait()
    {
        return mDeadlockRetryWait;
    }

}
