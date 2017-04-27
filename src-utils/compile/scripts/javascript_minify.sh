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

if [ -z "$MOOGSOFT_SRC_HOME" ]; then
    echo "Need to set Moogsoft Home"
    exit 1
fi

#
# determine if any js files are missing
#
$MOOGSOFT_SRC_HOME/src-utils/compile/scripts/cat_check_missing_js.sh

#
# run the script to concatenate all js files together
#
$MOOGSOFT_SRC_HOME/src-utils/compile/scripts/cat_moog_js.sh

cd $MOOGSOFT_SRC_HOME/ui/js/
#
# ensure that all the cat script worked
#
if [[ ! -f moog_all.js ]]; then
   echo "Unable to find moog_all.js"
    exit 1
fi

#
# verify that java 7 is installed
#
if [ -z "$JAVA_HOME" ]; then
    echo "Need to set Java 7"
    exit 1
fi

#
# do the minification using google closure compiler
#
"$JAVA_HOME/bin/java" -jar $MOOGSOFT_COTS_HOME/packages/google-js-compiler20140730.jar --js=moog_all.js --js_output_file=moog_min.js --language_in=ECMASCRIPT5

if [[ ! -f moog_min.js ]]; then
    echo "Unable to find moog_min.js"
    exit 1
fi

#
# cleanup to remove any remaining js files
#
if [ "$1" != "dev" ]; then
    find . ! -name moog_all.js ! -name moog_min.js ! -name Login.js ! -name CParameters.js -delete
fi
