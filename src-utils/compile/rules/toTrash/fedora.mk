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

OS=Linux

CC=gcc
CXX=g++

ifndef DEBUG    
OPTFLAGS:=-O3
else
OPTFLAGS=-DDEBUG
endif

CFLAGS=-g $(OPTFLAGS) -Wall -D$(OS) -fPIC -D_POSIX_PTHREAD_SEMANTICS -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_REENTRANT
CXXFLAGS=$(CFLAGS)
CPPFLAGS=-DLINUX -DFedora -I$(MOOGSOFT_SRC_HOME)/include $(MYSQLINCLUDES)
LOCALLIBS=-lm -lstdc++ -lresolv -lcrypto -lpthread $(XMLLIBS) $(XSLTLIBS) $(MYSQLLIBS)
NETLIBS=-lnsl -lrt -ldl
IDENTLIBS=-lpam -lldap_r
BOOSTINCLUDES=-I/usr/local/include/boost-1_33
XMLINCLUDES=`xml2-config --cflags`
XMLLIBS=`xml2-config --libs`
XSLTINCLUDES=`xslt-config --cflags`
XSLTLIBS=`xslt-config --libs`
POSTGRESLIBS=-L$(POSTGRESHOME)/lib -lpq
DYNAMICLIBLDFLAGS=-fPIC -shared -L$(MOOGSOFT_HOME)/lib $(LOCALLIBS)
MYSQLINCLUDES=-I/usr/include/mysql
MYSQLLIBS=-L/usr/lib/mysql -lmysqlclient
EXELDFLAGS=-L$(MOOGSOFT_HOME)/lib $(LOCALLIBS)
DYNAMIC_LIB_EXT=so
STATIC_LIB_EXT=a
INSTALL=/usr/bin/install
RANLIB=ranlib

MKDIR=mkdir -p
RM=rm -f
RMDIR=rm -rf
MV=mv -f
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

RPCSVCDIR=/usr/include/rpcsvc
RPCGEN=rpcgen -C

ifndef BDBHOME
BDBHOME=/usr/local/BerkeleyDB.4.4
endif

ifndef POSTGRESHOME
POSTGRESHOME=/usr/local/pgsql
endif

ifndef MYSQLHOME
MYSQLHOME=/opt/mysql
endif

ifndef CPPTESTHOME
CPPTESTHOME=/usr/local
endif

ifndef OPENSSLHOME
OPENSSLHOME=/usr/local/ssl
endif

ifndef PKG_CONFIG_PATH
PKG_CONFIG_PATH=/usr/local/lib/pkgconfig
else
PKG_CONFIG_PATH:=/usr/local/lib/pkgconfig:$(PKG_CONFIG_PATH)
endif

ifndef JUNIT_JAR
JUNIT_JAR=/usr/local/junit4.3/junit-4.3.jar
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
