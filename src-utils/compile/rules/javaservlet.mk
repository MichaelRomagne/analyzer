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
DELIVER_DIR=$(APPSERVER_HOME)/webapps
endif

ifndef WAR_BUILD_DIR
WAR_BUILD_DIR=$(MOOGSOFT_HOME)/lib
endif

ifndef NO_INSTALL
ifdef APPSERVER_HOME
DELIVER_CMD=$(INSTALL) $(TARGET).$(WAR_EXT)  $(DELIVER_DIR)
endif
WAR_BUILD_DELIVER_CMD=$(INSTALL) $(TARGET).$(WAR_EXT) $(WAR_BUILD_DIR)
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

ifdef CTXT_FILE
    DO_CTXT=( $(MKDIR) $(CLASS_DIR_ROOT)/META-INF; \
            $(CP) $(CTXT_FILE) $(CLASS_DIR_ROOT)/META-INF/; \
            cd $(CLASS_DIR_ROOT); \
            $(JAR) -uf $@.$(WAR_EXT) META-INF/$(CTXT_FILE) )
else
DO_CTXT=@ echo "No servlet context to install"
endif

$(TARGET): $(JAVA_SRCS)
	@$(MAKE) .prebuild
	$(MAKE) classes
	$(BUILD_WAR) $(CLASS_DIR_ROOT)
	(cd $(CLASS_DIR_ROOT); $(CP) -R $(PKG_ROOT) WEB-INF/classes/ ;)
	$(CP) $(WEB_FILE) $(CLASS_DIR_ROOT)/WEB-INF
	@for i in  $(COTS_JARS) ; \
	do \
		echo "Installing " $(MOOGSOFT_COTS_PKGS)/$$i ; \
		($(INSTALL) $(MOOGSOFT_COTS_PKGS)/$$i $(CLASS_DIR_ROOT)/WEB-INF/lib);  \
	done
	@for i in  $(MOOG_JARS) ; \
	do \
		echo "Installing " $(MOOGSOFT_COTS_PKGS)/$$i ; \
		($(INSTALL) $(MOOGSOFT_HOME)/lib/$$i $(CLASS_DIR_ROOT)/WEB-INF/lib);  \
	done
	(cd $(CLASS_DIR_ROOT); $(JAR) -cvf $@.$(WAR_EXT) $(WAR_ROOT) )
	$(DO_CTXT)
	(cd $(CLASS_DIR_ROOT); $(MV) $@.$(WAR_EXT) ..) 

.prebuild:
	@ $(MKDIR) -p $(CLASS_DIR)
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
	$(MKDIR) $(WAR_BUILD_DIR)
	$(WAR_BUILD_DELIVER_CMD)

install: deliver
	$(DELIVER_CMD)

build: $(TARGET)
	$(BUILD_CMD)

clean:
	$(RM) -rf $(CLASS_DIR_ROOT)/* $(TARGET).$(WAR_EXT)
	$(RM) .prebuild
	$(CLEAN_CMDS)
	$(RM) $(DELIVER_DIR)/$(TARGET).$(WAR_EXT)
	$(RM) $(WAR_BUILD_DIR)/$(TARGET).$(WAR_EXT)
	$(RMDIR) results
