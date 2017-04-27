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

ETC_DELIVER_DIR=$(MOOGSOFT_HOME)/etc

MIBS_DELIVER_DIR=$(ETC_DELIVER_DIR)/mibs

ifdef MIBS
MIBS_DELIVER_CMD=@ for i in  $(MIBS) ; \
	do \
	( $(INSTALL) $$i $(MIBS_DELIVER_DIR) );  \
	done
else
MIBS_DELIVER_CMD= @ echo "No Mibs to install"
endif

ifdef MIBS
CLEAN_MIBS_CMD=@ for i in  $(MIBS) ; \
	do \
	($(RM) $(MIBS_DELIVER_DIR)/$$i );  \
	done
else
CLEAN_MIBS_CMD= @ echo "No scripts to remove"
endif

ifdef NO_BUILD
BUILD_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
else
BUILD_CMD=$(DELIVER_CMD)
endif

$(TARGET): $(MIBS)

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
	$(MKDIR) $(MIBS_DELIVER_DIR)
	@echo "Installing scripts"
	$(MIBS_DELIVER_CMD)

install: deliver

build: $(TARGET)
	echo "No compilation necessary"

clean:
	$(CLEAN_MIBS_CMD)
