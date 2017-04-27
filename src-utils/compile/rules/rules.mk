#-----------------------------------------------------------#
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
#  it without permission is asked to notify Moogsoft Inc    #
#  on +44 (0)7734 591962 or email to phil@moogsoft.com.     #
#  All intellectual property rights in this source code     #
#  are owned by Moogsoft Inc.  No part of this source code  #
#  may be reproduced, adapted or transmitted in any form or #
#  by any means, electronic, mechanical, photocopying,      #
#  recording or otherwise.                                  #
#                                                           #
#-----------------------------------------------------------#

ifndef RULES_DEFINED
RULES_DEFINED=1

# stop default SCCS get beahviour
%: s.%
%: SCCS/s.%

# generate a list of platform & processor sources in this directory
# e.g. CTimeUtils_Linux.cc CByteOrderSwap_x86.cc
# if a platform and processor file exists for the same prefix there
# will be a link error!
LOCAL_OS_SRCS=$(wildcard *_$(OS).cc ) $(wildcard *_$(PRCSR).cc )

# transform source list to os specific (if exists)
# e.g. CTimeUtils.cc CHwAddress.cc
# ->   CTimeUtils_Linux.cc CHwAddress.cc
ifneq ($(LOCAL_OS_SRCS),)
# This is pretty inefficient but as the number of items
# in the loop is so small this shouldn't be an issue
# for(src $PLATFORM_SRCS)
#   for(local $LOCAL_OS_SRCS)
#       if(src ~= local)
#           file=local
#           break
#   if(file)
#       OS_SRCS += file
#   else
#       OS_SRCS += src
OS_SRCS=$(foreach src, $(PLATFORM_SRCS), $(if $(strip $(foreach local,$(LOCAL_OS_SRCS),$(if $(findstring $(basename $(src)),$(local)),$(local),))), $(foreach local,$(LOCAL_OS_SRCS),$(if $(findstring $(basename $(src)),$(local)),$(local),)), $(src)))
else
OS_SRCS=$(PLATFORM_SRCS)
endif

#
# if you want the library to use openssl, make sure the next line is uncommented
# And we add the cots library here
#
CFLAGS +=   -DHAVE_OPENSSL

INCLUDE_PATH +=-I. -I$(MOOGSOFT_SRC_HOME)/common/include -I$(MOOGSOFT_SRC_HOME)/common


OBJS:=$(OBJS) $(patsubst %.cc, $(OBJS_DIR)/%.o, $(CC_SRCS))
OBJS:=$(OBJS) $(patsubst %.m, $(OBJS_DIR)/%.o, $(OC_SRCS))

#
# And the java classes are...
#
CLASS_DIR=.classes/$(subst .,/,$(PACKAGE))

CLASSES:=$(JAVA_SRCS:%.java= $(CLASS_DIR)/%.class)
APP_CLASSES:=$(CLASSES) $(patsubst %.java, $(CLASS_DIR)/%.class,$(JAVA_SRCS))

#
# Transform the lists of "COTS" jars and "MOOG" jars into classpaths
# that can be used for building the sources and running the sources
# (the difference being the additio of $TARGET.jar).
#  Allow addition of an explicit CLASSPATH for edge-cases
#
blank :=
space := $(blank) $(blank)
#
# ($$) in $$MOOGSOFT_HOME & $$IFAT_COTS_HOME stops expansion of these
# two env variables  
#
COTS_JAR_PATHS=$(addprefix $$IFAT_COTS_HOME/packages/,$(COTS_JARS))
MOOG_JAR_PATHS=$(addprefix $$MOOGSOFT_HOME/lib/,$(MOOG_JARS))
TARGET_JAR_PATH=$(addprefix $$MOOGSOFT_HOME/lib/,$(TARGET).$(JAR_EXT))

RUN_CLASSPATH=$(COTS_JAR_PATHS):$(MOOG_JAR_PATHS):$(CLASSPATH):$(TARGET_JAR_PATH)

# Adds classes to the classpath to allow us to unit test code in the servlets subdirectories.
ifdef ADD_TEST_CLASSES
BLD_CLASSPATH=../.classes:$(COTS_JAR_PATHS):$(MOOG_JAR_PATHS):$(CLASSPATH)
TEST_CLASSPATH=../.classes:$(COTS_JAR_PATHS):$(MOOG_JAR_PATHS):$(CLASSPATH):$(TARGET).$(JAR_EXT)
else
BLD_CLASSPATH=$(COTS_JAR_PATHS):$(MOOG_JAR_PATHS):$(CLASSPATH)
TEST_CLASSPATH=$(COTS_JAR_PATHS):$(MOOG_JAR_PATHS):$(CLASSPATH):$(TARGET).$(JAR_EXT)
endif

BLD_CLASSPATH:=$(strip $(BLD_CLASSPATH) )
RUN_CLASSPATH:=$(strip $(RUN_CLASSPATH) )
TEST_CLASSPATH:=$(strip $(TEST_CLASSPATH) )

BLD_CLASSPATH:=$(subst $(space),:,$(BLD_CLASSPATH) )
RUN_CLASSPATH:=$(subst $(space),:,$(RUN_CLASSPATH) )
TEST_CLASSPATH:=$(subst $(space),:,$(TEST_CLASSPATH) )

