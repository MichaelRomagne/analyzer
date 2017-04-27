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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

//-----------------------------------------------------------------
// CAnalyser class
//
//-----------------------------------------------------------------
public class CAnalyser
{
    //---------------------------------------------
    // Static constants
    //---------------------------------------------
    private static final String mUsage="graph_analyser [ --config=<path to config file> ] [ --loglevel (INFO|WARN|ALL) ] [ --loadcsv <filename>] [ --graph <name> ] [ --gmlfile <filename> ] [ --gmldir<directory> ] [ --version ] --points <datapoints> [ --maxk <max degree> ] [--asteroid <name>] [--totalize] [--degree_only] --skipload --maxVEN1 [ value ] --maxVEN2 [ value ] --maxCE [ value ] --maxCVEN2 [ value ] [ --scope ]";
    private static final String mBanner="\nMoogSoft graph_analyser: Graph Topology Entropy Analyser";

    //---------------------------------------------
    // Local variables
    //---------------------------------------------

    private CDbPool                     mDbPool=null;           // Pool of mysql connections
    private int                         mThreadCnt=5;           // How many threads
    private String                      mGraphName=null;        // Name of graph
    private String                      mEntropyType=null;      // Type of measure
    private Boolean                     mDegreeOnly=false;      // Degree only?
    private Boolean                     mScope=false;           // Scoped calc
    private Integer                     mPoints=null;           // How many points in distribution
    private Integer                     mMaxDegree=null;        // Max degree if specified
    private String                      mGmlFile=null;          // GML file to pre process
    private String                      mAstName=null;          // GML file to pre process
    private String                      mCsvFile=null;          // CSV file to pre process
    private String                      mGmlDir=null;           // Or alternatively all of them together!!
    private Boolean                     mTotalize=false;        // Do I create totalized values afterwards?
    private Boolean                     mSkipLoad=false;    // Am I skipping initial load?
    private Double                      mMaxVEN1=null;          // Max VEN1
    private Double                      mMaxVEN2=null;          // Max VEN2
    private Double                      mMaxCE=null;            // Max CE
    private Double                      mMaxCVEN2=null;         // Max CVEN2
    private Map<EEntropyType,Double>    mLimits=null;           // Max limits on distributions
    private Set<String>                 mScopedHosts=null;      // The set of scoped hosts

    //---------------------------------------------
    // Implementation
    //---------------------------------------------

    //
    // Constructor... just process
    //
    public CAnalyser(String args[])
    {
        mDbPool=null;

        //
        // Limits map
        //
        mLimits=new HashMap<EEntropyType,Double>();

        //
        // Consume those arguments
        //
        this.consumeArgs(args);
        CLogger.logger().info("Graphname [%s]",mGraphName);
    }
    
