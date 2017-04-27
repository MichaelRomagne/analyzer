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

include $(MOOGSOFT_SRC_HOME)/src-utils/compile/rules/javaexeccore.mk

$(TARGET): $(JAVA_SRCS)
	@$(MAKE) .prebuild
	$(MAKE) classes
	(cd $(CLASS_DIR_ROOT); $(JAR) -cvf $@.$(JAR_EXT) $(PKG_ROOT); $(MV) $@.$(JAR_EXT) ..)
	$(BUILD_APP) $(TARGET) $(RUN_CLASSPATH) $(TARGET_MAIN_CLASS) $(TARGET_VM_ARGS) $(OS) $(APP_JARS) $(NO_APP_CONFIG) $(NO_SPLASH) $(NO_BACKGROUND)

