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

include $(MOOGSOFT_SRC_HOME)/src-utils/compile/rules/stdmodule.mk

LDFLAGS:=$(LDFLAGS) $(EXELDFLAGS) $(LIB_PATH)

ps_string=$(shell ps -el | grep  cassandra | grep -v grep)

#  etc
#  |-- cots
#  |   |-- cassandra
#  |   |-- httpd
#  |   |-- mysql
#  |   |-- sphinx
#  |   `-- tomcat
#  |-- mibs
#  |-- moog
#  `-- moog-init

CONFIG_DELIVER_DIR=$(MOOGSOFT_HOME)/config

ETC_DELIVER_DIR=$(MOOGSOFT_HOME)/etc

ETC_COTS_DELIVER_DIR=$(ETC_DELIVER_DIR)/cots
ETC_IFAT_DELIVER_DIR=$(ETC_DELIVER_DIR)/ifat

MYSQL_DELIVER_DIR=$(ETC_COTS_DELIVER_DIR)/mysql


DELIVER_CMD= \
	$(INSTALL) ifat/$(SQL_FILE) $(ETC_IFAT_DELIVER_DIR); \
    $(INSTALL) ifat/$(SYSTEM_CONF_FILE) $(CONFIG_DELIVER_DIR);

ifdef NO_BUILD
BUILD_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
else
BUILD_CMD=$(DELIVER_CMD)
endif

loaddb: ifat/$(SQL_FILE)
	@ $(MAKE) .loadsql

.loadsql: ifat/$(SQL_FILE)
	@echo "Loading db"
	@ mysql --user=root < ifat/$(SQL_FILE)
	@touch .loadsql

deliver: $(TARGET)
	$(MKDIR) $(ETC_DELIVER_DIR)
	$(MKDIR) $(ETC_IFAT_DELIVER_DIR)
	$(MKDIR) $(CONFIG_DELIVER_DIR)
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
	@ $(RM) $(ETC_IFAT_DELIVER_DIR)/$(SQL_FILE)
	@ $(RM) $(MYSQL_DELIVER_DIR)/$(MYSQL_CONF_FILE)
	@ $(RM) $(CONFIG_DELIVER_DIR)/$(SYSTEM_CONF_FILE)

	@ $(RM) .loadsql
	@ $(RM) .loadreferencesql
	@ $(RM) .loadcass

