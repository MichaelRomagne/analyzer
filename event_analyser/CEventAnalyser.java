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
import com.moogsoft.ifat.CDbPool;
import com.moogsoft.ifat.CLogger;

import org.jgrapht.graph.Multigraph;

import com.moogsoft.graph.EEntropyType;
import com.moogsoft.graph.CEdge;
import com.moogsoft.graph.CGraphLoader;
import com.moogsoft.graph.CEntropyFactory;
import com.moogsoft.graph.CAbstractEntropyCalculator;

import com.moogsoft.event.CEventBuilder;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

//-----------------------------------------------------------------
// CEventAnalyser class
//
//-----------------------------------------------------------------
public class CEventAnalyser
{
    //---------------------------------------------
    // Static constants
    //---------------------------------------------
    private static final String mUsage="event_analyser [ --config=<path to config file> ] --scope [ --loglevel (INFO|WARN|ALL) ] [ --eventfile <filename> ] [ --eventdir <dirname>[ --version ] --points <datapoints> [--totalize] --skipload  --maxVEN1 [ value ] --maxVEN2 [ value ] --maxCE [ value ] --maxCVEN2 [ value ] --maxBC [ value ]";
    private static final String mBanner="\nMoogSoft event_analyser: Graph Topology Event Analyser";

    
    private static final String EVENT_CFG_KEY="events";
    private static final String DELIM_KEY="delimiter";

    //---------------------------------------------
    // Local variables
    //---------------------------------------------

    private CDbPool                     mDbPool=null;       // Pool of mysql connections
    private int                         mThreadCnt=5;       // How many threads
    private Integer                     mPoints=null;       // How many points in distribution
    private String                      mEventFile=null;    // Event file to pre process
    private String                      mEventDir=null;     // Or alternatively all of them together!!
    private Boolean                     mTotalize=false;    // Do I create totalized values afterwards?
    private Boolean                     mScope=false;       // Do I just do scope
    private Boolean                     mSkipLoad=false;    // Am I skipping initial load?
    private Map<Integer,CEventBuilder>  mEventBuilders=null;// Keyed map of event builders
    private String                      mDelimiter=null;    // Field splitter
    private Double                      mMaxVEN1=null;      // Max VEN1
    private Double                      mMaxVEN2=null;      // Max VEN2
    private Double                      mMaxCE=null;        // Max CE
    private Double                      mMaxCVEN2=null;     // Max CVEN2
    private Double                      mMaxBC=null;        // Max BC

    //---------------------------------------------
    // Implementation
    //---------------------------------------------

    //
    // Constructor... just process
    //
    public CEventAnalyser(String args[])
    {
        mDbPool=null;

        //
        // Consume those arguments
        //
        this.consumeArgs(args);
    }
    
    //
    // Initialise the application
    //
    @SuppressWarnings("unchecked")
    public void init()
    {
        //
        // Set the threads
        //
        mThreadCnt = ((Long)CAnalyserParams.valueOf("threads")).intValue();

        //
        // Init the Db pool
        //
        this.initDbPool( true );

        //
        // Now build the digest
        //
        Map<String,Object> digestConfig=(Map<String, Object> )CAnalyserParams.valueOf( "digest" );
        
        //
        // And build the config
        //
        this.buildDigesters( digestConfig );

        //
        // All done
        //
        return;
    }

    //
    // From the config, buid the digesters
    //
    @SuppressWarnings("unchecked")
    public void buildDigesters(Map<String,Object> config)
    {
        //
        // Build the map
        //
        mEventBuilders=new HashMap<Integer,CEventBuilder>();

        //
        // Grab the delimiter
        //
        mDelimiter=(String )config.get( DELIM_KEY );

        //
        // Get the list of event configs
        //
        List< Map<String,Object> > digestors=(List< Map<String,Object> > )config.get( EVENT_CFG_KEY );
        if( digestors == null )
        {
            CLogger.logger().fatal( "Bad config file, no event digests" );
        }

        //
        // Walk em
        //
        Iterator< Map<String,Object> > itr=digestors.iterator();
        while( itr.hasNext() )
        {
            //
            // Grab the config
            //
            Map<String,Object> digestConfig=itr.next();

            //
            // Create the event builder
            //
            CEventBuilder builder=new CEventBuilder( digestConfig );

            //
            // And store
            //
            mEventBuilders.put( builder.getFields(), builder );
        }

        //
        // All done
        //
        return;
    }

