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

include $(MOOGSOFT_SRC_HOME)/src-utils/compile/rules/stdmodule.mk

LDFLAGS:=$(LDFLAGS) $(DYNAMICLIBLDFLAGS) $(LIB_PATH)

DELIVER_DIR=$(MOOGSOFT_HOME)/lib

ifndef NO_INSTALL
DELIVER_CMD=$(INSTALL) $(TARGET) $(DELIVER_DIR)
else
DELIVER_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
endif

ifndef NO_BUILD
BUILD_CMD=$(DELIVER_CMD)
else
BUILD_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
endif

$(TARGET): $(OBJS)
	$(CXX) -o $@ $(OBJS) $(LDFLAGS) $(LIBS)

test:
	$(TEST_CMDS)

compile:
	$(COMPILE_CMDS)

relink:
ifdef TARGET
	$(RM) $(TARGET)
	$(RM) $(DELIVER_DIR)/$(TARGET)
endif
	$(RELINK_CMDS)
	$(MAKE) all

deliver: $(TARGET)
	$(MKDIR) $(DELIVER_DIR)
	$(DELIVER_CMD)

install: deliver

build: $(TARGET)
	$(BUILD_CMD)

clean:
	$(RM) $(OBJS_DIR)/*.o $(OBJS_DIR)/*.d* $(APP_CLEAN) core $(TARGET)
	$(CLEAN_CMDS)
ifdef TARGET
	$(RM) $(DELIVER_DIR)/$(TARGET)
endif
	$(RMDIR) results
