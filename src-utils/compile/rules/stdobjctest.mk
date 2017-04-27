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

include $(MOOGSOFT_SRC_HOME)/src-utils/compile/rules/stdtest.mk

DEFAULT_TEST_CMDS=`pwd`/$(TARGET) $(TEST_ARGS)

INCLUDE_PATH:=$(INCLUDE_PATH) -I$(CPPTESTHOME)/include
LIB_PATH:=$(LIB_PATH) -L$(CPPTESTHOME)/lib

# all the test cases this is used for use the Core library
# We keep the dependancies in here to a minimum so we can test
# libraries in isolation. Otherwise All libraries would be needed before
# we could test any.
REQUIRED_LIBS= -lcppunit

LIBS:=$(filter-out $(REQUIRED_LIBS), $(LIBS)) $(REQUIRED_LIBS)

CPPFLAGS:=$(CPPFLAGS) $(INCLUDE_PATH)
LDFLAGS:=$(LDFLAGS) $(EXELDFLAGS) $(LIB_PATH)


# add extra library linking if required
ifdef EXTRA_LIB_PATH
LDFLAGS+=-L$(EXTRA_LIB_PATH)
endif

# do we db install before and after?
ifndef PRE_TEST_CMDS
PRE_TEST_CMDS:=$(DB_INSTALL)
endif

ifndef POST_TEST_CMDS
POST_TEST_CMDS:=$(DB_INSTALL)
endif

# prepend parent directory to $(DY)LD_LIBRARY_PATH
PARENT_LIBRARY_PATH_CMDS=$(OS_LIBRARY_PATH)=..:$$$(OS_LIBRARY_PATH); export $(OS_LIBRARY_PATH)


$(TARGET): $(OBJS)
	$(CXX) -o $@ $(OBJS) $(LDFLAGS) $(LIBS)

clean:
	$(RM) $(OBJS_DIR)/*.o $(OBJS_DIR)/*.d* $(APP_CLEAN) core $(TARGET)
	$(RMDIR) results
	$(RMDIR) data

relink:
	$(RM) $(TARGET)
	$(MAKE)

compile: $(TARGET)

test: $(TARGET)