    //
    // Analyse a graph
    //
    public void analyseEventFile(String path)
    {
        //
        // Some stats
        //
        int lines=0;
        int matched=0;

        //
        // Analyse that file
        //
        try
        {
            //
            // Create a file reader
            //
            FileReader reader=new FileReader( path );
            
            //
            // And read as a buffered stream
            //
            BufferedReader in=new BufferedReader( reader );
            String inputLine = null;
            while ((inputLine = in.readLine()) != null)
            {
                //
                // Count a line
                //
                lines++;

                //
                // Now tokenize this
                //
                List<String> tokens=this.tokenise( inputLine );
                CLogger.logger().info("Line [%s] length [%d]  has tokens [%d] with delimiter[%s]",inputLine,inputLine.length(),tokens.size(),mDelimiter );
    
                //
                // So, do we have an event digestor for this
                //
                Integer fieldCount=tokens.size();
                CEventBuilder builder=mEventBuilders.get( fieldCount );
                if( builder != null )
                {
                    //
                    // And we matched 
                    //
                    matched++;

                    //
                    // So, process the event
                    //
                    builder.process( tokens,mDbPool,mScope );
                }
            }

        }
        catch( FileNotFoundException ex )
        {
            CLogger.logger().fatal("Events file [%s] not found, exception [%s]",path,ex.toString());
        }
        catch( IOException ex )
        {
            CLogger.logger().fatal("Events file [%s] IO Exception, exception [%s]",path,ex.toString());
        }

        //
        // Final stats
        //
        CLogger.logger().info("Analysed [%d] raw and processed [%d] through system",lines,matched);

        //
        //
        // All done
        //
        return;
    }

    //
    // Turn a string into an array of tokens according to delimiter
    // 
    public List<String> tokenise(String input)
    {
        //
        // A String accumulator
        //
        StringBuilder builder=new StringBuilder( input.length() );
        StringBuilder accum=new StringBuilder( mDelimiter.length() );

        //
        // Token list
        //
        List<String> tokenList=new ArrayList<String>();

        //
        // Brute force tokenizer!
        //
        int len=input.length();
        int delimLength=mDelimiter.length();
        int delimTrap=0;
        for( int idx=0; idx < len; idx++ )
        {
            //
            // Grab a character
            //
            char ch=input.charAt( idx );

            //
            // Part of delimiter sequence or not?
            //
            if( ch == mDelimiter.charAt( delimTrap ) )
            {
                //
                // Hold on to character in case we bail
                //
                accum.append( ch );

                //
                // Increment value
                //
                delimTrap++;
                if( delimTrap >= delimLength )
                {
                    //
                    // Ok, we got a new token
                    //
                    String token=builder.toString();

                    //
                    // Add the token
                    //
                    tokenList.add( token );

                    //CLogger.logger().info("Delimeter fired - token [%s]",token);

                    //
                    // Ok, we fired. Shove accumulate string
                    // into array and start again
                    //
                    delimTrap=0;

                    //
                    // Reset builder
                    //
                    builder=new StringBuilder( input.length() );
                    accum=new StringBuilder( mDelimiter.length() );
                }
            }
            else
            {
                //
                // Check for early delimiter bail
                //
                if( delimTrap < delimLength )
                {
                    //
                    // Append missing chars
                    //
                    builder.append( accum );   

                    //
                    // Reset delimiter trackers
                    //
                    delimTrap=0;
                    accum=new StringBuilder( mDelimiter.length() );
                    
                }

                //
                // Whack it in
                //
                builder.append( ch );    
            }
        }

        //
        // Check for non zero last token
        //
        String lastToken=builder.toString();
        if( lastToken.length() > 0 )
        {
            //
            // Add the token
            //
            tokenList.add( lastToken );

            //CLogger.logger().info("Delimeter fired - token [%s]",lastToken);
        }

        //
        // Send it home
        //
        return( tokenList );
    }