#
# This stops variable expansion when passed as arg2 to build_harness.sh
#
RUN_CLASSPATH:=$(subst $$IFAT_COTS_HOME,'$$IFAT_COTS_HOME',$(RUN_CLASSPATH) )
RUN_CLASSPATH:=$(subst $$MOOGSOFT_HOME,'$$MOOGSOFT_HOME',$(RUN_CLASSPATH) )

TEST_CLASSPATH:=$(subst $$IFAT_COTS_HOME,'$$IFAT_COTS_HOME',$(TEST_CLASSPATH) )
TEST_CLASSPATH:=$(subst $$MOOGSOFT_HOME,'$$MOOGSOFT_HOME',$(TEST_CLASSPATH) )

#
# JS compiled sources
#
JSCOMPS:=$(JS_SRCS:%.js=.compiled/%.js)

# define MAKE_PREFIX_SRC_PATH to prefix source file names with their path
# leave undefined for normal behaviour

$(OBJS_DIR)/%.o: %.cc
	$(CXX) -c $(CXXFLAGS) $(CPPFLAGS) $(if $(MAKE_PREFIX_SRC_PATH), $(shell pwd)/)$< -o $@

$(OBJS_DIR)/%.o: %.m
	$(CXX) -c $(CXXFLAGS) -x objective-c++ $(CPPFLAGS) $(if $(MAKE_PREFIX_SRC_PATH), $(shell pwd)/)$< -o $@

$(OBJS_DIR)/%.o: %.c
	$(CC) -c $(CFLAGS) $(CPPFLAGS) $(if $(MAKE_PREFIX_SRC_PATH), $(shell pwd)/)$< -o $@

$(CLASS_DIR)/%.class : ./%.java
	$(JAVAC) -sourcepath . -d $(CLASS_DIR_ROOT) -classpath $(BLD_CLASSPATH) $(JCFLAGS) $(JAVA_SRCS) 

$(JSCOMPDIR)/%.js: %.js
	$(JSCC) $< -o $@

$(OBJS_DIR)/%.d: %.cc
	@set -e; rm -f $@; \
	$(CXX) -MM $(CXXFLAGS) $(CPPFLAGS) $< > $@.$$$$; \
	sed 's,\($*\)\.o[ :]*,$(OBJS_DIR)\/\1.o $@ : ,g' < $@.$$$$ > $@; \
	rm -f $@.$$$$

$(OBJS_DIR)/%.d: %.c
	@set -e; rm -f $@; \
	$(CC) -MM $(CFLAGS) $(CPPFLAGS) $< > $@.$$$$; \
	sed 's,\($*\)\.o[ :]*,$(OBJS_DIR)\/\1.o $@ : ,g' < $@.$$$$ > $@; \
	rm -f $@.$$$$

#DEPS=$(patsubst %.cc, $(OBJS_DIR)/%.d, $(SRCS)) 

ifneq ($(MAKECMDGOALS), clean)
ifdef DEPS
include $(DEPS)
endif
endif

# whatever level we run make test from create a
# results dir
ifeq ($(MAKECMDGOALS), test)
ifeq ($(MAKELEVEL), 0)
TEST_RESULTS_DIR=$(shell pwd)/results
export TEST_RESULTS_DIR
endif
endif

# if this uses BDB add the paths
ifdef USE_BDB
INCLUDE_PATH+=-I$(BDBHOME)/include

# NOTE that the link order is vital here - the BDB libs MUST appear before all
# the other local libs else resd will die when attempting to initialize the BDB
# environment
LOCALLIBS:=-L$(BDBHOME)/lib -ldb $(LOCALLIBS)
endif

# if this uses GLIBMM add the paths
ifdef USE_GLIBMM
INCLUDE_PATH+=`PKG_CONFIG_PATH=$(PKG_CONFIG_PATH) pkg-config --cflags glibmm-2.4`
LOCALLIBS:=`PKG_CONFIG_PATH=$(PKG_CONFIG_PATH)  pkg-config --libs glibmm-2.4` $(LOCALLIBS) -liconv
endif

# if this uses BOOST add the paths
ifdef USE_BOOST
INCLUDE_PATH+=$(BOOSTINCLUDES)
endif

# if this uses libxmlpp, then add the paths
ifdef USE_LIBXMLPP
INCLUDE_PATH+=`PKG_CONFIG_PATH=$(PKG_CONFIG_PATH) pkg-config --cflags libxml++-2.6`
LOCALLIBS+=`PKG_CONFIG_PATH=$(PKG_CONFIG_PATH)  pkg-config --libs libxml++-2.6`
endif

ifdef USE_VXFS
ifdef VXFSHOME
INCLUDE_PATH+=-I$(VXFSHOME)/include

# NOTE that the link order is vital here - the BDB libs MUST appear before all
# the other local libs else resd will die when attempting to initialize the BDB
# environment
LOCALLIBS:=$(LOCALLIBS) -L$(VXFSHOME)/lib -lvxfsutil
endif
endif

ifdef USE_PCRE
INCLUDE_PATH+=$(PCREFLAGS)
LOCALLIBS+=$(PCRELIBS)
endif

endif
