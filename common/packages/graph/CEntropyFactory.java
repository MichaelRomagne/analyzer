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


import com.moogsoft.ifat.CDbPool;
import com.moogsoft.ifat.CLogger;

import org.jgrapht.graph.Multigraph;

//-----------------------------------------------------------------
// CEntropyFactory class
//
// From a descriptio string build a class and initialise it
// ready for calculation
//
//-----------------------------------------------------------------
public class CEntropyFactory
{
    //---------------------------------------------
    // Members
    //---------------------------------------------

    //---------------------------------------------
    // Implementation
    //---------------------------------------------

    //
    // From string
    //
    public static CAbstractEntropyCalculator buildCalculator( Multigraph<String,CEdge> graph, String type, CGraphLoader loader )
    {
        //
        // The type of the entropy value
        //
        EEntropyType entType=EEntropyType.fromString( type );

        //
        // And build it
        //
        return( CEntropyFactory.buildCalculator( graph, entType, loader ) );
    }

    //
    // From enum
    //
    public static CAbstractEntropyCalculator buildCalculator( Multigraph<String,CEdge> graph, EEntropyType entType, CGraphLoader loader )
    {
        //
        // The target calculator
        //
        CAbstractEntropyCalculator calculator=null;

        //
        // Build and initialise the calculator
        //
        switch( entType )
        {
            case EAll:
                {
                }
                break;
            case EKorner:
                {
                }
                break;
            case EChromatic:
                {
                }
                break;
            case EVonNeumann:
                {
                }
                break;
            case EInverseDegree:
                {
                    //
                    // Build specific calculator.
                    //
                    calculator = new CInverseDegreeEntropy( loader );

                    //
                    // And initialise
                    //
                    calculator.initialise( graph );
                }
                break;
            case EFractionalDegree:
                {
                    //
                    // Build specific calculator.
                    //
                    calculator = new CFractionalDegreeEntropy( loader );

                    //
                    // And initialise
                    //
                    calculator.initialise( graph );
                }
                break;
            case ENormalizedFractDegree:
                {
                    //
                    // Build specific calculator.
                    //
                    calculator = new CNormFractDegreeEntropy( loader );

                    //
                    // And initialise
                    //
                    calculator.initialise( graph );
                }
                break;
            case ELinNormFractDegree:
                {
                    //
                    // Build specific calculator.
                    //
                    calculator = new CLinNormFractDegree( loader );

                    //
                    // And initialise
                    //
                    calculator.initialise( graph );
                }
                break;
            case ENormalizedInvDegree:
                {
                    //
                    // Build specific calculator.
                    //
                    calculator = new CNormInverseDegreeEntropy( loader );

                    //
                    // And initialise
                    //
                    calculator.initialise( graph );
                }
                break;
            case ECluster:
                {
                    //
                    // Build specific calculator.
                    //
                    calculator = new CClusterEntropy( loader );

                    //
                    // And initialise
                    //
                    calculator.initialise( graph );
                }
                break;
            case EClusterFract:
                {
                    //
                    // Build specific calculator.
                    //
                    calculator = new CClusterFractEntropy( loader );

                    //
                    // And initialise
                    //
                    calculator.initialise( graph );
                }
                break;
            case EBetween:
                {
                    //
                    // Build specific calculator.
                    //
                    calculator = new CBetweenEntropy( loader );

                    //
                    // And initialise
                    //
                    calculator.initialise( graph );
                }
                break;
        }

        //
        // And off it goes
        //
        return( calculator );
    }
}