    //
    // Build the node event count distribution
    //
    public void buildEventCountDist()
    {
        CLogger.logger().info("Building Event Count analysis for");
        //
        // Declare db objects for "finally" block
        //
        CallableStatement cStmnt=null;
        Connection db_con=null;
        try
        {
            //
            // Grab it
            //
            db_con=mDbPool.checkOut();

            //
            // Reset the stats
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.reset_event_stats( ) }" );

            //
            // Run it
            //
            ResultSet results=cStmnt.executeQuery();

            //
            // Close the CallableStatement
            //
            cStmnt.close();

            //
            // Now determine bounds for distribution
            //
            Integer minCount=0;
            Integer maxCount=0;
            //
            // Build & run the sql
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.get_count_bounds(  ) }" );

            //
            // Run it
            //
            results=cStmnt.executeQuery();
            while( results.next() )
            {
                minCount=results.getInt( "MinCount" );
                maxCount=results.getInt( "MaxCount" );
            }

            //
            // Close the CallableStatement
            //
            cStmnt.close();

            Integer sampleStep=1;
            if( mPoints != 0 )
            {
                //
                // Calculate step
                //
                Double step=(maxCount.doubleValue() - minCount.doubleValue() ) / mPoints.doubleValue() ;
                if( step > 1 )
                {
                    //
                    // Round up 
                    //
                    step=Math.ceil( step ); 
    
                    //
                    // And set the sample Step.
                    //
                    sampleStep=step.intValue();
                }
            }

            CLogger.logger().info("Sample step [%d] min [%d], max [%d]",sampleStep,minCount,maxCount);

