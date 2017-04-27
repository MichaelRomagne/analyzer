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

import java.util.Set;

import org.jgrapht.graph.Multigraph;

//-----------------------------------------------------------------
// CAbstractEntropyCalculator class
//
// For a Multigraph, extend this baby to calculate each variant of
// entropy
//
//-----------------------------------------------------------------
public abstract class CAbstractEntropyCalculator
{
    //---------------------------------------------
    // Members
    //---------------------------------------------
    protected CGraphLoader      mLoader=null;   // Reference to my graph store

    //---------------------------------------------
    // Implementation
    //---------------------------------------------

    //
    // For now, empty constructor
    //
    public CAbstractEntropyCalculator( CGraphLoader loader )
    {
        mLoader=loader;
    }

    //
    // Initialise with a multigraph
    //
    public abstract void initialise( Multigraph<String,CEdge> graph );

    //
    // Calculate entropy of whole graph
    //
    public abstract Double calculateEntropy( Integer gid, CDbPool pool, Set<String> scope );

    //
    // Update database record of graph
    //
    public abstract void updateGraphDb( Integer gid, Double val, CDbPool pool );

    //
    // Update database record of vertex
    //
    public abstract void updateVertexDb( String node, Double val, CDbPool pool );

    //
    // Build the distribution table for calculation
    //
    public abstract void buildDistribution( String graph, Integer points,  CDbPool pool ,Double maxVal);

    //
    // Build the distribution table for calculation
    // for all graphs
    //
    public abstract void buildAllGraphDistribution( String graph, Integer points,  CDbPool pool );
}
