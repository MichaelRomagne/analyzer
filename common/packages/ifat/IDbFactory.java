/************************************************************
 *                                                          *
 *  Contents of file Copyright (c) Moogsoft Inc 2014        *
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


//
// An interface to a database connection factory
//
public interface IDbFactory
{


	//
	// Create a new connection.
	// The connection must be closed by the caller.
	//
    public Connection getConnection() throws SQLException;

	//
	// Return the maximal number of retries for deadlock
	//
    public int getDeadlockMaxRetries();

	//
	// Return the number of miliseconds to wait before deadlock retries
	//
    public int getDeadlockRetryWait();

}
