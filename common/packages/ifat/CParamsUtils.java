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

import java.lang.String;
import java.util.HashMap;
import java.util.Map;

//--------------------------------------------------
//
// CParamsUtils
//
// Ideally we want the "Config" objects for each app
// to be singletons. Couple that with the fact we have
// much repeated code we also would want to use inheritance
// Inheritance and Singletons don't match so put the share
// code in this utility class and call it from the app-specic
// config classes
//
//--------------------------------------------------
public class CParamsUtils
{

    private Map< String, Object >   mParams = null;

    // where in the main parameter is the root of the config?
    private String                  mRootNode = null;

    public CParamsUtils( )
    {
        mRootNode = "config";
    }
    
    public CParamsUtils(Map<String, Object> params)
    {
        this();
        
        //
        // Takes a copy of the params map parameter
        //
        mParams = new HashMap<String, Object>(params);
    }

    public CParamsUtils( String rootNode )
    {
        if( rootNode != null &&
            !rootNode.isEmpty() )
        {
            mRootNode = rootNode;
        }
    }

    public boolean readConfig( String path )
    {
        if( (path==null) ||
            (path.length()==0 ) )
        {
            return false;
        }

        mParams=CJSONCodec.decodeFileToMap( path );

        return (mParams != null );
    }

    //
    // Get the object pointed to by address
    //
    @SuppressWarnings("unchecked")
    public Object valueOf( String name )
    {
        //
        // Check we aren't empty
        //
        if( (name==null) ||
            (name.length()==0 ) )
        {
            //
            // And complain
            //
            return null;
        }

        //
        // Do I have any parameters to navigate
        //
        if(mParams==null)
        {
            CLogger.logger().info("No config present");
            return null;
        }

        Map< String, Object >   paramsRoot=this.getParamsRoot();
        if(paramsRoot == null)
        {
            CLogger.logger().info("Empty config");
            return false;
        }

        //
        // And parse the address into components
        //
        String []address=name.split("\\.");
        if(address == null)
        {
            CLogger.logger().warning("Failed to parse address [%s]",name);

            return null;
        }

        //
        // Walk the address
        //
        String comp=null;
        Object result=null;
        for( int idx=0 ; idx<address.length ; idx++ )
        {
            //
            // Grab the component
            //
            comp=address[ idx ];

            //
            // Attempt to get from the config
            //
            result=paramsRoot.get(comp);
            if(result==null)
            {
                // if the result is null the key may still exist
                return null;
            }

            //
            // swap dictionaries if there are more items in the address
            //
            if( idx < ( address.length - 1 ) )
            {
                //
                // And swap dictionaries - if we can
                //
                if(result instanceof Map)
                {
                    paramsRoot=(Map<String,Object>)result;
                }
            }
        }

        //
        // Send it home
        //
        return result;
    }

    //
    // Does the key exist in the parameters regardless of value?
    //
    @SuppressWarnings("unchecked")
    public Boolean exists( String name )
    {
        //
        // Check we aren't empty
        //
        if( (name==null) ||
            (name.length()==0 ) )
        {
            return false;
        }

        //
        // Do I have any parameters to navigate
        //
        if(mParams==null)
        {
            CLogger.logger().info("No config present");
            return false;
        }

        Map< String, Object >   paramsRoot=this.getParamsRoot();
        if(paramsRoot == null)
        {
            CLogger.logger().info("Empty config");
            return false;
        }

        //
        // And parse the address into components
        //
        String []address=name.split("\\.");
        if(address == null)
        {
            CLogger.logger().warning("Failed to parse address [%s]",name);

            return false;
        }

        //
        // Walk the address
        //
        String comp=null;
        Object result=null;
        for( int idx=0 ; idx<address.length ; idx++ )
        {
            //
            // Grab the component
            //
            comp=address[ idx ];

            //
            // Attempt to get from the config
            //
            result=paramsRoot.get(comp);
            if(result==null)
            {
                // if the result is null the key may still exist
                return paramsRoot.containsKey( comp );
            }

            //
            // swap dictionaries if there are more items in the address
            //
            if( idx < ( address.length - 1 ) )
            {
                //
                // And swap dictionaries - if we can
                //
                if(result instanceof Map)
                {
                    paramsRoot=(Map<String,Object>)result;
                }
            }
        }

        //
        // Send it home
        //
        return true;
    }

    //
    // Get the section of the parameters map defined by mRootNode
    //
    @SuppressWarnings("unchecked")
    private Map< String, Object > getParamsRoot()
    {
        Map< String, Object > paramsRoot=mParams;

        if( mRootNode != null )
        {
            paramsRoot = ( Map<String, Object > )mParams.get( mRootNode );
        }

        return paramsRoot;
    }
}


