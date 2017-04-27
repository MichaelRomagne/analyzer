#!/bin/sh
#############################################################
#                                                           #
#  SCCS: %M%    %I%     %E%                                 #
#                                                           #
#############################################################
#############################################################
#                                                           #
#  Contents of file Copyright (c) Moogsoft Inc 2010        	#
#                                                           #
#-----------------------------------------------------------#
#-----------------------------------------------------------#
#  WARNING:                                                 #
#  THIS FILE CONTAINS UNPUBLISHED PROPRIETARY               #
#  SOURCE CODE WHICH IS THE PROPERTY OF MOOTRONIX INC AND   #
#  WHOLLY OWNED SUBSIDIARY COMPANIES.						#
#  PLEASE READ THE FOLLOWING AND TAKE CAREFUL NOTE:			#
#                                                           #
#  This source code is confidential and any person who      #
#  receives a copy of it, or believes that they are viewing	#
#  it without permission is asked to notify Phil Tee	    #
#  on 07734 591962 or email to phil@moogsoft.com.			#
#  All intellectual property rights in this source code     #
#  are owned by Moogsoft Inc.  No part of this source code 	#
#  may be reproduced, adapted or transmitted in any form or	#
#  by any means, electronic, mechanical, photocopying,		#
#  recording or otherwise.									#
#                                                           *
#  You have been warned....so be good for goodness sake...	#
# 															#
#-----------------------------------------------------------#
#                                                           #
#                                                           #
#############################################################
#
# Build the servlet directory structure
#
mkdir -p $1/WEB-INF
chmod 755 $1/WEB-INF
mkdir -p $1/WEB-INF/classes
chmod 755 $1/WEB-INF/classes
mkdir -p $1/WEB-INF/lib
chmod 755 $1/WEB-INF/lib
