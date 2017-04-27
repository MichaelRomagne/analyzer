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

ifndef MOOB_DELIVER_DIR
MOOB_DELIVER_DIR=$(MOOGSOFT_HOME)/moobs
endif

ifndef GADGET_DELIVER_DIR
GADGET_DELIVER_DIR=$(MOOGSOFT_HOME)/gadgets
endif

ifdef GADGETS
GADGET_DELIVER_CMD=@ for i in  $(GADGETS) ; \
     do \
     ($(INSTALL) $$i $(GADGET_DELIVER_DIR) );  \
     done
else
GADGET_DELIVER_CMD= @ echo "No gadgets to install"
endif

ifdef GADGETS
CLEAN_GADGET_CMD=@ for i in  $(GADGETS) ; \
     do \
     ($(RM) $(MOOGSOFT_HOME)/gadgets/$$i );  \
     done
else
CLEAN_GADGET_CMD= @ echo "No gadgets to remove"
endif

ifdef MOOBS
MOOB_DELIVER_CMD=@ for i in  $(MOOBS) ; \
     do \
     ($(INSTALL) $$i $(MOOB_DELIVER_DIR) );  \
     done
else
MOOB_DELIVER_CMD= @ echo "No moobs to install"
endif

ifdef MOOBS
CLEAN_MOOB_CMD=@ for i in  $(MOOBS) ; \
     do \
     ($(RM) $(MOOGSOFT_HOME)/moobs/$$i );  \
     done
else
CLEAN_MOOB_CMD= @ echo "No moobs to remove"
endif

ifdef NO_BUILD
BUILD_CMD=@ echo "$(TARGET) IS NOT INSTALLED"
else
BUILD_CMD=$(DELIVER_CMD)
endif

$(TARGET): $(GADGETS) $(MOOBS)

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
	@echo "Installing moobs"
	@ $(MKDIR) $(MOOB_DELIVER_DIR)
	@ $(MKDIR) $(GADGET_DELIVER_DIR)
	$(MOOB_DELIVER_CMD)
	@echo "Installing gadgets"
	$(GADGET_DELIVER_CMD)

install: deliver

build: $(TARGET)
	echo "No compilation necessary"

clean:
	$(CLEAN_GADGET_CMD)
	$(CLEAN_MOOB_CMD)
	$(RMDIR) results
