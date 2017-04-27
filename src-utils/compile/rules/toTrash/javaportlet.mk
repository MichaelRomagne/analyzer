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

ifndef DELIVER_DIR
DELIVER_DIR=$(PORTLETCNTR_HOME)/webapps
endif

ifndef NO_INSTALL
DELIVER_CMD=$(INSTALL) $(TARGET).$(WAR_EXT)  $(DELIVER_DIR)
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

$(TARGET): $(CLASSES)
	$(BUILD_WAR) $(CLASS_DIR)
	(cd $(CLASS_DIR); $(CP) *.class WEB-INF/classes/ ;)
	$(CP) $(WEB_FILE) $(CLASS_DIR)/WEB-INF
	(cd $(CLASS_DIR); $(JAR) -cvf $@.$(WAR_EXT) $(WAR_ROOT); $(MV) $@.$(WAR_EXT) ..) 

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

install: deliver
	$(DELIVER_CMD)

build: $(TARGET)
	$(BUILD_CMD)

clean:
	$(RM) -rf $(CLASS_DIR)/* $(TARGET).$(WAR_EXT)
	$(CLEAN_CMDS)
	$(RM) $(DELIVER_DIR)/$(TARGET).$(WAR_EXT)
	$(RMDIR) results
