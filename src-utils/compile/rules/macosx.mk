#-----------------------------------------------------------#
#                                                           #
#  Contents of file Copyright (c) Moogsoft Inc 2010        #
#                                                           #
#-----------------------------------------------------------#
#                                                           #
#  WARNING:                                                 #
#  THIS FILE CONTAINS UNPUBLISHED PROPRIETARY               #
#  SOURCE CODE WHICH IS THE PROPERTY OF MOOGSOFT INC AND   #
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

OS=MacOSX

CC=gcc
CXX=g++

ifndef DEBUG
OPTFLAGS=-O3
else
OPTFLAGS=-DDEBUG
endif

CFLAGS=-g $(OPTFLAGS) -Wall -D$(OS)
CXXFLAGS=$(CFLAGS)
CPPFLAGS=-I$(MOOGSOFT_SRC_HOME)/include $(MYSQLINCLUDES)

LOCALLIBS=-lcrypto -lpthread $(XMLLIBS) $(XSLTLIBS) $(MYSQLLIBS) -framework Foundation -lssl -Wl,-install_name,$(MOOGSOFT_HOME)/lib/$(TARGET)
RTLIB=
NETLIBS=
IDENTLIBS=-lldap_r
BOOSTINCLUDES=-I/usr/local/include/boost-1_33
XMLINCLUDES=`xml2-config --cflags`
XMLLIBS=`xml2-config --libs`
XSLTINCLUDES=`xslt-config --cflags`
XSLTLIBS=`xslt-config --libs`
DYNAMICLIBLDFLAGS=-dynamiclib -L$(MOOGSOFT_HOME)/lib $(LOCALLIBS)
MYSQLINCLUDES=-I$(MYSQLHOME)/include
MYSQLLIBS=-L$(MYSQLHOME)/lib -lmysqlclient
EXELDFLAGS=-bind_at_load -L$(MOOGSOFT_HOME)/lib $(LOCALLIBS)
DYNAMIC_LIB_EXT=dylib
STATIC_LIB_EXT=a
INSTALL=/usr/bin/install
RANLIB=ranlib
PCREFLAGS=$(shell pcre-config --cflags)
PCRELIBS=$(shell pcre-config --libs)


MKDIR=mkdir -p
RM=rm -f
RMDIR=rm -rf
TOUCH=touch
YACC=bison -yvd
LEX=flex 
MV=mv -f
CP=cp
SED=sed
ECHO=echo
CAT=cat
PURIFY=echo "PURIFY NOT FOUND ON MacOSX. Exiting.."; exit 1;
COV=echo "PURECOV NOT FOUND ON MacOSX. Exiting.."; exit 1;
LN=ln
YACC_SED_HACK='s/(YYPARSE_PARAM_ARG)/(void *lexp,void *res)/g'
AR=ar -ru
ID=/usr/bin/id -u
LEX_STAGES=1
LEX_SED_1='s/<FlexLexer.h>/<moo\/Lexer.h>/g'
LEX_SED_2=
YACC_STAGES=1
YACC_APP_SED_1='s/yyparse (void)/yyparse (void *lexp,void *app,void *rule)/g'
YACC_SED_1='s/yyparse (void)/yyparse (void *lexp,void *parser,void *rule)/g'
YACC_SED_2=

#
# JScript
#
JSCC=jscc
JSCOMPDIR=.compiled

#
# Java
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

GENERATE_RPC=1
RPCSVCDIR=/usr/include/rpcsvc
RPCGEN=rpcgen

ifndef MYSQLHOME
MYSQLHOME=/usr/local/mysql
endif

ifndef CPPTESTHOME 
CPPTESTHOME=/opt/local
endif

ifndef PKG_CONFIG_PATH
PKG_CONFIG_PATH=/opt/local/lib/pkgconfig
else
PKG_CONFIG_PATH:=$(PKG_CONFIG_PATH):/opt/local/lib/pkgconfig
endif

SYS_TEST_LIBS=\
		-lldap \

OS_LIBRARY_PATH=DYLD_LIBRARY_PATH
