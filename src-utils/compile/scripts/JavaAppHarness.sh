#!/bin/sh
#############################################################
#                                                           #
#  Contents of file Copyright (c) Moogsoft Inc 2010         #
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
#                                                           *
#  You have been warned....so be good for goodness sake...  #
#                                                           #
#############################################################
#
# NOTE - this is a template used by the build_harness.sh to generate shell files to 
# start up the java apps by substituting in the appropriate values.  
# 
#

#
# Name of the main class
#
java_main_class="MAINCLASS"

#
# Set up the java environment
#
java_classpath="CLASSPATH"
java_vm=$JAVA_HOME/bin/java


#
# print a splash on the terminal if we want one
#
APP_SPLASH

#
# trap the sigterm to do nothing
#
trap : SIGTERM SIGINT

# Create the process name from the file used to run this process.
proc_name=`basename $0`

#
# Run app
#
$java_vm VMARGS -Xmx16g -DprocName=$proc_name -DMOOGSOFT_HOME=$MOOGSOFT_HOME -classpath $java_classpath $java_main_class "$@" BACKGROUND

#
# Wait for the backgrounded java process to finish
#
JAVA_PID=$!
wait $JAVA_PID

#
# Store the exit code of the java process
#
EXIT_CODE=$?

#
# If the wait was terminated then kill the
# java process
#
if [[ $EXIT_CODE -gt 128 ]]
then
    kill $JAVA_PID
fi

#
# Exit the bash script with the exit code of the java program
#
exit $EXIT_CODE