            // 
            // Now create the distribution
            // 
            int floor=0;
            for( int idx=minCount; idx < maxCount; idx=idx+sampleStep )
            {
                    CLogger.logger().info("Calcing dist event count");
                //
                // Build & run the sql
                //
                cStmnt=db_con.prepareCall( "{ call ifatdb.populate_event_node_count_dist( ?, ? ) }" );

                //
                // For graph
                //

                //
                // Use florr
                //
                cStmnt.setInt( "floor", floor );

                //
                // Increment the floor
                //
                floor=floor+sampleStep;

                //
                // And use as ceiling
                //
                cStmnt.setInt( "ceiling", floor );

                //
                // Run it
                //
                results=cStmnt.executeQuery();

                //
                // Close the CallableStatement
                //
                cStmnt.close();
            }

        }
        catch(SQLException e)
        {
            CLogger.logger().info("SQL Exception: %s ", e);
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if( cStmnt!=null )
                {
                    cStmnt.close();
                }
            }
            catch(SQLException e)
            {
                CLogger.logger().info("SQL Exception: %s ", e);
            }

            mDbPool.checkIn(db_con);
        }

        //
        // All done
        //
        return;
    }

    //
    // Build the node event by entropy dist
    //
    public void buildEventEntropyDist()
    {
        CLogger.logger().info("Building Event Entropy Count analysis for");

        //
        // The entropy types...
        //
        String [] entTypes={ "VE1","VE2","VEN1","VEN2","CE","CVEN2","VEN3","BC" };

        //
        // Declare db objects for "finally" block
        //
        CallableStatement cStmnt=null;
        Connection db_con=null;
        try
        {
            //
            // Grab it
            //
            db_con=mDbPool.checkOut();

            //
            // Reset the stats
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.reset_event_ent_stats( ) }" );

            //
            // Run it
            //
            ResultSet results=cStmnt.executeQuery();

            //
            // Close the CallableStatement
            //
            cStmnt.close();

            //
            // Now determine bounds for distribution
            //
            Double [] bounds={ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };

            //
            // Build & run the sql
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.get_all_event_bounds(  ) }" );

            //
            // Run it
            //
            results=cStmnt.executeQuery();
            while( results.next() )
            {
                bounds[0]=results.getDouble( "MinVE1" );
                bounds[1]=results.getDouble( "MaxVE1" );
                bounds[2]=results.getDouble( "MinVE2" );
                bounds[3]=results.getDouble( "MaxVE2" );
                bounds[4]=results.getDouble( "MinVEN1" );
                bounds[5]=results.getDouble( "MaxVEN1" );
                bounds[6]=results.getDouble( "MinVEN2" );
                bounds[7]=results.getDouble( "MaxVEN2" );
                bounds[8]=results.getDouble( "MinCE" );
                bounds[9]=results.getDouble( "MaxCE" );
                bounds[10]=results.getDouble( "MinCVEN2" );
                bounds[11]=results.getDouble( "MaxCVEN2" );
                bounds[12]=results.getDouble( "MinVEN3" );
                bounds[13]=results.getDouble( "MaxVEN3" );
                bounds[14]=results.getDouble( "MinBC" );
                bounds[15]=results.getDouble( "MaxBC" );
            }

            //
            // Hard bounds
            //
            if( mMaxVEN1 != null )
            {
                bounds[5]=mMaxVEN1;
            }

            if( mMaxVEN2 != null )
            {
                bounds[7]=mMaxVEN2;
            }

            if( mMaxCE != null )
            {
                bounds[9]=mMaxCE;
            }

            if( mMaxCVEN2 != null )
            {
                bounds[11]=mMaxCVEN2;
            }

            if( mMaxBC != null )
            {
                bounds[15]=mMaxBC;
            }
            //
            // Close the CallableStatement
            //
            cStmnt.close();

            //
            // Build for each type
            //
            for( int tidx=0;tidx < entTypes.length; tidx ++ )
            {
                String ent=entTypes[tidx];

                //
                // Get the min max value
                //
                int boundsFloor=2*tidx;
                Double minVal=bounds[ boundsFloor ];
                Double maxVal=bounds[ boundsFloor + 1 ];

                //
                // So calculate the step
                //
                double step=(maxVal - minVal)/mPoints.doubleValue();

                CLogger.logger().info("Sample step [%f] min [%f], max [%f]",step,minVal,maxVal);

                // 
                // Now create the distribution
                // 
                double floor=minVal;
                for( int idx=0; idx < mPoints; idx++ )
                {
                    CLogger.logger().info("Calcing dist event entropy for [%s] count",ent);

                    //
                    // Build & run the sql
                    //
                    cStmnt=db_con.prepareCall( "{ call ifatdb.populate_event_entropy( ?, ?, ? ) }" );

                    //
                    // For type
                    //
                    cStmnt.setString( "tp", ent );

                    //
                    // Use florr
                    //
                    if( idx == 0 )
                    {
                        cStmnt.setDouble( "floor", 0.0 );
                    }
                    else
                    {
                        cStmnt.setDouble( "floor", floor );
                    }

                    //
                    // Increment the floor
                    //
                    floor=floor+step;

                    //
                    // And use as ceiling
                    //
                    cStmnt.setDouble( "ceiling", floor );

                    //
                    // Run it
                    //
                    results=cStmnt.executeQuery();

                    //
                    // Close the CallableStatement
                    //
                    cStmnt.close();
                }
            }

        }
        catch(SQLException e)
        {
            CLogger.logger().info("SQL Exception: %s ", e);
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if( cStmnt!=null )
                {
                    cStmnt.close();
                }
            }
            catch(SQLException e)
            {
                CLogger.logger().info("SQL Exception: %s ", e);
            }

            mDbPool.checkIn(db_con);
        }

        //
        // All done
        //
        return;
    }

    //
    // Build the node event by degree dist
    //
    public void buildEventDegreeDist()
    {
        CLogger.logger().info("Building Event Degree Count analysis for");

        //
        // Declare db objects for "finally" block
        //
        CallableStatement cStmnt=null;
        Connection db_con=null;
        try
        {
            //
            // Grab it
            //
            db_con=mDbPool.checkOut();

            //
            // Build & run the sql
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.get_all_degree_bounds(  ) }" );

            Integer maxDegree=0;
            Integer minDegree=0;
            //
            // Run it
            //
            ResultSet results=cStmnt.executeQuery();
            while( results.next() )
            {
                maxDegree=results.getInt( "MaxDeg" );
                minDegree=results.getInt( "MinDeg" );
            }

            //
            // Close the CallableStatement
            //
            cStmnt.close();

            //
            // Build 
            //
            Integer sampleStep=1;
            if( mPoints != 0 )
            {
                //
                // Calculate step
                //
                Double step=(maxDegree.doubleValue() - minDegree.doubleValue() ) / mPoints.doubleValue() ;
                if( step > 1 )
                {
                    //
                    // Round up
                    //
                    step=Math.ceil( step );

                    //
                    // And set the sample Step.
                    //
                    sampleStep=step.intValue();
                }
            }

            CLogger.logger().info("Sample step [%d] min [%d], max [%d]",sampleStep,minDegree,maxDegree);

            //
            // Now create the distribution
            //
            int floor=0;
            CLogger.logger().info("Sample step [%d] min [%d], max [%d]",sampleStep,minDegree,maxDegree);
            for( int idx=minDegree; idx < maxDegree; idx=idx+sampleStep )
            {

                //
                // Build & run the sql
                //
                cStmnt=db_con.prepareCall( "{ call ifatdb.populate_event_degree( ?, ? ) }" );

                //
                // Use florr
                //
                cStmnt.setInt( "floor", floor );

                //
                // Increment the floor
                //
                floor=floor+sampleStep;

                //
                // And use as ceiling
                //
                cStmnt.setInt( "ceiling", floor );

                //
                // Run it
                //
                results=cStmnt.executeQuery();

                //
                // Close the CallableStatement
                //
                cStmnt.close();
            }

        }
        catch(SQLException e)
        {
            CLogger.logger().info("SQL Exception: %s ", e);
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if( cStmnt!=null )
                {
                    cStmnt.close();
                }
            }
            catch(SQLException e)
            {
                CLogger.logger().info("SQL Exception: %s ", e);
            }

            mDbPool.checkIn(db_con);
        }

        //
        // All done
        //
        return;
    }

    //
    // Build the node event by degree dist
    //
    public void buildEventClusterDist()
    {
        CLogger.logger().info("Building Event Cluster Coefficient analysis for");

        //
        // Declare db objects for "finally" block
        //
        CallableStatement cStmnt=null;
        Connection db_con=null;
        try
        {
            //
            // Grab it
            //
            db_con=mDbPool.checkOut();

            //
            // Build & run the sql
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.get_all_cluster_bounds(  ) }" );

            Double maxCluster=0.0;
            Double minCluster=0.0;
            //
            // Run it
            //
            ResultSet results=cStmnt.executeQuery();
            while( results.next() )
            {
                maxCluster=results.getDouble( "MaxCls" );
                minCluster=results.getDouble( "MinCls" );
            }

            //
            // Close the CallableStatement
            //
            cStmnt.close();

            //
            // Build 
            //
            Double sampleStep=1.0;
            if( mPoints != 0 )
            {
                //
                // Calculate step
                //
                sampleStep=(maxCluster - minCluster ) / mPoints.doubleValue() ;
            }

            CLogger.logger().info("Sample step [%f] min [%f], max [%f]",sampleStep,minCluster,maxCluster);

            //
            // Now create the distribution
            //
            double floor=0;
            for( double idx=minCluster; idx < maxCluster; idx=idx+sampleStep )
            {

                //
                // Build & run the sql
                //
                cStmnt=db_con.prepareCall( "{ call ifatdb.populate_event_cluster( ?, ? ) }" );

                //
                // Use florr
                //
                cStmnt.setDouble( "floor", floor );

                //
                // Increment the floor
                //
                floor=floor+sampleStep;

                //
                // And use as ceiling
                //
                cStmnt.setDouble( "ceiling", floor );

                //
                // Run it
                //
                results=cStmnt.executeQuery();

                //
                // Close the CallableStatement
                //
                cStmnt.close();
            }

        }
        catch(SQLException e)
        {
            CLogger.logger().info("SQL Exception: %s ", e);
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if( cStmnt!=null )
                {
                    cStmnt.close();
                }
            }
            catch(SQLException e)
            {
                CLogger.logger().info("SQL Exception: %s ", e);
            }

            mDbPool.checkIn(db_con);
        }

        //
        // All done
        //
        return;
    }

    //
    // Access my db pool
    //
    public CDbPool dbpool()
    {
        return( mDbPool );
    }

    //
    // Process the arguments
    //
    private void consumeArgs(String []args)
    {
        //
        // store the argument vals
        //
        String logLevel=null;
        

        //
        // Ok, a command line parser
        //
        CommandLineParser parser = new PosixParser();

        //
        // And a set of options to process
        //
        Options options = new Options();

        //
        // How many points
        //
        OptionBuilder.withLongOpt("points");
        OptionBuilder.withDescription("How many points to use for distribution analysis");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new Integer(50) );
        Option instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Totalise?
        //
        OptionBuilder.withLongOpt("totalize");
        OptionBuilder.withDescription("Do I totalize the value?");
        OptionBuilder.withType( new Boolean(false) );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Scope?
        //
        OptionBuilder.withLongOpt("scope");
        OptionBuilder.withDescription("Do I just do scope?");
        OptionBuilder.withType( new Boolean(false) );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // skip load?
        //
        OptionBuilder.withLongOpt("skipload");
        OptionBuilder.withDescription("Do I just do distrib");
        OptionBuilder.withType( new Boolean(false) );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Max VEN1
        //
        OptionBuilder.withLongOpt("maxVEN1");
        OptionBuilder.withDescription("max VEN1");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new Double(0.0) );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Max VEN2
        //
        OptionBuilder.withLongOpt("maxVEN2");
        OptionBuilder.withDescription("max VEN2");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new Double(0.0) );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Max CE
        //
        OptionBuilder.withLongOpt("maxCE");
        OptionBuilder.withDescription("max CE");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new Double(0.0) );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Max CVEN2
        //
        OptionBuilder.withLongOpt("maxCVEN2");
        OptionBuilder.withDescription("max CVEN2");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new Double(0.0) );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Max BC
        //
        OptionBuilder.withLongOpt("maxBC");
        OptionBuilder.withDescription("max BC");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new Double(0.0) );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Specify a event file
        //
        OptionBuilder.withLongOpt("eventfile");
        OptionBuilder.withDescription("Event file to load into db and process");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new String() );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Specify a whole directory of event files
        //
        OptionBuilder.withLongOpt("eventdir");
        OptionBuilder.withDescription("Directory of Event files to load into db and process");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new String() );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Set the log level
        //
        OptionBuilder.withLongOpt("loglevel");
        OptionBuilder.withDescription("Specify (INFO|WARN|ALL) to choose the amount of debug output - warning ALL is very verbose!");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new String() );
        Option logLevelOpt=  OptionBuilder.create();
        options.addOption(logLevelOpt);

        //
        // Set the config
        //
        OptionBuilder.withLongOpt("config");
        OptionBuilder.withDescription("Specify a non default config file");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new String() );
        instanceOpt=  OptionBuilder.create();
        options.addOption( instanceOpt );

        //
        // Version
        //
        OptionBuilder.withLongOpt("version");
        OptionBuilder.withDescription("Return current version of the Moog software");
        Option versionOpt=  OptionBuilder.create();
        options.addOption(versionOpt);


        //
        // And do it
        //
        boolean parsed=true;
        try
        {
            //
            // So parse
            //
            CommandLine line=parser.parse(options,args);

            //
            // Version?
            //
            if(line.hasOption("version"))
            {
                System.out.println("graph_analyser (c) Moogsoft Ltd, Version 0.9");
                //
                // Just exit
                //
                System.exit(0);
            }

            //
            // Overriden config?
            //
            String configPath=line.getOptionValue( "config" );
            if( configPath != null )
            {
                CAnalyserParams.setConfigPath( configPath );
            }

            //
            // Get the graph name
            //
            mEventDir=line.getOptionValue("eventdir");
            mEventFile=line.getOptionValue("eventfile");
            mTotalize=line.hasOption("totalize");
            if( mTotalize )
            {
                CLogger.logger().info("Doing Totalize");
            }

            mScope=line.hasOption("scope");
            if( mScope )
            {
                CLogger.logger().info("Doing Scope");
            }

            mSkipLoad=line.hasOption("skipload");
            if( mSkipLoad )
            {
                CLogger.logger().info("Building Distrib data only");
            }

            //
            // Max values
            //
            if(line.hasOption("maxVEN1"))
            {
                mMaxVEN1=new Double( line.getOptionValue("maxVEN1") );
            }

            if(line.hasOption("maxVEN2"))
            {
                mMaxVEN2=new Double( line.getOptionValue("maxVEN2") );
            }

            if(line.hasOption("maxCE"))
            {
                mMaxCE=new Double( line.getOptionValue("maxCE") );
            }

            if(line.hasOption("maxCVEN2"))
            {
                mMaxCVEN2=new Double( line.getOptionValue("maxCVEN2") );
            }

            if(line.hasOption("maxBC"))
            {
                mMaxBC=new Double( line.getOptionValue("maxBC") );
            }

            //
            // Make sure we have something to do
            //
            if( mEventFile == null && mEventDir==null  && (mSkipLoad == false)  )
            {
                CLogger.logger().fatal("No event file or directory specified");
                //
                // Just exit
                //
                System.exit(0);
                return;
            }

            //
            // Get the number of points
            //
            mPoints=new Integer( line.getOptionValue("points") );
            if(mPoints == null)
            {
                CLogger.logger().fatal("No points value specified");
                //
                // Just exit
                //
                System.exit(0);
                return;
            }

            //
            // Same for the log level
            //
            logLevel=line.getOptionValue("loglevel");
            if(logLevel!=null)
            {
                //
                // What level?
                //
                if(logLevel.compareTo("WARN")==0)
                {
                    CLogger.setLevel(CLogger.Level.E_Warn);
                }
                else
                {
                    if(logLevel.compareTo("INFO")==0)
                    {
                        CLogger.setLevel(CLogger.Level.E_Info);
                    }
                }
            }
        }
        catch(ParseException ex)
        {
            //
            // Just set the parsed flag for false...
            //
            parsed=false;
        }

        //
        // Did we pass?
        // If not exit...
        //
        if(parsed==false)
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(mUsage,mBanner,options,null);

            //
            // Just exit
            //
            System.exit(0);
        }

        //
        // All done
        //
        return;
    }

    //
    // Start processing
    // 
    public void start()
    {
        //
        // Am I doing a directory or multiples?
        //
        if( mSkipLoad == false )
        {
            if( mEventDir != null )
            {
                //
                // Ok, directory?
                //
                File evDir=new File( mEventDir );
                if( evDir != null && evDir.isDirectory() )
                {
                    //
                    // Walk all of the text files
                    //
                    String [] files=evDir.list();
                    int len=files.length;
                    for( int idx=0;idx<len;idx++ )
                    {
                        String filename=files[ idx ];
    
                        //
                        // Build Path
                        //
                        StringBuilder pathBuilder=new StringBuilder( 512 );
                        pathBuilder.append( mEventDir );
                        pathBuilder.append( "/" );
                        pathBuilder.append( filename );
                        String eventPath=pathBuilder.toString();
    
                        //
                        // Am I a director
                        //
                        File evFile=new File( eventPath );
                        if( evFile.isDirectory() )
                        {
                            //
                            // Just iterate
                            //
                        continue;
                        } 
    
                        CLogger.logger().info( "Analysing [%s]", eventPath); 
                        //
                        // load it
                        //
                        this.analyseEventFile( eventPath );
                    }
                }
            }
            else
            {
                //
                // Just do one file
                //
                this.analyseEventFile( mEventFile );
            }
        }

        if( mScope == false )
        {
            //
            // Build event count distribution
            //
            this.buildEventCountDist();

            //
            // And build distributions;
            //
            this.buildEventEntropyDist();

            //
            // Same again for degree distributions
            //
            this.buildEventDegreeDist();

            //
            // Same again for degree distributions
            //
            this.buildEventClusterDist();
        }

        //
        // All done
        // 
        return;
    }

    //
    // Init (or re-init) the db pool
    //
    private void initDbPool(boolean rebuild)
    {
        //
        // MySQL Pool
        //
        try
        {
            //
            // Close the original db pool if the size has changed
            //
            if ((rebuild) &&
                (mDbPool != null))
            {
                mDbPool.close();
                mDbPool = null;
            }
            
            //
            // Rebuild the db pool
            //
            if (mDbPool == null)
            {
                mDbPool=new CDbPool(mThreadCnt);
            }
            else
            {
                CLogger.logger().info("Re-using the existing db pool as connection count unchanged");
            }
        }
        catch(ClassNotFoundException e)
        {
            CLogger.logger().info("Failed to open database pool " + e);
        }
        catch(SQLException e)
        {
            CLogger.logger().info("Failed to open database pool " + e);
        }
    }
}
