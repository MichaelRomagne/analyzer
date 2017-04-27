#!/bin/bash
#############################################################
#                                                           #
#  Contents of file Copyright (c) Moogsoft Inc 2014         #
#                                                           #
#-----------------------------------------------------------#
#                                                           #
#  WARNING:                                                 #
#  THIS FILE CONTAINS UNPUBLISHED PROPRIETARY               #
#  SOURCE CODE WHICH IS THE PROPERTY OF MOOGSOFT INC AND    #
#  WHOLLY OWNED SUBSIDIARY COMPANIES.                       #
#  PLEASE READ THE FOLLOWING AND TAKE CAREFUL NOTE:         #
#                                                           #
#  This source code is confidential and any person who      #
#  receives a copy of it, or believes that they are viewing #
#  it without permission is asked to notify Phil Tee        #
#  on 07734 591962 or email to phil@moogsoft.com.           #
#  All intellectual property rights in this source code     #
#  are owned by Moogsoft Inc.  No part of this source code  #
#  may be reproduced, adapted or transmitted in any form or #
#  by any means, electronic, mechanical, photocopying,      #
#  recording or otherwise.                                  #
#                                                           #
#  You have been warned....so be good for goodness sake...  #
#                                                           #
#############################################################

cd $MOOGSOFT_SRC_HOME/ui/js/

MISSING_FILES="FALSE"

while read jsFile
do
	if [[ $jsFile != "Login.js" ]] && [[ $jsFile != "CEnvironment.js" ]]; then
		
		if [[ `grep $jsFile $MOOGSOFT_SRC_HOME/src-utils/compile/scripts/cat_moog_js.sh` == "" ]]; then
			MISSING_FILES="TRUE"
			echo "File $jsFile not in cat_moog_js.sh"
		fi
		
	fi
done < <(find . -name \*.js | sed 's/^\.\///')

if [[ "$MISSING_FILES" == "TRUE" ]]; then
	exit 1
else
	echo "All Javascript files in $MOOGSOFT_SRC_HOME/ui/js/ present in cat_moog_js.sh"
fi
