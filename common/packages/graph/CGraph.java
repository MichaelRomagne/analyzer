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

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

//-----------------------------------------------------------------
// CGraph class
//
//
//-----------------------------------------------------------------
public class CGraph 
{
    //---------------------------------------------
    // Members
    //---------------------------------------------

    SimpleGraph<String,DefaultEdge> mGraph=null;

    //---------------------------------------------
    // Implementation
    //---------------------------------------------

    //
    // Start one
    //
    public CGraph()
    {
        mGraph=new SimpleGraph<String,DefaultEdge>(DefaultEdge.class);
    }

    public void mergeWith(CGraph graph)
    {
        //
        // All done
        //
        return;
    }

    public void addVertex(CVertex vertex)
    {
        //
        // All done
        //
        return;
    }

    public void addEdge(CEdge edge)
    {
        //
        // All done
        //
        return;
    }

    public boolean contains(String vertex)
    {
        return( true );
    }
}
