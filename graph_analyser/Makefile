#############################################################
#                                                           #
#  Contents of file Copyright (c) Moogsoft Inc 2010         #
#                                                           #
#-----------------------------------------------------------#
#                                                           #
#  WARNING:                                                 #
#  THIS FILE CONTAINS UNPUBLISHED PROPRIETARY               #
#  SOURCE CODE WHICH IS THE PROPERTY OF MOOGSOFT INC AND    #
#  WHOLLY OWNED SUBSIDIARY COMPANIES.                       #
#  PLEASE READ THE FOLLOWING AND TAKE CAREFUL NOTE:         #
#                                                           #
#  This source code is confidential and any person who      #
#  receives a copy of it, or believes that they are viewing #
#  it without permission is asked to notify Phil Tee        #
#  on 07734 591962 or email to phil@moogsoft.com.           #
#  All intellectual property rights in this source code     #
#  are owned by Moogsoft Inc.  No part of this source code  #
#  may be reproduced, adapted or transmitted in any form or #
#  by any means, electronic, mechanical, photocopying,      #
#  recording or otherwise.                                  #
#                                                           *
#  You have been warned....so be good for goodness sake...  #
#                                                           #
#############################################################
DEBUG=1

include $(MOOGSOFT_SRC_HOME)/src-utils/compile/rules/defines.mk

TARGET=graph_analyser
PKG_ROOT=.
TARGET_MAIN_CLASS=CAnalyserMain
TARGET_CONF=analyser.conf
TARGET_VM_ARGS="-server"


JCFLAGS+=-Xlint

COTS_JARS=\
    jblas-1.2.4.jar \
    mysql-connector-java-5.1.17-bin.jar \
    jackson-core-2.0.6.jar\
    jackson-databind-2.0.6.jar\
    jackson-annotations-2.0.6.jar\
    commons-io-1.2.jar \
    commons-cli-1.2.jar \
    jgrapht-core-0.9.0.jar \

MOOG_JARS=\
        ifat.jar \
        graph.jar \

APP_JAR_CONTENTS=*.class

all: $(TARGET) deliver

JAVA_SRCS= \
    CAnalyser.java \
    CAnalyserMain.java \

SCRIPTS= \

IMAGE_FILES=\

NO_TEST_CASES=1

include $(MOOGSOFT_SRC_HOME)/src-utils/compile/rules/javaapp.mk
