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

LDFLAGS:=$(LDFLAGS) $(DYNAMICLIBLDFLAGS) $(LIB_PATH)

BOT_DELIVER_DIR=$(MOOGSOFT_HOME)/bots
MOOBOT_DELIVER_DIR=$(BOT_DELIVER_DIR)/moobots
CONFIG_DELIVER_DIR=$(MOOGSOFT_HOME)/config


ifndef DELIVER_DIR
DELIVER_DIR=$(MOOGSOFT_HOME)/lib
endif

ifndef CONF_DELIVER_DIR
CONF_DELIVER_DIR=$(MOOGSOFT_HOME)/config
endif

ifndef NO_INSTALL
DELIVER_CMD=\
	$(MKDIR) $(DELIVER_DIR) ; \
    $(INSTALL) $(TARGET).$(JAR_EXT) $(DELIVER_DIR)
else
DELIVER_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
endif

ifndef NO_BUILD
BUILD_CMD=$(DELIVER_CMD)
else
BUILD_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
endif

ifndef PKG_ROOT
PKG_ROOT=$(TARGET)
endif



ifdef CONFIG
CONFIG_DELIVER_CMD=\
	$(MKDIR) $(CONFIG_DELIVER_DIR) ; \
	for i in  $(CONFIG) ; \
	do \
	($(INSTALL) $$i $(CONFIG_DELIVER_DIR) );  \
	done
else
CONFIG_DELIVER_CMD=@ echo "No config installed for $(TARGET)"
endif

ifdef CONFIG
CLEAN_CONFIG_CMD=@ for i in  $(CONFIG) ; \
     do \
     ($(RM) $(CONFIG_DELIVER_DIR)/$$i );  \
     done
else
CLEAN_CONFIG_CMD= @ echo "No config to remove for $(TARGET)"
endif




ifdef MOOBOTS
BOT_DELIVER_CMD=\
	$(MKDIR) $(MOOBOT_DELIVER_DIR) ; \
	for i in  $(MOOBOTS) ; \
	do \
	($(INSTALL) $$i $(MOOBOT_DELIVER_DIR) );  \
	done
else
BOT_DELIVER_CMD= @ echo "No bots to install for $(TARGET)"
endif

ifdef MOOBOTS
CLEAN_BOT_CMD=@ for i in  $(MOOBOTS) ; \
     do \
     ($(RM) $(MOOBOT_DELIVER_DIR)/$$i );  \
     done
else
CLEAN_BOT_CMD= @ echo "No bots to remove for $(TARGET)"
endif






$(TARGET): $(JAVA_SRCS)
	@$(MAKE) .prebuild
	$(MAKE) classes
	(cd $(CLASS_DIR_ROOT); $(JAR) -cvf $@.$(JAR_EXT) $(PKG_ROOT); $(MV) $@.$(JAR_EXT) ..) 

.prebuild:
	@ $(MKDIR) -p $(CLASS_DIR)
	$(EXT_JAR_DELIVER_CMD)
	$(JAVAC) -sourcepath . -d $(CLASS_DIR_ROOT) -classpath $(BLD_CLASSPATH) $(JCFLAGS) $(JAVA_SRCS)
	@ $(TOUCH) .prebuild

classes: $(CLASSES)

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
	$(DELIVER_CMD)
	$(CONFIG_DELIVER_CMD)
	$(BOT_DELIVER_CMD)
	@echo "Installing scripts"
	$(MOOBOT_DELIVER_CMD)

install: deliver

build: $(TARGET)
	$(BUILD_CMD)

clean:
	$(RM) -rf $(CLASS_DIR)/* $(TARGET).$(JAR_EXT) $(APP_CLEAN)
	$(RM) .prebuild
	$(CLEAN_CMDS)
	$(CLEAN_BOT_CMD)
	$(CLEAN_CONFIG_CMD)
	# @ echo "Cleaning config"
	# @ if [ -f "$(CONF_DELIVER_DIR)/$(PKG_CONFIG)" ]; then \
			$(RM) $(CONF_DELIVER_DIR)/$(PKG_CONFIG) ;\
	# fi; 
	$(RM) $(DELIVER_DIR)/$(TARGET).$(JAR_EXT)
	$(RMDIR) results
