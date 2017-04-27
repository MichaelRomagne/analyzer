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
//
// Package definition for core classes
// in ifat package
//
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


public class CLogger
{
    private static final int HEADER_BUFFER_SIZE = 142;
    //
    // Log level
    //
    public enum Level { E_Debug, E_Info, E_Warn, E_Fatal };

    //
    // The private one and only instance
    //
    private static final CLogger msInstance=new CLogger();
    private static final SimpleDateFormat msdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS Z");

    private OutputStreamWriter  mOut = null;
    private Level        mLevel=Level.E_Debug;

    //
    // Hide the constructor, for singleton status
    //
    private CLogger()
    {
        // set the print stream to use UTF-8
        try
        {
            mOut = new OutputStreamWriter( new FileOutputStream(FileDescriptor.out), "UTF-8" );
        }
        catch ( java.io.UnsupportedEncodingException ex )
        {
            mOut = new OutputStreamWriter( new FileOutputStream(FileDescriptor.out) );
        }
    }

    //
    // Get the logger
    //
    public static CLogger logger()
    {
        return( msInstance );
    }

    public static void setLevel( Level level )
    {
        msInstance.mLevel=level;
    }

    public static void setDebug()
    {
        msInstance.mLevel=Level.E_Debug;
    }

    public static void setInfo()
    {
        msInstance.mLevel=Level.E_Info;
    }

    public static void setWarn()
    {
        msInstance.mLevel=Level.E_Warn;
    }

    public static void setFatal()
    {
        msInstance.mLevel=Level.E_Fatal;
    }

    //
    // The meat of the class... log a message
    //
    private void log(Level _level,String _format,Object ... _args)
    {
        if( mLevel.compareTo( _level ) > 0 )
        {
            return;
        }

        //
        // Build the core structure of the message
        //
        StringBuilder msg = new StringBuilder(HEADER_BUFFER_SIZE);

        // Add level
        switch(_level)
        {
            case E_Debug:
                {
                    msg.append("DEBUG: ");
                }
                break;
            case E_Info:
                {
                    msg.append("INFO : ");
                }
                break;
            case E_Warn:
                {
                    msg.append("WARN : ");
                }
                break;
            case E_Fatal:
                {
                    msg.append("FATAL: ");
                }
                break;
        }

        // Add thread name
        String thread = (Thread.currentThread().getName() + "       ").substring(0,7);
        msg.append("[").append(thread).append("]");
        
        // Add current date/time
        String date = msdf.format(new Date());
        msg.append("[").append(date).append("]");
        
        // Add filename and line number
        StackTraceElement st = (new Throwable()).getStackTrace()[2];
        msg.append(" [").append(st.getFileName()).append("]:").append(st.getLineNumber());
        
        // And finally the format
        msg.append(" +|").append(_format).append("|+\n");
            
        //
        // Fix per MOOG-564 (exception if there's a % in log message)
        //
        try
        {
            String formatted = String.format(msg.toString(), _args);
            
            synchronized (mOut)
            {
                mOut.write(formatted);
                mOut.flush();
            }
        }
        catch (Exception e)
        {
            try
            {
                synchronized (mOut)
                {
                    mOut.write("Logger exception");
                    e.printStackTrace(new PrintWriter(mOut));
                    mOut.flush();
                }
            }
            catch (IOException ex)
            {
                // NOP
            }
        }

        // And check for an abort
        if (_level == Level.E_Fatal)
        {
            assert false;
            System.exit(-1);
        }
    }

    //
    // Log a message with no 'header'
    // Same as println except using mOut
    //
    public void println(String msg)
    {
        try
        {
            synchronized (mOut)
            {
                mOut.write(msg);
                mOut.flush();
            }
        }
        catch (IOException ex)
        {
            // NOP
        }
    }

    //
    // log a debug
    //
    public void debug(String _format,Object ... _args)
    {
        this.log( Level.E_Debug, _format, _args );
    }

    //
    // log a info
    //
    public void info(String _format,Object ... _args)
    {
        this.log( Level.E_Info, _format, _args );
    }

    //
    // log a warn
    //
    public void warning(String _format,Object ... _args)
    {
        this.log( Level.E_Warn, _format, _args );
    }

    //
    // log a fatal
    //
    public void fatal(String _format,Object ... _args)
    {
        this.log( Level.E_Fatal, _format, _args );
    }

    // 
    // Helper functions to set the log level from conf or command line
    //
    public static void parseAndSetLevelAdHoc(String logLevel, Level defaultLevel)
    {
        if (logLevel != null)
        {
            logLevel = logLevel.toUpperCase();
            if (logLevel.equals("ERROR") || logLevel.equals("FATAL"))
            {
                CLogger.logger().info("Setting log level to [%s]", Level.E_Fatal);
                CLogger.setLevel(Level.E_Fatal);
    }
            else if (logLevel.equals("WARN") || logLevel.equals("WARNING"))
            {
                CLogger.logger().info("Setting log level to [%s]", Level.E_Warn);
                CLogger.setLevel(Level.E_Warn);
            }
            else if (logLevel.equals("INFO"))
    {
                CLogger.logger().info("Setting log level to [%s]", Level.E_Info);
                CLogger.setLevel(Level.E_Info);
    }
            else if (logLevel.equals("ALL") || logLevel.equals("DEBUG"))
    {
                CLogger.logger().info("Setting log level to [%s]", Level.E_Debug);
                CLogger.setLevel(Level.E_Debug);
    }
            else
    {
                CLogger.logger().warning("Could not understand loglevel %s, setting log level to default [%s]", logLevel, defaultLevel);
                CLogger.setLevel(defaultLevel);
    }
        }
        else
    {
            CLogger.logger().info("Setting log level to default [%s]", defaultLevel);
            CLogger.setLevel(defaultLevel);
        }
    }

    //
    // Print the  Throwable stack trace with a message
    // The stack trace string is passed as argument an argument to the
    // standard log functions explicitly as a string (%s) format
    // in case the stack trace contains reserved characters
    //
    public void debugThrowable( String message, Throwable t )
    {
        // don't do the stack trace unless absolutley necessary
        if( mLevel.compareTo( Level.E_Debug ) > 0 )
        {
            return;
        }

        this.log( Level.E_Debug, "%s", this.traceString(message, t ) );
    }

    public void infoThrowable( String message, Throwable t )
    {
        // don't do the stack trace unless absolutley necessary
        if( mLevel.compareTo( Level.E_Info ) > 0 )
        {
            return;
        }

        this.log( Level.E_Info, "%s", this.traceString(message, t ) );
    }

    public void warnThrowable( String message, Throwable t )
    {
        // don't do the stack trace unless absolutley necessary
        if( mLevel.compareTo( Level.E_Warn ) > 0 )
        {
            return;
        }

        this.log( Level.E_Warn, "%s", this.traceString(message, t ) );
    }

    public void logThrowable( String message, Throwable t )
    {
        this.warnThrowable( message, t );
    }

    public void fatalThrowable( String message, Throwable t )
    {
        // don't do the stack trace unless absolutley necessary
        if( mLevel.compareTo( Level.E_Fatal ) > 0 )
        {
            return;
        }

        this.log( Level.E_Fatal, "%s", this.traceString(message, t ) );
    }

    private String traceString(String message, Throwable t)  
    {
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);

        writer.println(message);
        t.printStackTrace(writer);

        return out.toString();
    }


}
