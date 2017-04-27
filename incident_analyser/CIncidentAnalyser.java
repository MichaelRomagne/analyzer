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

import com.moogsoft.event.CIncidentBuilder;

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
// CIncidentAnalyser class
//
//-----------------------------------------------------------------
public class CIncidentAnalyser
{
    //---------------------------------------------
    // Static constants
    //---------------------------------------------
    private static final String mUsage="incident_analyser [ --config=<path to config file> ] --scope [ --loglevel (INFO|WARN|ALL) ] [ --incidentfile <filename> ] [ --incidentdir <dirname>[ --version ] --points <datapoints> [--totalize] --skipload --maxVEN1 [ value ] --maxVEN2 [ value ] --maxCE [ value ] --maxCVEN2 [ value ] --maxBC [ value ]";
    private static final String mBanner="\nMoogSoft incident_analyser: Graph Topology Incident Analyser";

    
    private static final String EVENT_CFG_KEY="incidents";
    private static final String DELIM_KEY="delimiter";

    //---------------------------------------------
    // Local variables
    //---------------------------------------------

    private CDbPool                         mDbPool=null;       // Pool of mysql connections
    private int                             mThreadCnt=5;       // How many threads
    private Integer                         mPoints=null;       // How many points in distribution
    private String                          mIncidentFile=null;    // Incident file to pre process
    private String                          mIncidentDir=null;     // Or alternatively all of them together!!
    private Boolean                         mTotalize=false;    // Do I create totalized values afterwards?
    private Boolean                         mScope=false;       // Is it just scope
    private Boolean                         mSkipLoad=false;    // Am I skipping initial load?
    private Map<Integer,CIncidentBuilder>   mIncidentBuilders=null;// Keyed map of incident builders
    private String                          mDelimiter=null;    // Field splitter
    private Set<String>                     mTickets=null;      // List of all tickets
    private Double                          mMaxVEN1=null;      // Max VEN1
    private Double                          mMaxVEN2=null;      // Max VEN2
    private Double                          mMaxCE=null;        // Max CE
    private Double                          mMaxCVEN2=null;     // Max CVEN2
    private Double                          mMaxBC=null;        // Max BC

    //---------------------------------------------
    // Implementation
    //---------------------------------------------

    //
    // Constructor... just process
    //
    public CIncidentAnalyser(String args[])
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
        mThreadCnt = ((Long)CIncidentParams.valueOf("threads")).intValue();

        //
        // Init the Db pool
        //
        this.initDbPool( true );

        //
        // Collect tickets
        //
        mTickets=new HashSet<String>();

        //
        // Now build the digest
        //
        Map<String,Object> digestConfig=(Map<String, Object> )CIncidentParams.valueOf( "digest" );
        
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
        mIncidentBuilders=new HashMap<Integer,CIncidentBuilder>();

        //
        // Grab the delimiter
        //
        mDelimiter=(String )config.get( DELIM_KEY );

        //
        // Get the list of incident configs
        //
        List< Map<String,Object> > digestors=(List< Map<String,Object> > )config.get( EVENT_CFG_KEY );
        if( digestors == null )
        {
            CLogger.logger().fatal( "Bad config file, no incident digests" );
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
            // Create the incident builder
            //
            CIncidentBuilder builder=new CIncidentBuilder( digestConfig );

            //
            // And store
            //
            mIncidentBuilders.put( builder.getFields(), builder );
        }

