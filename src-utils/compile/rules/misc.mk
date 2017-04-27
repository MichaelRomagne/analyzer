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
SVCS_DELIVER_DIR=$(ETC_DELIVER_DIR)/service-wrappers

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

#
# Service Wrappers
#
SVCS_DELIVER_CMD=
ifdef SVC_WRAPPERS
SVCS_DELIVER_CMD=\
	@ $(MKDIR) $(SVCS_DELIVER_DIR) ; \
    for i in  $(SVC_WRAPPERS) ; \
    do \
    ($(INSTALL) $$i $(SVCS_DELIVER_DIR) );  \
    done
endif

SVCS_CLEAN_CMD=
ifdef SVC_WRAPPERS
SVCS_CLEAN_CMD=@ for i in  $(SVC_WRAPPERS) ; \
     do \
     ($(RM) $(SVCS_DELIVER_DIR)/$$i );  \
     done
endif



ifdef NO_BUILD
BUILD_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
else
BUILD_CMD=$(DELIVER_CMD)
endif




$(TARGET): $(UTILS) $(SVC_WRAPPERS) $(SCRIPTS)

test:
	$(TEST_CMDS)

compile:
	echo "No compilation necessary"

relink:
ifdef TARGET
	$(RM) $(TARGET)
	$(RM) $(DELIVER_DIR)/$(TARGET)
endif
	$(MAKE) all
	$(RELINK_CMDS)

deliver: $(TARGET)

	@echo "Installing scripts, utils, svc wrappers etc..."
	$(SCRIPTS_DELIVER_CMD)
	$(UTILS_DELIVER_CMD)
	$(SVCS_DELIVER_CMD)

install: deliver

build: $(TARGET)
	echo "No compilation necessary"

clean:
	@echo "Removing scripts, utils, svc wrappers etc..."
	$(SCRIPTS_CLEAN_CMD)
	$(SVCS_CLEAN_CMD)
	$(UTILS_CLEAN_CMD)
