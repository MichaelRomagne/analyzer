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
package com.moogsoft.event;

//
// Packages
//
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import com.moogsoft.ifat.CDbPool;
import com.moogsoft.ifat.CLogger;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



//-----------------------------------------------------------------
// CEventBuilder class
//
// From a delimited string construct an event for analysis
//-----------------------------------------------------------------
public class CEventBuilder 
{
    //---------------------------------------------
    // Constants
    //---------------------------------------------
    private static final String FIELDS_KEY="fields";
    private static final String MAPPING_KEY="mapping";
    private static final String HASH_KEY="hash";
    private static final String HOST_KEY="host";
    private static final String TIME_KEY="timestamp";
    private static final String DESC_KEY="description";
    private static final String TYPE_KEY="type";

    //---------------------------------------------
    // Members
    //---------------------------------------------
    private Integer         mFields=null;
    private List<String>    mMappings=null;
    private List<String>    mSignature=null;
    private String          mHostField=null;
    private String          mTimeField=null;
    private String          mDescField=null;
    private String          mTypeField=null;


    //---------------------------------------------
    // Implementation
    //---------------------------------------------

    //
    // Start one
    //
    @SuppressWarnings("unchecked")
    public CEventBuilder(Map<String,Object> config)
    {
        //
        // Extract the fixed stuff
        //
        mFields=((Number )config.get( FIELDS_KEY )).intValue();
        mHostField=(String )config.get( HOST_KEY );
        mTimeField=(String )config.get( TIME_KEY );
        mDescField=(String )config.get( DESC_KEY );
        mTypeField=(String )config.get( TYPE_KEY );

        //
        // And the lists
        //
        mMappings=(List<String> )config.get( MAPPING_KEY );
        mSignature=(List<String> )config.get( HASH_KEY );
    }

    //
    // Simple retrieve of the fields
    //
    public Integer getFields()
    {
        return( mFields );
    }

    //
    // Process a set of tokens...
    //
    public void process( List<String> tokens, CDbPool pool, boolean onlyScope)
    {
        //
        // Index the fields
        //
        Map<String,String> params=new HashMap<String,String>();

        Iterator<String> itr=tokens.iterator();
        Iterator<String> kItr=mMappings.iterator();
        while( kItr.hasNext() )
        {
            //    
            // Grab the key
            //    
            String key=kItr.next();

            //
            // Do we have a mapping?
            //
            if( itr.hasNext() )
            {
                params.put( key, itr.next() );
            }
            else
            {
                CLogger.logger().fatal("Corrupt config - bailing");
            }

        }

        //
        // Ok, build signature
        //
        StringBuilder sigBuilder=new StringBuilder( 512 );
        itr=mSignature.iterator();
        while( itr.hasNext() )
        {
            sigBuilder.append( params.get( itr.next() ) );
            sigBuilder.append( ":" );
        }

        //
        // Essential components of event
        //
        String signature=sigBuilder.toString();
        String host=params.get( mHostField );
        String desc=params.get( mDescField );

        CLogger.logger().info("Event signature [%s] for host [%s]",signature,host );
        
        //
        // Declare db objects for "finally" block
        //
        CallableStatement cStmnt=null;
        Connection db_con=null;
        try
        {
            //
            // Grab it
            //
            db_con=pool.checkOut();

            if( onlyScope == true )
            {
                //
                // Build & run the sql
                //
                cStmnt=db_con.prepareCall( "{ call ifatdb.add_node( ? ) }" );
                cStmnt.setString( "nd", host );
            }
            else
            {
                //
                // Build & run the sql
                //
                cStmnt=db_con.prepareCall( "{ call ifatdb.new_event( ?, ?, ? ) }" );
                cStmnt.setString( "sig", signature );
                cStmnt.setString( "host", host );
                cStmnt.setString( "dsc", desc );
            }
   
            //
            // Run it
            //
            ResultSet results=cStmnt.executeQuery();

            //
            // Close the CallableStatement
            //
            cStmnt.close();
        }
        catch(SQLException e)
        {
            CLogger.logger().info("SQL Exception: %s ", e);
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if( cStmnt!=null )
                {
                    cStmnt.close();
                }
            }
            catch(SQLException e)
            {
                CLogger.logger().info("SQL Exception: %s ", e);
            }

            pool.checkIn(db_con);
        }


        //
        // All done
        //
        return;
    }
}