        //
        // All done
        //
        return;
    }

    //
    // Analyse a graph
    //
    public void analyseIncidentFile(String path)
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
                // So, do we have an incident digestor for this
                //
                Integer fieldCount=tokens.size();
                CIncidentBuilder builder=mIncidentBuilders.get( fieldCount );
                if( builder != null )
                {
                    //
                    // And we matched 
                    //
                    matched++;

                    //
                    // So, process the incident
                    //
                    String ticket=builder.process( tokens,mDbPool,mScope );

                    //
                    // Accumulate for pruning
                    //
                    if( ticket != null )
                    {
                        mTickets.add( ticket );
                    }
                }
            }

        }
        catch( FileNotFoundException ex )
        {
            CLogger.logger().fatal("Incidents file [%s] not found, exception [%s]",path,ex.toString());
        }
        catch( IOException ex )
        {
            CLogger.logger().fatal("Incidents file [%s] IO Exception, exception [%s]",path,ex.toString());
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
    // prune collateral nodes
    //
    public void pruneCollateral()
    {
        CLogger.logger().info("Pruning collateral");

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

            Iterator<String> itr=mTickets.iterator();
            while( itr.hasNext() )
            {
                //
                // Build & run the sql
                //
                cStmnt=db_con.prepareCall( "{ call ifatdb.prune_collateral_nodes( ? ) }" );
                //
                // For type
                //
                cStmnt.setString( "tkt", itr.next() );

                //
                // Run it
                //
                ResultSet results=cStmnt.executeQuery();

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
    // Build the node incident by entropy dist
    //
    public void buildIncidentEntropyDist()
    {
        CLogger.logger().info("Building Incident Entropy Count analysis for");

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
            cStmnt=db_con.prepareCall( "{ call ifatdb.reset_incident_ent_stats( ) }" );

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
            Double [] bounds={ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

            //
            // Build & run the sql
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.get_all_incident_bounds(  ) }" );

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
                    CLogger.logger().info("Calcing dist incident entropy for [%s] count",ent);

                    //
                    // Build & run the sql
                    //
                    cStmnt=db_con.prepareCall( "{ call ifatdb.populate_incident_entropy( ?, ?, ? ) }" );

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
    // Build the node incident by degree dist
    //
    public void buildIncidentDegreeDist()
    {
        CLogger.logger().info("Building Incident Degree Count analysis for");

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
                cStmnt=db_con.prepareCall( "{ call ifatdb.populate_incident_degree( ?, ? ) }" );

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
    // Build the node incident by cluster coefficient dist
    //
    public void buildIncidentClusterDist()
    {
        CLogger.logger().info("Building Incident Cluster Coefficient analysis for");

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
                cStmnt=db_con.prepareCall( "{ call ifatdb.populate_incident_cluster( ?, ? ) }" );

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
        // Scope
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
        // Specify a incident file
        //
        OptionBuilder.withLongOpt("incidentfile");
        OptionBuilder.withDescription("Incident file to load into db and process");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new String() );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Specify a whole directory of incident files
        //
        OptionBuilder.withLongOpt("incidentdir");
        OptionBuilder.withDescription("Directory of Incident files to load into db and process");
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
                CIncidentParams.setConfigPath( configPath );
            }

            //
            // Get the graph name
            //
            mIncidentDir=line.getOptionValue("incidentdir");
            mIncidentFile=line.getOptionValue("incidentfile");
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
            if( (mIncidentFile == null && mIncidentDir==null) && (mSkipLoad == false)  )
            {
                CLogger.logger().fatal("No incident file or directory specified");
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
            if( mIncidentDir != null )
            {
                //
                // Ok, directory?
                //
                File incDir=new File( mIncidentDir );
                if( incDir != null && incDir.isDirectory() )
                {
                    //
                    // Walk all of the text files
                    //
                    String [] files=incDir.list();
                    int len=files.length;
                    for( int idx=0;idx<len;idx++ )
                    {
                        String filename=files[ idx ];
    
                        //
                        // Build Path
                        //
                        StringBuilder pathBuilder=new StringBuilder( 512 );
                        pathBuilder.append( mIncidentDir );
                        pathBuilder.append( "/" );
                        pathBuilder.append( filename );
                        String incidentPath=pathBuilder.toString();
    
                        CLogger.logger().info("Handling [%s]",filename);
    
                        //
                        // Am I a director
                        //
                        File incFile=new File( incidentPath );
                        if( incFile.isDirectory() == false )
                        {
                            //
                            // Just iterate
                            //
                            continue;
                        } 
    
                        //
                        // So look one layer down
                        //
                        String [] subfiles=incFile.list();
                        int nestLen=subfiles.length;
                        for(int sidx=0;sidx<nestLen;sidx++)
                        {
                            //
                            // Build new path
                            //
                            String nestPath=null;
                            pathBuilder=new StringBuilder(512);
                            pathBuilder.append( incidentPath );
                            pathBuilder.append( "/" );
                            pathBuilder.append( subfiles[ sidx ] );
                            nestPath=pathBuilder.toString();
    
                            CLogger.logger().info( "Analysing [%s]", nestPath); 
    
                            //
                            // load it
                            //
                            this.analyseIncidentFile( nestPath );
                        }
    
                    }
                }
            }
            else
            {
                //
                // Just do one file
                //
                this.analyseIncidentFile( mIncidentFile );
            }
        }

        //
        // Doing just scope?
        //
        if( mScope == false )
        {
            //
            // Before we do this, prune collateral entries
            //
            this.pruneCollateral();

            //
            // And build distributions;
            //
            this.buildIncidentEntropyDist();

            //
            // Same for degree distributions
            //
            this.buildIncidentDegreeDist();

            //
            // Same for cluster distributions
            //
            this.buildIncidentClusterDist();
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