    //
    // Initialise the application
    //
    public void init()
    {
        //
        // Init the Db pool
        //
        this.initDbPool( true );

        //
        // Build a graph loader
        // 
        CGraphLoader loader=new CGraphLoader( mDbPool );

        //
        // Reset and load
        //
        loader.reset();

        CLogger.logger().info("Loaded graph");

        //
        // Do I scope the calculation?
        //
        if( mScope == true )
        {
            this.loadScope();
        }


        if( mAstName != null )
        {
            //
            // And build the degree distribution
            //
            loader.buildSimpleDist( mAstName, mPoints, mMaxDegree );
        }

        //
        // Am I just doing distribution?
        //
        if( mSkipLoad == true )
        {
            CLogger.logger().info("Distribution rebuild in progress [%s]",mGraphName);

            //
            // And build the degree distribution
            //
            loader.buildDegreeDist( mGraphName, mPoints, mMaxDegree );
        
            //
            // And entropy distributions
            //
            this.rebuildDistributions( mGraphName, loader );

            //
            // And home
            //
            return;
        }

        if( mCsvFile != null )
        {
            //
            // Reset the graph name here
            //
            mGraphName=loader.loadCSV( mCsvFile );
            //mGraphName=loader.loadCSV2( mCsvFile );

            //
            // And debug
            //
            CLogger.logger().info("Processing graph [%s]",mGraphName);

            //
            // Build subgraphs
            //
            CLogger.logger().info("Building subgraphs for [%s]",mGraphName);
            loader.buildSubgraphs( mGraphName );
            CLogger.logger().info("Building degree analysis");

            //
            // And build the degree distribution
            //
            loader.buildDegreeDist( mGraphName, mPoints, mMaxDegree );

            //
            // And analyse the graph
            //
            this.analyseGraph(loader, mGraphName, false);
        }
        else
        {
            //
            // Are we doing
            //
            if( mGmlDir != null )
            {
                //
                // Ok, directory?
                //
                File gmlDir=new File( mGmlDir );
                if( gmlDir != null && gmlDir.isDirectory() )
                {
                    String [] files=gmlDir.list();
                    int len=files.length;
                    for( int idx=0;idx<len;idx++ )
                    {
                        String fileName=files[idx];
                        if( fileName.matches( ".*\\.graphml" ) )
                        {
                            //
                            // Build Path
                            // 
                            StringBuilder pathBuilder=new StringBuilder( 512 );
                            pathBuilder.append( mGmlDir );
                            pathBuilder.append( "/" );
                            pathBuilder.append( fileName );
                            String gmlPath=pathBuilder.toString();

                            //
                            // Load it
                            //
                            CLogger.logger().info("Processing file [%s] [%s]",fileName,gmlPath);

                            if( mDegreeOnly == true )
                            {
                                loader.reset();
                                mGraphName=loader.loadGraphML2( gmlPath );
                    
                                //
                                // And debug
                                //
                                CLogger.logger().info("Processing graph [%s]",mGraphName);
                    
                                //
                                // Build subgraphs
                                //
                                CLogger.logger().info("Building subgraphs for [%s]",mGraphName);
                                loader.buildSubgraphs( mGraphName );
                                CLogger.logger().info("Building degree analysis");
                    
                                //
                                // And build the degree distribution
                                //
                                loader.buildDegreeDist( mGraphName, mPoints, mMaxDegree );
                            }
                            else
                            {
                                //
                                // Load the file for analysis
                                //
                                loader.reset();
                                mGraphName=loader.loadGraphML( gmlPath );
            
                                //
                                // And debug and process
                                //
                                CLogger.logger().info("Processing graph [%s]",mGraphName);
                        
                                //
                                // And analyse the graph
                                //
                                this.analyseGraph(loader, mGraphName,true);
                            }
                        }
                    } 
                }
                else
                {
                    CLogger.logger().fatal("File [%s] is not a valid directory",mGmlDir);
                }
            }
            else
            {
                //
                // Only GML
                //
                if( mDegreeOnly == true )
                {
                    if( mGmlFile != null )
                    {
                        mGraphName=loader.loadGraphML2( mGmlFile );
            
                        //
                        // And debug
                        //
                        CLogger.logger().info("Processing graph [%s]",mGraphName);
            
                        //
                        // Build subgraphs
                        //
                        CLogger.logger().info("Building subgraphs for [%s]",mGraphName);
                        loader.buildSubgraphs( mGraphName );
                        CLogger.logger().info("Building degree analysis");
            
                        //
                        // And build the degree distribution
                        //
                        loader.buildDegreeDist( mGraphName, mPoints, mMaxDegree );
                    }
                }
                else
                {
                    //
                    // Reset the graph name here
                    //
                    if( mGmlFile != null )
                    {
                        mGraphName=loader.loadGraphML( mGmlFile );

                        //
                        // And debug
                        //
                        CLogger.logger().info("Processing graph [%s]",mGraphName);
        
                        //
                        // And analyse the graph
                        //
                        this.analyseGraph(loader, mGraphName,true);
                    }
                    else
                    {
                        CLogger.logger().info("Oh shit");

                        //
                        // And debug
                        //
                        CLogger.logger().info("Processing graph [%s]",mGraphName);
        
                        //
                        // And analyse the graph
                        //
                        this.analyseGraph(loader, mGraphName,true);
                    }
                }
            }
        }

        //
        // If we are totalizing, totalize
        //
        if( mTotalize == true )
        {
            //
            // Build the degree distribution
            //
            loader.buildTotalDegreeDist( mPoints );

            //
            // And do the same for the entropy types
            //
            for( EEntropyType calculatorType : EEntropyType.values() )
            {
                //
                // Ok, we need a new calculator
                //
                CAbstractEntropyCalculator calculator=CEntropyFactory.buildCalculator( null, calculatorType, loader );
                if( calculator != null )
                {
                    //
                    // And totalize
                    //
                    calculator.buildAllGraphDistribution( CGraphLoader.mTotalizer, mPoints, mDbPool );
                }
            }
        }
        
        //
        // All done
        //
        return;
    }

