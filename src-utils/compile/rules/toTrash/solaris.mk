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
OS=Solaris2
CC=gcc
CXX=g++
GCJ=gcj
GCJH=gcjh
GCJ_OPTS=-g --classpath $(CLASSPATH) GCJH_OPTS=--classpath $(CLASSPATH) 

ifndef DEBUG    
OPTFLAGS=-O3
else
OPTFLAGS=-DDEBUG
endif

CFLAGS=-g $(OPTFLAGS) -Wall -D$(OS) -fPIC -D_POSIX_PTHREAD_SEMANTICSCXXFLAGS=$(CFLAGS)
CPPFLAGS=-I$(MOOGSOFT_SRC_HOME)/include
LOCALLIBS=-lm -lstdc++ -lresolv -L$(OPENSSLHOME)/lib -lcrypto -lpthread $(XMLLIBS) $(XSLTLIBS)
RTLIB=-lrt
NETLIBS=-lsocket -lnsl $(RTLIB) -ldl
IDENTLIBS=-lpam -lldap_r
POSTGRESLIBS=-L$(POSTGRESHOME)/lib -lpq
XMLINCLUDES=`xml2-config --cflags`
XMLLIBS=`xml2-config --libs`
XSLTINCLUDES=`xslt-config --cflags`
XSLTLIBS=`xslt-config --libs`
DYNAMICLIBLDFLAGS=-fPIC -shared -L$(MOOGSOFT_HOME)/lib $(LOCALLIBS)EXELDFLAGS=-L$(MOOGSOFT_HOME)/lib $(LOCALLIBS)
DYNAMIC_LIB_EXT=so
STATIC_LIB_EXT=a
INSTALL=/usr/local/bin/install
RANLIB=ranlib
MKDIR=mkdir -p
RM=rm -fR
MDIR=rm -rf
YACC=/opt/FSFbison/bin/bison -yvd
LEX=flex 
MV=mv -f
SED=sed
ECHO=echo
CAT=cat
PURIFY=purify
PUREOPT= -demangle-program=/opt/sfw/bin/gc++filt -max_threads=100 -chain_length=30COV=purecov
COVOPT= -demangle-program=/opt/sfw/bin/gc++filt LN=ln
YACC_SED_HACK='s/(YYPARSE_PARAM)/(void *lexp,void *res)/g'
AR=ar -ruI
D=/usr/xpg4/bin/id -u
RPCSVCDIR=/usr/include/rpcsvc
RPCGEN=rpcgen -C
ifndef BDBHOME
BDBHOME=/usr/local/BerkeleyDB.4.4
endif
ifndef POSTGRESHOME
POSTGRESHOME=/usr/local/pgsql
endif
ifndef CPPTESTHOME
CPPTESTHOME=/usr/local
endif
ifndef OPENSSLHOME
OPENSSLHOME=/usr/local/ssl
endif
ifndef SAMBASRCHOME
SAMBASRCHOME=/usr/local/samba4/source
endif
ifndef PKG_CONFIG_PATH
PKG_CONFIG_PATH=/usr/local/lib/pkgconfig
else
PKG_CONFIG_PATH:=/usr/local/lib/pkgconfig:$(PKG_CONFIG_PATH)
endif
SYS_TEST_LIBS=\	
		-lpopt \	
		-ldl \	
		-lnsl \	
		-liconv \	
		-lsocket \	
		-L$(MOOGSOFT_SRC_HOME)/test_tools/torture/lib \	
		-lsmbtorture
OS_LIBRARY_PATH=LD_LIBRARY_PATH
