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

ifndef DEFAULT_TEST_CMDS
DEFAULT_TEST_CMDS=`pwd`/$(TARGET) $(TEST_ARGS)

# add directory if defined
ifdef TEST_RESULTS_DIR
DEFAULT_TEST_CMDS+= -dir $(TEST_RESULTS_DIR)
endif

ifdef TEST_CASE
DEFAULT_TEST_CMDS+= $(TEST_CASE)
endif

endif # ifndef DEFAULT_TEST_CMDS


# add extra library linking if required
ifdef EXTRA_LIB_PATH
# prepend directory to $(DY)LD_LIBRARY_PATH
DEFAULT_TEST_CMDS:=$(OS_LIBRARY_PATH)=$(EXTRA_LIB_PATH):$$$(OS_LIBRARY_PATH);export $(OS_LIBRARY_PATH); $(DEFAULT_TEST_CMDS)
endif

#if we haven't defined a specific test just run it
ifndef TEST_CMDS
TEST_CMDS=$(DEFAULT_TEST_CMDS)
endif

ifndef PRE_TEST_CMDS
PRE_TEST_CMDS=$(DB_INSTALL)
endif

ifndef POST_TEST_CMDS
POST_TEST_CMDS=sleep 1;$(DB_INSTALL)
endif
