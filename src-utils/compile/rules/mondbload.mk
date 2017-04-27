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

LDFLAGS:=$(LDFLAGS) $(EXELDFLAGS) $(LIB_PATH)

ps_string=$(shell ps -el | grep  cassandra | grep -v grep)

ifndef DELIVER_DIR
DELIVER_DIR=$(MOOGSOFT_HOME)/etc
endif

ifndef CONF_DELIVER_DIR
CONF_DELIVER_DIR=$(MOOGSOFT_HOME)/etc
endif

ifndef NO_INSTALL
DELIVER_CMD=$(INSTALL) $(MONITOR_SQL_FILE) $(DELIVER_DIR);$(INSTALL) $(MONITOR_JS_FILE) $(DELIVER_DIR)

ifndef NO_APP_CONF
CONF_DELIVER_CMD=$(INSTALL) $(MONITOR_SQL_FILE) $(DELIVER_DIR)
MIME_DELIVER_CMD=
endif
else
DELIVER_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
endif

ifdef NO_BUILD
BUILD_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
else
BUILD_CMD=$(DELIVER_CMD)
endif

loadmonitordb: $(MONITOR_SQL_FILE)
	@ $(MAKE) .loadmonitordb

.loadmonitordb: $(MONITOR_SQL_FILE)
	@echo "Loading monitor db"
	@ mysql --user=root < $(MONITOR_SQL_FILE)
	@touch .loadmonitordb

loadmongodb: $(MONITOR_JS_FILE)
	@ $(MAKE) .loadmongodb

.loadmongodb: $(MONITOR_JS_FILE)
	@echo "Loading monitor mongo db scripts"
	@ mongo $(MONITOR_JS_FILE)
	@touch .loadmongodb

deliver: $(TARGET)
	$(MKDIR) $(CONF_DELIVER_DIR)
	$(DELIVER_CMD)

compile:
	@$(COMPILE_CMDS)

relink:
ifdef TARGET
	$(RM) $(TARGET)
	$(RM) $(DELIVER_DIR)/$(TARGET)
endif
	$(MAKE) all
	$(RELINK_CMDS)

build: $(TARGET)
	$(BUILD_CMD)

test:
	$(TEST_CMDS)

clean:
	@ echo "Cleaning results"
	@ $(RM) $(DELIVER_DIR)/$(MONITOR_SQL_FILE)
	@ $(RM) $(DELIVER_DIR)/$(MONITOR_JS_FILE)
	@ $(RM) .loadmonitordb
	@ $(RM) .loadmongodb
