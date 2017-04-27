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

include $(MOOGSOFT_SRC_HOME)/src-utils/compile/rules/rules.mk

CPPFLAGS:=$(CPPFLAGS) $(INCLUDE_PATH)

ifndef NO_TEST_CASES
ifndef TEST_DIRS
TEST_DIRS=testsuite
endif
endif

ifdef TEST_DIRS
CLEAN_CMDS=@ for i in $(TEST_DIRS) ; \
do \
(cd $$i; $(MAKE) clean ); \
done
endif

ifndef DEFAULT_TEST_CMDS
DEFAULT_TEST_CMDS=\
for i in $(TEST_DIRS) ; \
do \
	(cd $$i; \
	$(MAKE) test; \
	if [ "$$?" != "0" ]; then \
		echo "`pwd` make test FAILURE!!!"; \
		echo "`pwd` make test FAILURE!!!" >> $(TEST_RESULTS_DIR)/summary; \
	fi); \
done
endif

ifndef TEST_CMDS
TEST_CMDS= @ $(MAKE);
ifdef NO_TEST_CASES
TEST_CMDS+=echo 'ERROR!!!! NO UNIT TESTS EXIST FOR $(TARGET)';
else
TEST_CMDS+=$(DEFAULT_TEST_CMDS)
endif
endif

ifndef COMPILE_CMDS
COMPILE_CMDS= @ $(MAKE);
ifdef NO_TEST_CASES
COMPILE_CMDS+=echo 'ERROR!!!! NO UNIT TESTS EXIST FOR $(TARGET)';
else
COMPILE_CMDS+=\
for i in $(TEST_DIRS) ; \
do \
	(cd $$i; $(MAKE) compile ); \
done;
endif
endif

ifndef RELINK_CMDS
RELINK_CMDS= @ $(MAKE);
ifndef NO_TEST_CASES
RELINK_CMDS+=\
for i in $(TEST_DIRS) ; \
do \
	(cd $$i; $(MAKE) relink ); \
done;
endif
endif

