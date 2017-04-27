#!/bin/bash
#############################################################
#                                                           #
#  Contents of file Copyright (c) Moogsoft Inc 2010        	#
#                                                           #
#-----------------------------------------------------------#
#                                                           #
#  WARNING:                                                 #
#  THIS FILE CONTAINS UNPUBLISHED PROPRIETARY               #
#  SOURCE CODE WHICH IS THE PROPERTY OF MOOGSOFT INC AND   	#
#  WHOLLY OWNED SUBSIDIARY COMPANIES.						#
#  PLEASE READ THE FOLLOWING AND TAKE CAREFUL NOTE:			#
#                                                           #
#  This source code is confidential and any person who      #
#  receives a copy of it, or believes that they are viewing	#
#  it without permission is asked to notify Phil Tee	    #
#  on 07734 591962 or email to phil@moogsoft.com.			#
#  All intellectual property rights in this source code     #
#  are owned by Moogsoft Inc.  No part of this source code 	#
#  may be reproduced, adapted or transmitted in any form or	#
#  by any means, electronic, mechanical, photocopying,		#
#  recording or otherwise.									#
#                                                           *
#  You have been warned....so be good for goodness sake...	#
# 															#
#############################################################
#
# Set the umask
#
umask 113

#
# Build the class path processor
#
current_dir=`pwd`
(cd $MOOGSOFT_SRC_HOME/src-utils/compile/scripts ;javac ProcClassPath.java;cd $current_dir);

#
# In this simplified version of the ns command, the file is a s spec'd
# on the command line.
#
app_name=$1
classpath=$2
main_class=$3
vm_args=$4
os=$5
no_splash=$6
no_background=$7

echo "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo "Java CLASSPATH:"
echo 
echo $2
echo "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"

if [[ ( "$no_splash" -gt 0 ) && ( -n "$no_splash" ) ]] ;
then
    app_splash="# NO SPLASH REQUIRED"
else
    app_splash="echo \"\\\n-------- Copyright MoogSoft 2012-2014 --------\\\n\\\n  Executing:  APPNAME\\\n\\\n------------ All Rights Reserved -------------\\\n\""
fi

background="\&"
if [[ ( "$no_background" -gt 0 ) ]] ;
then
    echo "Setting background to nothing"
    background=""
fi

safe_sedstring='s/\//\\\//g'
cots_replace='s/MOOGSOFT_COTS_HOME\\\/packages/MOOGSOFT_HOME\\\/lib\\\/cots/g'

#
# Clean classpath
#
safe_classpath=`echo $classpath | sed $safe_sedstring | sed $cots_replace`
safe_vmargs=`echo $vm_args | sed $safe_sedstring`


#
# Dependent upon which extension is
# used the appropriate copyright file is
# copied to the given name
#
cp $MOOGSOFT_SRC_HOME/src-utils/compile/scripts/JavaAppHarness.sh temp1

#
# Now we need to sed in a few things
#
sed_main='s/MAINCLASS/'$main_class'/g'
sed_classpath='s/CLASSPATH/'$safe_classpath'/g' 
sed_appname="s/APPNAME/$app_name/g" 
sed_vmargs='s/VMARGS/'$safe_vmargs'/g' 
sed_os='s/OS_NAME/'$os'/g' 

sed_splash="s^APP_SPLASH^${app_splash}^g" 
sed_background="s^BACKGROUND^$background^g"

#
# And perform substitutions
#
sed "$sed_main" temp1 > temp2
sed "$sed_splash" temp2 > temp3
sed "$sed_classpath" temp3 > temp4
sed "$sed_appname" temp4 > temp5
sed "$sed_os" temp5 > temp6
sed "$sed_vmargs" temp6 > temp7
sed "$sed_background" temp7 > $app_name
#
# Clean up
#
rm -f temp1 temp2 temp3 temp4 temp5 temp6 temp7

#
# And set executable
#
chmod 755 $app_name

#
# Clean-up
#
(cd $MOOGSOFT_SRC_HOME/src-utils/compile/scripts ;rm ProcClassPath.class;cd $current_dir);



