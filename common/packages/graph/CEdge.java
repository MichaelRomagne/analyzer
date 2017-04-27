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

//-----------------------------------------------------------------
// CEdge class
//
// Simple class that stores data about a graph vertex
//-----------------------------------------------------------------
public class CEdge extends DefaultEdge
{
    //---------------------------------------------
    // We care not about the serialisability
    //---------------------------------------------
    public static final long serialVersionUID=0L;

    //---------------------------------------------
    // Members
    //---------------------------------------------

    private String mName=null;

    //---------------------------------------------
    // Implementation
    //---------------------------------------------

    //
    // Construct
    // 
    public CEdge()
    {
    }

    public String getSource()
    {
        return( super.getSource().toString() );
    }
    public String getTarget()
    {
        return( super.getTarget().toString() );
    }
}
