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

# OS and makefile suffices

# Mac
macOS=Darwin

# Linux
linuxOS=Linux
centos=2.6.32-131.17.1.el6.x86_64

# Processors
i686:=i686
i386:=i386
x86:=x86
amd:=x86_64
unknown=unknown

OS=$(shell uname)
REV=$(shell uname -r)
PRCSR=$(shell uname -p)

# Will return FQDN (Fully Qualified Domain Name) on all platforms except
# Solaris. On Solaris it will return the name of the machine.
HOSTNAME=$(shell uname -n)

#not a very sophisticated way of finding x86 processors - but it works
ifeq ($(PRCSR),$(i686))
PRCSR=$(x86)
else
ifeq ($(PRCSR),$(i386))
PRCSR=$(x86)
endif
endif

ifeq ($(PRCSR),$(unknown))
PRCSR=$(x86)
endif

#
# And a centos hack to pick up amd processor
#
ifeq ($(PRCSR),$(amd))
PRCSR=$(x86)
endif

#set Linux
ifeq ($(OS),$(linuxOS))
ifeq ($(REV),$(centos))
include $(MOOGSOFT_SRC_HOME)/src-utils/compile/rules/centos.mk
else
include $(MOOGSOFT_SRC_HOME)/src-utils/compile/rules/linux.mk
endif
PLATFORM=Linux
endif

#set MacOSX
ifeq ($(OS),$(macOS))
include $(MOOGSOFT_SRC_HOME)/src-utils/compile/rules/macosx.mk 
endif

# objs directory is either in a source sub directory (.objs) or (.objs/objs)
# which is a link to local directories. This is useful on an encrypted file
# system as the object files can be stored on fast disk.
ifdef MOOGSOFT_OBJ_HOME
OBJS_DIR=.objs/objs
else
OBJS_DIR=.objs
endif

IFAT_COTS_PKGS=$(IFAT_COTS_HOME)/packages
#
# Somewhere to place classes
#
CLASS_DIR_ROOT=.classes