    //
    // Load the scope?
    //
    public void loadScope()
    {
        mScopedHosts=new HashSet<String>();

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
            cStmnt=db_con.prepareCall( "{ call ifatdb.fetch_scope( ) }" );

            //
            // Run it
            //
            ResultSet results=cStmnt.executeQuery();

            //
            // Edge set...
            //
            results=cStmnt.getResultSet();
            while(results.next())
            {
                //
                // Go for it
                //
                String host=results.getString( "node" );
                mScopedHosts.add( host );
                CLogger.logger().info("Scoping in %s",host);
            }
            CLogger.logger().info("Have [%d scoped hosts",mScopedHosts.size() );
            
            //
            // Close the CallableStatement
            //
            cStmnt.close();
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
    // Analyse a graph
    //
    public void analyseGraph(CGraphLoader loader, String graphName, boolean reset)
    {
        //
        // Reset and load
        //
        if( reset == true )
        {
            loader.reset();

            //
            // Load
            //
            loader.load( graphName );
            CLogger.logger().info("Building subgraphs for [%s]",graphName);
            loader.buildSubgraphs( graphName );
            CLogger.logger().info("Building degree analysis");
        
            //
            // And build the degree distribution
            //
            loader.buildDegreeDist( graphName, mPoints, mMaxDegree );
        }

        //
        // Ok, we are analysing, build and cache cluster analysis dist
        //
        loader.buildClusterDist( graphName, mPoints, mScopedHosts );

        CLogger.logger().info("Starting analysis");

        //
        // Do analysis
        //
        if( EEntropyType.fromString( mEntropyType ) == EEntropyType.EAll )
        {
            //
            // Do all calcs
            //
            this.analyseAll( loader );
        }
        else
        {
            //
            // And analyse graphs
            //
            this.analyse( loader );
        }
        
        //
        //
        // All done
        //
        return;
    }

    //
    // Rebuild All Distributions
    //
    public void rebuildDistributions(String graphName, CGraphLoader loader)
    {
        //
        // Ok, we are analysing, build and cache cluster analysis dist
        //
        loader.buildClusterDist( graphName, mPoints , mScopedHosts);

        CLogger.logger().info("Starting analysis of [%s]", graphName);

        //
        // do all analyses
        //
        for( EEntropyType calculatorType : EEntropyType.values() )
        {
            CAbstractEntropyCalculator calculator=CEntropyFactory.buildCalculator( null, calculatorType, loader );
            if( calculator != null )
            {
                CLogger.logger().info("Rebuilding analysis for [%s]",calculatorType.toString() );

                //
                // Get upper limit
                //
                Double upperLimit=mLimits.get( calculatorType );
                if( upperLimit != null )
                {
                    CLogger.logger().info("Max value for analysis is [%s]=[%f]",calculatorType.toString(),upperLimit );
                }

                //
                // Build distribution from last calculator
                //
                calculator.buildDistribution( graphName, mPoints, mDbPool, upperLimit );
            }
    
        }

        //
        //
        // All done
        //
        return;
    }

    //
    // Perform each analysis
    //
    public void analyseAll(CGraphLoader loader)
    {
        //
        // do all analyses
        //
        for( EEntropyType calculatorType : EEntropyType.values() )
        {
            //
            // Ok, we need to loop through the graphs
            //
            Map<Integer,Multigraph<String,CEdge>> graphs=loader.getIndexedSubgraphs();

            CLogger.logger().info("Starting specific analysis for [%s]",calculatorType.toString() );
    
            //
            // And iterate
            //
            Iterator<Integer> itr=graphs.keySet().iterator();  
            CAbstractEntropyCalculator calculator=null;
            while( itr.hasNext() )
            {
                //
                // Get id and graph
                //
                Integer gid=itr.next();
                CLogger.logger().info("Analyysing grp [%d] for [%s]",gid,calculatorType.toString() );
                Multigraph<String,CEdge> graph=graphs.get( gid );
    
                //
                // Ok, we need a new calculator
                //
                calculator=CEntropyFactory.buildCalculator( graph, calculatorType, loader );
                if( calculator != null )
                {
                    //
                    // Calculate the value
                    //
                    Double totalEntropy=calculator.calculateEntropy( gid, mDbPool, mScopedHosts );
                }
                else
                {
                    break;
                }
            }

            //
            // Get upper limit
            //
            Double upperLimit=mLimits.get( calculatorType );
            if( upperLimit != null )
            {
                CLogger.logger().info("Max value for analysis is [%s]=[%f]",calculatorType.toString(),upperLimit );
            }
    
            if( calculator != null )
            {
                CLogger.logger().info("Building distribution for [%s]",calculatorType.toString() );
                //
                // Build distribution from last calculator
                //
                calculator.buildDistribution( mGraphName, mPoints, mDbPool, upperLimit );
            }
        }

        //
        // All done
        //
        return;
    }
    
    //
    // Do the analysis
    //
    public void analyse(CGraphLoader loader)
    {
        //
        // Ok, we need to loop through the graphs
        //
        Map<Integer,Multigraph<String,CEdge>> graphs=loader.getIndexedSubgraphs();

        //
        // And iterate
        //
        Iterator<Integer> itr=graphs.keySet().iterator();  
        CAbstractEntropyCalculator calculator=null;
        while( itr.hasNext() )
        {
            //
            // Get id and graph
            //
            Integer gid=itr.next();
            Multigraph<String,CEdge> graph=graphs.get( gid );

            //
            // Ok, we need a new calculator
            //
            calculator=CEntropyFactory.buildCalculator( graph, mEntropyType, loader );

            //
            // Calculate the value
            //
            Double totalEntropy=calculator.calculateEntropy( gid, mDbPool, mScopedHosts );
        }

        //
        // Get upper limit
        //
        Double upperLimit=mLimits.get( mEntropyType );
        if( upperLimit != null )
        {
            CLogger.logger().info("Max value for analysis is [%s] [%f]",mEntropyType.toString(),upperLimit );
        }

        //
        // Build distribution from last calculator
        //
        calculator.buildDistribution( mGraphName, mPoints, mDbPool, upperLimit );

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
        // Set the graph name
        //
        OptionBuilder.withLongOpt("graph");
        OptionBuilder.withDescription("Name of the graph to analyse");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new String() );
        Option instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Set the graph name
        //
        OptionBuilder.withLongOpt("points");
        OptionBuilder.withDescription("How many points to use for distribution analysis");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new Integer(50) );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Set the max degree
        //
        OptionBuilder.withLongOpt("maxk");
        OptionBuilder.withDescription("Cutoff for max degree");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new Integer(50) );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Totalize?
        //
        OptionBuilder.withLongOpt("totalize");
        OptionBuilder.withDescription("Do I totalize the value?");
        OptionBuilder.withType( new Boolean(false) );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Degree only?
        //
        OptionBuilder.withLongOpt("degree_only");
        OptionBuilder.withDescription("Do I just do degree?");
        OptionBuilder.withType( new Boolean(false) );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Scope?
        //
        OptionBuilder.withLongOpt("scope");
        OptionBuilder.withDescription("Scoped calculation?");
        OptionBuilder.withType( new Boolean(false) );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // skip load?
        //
        OptionBuilder.withLongOpt("skipload");
        OptionBuilder.withDescription("Do I just do distrib");
        OptionBuilder.withType( new String("test") );
        OptionBuilder.hasArg();
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
        // Set the degree only
        //
        OptionBuilder.withLongOpt("degree_only");
        OptionBuilder.withDescription("Do I just do degree?");
        OptionBuilder.withType( new Boolean(false) );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Specify a gml file
        //
        OptionBuilder.withLongOpt("gmlfile");
        OptionBuilder.withDescription("GraphML file to load into db and process");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new String() );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Specify a CSV file
        //
        OptionBuilder.withLongOpt("csvfile");
        OptionBuilder.withDescription("CSV file to load into db and process");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new String() );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Specify an Asteroid file
        //
        OptionBuilder.withLongOpt("asteroid");
        OptionBuilder.withDescription("Asteroid graph to load into db and process");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new String() );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Specify a whole directory of gml
        //
        OptionBuilder.withLongOpt("gmldir");
        OptionBuilder.withDescription("Directory of GraphML files to load into db and process");
        OptionBuilder.hasArg();
        OptionBuilder.withType( new String() );
        instanceOpt=  OptionBuilder.create();
        options.addOption(instanceOpt);

        //
        // Set the entropy type
        //
        OptionBuilder.withLongOpt("entropy");
        OptionBuilder.withDescription("Type of the entropy to analyse");
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
            // Get the graph name
            //
            mAstName=line.getOptionValue("asteroid");
            mCsvFile=line.getOptionValue("csvfile");
            mGmlDir=line.getOptionValue("gmldir");
            mGmlFile=line.getOptionValue("gmlfile");
            mTotalize=line.hasOption("totalize");
            if( mTotalize )
            {
                CLogger.logger().info("Doing Totalize");
            }
            mDegreeOnly=line.hasOption("degree_only");
            if( mDegreeOnly )
            {
                CLogger.logger().info("Only degree");
            }
            mScope=line.hasOption("scope");
            if( mScope )
            {
                CLogger.logger().info("Scoped calculation");
            }

            mSkipLoad=line.hasOption("skipload");
            if( mSkipLoad )
            {
                mGraphName=line.getOptionValue("skipload");
                CLogger.logger().info("Building Distrib data only [%s]",mGraphName);
            }

            //
            // Max values
            //
            if(line.hasOption("maxVEN1"))
            {
                mMaxVEN1=new Double( line.getOptionValue("maxVEN1") );
                mLimits.put( EEntropyType.fromString( "NormInvDegree" ), mMaxVEN1 );
            }

            if(line.hasOption("maxVEN2"))
            {
                mMaxVEN2=new Double( line.getOptionValue("maxVEN2") );
                mLimits.put( EEntropyType.fromString( "NormFractDegree" ), mMaxVEN2 );
            }

            if(line.hasOption("maxCE"))
            {
                mMaxCE=new Double( line.getOptionValue("maxCE") );
                mLimits.put( EEntropyType.fromString( "Cluster" ), mMaxCE );
            }

            if(line.hasOption("maxCVEN2"))
            {
                mMaxCVEN2=new Double( line.getOptionValue("maxCVEN2") );
                mLimits.put( EEntropyType.fromString( "ClusterFract" ), mMaxCVEN2 );
            }

            //
            // Get the graph name
            //
            if( mGraphName == null )
            {
                mGraphName=line.getOptionValue("graph");
            }
            if(mGraphName == null && ( mGmlFile == null && mGmlDir==null ) && mCsvFile==null && mAstName == null )
            {
                //
                // If we are not skip loading error
                //
                if( mSkipLoad != true )
                {
                    CLogger.logger().fatal("No graph name, GraphML file, CSV file or directory specified");
                    //
                    // Just exit
                    //
                    System.exit(0);
                    return;
                }
            }

            //
            // Get the graph name
            //
            mEntropyType=line.getOptionValue("entropy");
            if(mEntropyType == null)
            {
                CLogger.logger().fatal("No entropy type  specified");
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
            // Get the number of points
            //
            Object maxDegree=line.getOptionValue("maxk");
            if(maxDegree == null)
            {
                //
                // Debug
                //
                CLogger.logger().info("No max degree value specified");
            }
            else
            {
                mMaxDegree=new Integer( line.getOptionValue("maxk") );
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
