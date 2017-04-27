 /***********************************************************
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

import java.lang.String;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.moogsoft.ifat.CParamsUtils;
import com.moogsoft.ifat.CLogger;
import com.moogsoft.ifat.CMoogEnvironment;

//-----------------------------------------------------------------
// CAnalyserParams Definition
//
// Central point for accessing farmd config (from db and file).
//-----------------------------------------------------------------
public class CAnalyserParams
{
    private static final String DEFAULT_CONFIG_NODE = "default";

    //---------------------------------------------
    // Hold here an instance of the singleton
    //---------------------------------------------
    private static CParamsUtils    msUtils = null;
    
    private static String          msConfPath = null;
    private static String          msInstanceName = null;

    //
    // Access the shared instance
    //
    private static synchronized void create()
    {
        msUtils = new CParamsUtils();

        //
        // Either override or read from home var
        //
        if(msConfPath!=null)
        {
            CLogger.logger().info( "Reading moog_farmd config from: [%s]", msConfPath );
            msUtils.readConfig( msConfPath );
        }
        else
        {
            //
            // Now we must build the configuration path...
            //
            if( CMoogEnvironment.checkMoogHome() == false )
            {
                //
                // Abort, we need this as config as a minimum
                //
                CLogger.logger().fatal("MOOGSOFT_HOME has not been set");
            }
    
            //
            // Build the config path
            //
            String conf_path = CMoogEnvironment.getConfigPath( "event.conf" );
    
            CLogger.logger().info( "Reading event_analyser config from: %s", conf_path );
            msUtils.readConfig( conf_path );
        }
        
    }

    //----------------------------------------
    // Instance methods
    //----------------------------------------

    //
    // Set a fixed path
    //
    public static void setConfigPath(String path)
    {
        // Set the path
        msConfPath=path;
    }
    
    //
    // Get the value of the named parameter.  If the parameter exists
    // in the db then use that, otherwise use the file.
    //
    public static synchronized Object valueOf( String param )
    {
        Object value = null;
        
        //
        // Ok, create.
        //
        if( msUtils == null )
        {
            CAnalyserParams.create();
        }
        
        //
        // Get the value
        //
        value = msUtils.valueOf(param);

        //
        // And done
        //
        return( value );
    }

    public static synchronized Boolean exists( String param )
    {
        boolean exists = false;
        
        // avoid threading issues during initialization
        if( msUtils == null )
        {
            CAnalyserParams.create();
        }
        
        if (!exists)
        {
            exists = msUtils.exists(param);
        }
        
        return( exists );
    }


    //
    // Force a re read
    //
    public static synchronized void reread( )
    {
        CAnalyserParams.create();
    }
}
