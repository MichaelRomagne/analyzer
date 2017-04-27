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
package com.moogsoft.graph;

import java.util.Map;
import java.util.HashMap;
//-----------------------------------------------------------------
// EEntropyType enum
// Used by ARE support system to allow for smooth generation of relevant
// rule types from the bus
//-----------------------------------------------------------------
public enum EEntropyType
{
    EAll                    ( "All" ),              // The entire enchilada
    EKorner                 ( "Korner" ),           // Structural Entropy
    EChromatic              ( "Chromatic" ),        // Chromatic Entropy
    EVonNeumann             ( "VonNeurmann" ),      // VonNeumann
    EInverseDegree          ( "InvDegree" ),        // Inverse Degree
    EFractionalDegree       ( "FractDegree" ),      // Fractional Degree
    ENormalizedFractDegree  ( "NormFractDegree" ),  // Normalised Fractional
    ENormalizedInvDegree    ( "NormInvDegree" ),    // Normalised Inv Degree
    ELinNormFractDegree     ( "LinNormFractDegree" ),    // Normalised Inv Degree
    ECluster                ( "Cluster" ),          // Cluster Entropy
    EClusterFract           ( "ClusterFract" ),     // Cluster Fract Entropy
    EBetween                ( "Between" );     // Betweenness Centraity

    private final String   mType;  // Action code

    private static final Map<String, EEntropyType > msMap = new HashMap<String, EEntropyType>();

    static
    {
        for( EEntropyType type : EEntropyType.values() )
        {
            msMap.put( type.mType, type );
        }
    }

    private EEntropyType(String type)
    {
        this.mType=type;
    }

    public String toString()
    {
        return( mType );
    }

    public static EEntropyType fromString(String type)
    {
        //
        // Attempt get, if unrecognised, send back an unknown
        //
        EEntropyType tp=msMap.get(type);
        if(tp==null)
        {
            tp=msMap.get("Unknown");
        }
        return( tp );
    }
};
