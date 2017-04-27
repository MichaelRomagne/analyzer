/*  Contents of file Copyright (c) Moogsoft Inc 2010        *
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


import java.util.Map;
import java.util.List;

//-----------------------------------------------------------
//
// CSystemParams
//
// Convenience class to access global system configuration
// information.
//
//-----------------------------------------------------------
public class CSystemParams
{
    // Default heartbeat is 10 seconds (10,000 milliseconds).
    private static final long DEFAULT_HEARTBEAT = 10000;
    private static final long MIN_HEARTBEAT_LIMIT = 1000;

    private String    mConfPath=null;
    private Map<String, Object> mSystemConf = null;

    public CSystemParams()
    {
        if( CMoogEnvironment.checkMoogHome() == false )
        {
            CLogger.logger().warning("MOOGSOFT_HOME not set");
        }
        else
        {
            mConfPath = CMoogEnvironment.getSystemConfigPath();

            this.readConfigFile();
        }
    }

    public CSystemParams(String configPath)
    {
        mConfPath = configPath;
        readConfigFile();
    }


    //
    // Get the JSON from config File
    //
    private void readConfigFile()
    {
        if (mConfPath==null || mConfPath.length()==0)
        {
            CLogger.logger().warning("No system config path defined");
        }
        else
        {
            CLogger.logger().info("Reading system config from %s", mConfPath);

            mSystemConf = CJSONCodec.decodeFileToMap(mConfPath);

            if(mSystemConf==null)
            {
                CLogger.logger().warning("File on Config Path could not be parsed");
            }
        }
    }

    //---------------------------------------------
    //
    // General configuration data methods
    //
    //---------------------------------------------

    @SuppressWarnings("unchecked")
    public Map <String, Object> getSystemConfigData()
    {
        return mSystemConf;
    }

    //---------------------------------------------
    //
    // MooMs configuration data methods
    //
    //---------------------------------------------
    
    @SuppressWarnings("unchecked")
    public Map <String, Object> getMoomsData()
    {
        Map <String, Object> moomsData = null;
        if (mSystemConf != null)
        {
            try
            {
                moomsData = (Map <String, Object>) mSystemConf.get("mooms");     
            }
            catch (ClassCastException e)
            {
                CLogger.logger().warning(e.getMessage());
            }
        }
        return moomsData;
    }
    
    //---------------------------------------------
    //
    // MySQL configuration data methods
    //
    //---------------------------------------------

    @SuppressWarnings("unchecked")
    public Map <String, Object> getMysqlData()
    {
        Map <String, Object> mysqlData = null;
        if (mSystemConf != null)
        {
            try
            {
                mysqlData = (Map <String, Object>) mSystemConf.get("mysql");
            }
            catch (ClassCastException e)
            {
                CLogger.logger().warning(e.getMessage());
            }
        }
        return mysqlData;
    }

    //---------------------------------------------
    //
    // Sphinx configuration data methods
    //
    //---------------------------------------------

    @SuppressWarnings("unchecked")
    public Map <String, Object> getSphinxData()
    {
        Map <String, Object> sphinxData = null;
        if (mSystemConf != null)
        {
            try
            {
                sphinxData = (Map <String, Object>) mSystemConf.get("sphinx");
            }
            catch (ClassCastException e)
            {
                CLogger.logger().warning(e.getMessage());
            }
        }
        return sphinxData;
    }

    //---------------------------------------------
    //
    // Process monitor data methods
    //
    //---------------------------------------------
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getReservedProcesses()
    {
        List<Map<String,Object>> reservedProcs = null;

        if (mSystemConf != null)
        {
            try
            {
                Map<String,Object> processInfo = (Map<String,Object>)mSystemConf.get("process_monitor");
                reservedProcs = (List<Map<String,Object>>)processInfo.get("processes");
            }
            catch (ClassCastException e)
            {
                CLogger.logger().warning(e.getMessage());
            }
        }

        return reservedProcs;
    }

    //
    // Encription configuration data
    //
    @SuppressWarnings("unchecked")
    public Map <String, Object> getEncryptionData()
    {
        Map <String, Object> ret = null;
        if (mSystemConf != null)
        {
            try
            {
                ret = (Map <String, Object>) mSystemConf.get("encryption");
            }
            catch (ClassCastException e)
            {
                CLogger.logger().warning(e.getMessage());
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public Long getHeartbeat()
    {
        Long heartbeat = null;

        if (mSystemConf != null)
        {
            try
            {
                Map<String,Object> processInfo = (Map<String,Object>)mSystemConf.get("process_monitor");
                heartbeat = (Long)processInfo.get("heartbeat");

                // Return a default value if nothing is configured.
                if ((heartbeat == null) ||
                    (heartbeat < MIN_HEARTBEAT_LIMIT))
                {
                    heartbeat = DEFAULT_HEARTBEAT;
                    CLogger.logger().warning(
                        "System heartbeat was not set, or was set to an invalid value. Setting to: %d", heartbeat);
                }
            }
            catch (ClassCastException e)
            {
                CLogger.logger().warning(e.getMessage());
            }
        }

        return heartbeat;
    }
}
