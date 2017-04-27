#-----------------------------------------------------------#
#                                                           #
#  Contents of file Copyright (c) Moogsoft Inc 2010        #
#                                                           #
#-----------------------------------------------------------#
#                                                           #
#  WARNING:                                                 #
#  THIS FILE CONTAINS UNPUBLISHED PROPRIETARY               #
#  SOURCE CODE WHICH IS THE PROPERTY OF MOOGSOFT INC AND     #
#  WHOLLY OWNED SUBSIDIARY COMPANIES.                       #
#  PLEASE READ THE FOLLOWING AND TAKE CAREFUL NOTE:         #
#                                                           #
#  This source code is confidential and any person who      #
#  receives a copy of it, or believes that they are viewing #
#  it without permission is asked to notify Moogsoft Inc   #
#  on +44 (0)7734 591962 or email to phil@moogsoft.com.    #
#  All intellectual property rights in this source code     #
#  are owned by Moogsoft Inc.  No part of this source code #
#  may be reproduced, adapted or transmitted in any form or #
#  by any means, electronic, mechanical, photocopying,      #
#  recording or otherwise.                                  #
#                                                           #
#-----------------------------------------------------------#
OS=Centos

CC=gcc
CXX=g++

ifndef DEBUG    
OPTFLAGS:=-O3
else
OPTFLAGS=-DDEBUG
endif

ifndef GNUSTEP_HOME
GNUSTEP_HOME=/usr/GNUstep/System/Library
endif
GNUSTEP_INCLUDES=-I$(GNUSTEP_HOME)/Headers
GNUSTEPLIBS=-L/$(GNUSTEP_HOME)/Libraries -lgnustep-base

ifndef KQUEUE_INCLUDES
KQUEUE_INCLUDES=-I/usr/include/kqueue
endif


# CFLAGS=-g $(OPTFLAGS) -Wall -D$(OS) -fPIC -D_POSIX_PTHREAD_SEMANTICS -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_REENTRANT
#   When you configure gnustep-base, it records whether gnustep-make tells it to 
#   use native exceptions or not ... by recording the value of 
#   _NATIVE_OBJC_EXCEPTIONS in  BASE_NATIVE_OBJC_EXCEPTIONS in GSConfig.h
#   
#   When you build any software suing base, the header file NSException.h checks 
#   that gnustep-make is still saying that native exceptions should be used (ie 
#   checking that the current value of _NATIVE_OBJC_EXCEPTIONS supplied by 
#   gnustep-make is the same as the value recorded in GSConfig.h).
#   
#   If you are building software without gnustep-make, you need to define 
#   _NATIVE_OBJC_EXCEPTIONS appropriately.
#

CFLAGS=-g $(OPTFLAGS) -Wall -D$(OS) -fPIC -fconstant-string-class=NSConstantString -fobjc-call-cxx-cdtors -D_NATIVE_OBJC_EXCEPTIONS
CXXFLAGS=$(CFLAGS)
CPPFLAGS=-DLinux -Wno-deprecated $(GNUSTEP_INCLUDES) $(KQUEUE_INCLUDES) -I$(MOOGSOFT_SRC_HOME)/include $(MYSQLINCLUDES)

LOCALLIBS=-lm -lstdc++ -lresolv -lcrypto -lpthread -lssl -lobjc -lkqueue $(XMLLIBS) $(XSLTLIBS) $(MYSQLLIBS) $(GNUSTEPLIBS)

NETLIBS=-lnsl -lrt -ldl
IDENTLIBS=-lpam -lldap_r
BOOSTINCLUDES=-I/usr/local/include/boost-1_33		# not there
XMLINCLUDES=`xml2-config --cflags`
XMLLIBS=`xml2-config --libs`
XSLTINCLUDES=`xslt-config --cflags`
XSLTLIBS=`xslt-config --libs`
POSTGRESLIBS=-L$(POSTGRESHOME)/lib -lpq			# not there
DYNAMICLIBLDFLAGS=-fPIC -shared -L$(MOOGSOFT_HOME)/lib $(LOCALLIBS)
MYSQLINCLUDES=-I/usr/include/mysql
MYSQLLIBS=-L/usr/lib64/mysql -lmysqlclient
EXELDFLAGS=-L$(MOOGSOFT_HOME)/lib $(LOCALLIBS)
DYNAMIC_LIB_EXT=so
STATIC_LIB_EXT=a
INSTALL=/usr/bin/install
RANLIB=ranlib
PCRELIBS=`pcre-config --libs`
PCREFLAGS=`pcre-config --cflags`

MKDIR=mkdir -p
RM=rm -f
RMDIR=rm -rf
TOUCH=touch
MV=mv -f
CP=cp
SED=sed
ECHO=echo
CAT=cat
PURIFY=purify
LN=ln
AR=ar -ru
ID=/usr/bin/id -u

LEX=flex
LEX_STAGES=1
LEX_SED_1='s/<FlexLexer.h>/<moo\/Lexer.h>/g'
LEX_SED_2=
YACC=bison -yvd
YACC_STAGES=2
YACC_APP_SED_1='s/yyparse (void)/yyparse (void *lexp,void *app,void *rule)/g'
YACC_SED_1='s/yyparse (void)/yyparse (void *lexp,void *parser,void *rule)/g'
YACC_SED_2='s/ __attribute__ ((__unused__))/;/g'

#
# Javascript
#
JSCC=jscc
JSCOMPDIR=.compiled

#
## Java
#
JAVAC=$(JAVA_HOME)/bin/javac
JAR=$(JAVA_HOME)/bin/jar
WAR_EXT=war
ifndef WAR_ROOT
WAR_ROOT=WEB-INF
endif
JAR_EXT=jar
BUILD_APP=$(MOOGSOFT_SRC_HOME)/src-utils/compile/scripts/build_harness.sh
BUILD_WAR=$(MOOGSOFT_SRC_HOME)/src-utils/compile/scripts/build_war_struct.sh
ifndef CLASSPATH
CLASSPATH=.
endif
ifndef WEB_FILE
WEB_FILE=web.xml
endif

RPCSVCDIR=/usr/include/rpcsvc
RPCGEN=rpcgen -C


ifndef BDBHOME
BDBHOME=/usr/local/BerkeleyDB.4.4				# ????
endif

ifndef POSTGRESHOME
POSTGRESHOME=/usr/local/pgsql					# ????
endif

ifndef MYSQLHOME
MYSQLHOME=/usr/share/mysql
endif

ifndef CPPTESTHOME
CPPTESTHOME=/usr/local
endif

# Kind of irrelevant libraries (crypt & ssl) are in /usr/lib64
# ifndef OPENSSLHOME
# OPENSSLHOME					
# endif

ifndef PKG_CONFIG_PATH
PKG_CONFIG_PATH=/usr/lib64/pkgconfig			
endif

ifndef JUNIT_JAR
JUNIT_JAR=/usr/share/java/junit.jar
endif

ifndef CLASSPATH
CLASSPATH=.:$(JUNIT_JAR)
else
CLASSPATH:=.:$(JUNIT_JAR):$(CLASSPATH)
endif

SYS_TEST_LIBS=\
	-lpopt \
	-lcrypt \
	-lresolv \
	-lnsl \
	-ldl \
	-ldl \
	-lldap \
	-llber \

OS_LIBRARY_PATH=LD_LIBRARY_PATH
