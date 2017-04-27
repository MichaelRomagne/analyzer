/************************************************************
 *                                                          *
 *  Contents of file Copyright (c) Moogsoft Inc 2010        *
 *                                                          *
 *----------------------------------------------------------*
 *                                                          *
 *  WARNING:                                                *
 *  THIS FILE CONTAINS UNPUBLISHED PROPRIETARY              *
 *  SOURCE CODE WHICH IS THE PROPERTY OF MOOGSOFT INC AND   *
 *  WHOLLY OWNED SUBSIDIARY COMPANIES.                      *
 *  PLEASE READ THE FOLLOWING AND TAKE CAREFUL NOTE:        *
 *                                                          *
 *  This source code is confidential and any person who     *
 *  receives a copy of it, or believes that they are viewing*
 *  it without permission is asked to notify Phil Tee       *
 *  on 07734 591962 or email to phil@moogsoft.com.          *
 *  All intellectual property rights in this source code    *
 *  are owned by Moogsoft Inc.  No part of this source      *
 *  code may be reproduced, adapted or transmitted in any   *
 *  form or by any means, electronic, mechanical,           *
 *  photocopying, recording or otherwise.                   *
 *                                                          *
 *  You have been warned....so be good for goodness sake... *
 *                                                          *
 ************************************************************/
//-----------------------------------------------------------------
// ProcClassPath utility
//
// Take list of jars and create a friendly classpath for the java app
// harness scripts
//-----------------------------------------------------------------
public class ProcClassPath {
    public static void main(String args[])
    {
        //
        // Split the class path
        //
        String [] jars=args[0].split(":");

        //
        // Stack for strings
        //
        StringBuilder cpb=new StringBuilder(1024);

        //
        // Walk each arg and print out
        //
        boolean first=true;
        for(int i=0;i< jars.length; i++)
        {
            //
            // Split by slash
            //
            String [] path_components=jars[i].split("/");

            //
            // Grab last part
            //
            String last_part=path_components[path_components.length-1];
            if(last_part.compareTo(".classes") != 0 && last_part.compareTo(".") != 0)
            {
                //
                // Specials for vm arg processing
                //
                if(args[1].compareTo("vmargs") == 0 && last_part.compareTo("-splash") == 0)
                {
                    continue;
                }
                if(first==false)
                {
                    cpb.append(":");
                }
                else
                {
                    first=false;
                }
                if(args[1].compareTo("vmargs") == 0)
                {
                    if(last_part.compareTo("-server") != 0)
                    {
                        cpb.append("-splash:$MOOGSOFT_HOME/images/");
                    }
                }
                else
                {
                    cpb.append("$MOOGSOFT_HOME/lib/");
                }
                cpb.append(last_part);
            }
        }

        //
        // Write to stdout
        //
        System.out.println(cpb.toString());

        //
        // And home
        //
        return;
    }

}
