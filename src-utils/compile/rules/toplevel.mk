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

all: 
	@ for i in  $(COMPONENTS) ; \
	 do \
	 (cd $$i && $(MAKE) );  \
	 done 
# we use TOP_DIR_MKF to allow a directory to contain sub-directorues
# and source code
# The "normal" makefile is used to compiler the sub directories
# TOP_DIR_MKF is used to compile the sources source at this level and
# link these with the sub-directories
ifdef TOP_DIR_MKF
	@ $(MAKE) -f $(TOP_DIR_MKF)
endif

clean: 
	@ for i in  $(COMPONENTS) ; \
	 do \
	 (cd $$i && $(MAKE) clean );  \
	 done 
ifdef TOP_DIR_MKF
	@ $(MAKE) clean -f $(TOP_DIR_MKF)
endif

test:
	-@ for i in  $(COMPONENTS) ; do $(MAKE) -C $$i $@ ; done
ifdef TOP_DIR_MKF
	-@ $(MAKE) test -f $(TOP_DIR_MKF)
endif

compile: 
	@ for i in  $(COMPONENTS) ; \
	 do \
	 (cd $$i && $(MAKE) compile );  \
	 done 
ifdef TOP_DIR_MKF
	@ $(MAKE) compile -f $(TOP_DIR_MKF)
endif

relink: 
	@ for i in  $(COMPONENTS) ; \
	 do \
	 (cd $$i && $(MAKE) relink );  \
	 done 
ifdef TOP_DIR_MKF
	@ $(MAKE) relink -f $(TOP_DIR_MKF)
endif

install: 
	@ for i in  $(COMPONENTS) ; \
	 do \
	 (cd $$i && $(MAKE) install );  \
	 done 
ifdef TOP_DIR_MKF
	@ $(MAKE) install -f $(TOP_DIR_MKF)
endif

build: 
	@ set -e || exit 1 ; \
	 for i in  $(COMPONENTS) ; \
	 do \
	 (cd $$i && $(MAKE) build || exit 1 );  \
	 done 
ifdef TOP_DIR_MKF
	@ $(MAKE) build -f $(TOP_DIR_MKF)
endif

