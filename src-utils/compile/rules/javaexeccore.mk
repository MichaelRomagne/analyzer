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

EXT_JAR_DELIVER_DIR=$(MOOGSOFT_HOME)/lib/cots

ifndef EXT_JAR_DELIVER_CMD
EXT_JAR_DELIVER_CMD=@ for i in $(COTS_JARS) ; \
     do \
     $(INSTALL) $(IFAT_COTS_HOME)/packages/$$i $(EXT_JAR_DELIVER_DIR);  \
     done
endif

ifndef TARGET_DIR
DELIVER_DIR=$(MOOGSOFT_HOME)/bin
else
DELIVER_DIR=$(MOOGSOFT_HOME)/$(TARGET_DIR)
endif

JAR_DELIVER_DIR=$(MOOGSOFT_HOME)/lib

ifndef CONF_DELIVER_DIR
CONF_DELIVER_DIR=$(MOOGSOFT_HOME)/config
endif

ifndef TARGET_MAIN_CLASS
TARGET_MAIN_CLASS=$(TARGET)
endif

ifndef TARGET_VM_ARGS
TARGET_VM_ARGS=" "
endif

ifndef NO_INSTALL
DELIVER_CMD=$(INSTALL) $(TARGET) $(DELIVER_DIR)
JAR_DELIVER_CMD=$(INSTALL) $(TARGET).$(JAR_EXT) $(JAR_DELIVER_DIR)
ifndef NO_APP_CONF
CONF_DELIVER_CMD=$(INSTALL) $(TARGET_CONF) $(CONF_DELIVER_DIR)
endif
else
DELIVER_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
endif

ifdef NO_BUILD
BUILD_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
else
BUILD_CMD=$(DELIVER_CMD)
endif





BIN_DELIVER_DIR=$(MOOGSOFT_HOME)/bin
UTILS_DELIVER_DIR=$(BIN_DELIVER_DIR)/utils

#
# Production scripts
#
SCRIPTS_DELIVER_CMD=
ifdef SCRIPTS
SCRIPTS_DELIVER_CMD=\
	@ $(MKDIR) $(BIN_DELIVER_DIR) ; \
	for i in  $(SCRIPTS) ; \
	do \
	($(INSTALL) $$i $(BIN_DELIVER_DIR) );  \
	done
endif

SCRIPTS_CLEAN_CMD=
ifdef SCRIPTS
SCRIPTS_CLEAN_CMD=\
    @ for i in  $(SCRIPTS) ; \
    do \
    ($(RM) $(BIN_DELIVER_DIR)/$$i );  \
    done
endif

#
# Utilities
#
UTILS_DELIVER_CMD=
ifdef UTILS
UTILS_DELIVER_CMD=\
	@ $(MKDIR) $(UTILS_DELIVER_DIR) ; \
    for i in  $(UTILS) ; \
    do \
    ($(INSTALL) $$i $(UTILS_DELIVER_DIR) );  \
    done
endif

UTILS_CLEAN_CMD=
ifdef UTILS
UTILS_CLEAN_CMD=@ for i in  $(UTILS) ; \
     do \
     ($(RM) $(UTILS_DELIVER_DIR)/$$i );  \
     done
endif

.prebuild:
	@ $(MKDIR) -p $(CLASS_DIR)
	@ $(MKDIR) -p $(EXT_JAR_DELIVER_DIR)
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
	$(MAKE) all
	$(RELINK_CMDS)

deliver: $(TARGET)
	$(MKDIR) $(DELIVER_DIR)
	$(MKDIR) $(CONF_DELIVER_DIR)
	$(MKDIR) $(EXT_JAR_DELIVER_DIR)
	$(DELIVER_CMD)
	$(JAR_DELIVER_CMD)
	$(CONF_DELIVER_CMD)
	$(UTILS_DELIVER_CMD)
	$(SCRIPTS_DELIVER_CMD)
	@echo "Installing 3rd Party Jars" '$(EXT_JAR_DELIVER_CMD)'
	$(EXT_JAR_DELIVER_CMD)

install: deliver

build: $(TARGET)
	$(BUILD_CMD)

clean:
	@ echo "Cleaning 1..."
	$(RM) -rf $(CLASS_DIR)/* $(TARGET).$(JAR_EXT) $(TARGET)
	$(RM) .prebuild
	@ echo "Cleaning 2..."
	$(CLEAN_CMDS)
	@ echo "Cleaning bins"
	$(RM) -rf $(DELIVER_DIR)/$(TARGET)
	$(SCRIPTS_CLEAN_CMDS)
	$(UTILS_CLEAN_CMD)
	@ echo "Cleaning config"
	@ if [ -f "$(CONF_DELIVER_DIR)/$(TARGET_CONF)" ]; then \
		$(RM) $(CONF_DELIVER_DIR)/$(TARGET_CONF) ;\
	fi; 

	$(RM) $(JAR_DELIVER_DIR)/$(TARGET).$(JAR_EXT) $(DELIVER_DIR)/$(TARGET)
	$(RMDIR) results
