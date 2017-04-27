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
 ***********************************************************/
package com.moogsoft.ifat;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;


//
// A simple convenience class that encapsulate the various directories
// under MOOGSOFT_HOME directory
//
// This is not a complete set of functions for every dir under MOOGSOFT_HOME
// simply those dirs required by the java code base
//

public class CMoogEnvironment
{
    // the value of MOOGSOFT_HOME, assigned below
    private static String MOOG_HOME;

    // the value of MOOGSOFT_SRC_HOME. assigned below
    private static String MOOG_SRC_HOME;

    // general config directory
    private static final String CONFIG_DIR = "config/";

    // the contrib directory
    private static final String CONTRIB_DIR = "contrib/";

    // the etc directory
    private static final String ETC_DIR = "etc/";

    // external jar files directory
    private static final String COTS_JARS_DIR = "lib/cots/";

    // the moobots directory
    private static final String MOOBOTS_DIR = "bots/moobots/";

    // the system config file
    private static final String SYSTEM_CONFIG = "system.conf";

    //
    // assign the value of MOOGSOFT_HOME one anc for all at initialisation
    //
    static
    {
        MOOG_HOME = System.getenv( "MOOGSOFT_HOME" );

        if( (  MOOG_HOME != null ) &&
            ( !MOOG_HOME.isEmpty() ) )
        {
            MOOG_HOME += "/";
        }

        MOOG_SRC_HOME = System.getenv("MOOGSOFT_SRC_HOME");

        if ((MOOG_SRC_HOME != null) &&
            (!MOOG_SRC_HOME.isEmpty() ) )
        {
            MOOG_SRC_HOME += "/";
        }
    }

    //
    // does MOOGSOFT_HOME have a valid value
    //
    public static boolean checkMoogHome()
    {
        if( MOOG_HOME == null ||
            MOOG_HOME.isEmpty() )
        {
            return false;
        }

        return true;
    }

    //
    // get the value of MOOGSOFT_HOME
    //
    public static String getMoogHome()
    {
        return MOOG_HOME;
    }

    //
    // get the value of MOOGSOFT_SRC_HOME.  This should only be called in a
    // test environment.
    //
    public static String getMoogSrcHome()
    {
        return MOOG_SRC_HOME;
    }

    //
    // Get the absolute path of the given cots jar file under MOOGSOFT_HOME
    //
    public static String getCotsJarPath( String jar )
    {
        if( ( jar == null ) || jar.isEmpty() )
        {
            return null;
        }

        return MOOG_HOME + COTS_JARS_DIR + jar;
    }

    //
    // Get the absolute path of the system.conf file under MOOGSOFT_HOME
    //
    public static String getSystemConfigPath( )
    {
        return CMoogEnvironment.getConfigPath( SYSTEM_CONFIG );
    }

    //
    // Get the absolute path of the given config file in under MOOGSOFT_HOME
    //
    public static String getConfigPath( String config )
    {
        if( ( config == null ) || config.isEmpty() )
        {
            return null;
        }

        return MOOG_HOME + CONFIG_DIR + config;
    }

    //
    // Get the relative path of an etc file under MOOGSOFT_HOME
    //
    public static String getEtcRelativePath( String file )
    {
        if( ( file == null ) || file.isEmpty() )
        {
            return null;
        }

        return ETC_DIR + file;
    }

    //
    // Get the absolute path of the moobot directory under MOOGSOFT_HOME
    //
    public static String getMooBotDir( )
    {
        return MOOG_HOME + MOOBOTS_DIR;
    }

    //
    // Get the absolute path of the config directory under MOOGSOFT_HOME
    //
    public static String getContribDir()
    {
        return MOOG_HOME + CONTRIB_DIR;
    }

    //
    // Get the absolute path of the give moobot file under MOOGSOFT_HOME
    //
    public static String getMooBotPath( String moobot )
    {
        if( ( moobot == null ) || moobot.isEmpty() )
        {
            return null;
        }

        return MOOG_HOME + MOOBOTS_DIR + moobot;
    }

    //
    // Get the absolute path of the given moobot from the contrib directory
    // under MOOGSOFT_HOME
    //
    public static String getMooBotPathFromContrib(String moobot)
    {
        if( ( moobot == null ) || moobot.isEmpty() )
        {
            return null;
        }

        return MOOG_HOME + CONTRIB_DIR + moobot;
    }
}
