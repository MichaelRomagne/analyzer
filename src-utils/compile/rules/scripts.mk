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

BIN_DELIVER_DIR=$(MOOGSOFT_HOME)/bin
UTILS_DELIVER_DIR=$(BIN_DELIVER_DIR)/utils

ETC_DELIVER_DIR=$(MOOGSOFT_HOME)/etc
SVC_WRAPPERS_DELIVER_DIR=$(ETC_DELIVER_DIR)/service-wrappers

ifndef NO_INSTALL
DELIVER_CMD=\
	$(INSTALL) moog-init/$(TARGET_SERVICE_INIT) $(UTILS_DELIVER_DIR) ; \
	$(INSTALL) moog-init/$(TARGET_INITD_WRAPPER) $(UTILS_DELIVER_DIR) ; \
	$(INSTALL) moog-init/$(TARGET_INITDS) $(SVC_WRAPPERS_DELIVER_DIR) ; \
	$(INSTALL) sphinx/$(TARGET_INDEXER) $(BIN_DELIVER_DIR) ;
CLEAN_CMD=\
	$(RM) $(UTILS_DELIVER_DIR)/$(TARGET_SERVICE_INIT) ; \
	$(RM) $(UTILS_DELIVER_DIR)/$(TARGET_INITD_WRAPPER) ; \
	$(RM) $(SVC_WRAPPERS_DELIVER_DIR)/*; \
	$(RM) $(BIN_DELIVER_DIR)/$(TARGET_INDEXER) ;
else
DELIVER_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
endif

ifdef NO_BUILD
BUILD_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
else
BUILD_CMD=$(DELIVER_CMD)
endif

$(TARGET): 

test:
	$(TEST_CMDS)

compile:
	$(COMPILE_CMDS)

relink:
	$(MAKE) all
	$(RELINK_CMDS)

deliver: $(TARGET)
	$(MKDIR) $(BIN_DELIVER_DIR)
	$(MKDIR) $(UTILS_DELIVER_DIR)
	$(MKDIR) $(SVC_WRAPPERS_DELIVER_DIR)
	$(DELIVER_CMD)

install: deliver

build: $(TARGET)
	$(BUILD_CMD)

clean:
	$(CLEAN_CMD)
	$(RMDIR) results
