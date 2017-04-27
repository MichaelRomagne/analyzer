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

include $(MOOGSOFT_SRC_HOME)/src-utils/compile/rules/stdmodule.mk

LDFLAGS:=$(LDFLAGS) $(DYNAMICLIBLDFLAGS) $(LIB_PATH)

ifndef JAR_DELIVER_DIR
JAR_DELIVER_DIR=$(MOOGSOFT_HOME)/packages
endif

ifndef SCRIPT_DELIVER_DIR
SCRIPT_DELIVER_DIR=$(MOOGSOFT_HOME)/moobots
endif

ifdef SCRIPTS
SCRIPT_DELIVER_CMD=@ for i in  $(SCRIPTS) ; \
     do \
     ($(INSTALL) $$i $(SCRIPT_DELIVER_DIR) );  \
     done
else
SCRIPT_DELIVER_CMD= @ echo "No 3rd Party Jars to install"
endif

ifdef SCRIPTS
CLEAN_SCRIPT_CMD=@ for i in  $(SCRIPTS) ; \
     do \
     ($(RM) $(MOOGSOFT_HOME)/moobots/$$i );  \
     done
else
CLEAN_SCRIPT_CMD= @ echo "No scripts to remove"
endif

ifndef DELIVER_DIR
DELIVER_DIR=$(MOOGSOFT_HOME)/packages
endif

ifndef CONF_DELIVER_DIR
CONF_DELIVER_DIR=$(MOOGSOFT_HOME)/config
endif

ifndef NO_INSTALL
DELIVER_CMD=$(INSTALL) $(TARGET).$(JAR_EXT) $(DELIVER_DIR)
else
DELIVER_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
endif

ifdef PKG_CONFIG
CONF_DELIVER_CMD= $(INSTALL) $(PKG_CONFIG) $(CONF_DELIVER_DIR)
else
CONF_DELIVER_CMD=@ echo "NO CONFIG INSTALLED FOR $(TARGET)"
endif

ifndef NO_BUILD
BUILD_CMD=$(DELIVER_CMD)
else
BUILD_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
endif

ifndef PKG_ROOT
PKG_ROOT=$(TARGET)
endif


$(TARGET): $(JS_SRCS)
	$(MAKE) compiled

compiled: $(JSCOMPS)

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
	$(MKDIR) $(CONF_DELIVER_DIR)
	$(MKDIR) $(SCRIPT_DELIVER_DIR)
	$(DELIVER_CMD)
	$(CONF_DELIVER_CMD)
	@echo "Installing scripts"
	$(SCRIPT_DELIVER_CMD)

install: deliver

build: $(TARGET)
	$(BUILD_CMD)

clean:
	$(RM) -rf $(JSCOMPDIR)/* $(TARGET)
	$(CLEAN_CMDS)
