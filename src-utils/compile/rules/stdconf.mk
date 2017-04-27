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
#  on +44 (0)7734 5919621 or email to phil@moogsoft.com.   #
#  All intellectual property rights in this source code     #
#  are owned by Moogsoft Inc.  No part of this source code #
#  may be reproduced, adapted or transmitted in any form or #
#  by any means, electronic, mechanical, photocopying,      #
#  recording or otherwise.                                  #
#                                                           #
#-----------------------------------------------------------#

include $(MOOGSOFT_SRC_HOME)/src-utils/compile/rules/stdmodule.mk

LDFLAGS:=$(LDFLAGS) $(EXELDFLAGS) $(LIB_PATH)

ifndef DELIVER_DIR
DELIVER_DIR=$(MOOGSOFT_HOME)/bin
endif

ifndef CONF_DELIVER_DIR
CONF_DELIVER_DIR=$(MOOGSOFT_HOME)/config
endif

$(TARGET): $(CONFIG_FILES)

compile: deliver

relink: deliver

deliver: $(TARGET)
	$(MKDIR) $(CONF_DELIVER_DIR)
		@ for i in $(CONFIG_FILES) ; \
		do \
		$(INSTALL) $$i $(CONF_DELIVER_DIR); \
		done

install: deliver

build: deliver

clean:
	$(CLEAN_CMDS)
