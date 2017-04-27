#############################################################
#                                                           #
#  Contents of file Copyright (c) Moogsoft Inc 2010         #
#                                                           #
#-----------------------------------------------------------#
#-----------------------------------------------------------#
#  WARNING:                                                 #
#  THIS FILE CONTAINS UNPUBLISHED PROPRIETARY               #
#  SOURCE CODE WHICH IS THE PROPERTY OF MOOGSOFT INC AND    #
#  WHOLLY OWNED SUBSIDIARY COMPANIES.						#
#  PLEASE READ THE FOLLOWING AND TAKE CAREFUL NOTE:			#
#                                                           #
#  This source code is confidential and any person who      #
#  receives a copy of it, or believes that they are viewing	#
#  it without permission is asked to notify Phil Tee	    #
#  on 07734 591962 or email to phil@moogsoft.com.			#
#  All intellectual property rights in this source code     #
#  are owned by Moogsoft Inc.  No part of this source code  #
#  may be reproduced, adapted or transmitted in any form or	#
#  by any means, electronic, mechanical, photocopying,		#
#  recording or otherwise.									#
#                                                           *
#  You have been warned....so be good for goodness sake...	#
#                                                           #
#############################################################
DEBUG=1

include $(MOOGSOFT_SRC_HOME)/src-utils/compile/rules/defines.mk

TARGET=event
PKG_ROOT=com
PACKAGE=com.moogsoft.event

#
# Javac compilation flags
#
JCFLAGS+=-Xlint

COTS_JARS=\
    jgrapht-core-0.9.0.jar \

MOOG_JARS= \
		ifat.jar \

APP_JAR_CONTENTS=*.class

all: $(TARGET) deliver

# note these are transformed to CClass_Solaris2.cc etc. by the
# make rules where appropriate
PLATFORM_SRCS= \

JAVA_SRCS= \
        CEventBuilder.java \
        CIncidentBuilder.java \

NO_TEST_CASES=1

include $(MOOGSOFT_SRC_HOME)/src-utils/compile/rules/javapkg.mk

