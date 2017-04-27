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
package com.moogsoft.graph;


import com.moogsoft.ifat.CDbPool;
import com.moogsoft.ifat.CLogger;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//-----------------------------------------------------------------
// CGraphLoader class
//
// Load in the edges and vertices from the database for a named
// graph and form all of the necessary in memory structures ready
// for fun!
//
//-----------------------------------------------------------------
public class CGraphLoader
{
    public static final String mTotalizer="--Totalizer--"; //The name of the graph which is all graphs for distribs
    //---------------------------------------------
    // Members
    //---------------------------------------------

    private CDbPool                                 mDbPool=null;           // Reference to db pool
    private Map<String,Multigraph<String, CEdge>>   mNodeToGraph=null;      // We consolidate subgraphs here
    private Map<Integer,Multigraph<String, CEdge>>  mGidToGraph=null;       // We consolidate subgraphs here
    private Map<String,Double>                      mClusterCoeffs=null;    // Store cluster coeffs here
    private Set<Multigraph<String, CEdge>>          mGraphs=null;           // All my graphs
    private Map<String,CSubGraph>                   mNodeToSubGraph=null;      // We consolidate subgraphs here

    //---------------------------------------------
    // Implementation
    //---------------------------------------------

    //
    // Load a named list of edges and prepare structures
    //
    public CGraphLoader(CDbPool pool)
    {
        //
        // Grab reference
        // 
        mDbPool=pool;
    }

    public class CSubGraph {
        private Multigraph<String, CEdge>       mGraph=null;
        private Integer                         mId=0;

        //---------------------------------------------
        // Implementation
        //---------------------------------------------
        public CSubGraph(Multigraph<String, CEdge> graph, Integer id)
        {
            mGraph=graph;
            mId=id;
        }

        public Multigraph<String, CEdge> graph()
        {
            return( mGraph );
        }
        public Integer id()
        {
            return( mId );
        }
    }

    //
    // Empty ready for reload
    //
    public void reset()
    {
        //
        // Empty data structures
        //
        if( mGraphs != null )
        {
            mGraphs.clear();
        }
        if( mNodeToGraph != null )
        {
            mNodeToGraph.clear();
        }
        if( mNodeToSubGraph != null )
        {
            mNodeToSubGraph.clear();
        }
        if( mGidToGraph != null )
        {
            mGidToGraph.clear();
        }
        if( mClusterCoeffs != null )
        {
            mClusterCoeffs.clear();
        }

        //
        // And build
        //
        mGraphs=new HashSet<Multigraph<String, CEdge>>();
        mNodeToGraph=new TreeMap<String,Multigraph<String, CEdge>>();
        mNodeToSubGraph=new TreeMap<String,CSubGraph>();
        mGidToGraph=new TreeMap<Integer,Multigraph<String, CEdge>>();
        mClusterCoeffs=new HashMap<String,Double>();

        //
        // All done
        //
        return;
    }

    //
    // Get Graphs
    //
    public Set<Multigraph<String,CEdge>> getSubgraphs()
    {
        return( mGraphs );
    }
    public Map<Integer,Multigraph<String,CEdge>> getIndexedSubgraphs()
    {
        return( mGidToGraph );
    }

    //
    // From the subgraphs create the db artifacts
    //
    public void buildSubgraphs(String graphSource)
    {
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
            // So walk through each subgraph
            //
            Iterator <Multigraph<String,CEdge>> itr=mGraphs.iterator();
            while( itr.hasNext() )
            {
                //
                // Grab the subgraph
                //
                CLogger.logger().info("Loop subgraph");
                Multigraph<String,CEdge> graph=itr.next();

                //
                // Build & run the sql
                //
                cStmnt=db_con.prepareCall( "{ call ifatdb.create_subgraph( ?, ?, ? ) }" );
                CLogger.logger().info("Graph source [%s]",graphSource);
                cStmnt.setString( "grph", graphSource );
                cStmnt.setInt( "ec", graph.edgeSet().size() );
                cStmnt.setInt( "vc", graph.vertexSet().size() );
    
                //
                // Run it
                //
                ResultSet results=cStmnt.executeQuery();
    
                //
                // Edge set...
                //
                Integer gid=-1;
                results=cStmnt.getResultSet();
                while(results.next())
                {
                    gid=results.getInt( "gid" );
                }
                
                //
                // Store under index
                //
                if( gid >=0 )
                {
                    mGidToGraph.put( gid, graph );
                }
    
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
    // From the subgraphs create the degree distributions
    //
    public void buildDegreeDist(String graphSource,Integer points, Integer cutoff)
    {
        CLogger.logger().info("Building degree analysis for [%s]",graphSource);
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
            // So walk through each subgraph
            //
            Iterator <Multigraph<String,CEdge>> itr=mGraphs.iterator();
            while( itr.hasNext() )
            {
                //
                // Grab the subgraph
                //
                Multigraph<String,CEdge> graph=itr.next();

                //
                // For each node, update the degree
                //
                Iterator<String> n_itr=graph.vertexSet().iterator();
                while( n_itr.hasNext() )
                {
                    //
                    // The node
                    //
                    String vertex=n_itr.next();
                    CLogger.logger().info("updating degree [%s] degree [%d]",vertex,graph.degreeOf( vertex ));

                    //
                    // Build & run the sql
                    //
                    cStmnt=db_con.prepareCall( "{ call ifatdb.update_degree( ?, ?, ? ) }" );
                    cStmnt.setString( "nd", vertex );
                    cStmnt.setString( "grp", graphSource );
                    cStmnt.setInt( "dgr", graph.degreeOf( vertex ) );
    
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

            //
            // Reset the stats
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.reset_degree_stats( ? ) }" );
            cStmnt.setString( "grp", graphSource );

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
            Integer minDegree=0;
            Integer maxDegree=0;
            //
            // Build & run the sql
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.get_degree_bounds( ? ) }" );
            cStmnt.setString( "grp", graphSource );

            //
            // Run it
            //
            results=cStmnt.executeQuery();
            while( results.next() )
            {
                minDegree=results.getInt( "MinDeg" );
                maxDegree=results.getInt( "MaxDeg" );
            }

            //
            // Do I have a cutoff?
            //
            if( cutoff != null )
            {
                maxDegree=cutoff;
            }

            //
            // Close the CallableStatement
            //
            cStmnt.close();

            Integer sampleStep=1;
            if( points != 0 )
            {
                //
                // Calculate step
                //
                Double step=(maxDegree.doubleValue() - minDegree.doubleValue() ) / points.doubleValue() ;
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
            for( int idx=minDegree; idx <= maxDegree; idx=idx+sampleStep )
            {
                    CLogger.logger().info("Calcing dist degree");
                //
                // Build & run the sql
                //
                cStmnt=db_con.prepareCall( "{ call ifatdb.populate_degree_dist( ?, ?, ? ) }" );

                //
                // For graph
                //
                cStmnt.setString( "grp", graphSource );

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
    // From the subgraphs create the degree distributions
    //
    public void buildSimpleDist(String graphSource,Integer points, Integer cutoff)
    {
        CLogger.logger().info("Building degree analysis for [%s]",graphSource);
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
            cStmnt=db_con.prepareCall( "{ call ifatdb.reset_degree_stats( ? ) }" );
            cStmnt.setString( "grp", graphSource );

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
            Integer minDegree=0;
            Integer maxDegree=0;
            //
            // Build & run the sql
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.get_degree_bounds( ? ) }" );
            cStmnt.setString( "grp", graphSource );

            //
            // Run it
            //
            results=cStmnt.executeQuery();
            while( results.next() )
            {
                minDegree=results.getInt( "MinDeg" );
                maxDegree=results.getInt( "MaxDeg" );
            }

            //
            // Do I have a cutoff?
            //
            if( cutoff != null )
            {
                maxDegree=cutoff;
            }

            //
            // Close the CallableStatement
            //
            cStmnt.close();

            Integer sampleStep=1;
            if( points != 0 )
            {
                //
                // Calculate step
                //
                Double step=(maxDegree.doubleValue() - minDegree.doubleValue() ) / points.doubleValue() ;
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
            for( int idx=minDegree; idx <= maxDegree; idx=idx+sampleStep )
            {
                    CLogger.logger().info("Calcing dist degree");
                //
                // Build & run the sql
                //
                cStmnt=db_con.prepareCall( "{ call ifatdb.populate_degree_dist( ?, ?, ? ) }" );

                //
                // For graph
                //
                cStmnt.setString( "grp", graphSource );

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
    // From the subgraphs create the degree distributions
    // of all graphs
    //
    public void buildTotalDegreeDist(Integer points)
    {
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
            cStmnt=db_con.prepareCall( "{ call ifatdb.reset_degree_stats( ? ) }" );
            cStmnt.setString( "grp", mTotalizer );

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
            Integer minDegree=0;
            Integer maxDegree=0;
            //
            // Build & run the sql
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.get_all_degree_bounds( ) }" );

            //
            // Run it
            //
            results=cStmnt.executeQuery();
            while( results.next() )
            {
                minDegree=results.getInt( "MinDeg" );
                maxDegree=results.getInt( "MaxDeg" );
            }

            //
            // Close the CallableStatement
            //
            cStmnt.close();

            Integer sampleStep=1;
            if( points != 0 )
            {
                //
                // Calculate step
                //
                Double step=(maxDegree.doubleValue() - minDegree.doubleValue() ) / points.doubleValue() ;
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

            CLogger.logger().info("Totalizer Sample step [%d] min [%d], max [%d]",sampleStep,minDegree,maxDegree);

            // 
            // Now create the distribution
            // 
            int floor=0;
            for( int idx=minDegree; idx <= maxDegree; idx=idx+sampleStep )
            {
                    CLogger.logger().info("Calcing dist degree");
                //
                // Build & run the sql
                //
                cStmnt=db_con.prepareCall( "{ call ifatdb.populate_degree_dist( ?, ?, ? ) }" );

                //
                // For graph
                //
                cStmnt.setString( "grp", mTotalizer );

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
    // From the subgraphs create the degree distributions
    //
    public void buildClusterDist( String graphSource,Integer points, Set<String> scope)
    {
        CLogger.logger().info("Building cluster analysis for [%s]",graphSource);
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
            // So walk through each subgraph
            //
            Iterator <Multigraph<String,CEdge>> itr=mGraphs.iterator();
            while( itr.hasNext() )
            {
                //
                // Grab the subgraph
                //
                Multigraph<String,CEdge> graph=itr.next();

                //
                // For each node, update the degree
                //
                Iterator<String> n_itr=graph.vertexSet().iterator();
                while( n_itr.hasNext() )
                {
                    CLogger.logger().info("updating clustering coefficient");
                    //
                    // The node
                    //
                    String vertex=n_itr.next();

                    if( scope != null )
                    {
                        if( scope.contains( vertex ) == false )
                        {
                            CLogger.logger().info("Skipping vertex [%s]",vertex);
                            continue;
                        }
                    }

                    //
                    // Calculate the clustering coefficient
                    //
                    double clusterCoeff=this.clusteringCoefficient( vertex, graph );

                    //
                    // And cache
                    //
                    mClusterCoeffs.put( vertex, clusterCoeff );

                    //
                    // Build & run the sql
                    //
                    cStmnt=db_con.prepareCall( "{ call ifatdb.update_vertex_cluster_coeff( ?, ? ) }" );
                    cStmnt.setString( "nd", vertex );
                    cStmnt.setDouble( "val", clusterCoeff );
    
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

            //
            // Reset the stats
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.reset_cluster_stats( ? ) }" );
            cStmnt.setString( "grp", graphSource );

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
            Double minCluster=0.0;
            Double maxCluster=0.0;
            //
            // Build & run the sql
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.get_cluster_bounds( ? ) }" );
            cStmnt.setString( "grp", graphSource );

            //
            // Run it
            //
            results=cStmnt.executeQuery();
            while( results.next() )
            {
                minCluster=results.getDouble( "MinCls" );
                maxCluster=results.getDouble( "MaxCls" );
            }

            //
            // Close the CallableStatement
            //
            cStmnt.close();

            Double sampleStep=1.0;
            if( points != 0 )
            {
                //
                // Calculate step
                //
                sampleStep=( maxCluster - minCluster ) / points.doubleValue() ;
            }

            CLogger.logger().info("Sample step [%f] min [%f], max [%f]",sampleStep,minCluster,maxCluster);

            // 
            // Now create the distribution
            // 
            double floor=0.0;
            if( sampleStep == 0.0 )
            {
                sampleStep=1.0;
            }
            for( double idx=minCluster; idx <= maxCluster; idx=idx+sampleStep )
            {
                CLogger.logger().info("Calcing clustering coefficient dist");
                //
                // Build & run the sql
                //
                cStmnt=db_con.prepareCall( "{ call ifatdb.populate_cluster_dist( ?, ?, ? ) }" );

                //
                // For graph
                //
                cStmnt.setString( "grp", graphSource );

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
    // Access cache of clustering coeffs
    //
    public Double clusterCoefficient( String vertex )
    {
        return( mClusterCoeffs.get( vertex ) );
    }

    //-----------------------------------------------------------------
    //
    // For a given vertex calculate the clustering coefficient of 
    // a vertex
    // This is defined as (Ref: Watts, Strogatz "Collective dynamics of 'small-world' networks"
    // Nature 1998
    // as:
    //
    // C=2 x Neighborhood edges / degree * (degree + 1)
    //
    //-----------------------------------------------------------------
    public Double clusteringCoefficient( String vertex, Multigraph<String,CEdge> graph )
    {
        //
        // Start empty
        //
        Double clustCoeff=0.0;

        //
        // subgraph set
        //
        Set<String> subgraph=new HashSet<String>();

        //
        // Grab all edges
        //
        Set<CEdge> edges=graph.edgesOf( vertex );
        Iterator<CEdge> itr=edges.iterator();
        while( itr.hasNext() )
        {
            //
            // Grab edge and vertices
            //
            CEdge edge=itr.next();
            String target=edge.getTarget();
            String source=edge.getSource();

            //
            // Compile neighborhood
            //
            if( target.compareTo( vertex ) != 0 )
            {
                subgraph.add( target );
            }
            if( source.compareTo( vertex ) != 0 )
            {
                subgraph.add( source );
            }
        }

        //
        // And add the vertex
        //
        subgraph.add( vertex );


        //
        // Unique set of strings...
        //
        Set<String> oneHopEdges=new HashSet<String>();
        StringBuilder sb=null;

        //
        // Ok, the messy bit!!!
        //
        Double subgraphEdges=0.0;
        Iterator<String> nitr=subgraph.iterator();
        while( nitr.hasNext() )
        {
            String member=nitr.next();

            //
            // And check all others!
            //
            Iterator<String> citr=subgraph.iterator();
            while( citr.hasNext() )
            {
                String candVertex=citr.next();

                //
                // Get all connections
                //
                Set<CEdge> internalCons=graph.getAllEdges( member, candVertex );
                Iterator<CEdge> pairItr=internalCons.iterator();
                while( pairItr.hasNext() )
                {
                    //
                    // Build a unique "source:target" key and count
                    // unique occurrences
                    //
                    sb=new StringBuilder(512);
                    CEdge edge=pairItr.next();

                    sb.append( edge.getTarget() );
                    sb.append( ":" );
                    sb.append( edge.getSource() );

                    oneHopEdges.add( sb.toString () );

                    //
                    // And add the reverse
                    //
                    sb=new StringBuilder(512);

                    sb.append( edge.getSource() );
                    sb.append( ":" );
                    sb.append( edge.getTarget() );

                    oneHopEdges.add( sb.toString () );
                }
            }
        }

        //
        // Account for double counting
        //
        subgraphEdges=(double )oneHopEdges.size() / 2;

        //
        // Get degree
        //
        Double degree=( double )( graph.degreeOf( vertex ) );

        //
        // And calculate
        //
        clustCoeff = ( 2.0 * subgraphEdges ) / ( degree * (degree + 1 ) );
        if( clustCoeff == 0.0 )
        {
            clustCoeff=1.0;
            CLogger.logger().warning("Clustering coefficient of [%s] is nil - adjusting to 1",vertex);
        }

        //
        // Check for really screwy stuff
        //
        if( clustCoeff > 1.0 )
        {
            clustCoeff=1.0;
            CLogger.logger().warning("Clustering coefficient of [%s] is wrong! - adjusting to 1",vertex);
            CLogger.logger().warning("Data for [%s] is build from degree [%d], set size [%d] one hop edges [%f]! - adjusting to 1",vertex,graph.degreeOf( vertex ), subgraph.size(), subgraphEdges);
        }

        //
        // Send it home
        //
        return( clustCoeff );
    }

    //
    // Take a GraphML file and load the initial
    // database structures
    //
    public String loadCSV(String path)
    {
        //
        // Counters and heap
        //
        int createCount=0;
        int loopCount=0;

        Map<Integer,CSubGraph>  graphMap=new TreeMap<Integer,CSubGraph>();
        Integer sgid=0;

        //
        // Get the graph name
        // 
        File file = new File( path );
        String baseFile=file.getName();
        String [] components=baseFile.split( "\\." );
        String graphName=components[0];
        CLogger.logger().info("Graph name = [%s]", graphName);
        if( graphName == null )
        {
            graphName = baseFile;
        }

        //
        // Prevent Multigraph processing duplicate edges
        //
        Set<String> edgeSet=new HashSet<String>();
        StringBuilder edgeBuilder=null;

        //
        // Read through assuming
        // <source>,<sink>
        //
        try
        {
            BufferedReader br=new BufferedReader( new FileReader( path ) );

            //
            // Process each line
            //
            for(String line;(line=br.readLine()) != null; )
            {
                CLogger.logger().info("Got line [%s]",line);

                //
                // Ok, lets split the string around the ','
                //
                String [] parts=line.split(",");
                if( parts.length != 2 )
                {
                    CLogger.logger().fatal("Line [%s] un parseable",line);
                }
                else
                {
                    //
                    // Get the id and name
                    //
                    String  source=parts[0];
                    String  sink=parts[1];

                    edgeBuilder=new StringBuilder(128);
                    edgeBuilder.append(source);
                    edgeBuilder.append(sink);
                    String edgeKey=edgeBuilder.toString();

                    if( edgeSet.contains( edgeKey ) )
                    {
                        //continue;
                    }
                    else
                    {
                        edgeSet.add( edgeKey );
                    }

                    edgeBuilder=new StringBuilder(128);
                    edgeBuilder.append(sink);
                    edgeBuilder.append(source);
                    edgeKey=edgeBuilder.toString();

                    if( edgeSet.contains( edgeKey ) )
                    {
                        //continue;
                    }
                    else
                    {
                        edgeSet.add( edgeKey );
                    }
                    //
                    // Only process non loopback nodes
                    // This is a multigraph after all...
                    //
                    if(source.equals(sink)==false)
                    {
                        //
                        // So do we have an entry to map either of the nodes to a graph?
                        //
                        CSubGraph srcGraph=mNodeToSubGraph.get( source );
                        CSubGraph sinkGraph=mNodeToSubGraph.get( sink );

                        //
                        // Now, either:
                        // 1. srcGraph and sinkGraph are null
                        // 2. One is null - which means we are adding an edge
                        // 3. Both exist in which case:
                        //    a. srcGraph == sinkGraph and this is a duplicate
                        //    b. srcGraph != sinkGraph and this is a merge graph moment. We always merge smallest to
                        //       largest
                        //

                        //
                        // Ok, determine action
                        //
                        EResolveType type=EResolveType.E_Ignore;
                        CSubGraph targetGraph=null;
                        if( srcGraph == null && sinkGraph == null )
                        {
                            type=EResolveType.E_Creation;
                            createCount++;
                        }
                        else
                        {
                            //
                            // Case 1 sink is null
                            //
                            if( sinkGraph == null )
                            {
                                if( srcGraph == null )
                                {
                                    //
                                    // Src graph does not neither does sink - create
                                    //
                                    type=EResolveType.E_Creation;
                                    createCount++;
                                }
                                else
                                {
                                    //
                                    // Src graph exists, sink does not add to source
                                    //
                                    type=EResolveType.E_AddEdge;
                                    targetGraph=srcGraph;
                                    mNodeToSubGraph.put( sink, targetGraph );
                                }
                            }
                            else
                            {
                                //
                                // Case 2 sink graph exists
                                //
                                if( srcGraph == null )
                                {
                                    //
                                    // Src graph does not add to sink
                                    //
                                    type=EResolveType.E_AddEdge;
                                    targetGraph=sinkGraph;
                                    mNodeToSubGraph.put( source, targetGraph );
                                }
                                else
                                {
                                    //
                                    // Both graphs exist!
                                    //
                                    int sinkCount   = sinkGraph.graph().vertexSet().size();
                                    int srcCount    = srcGraph.graph().vertexSet().size();
                                    if( srcGraph == sinkGraph )
                                    {
                                        //
                                        // Its a link inside of the same graph, add edge
                                        //
                                        type=EResolveType.E_AddEdge;
                                        targetGraph=sinkGraph;
                                    }
                                    else
                                    {
                                        //
                                        // Ok, two different graphs merge
                                        // just choose the smallest graph to merge in
                                        //
                                        if( sinkCount <= srcCount )
                                        {
                                            type=EResolveType.E_MergeSink;
                                        }
                                        else
                                        {
                                            type=EResolveType.E_MergeSrc;
                                        }
                                    }
                                }
                            }
                        }

                        //
                        // Ok, lets perform actions
                        //
                        switch( type )
                        {
                            case E_Creation:
                                {
                                    //
                                    // Build a new graph
                                    //
                                    Multigraph<String, CEdge> graph=new Multigraph<String, CEdge>(CEdge.class);        

                                    //
                                    // Add both vertices
                                    //
                                    graph.addVertex( source );
                                    graph.addVertex( sink );

                                    //
                                    // And connect
                                    //
                                    graph.addEdge( source, sink );

                                    //
                                    // Create subgraph
                                    //
                                    CSubGraph sg=new CSubGraph( graph, sgid++ );
                                    CLogger.logger().info("Merging data adding graph before [%d]",sg.id());
        
                                    // 
                                    //  And add twice to the set
                                    // 
                                    mNodeToSubGraph.put( source, sg );
                                    mNodeToSubGraph.put( sink, sg );

                                    //
                                    // Add the graph
                                    //
                                    CLogger.logger().info("Merging data adding graph [%s]",graph.hashCode());

                                    //
                                    // Add to graph map
                                    //
                                    graphMap.put( sg.id(), sg );
                                }
                                break;
                            case E_AddEdge:
                                {
                                    //
                                    // Add both vertices
                                    //
                                    targetGraph.graph().addVertex( source );
                                    targetGraph.graph().addVertex( sink );

                                    //
                                    // And connect
                                    //
                                    targetGraph.graph().addEdge( source, sink );
                                }
                                break;
                            case E_MergeSink:
                                {
                                    //
                                    // Ok, strategy is to merge all sink nodes and edges into source
                                    //
                                    Set<String> sinkNodes=sinkGraph.graph().vertexSet();
                                    Set<CEdge>  sinkEdges=sinkGraph.graph().edgeSet();
                                    //
                                    // And remove sink
                                    //
                                    CLogger.logger().info("Merging Sink size before [%d] sink hashCode[%d] source hashCode [%d]",graphMap.size(),sinkGraph.id(),srcGraph.id());
                                    graphMap.remove( sinkGraph.id() );
                                    CLogger.logger().info("Merging Sink size after [%d]",graphMap.size());

                                    //
                                    // Merge in the graph
                                    //
                                    Iterator<String> nItr=sinkNodes.iterator();
                                    while( nItr.hasNext() )
                                    {
                                        String sVert=nItr.next();
        
                                        //
                                        // Add the node to the src graph and change reference in map
                                        //
                                        srcGraph.graph().addVertex( sVert );
                                        mNodeToSubGraph.remove( sVert );
                                        mNodeToSubGraph.put( sVert, srcGraph );
                                    }
        
                                    //
                                    // Add in the edges
                                    //
                                    Iterator<CEdge> eItr=sinkEdges.iterator();
                                    while( eItr.hasNext() )
                                    {
                                        CEdge edge=eItr.next();
                                        srcGraph.graph().addEdge( edge.getSource(), edge.getTarget() );
                                    }

                                    //
                                    // And connect the two new nodes.
                                    //
                                    srcGraph.graph().addEdge( source, sink );
                                }
                                break;
                            case E_MergeSrc:
                                {
                                    //
                                    // Ok, strategy is to merge all source nodes and edges into sink
                                    //
                                    Set<String> srcNodes=srcGraph.graph().vertexSet();
                                    Set<CEdge> srcEdges=srcGraph.graph().edgeSet();

                                    //
                                    // And remove source
                                    //
                                    CLogger.logger().info("Merging Source size before [%d]",graphMap.size());
                                    graphMap.remove( srcGraph.id() );
                                    CLogger.logger().info("Merging Source size after [%d]",graphMap.size());

                                    //
                                    // Merge in the graph
                                    //
                                    Iterator<String> nItr=srcNodes.iterator();
                                    while( nItr.hasNext() )
                                    {
                                        String sVert=nItr.next();
        
                                        //
                                        // Add the node to the src graph and change reference in map
                                        //
                                        sinkGraph.graph().addVertex( sVert );
                                        mNodeToSubGraph.remove( sVert );
                                        mNodeToSubGraph.put( sVert, sinkGraph );
                                    }
        
                                    //
                                    // Add in the edges
                                    //
                                    Iterator<CEdge> eItr=srcEdges.iterator();
                                    while( eItr.hasNext() )
                                    {
                                        CEdge edge=eItr.next();
                                        sinkGraph.graph().addEdge( edge.getSource(), edge.getTarget() );
                                    }

                                    //
                                    // And connect the two new nodes.
                                    //
                                    sinkGraph.graph().addEdge( source, sink );
                                }
                                break;
                            case E_Ignore:
                                {
                                    //
                                    // Nop
                                    //
                                }
                                break;
                        }

                    }
                }
            }
            
        }
        catch(Exception e)
        {
            CLogger.logger().info("Exception [%s]",e.toString() );
        }

        //
        // Ok, harvest legitimate subgraphs
        //
        Iterator<Integer> sgitr=graphMap.keySet().iterator();
        while( sgitr.hasNext() )
        {
            CSubGraph sg=graphMap.get( sgitr.next() );
            mGraphs.add( sg.graph() );
        }

        //
        // Now, build the fake GidToGraph index
        //
        Iterator<Multigraph<String, CEdge>> gitr=mGraphs.iterator();
        Integer gid=1;
        while( gitr.hasNext() )
        {
            Multigraph<String, CEdge> grp=gitr.next();
            CLogger.logger().info("Adding graph [%d]", gid);
            mGidToGraph.put( gid++,grp );
        }

        
        //
        // And send home graph name
        //
        return( graphName );
    }

    //
    // Take a GraphML file and load the initial
    // database structures
    //
    public String loadCSV2(String path)
    {
        //
        // Counters and heap
        //
        int createCount=0;
        int loopCount=0;
        //Set<String> nodeHeap=new HashSet<String>();

        //
        // Get the graph name
        // 
        File file = new File( path );
        String baseFile=file.getName();
        String [] components=baseFile.split( "\\." );
        String graphName=components[0];
        CLogger.logger().info("Graph name = [%s]", graphName);

        //
        // Prevent Multigraph processing duplicate edges
        //
        Set<String> edgeSet=new HashSet<String>();
        StringBuilder edgeBuilder=null;

        //
        // Read through assuming
        // <source>,<sink>
        //
        try
        {
            BufferedReader br=new BufferedReader( new FileReader( path ) );

            //
            // Process each line
            //
            Multigraph<String, CEdge> graph=new Multigraph<String, CEdge>(CEdge.class);        

            //
            // Add the graph
            //
            mGraphs.add( graph );

            for(String line;(line=br.readLine()) != null; )
            {
                CLogger.logger().info("Got line [%s]",line);

                //
                // Ok, lets split the string around the ','
                //
                String [] parts=line.split(",");
                if( parts.length != 2 )
                {
                    CLogger.logger().fatal("Line [%s] un parseable",line);
                }
                else
                {
                    //
                    // Get the id and name
                    //
                    String  source=parts[0];
                    String  sink=parts[1];

                    edgeBuilder=new StringBuilder(128);
                    edgeBuilder.append(source);
                    edgeBuilder.append(sink);
                    String edgeKey=edgeBuilder.toString();

                    if( edgeSet.contains( edgeKey ) )
                    {
                        continue;
                    }
                    else
                    {
                        edgeSet.add( edgeKey );
                    }

                    edgeBuilder=new StringBuilder(128);
                    edgeBuilder.append(sink);
                    edgeBuilder.append(source);
                    edgeKey=edgeBuilder.toString();

                    if( edgeSet.contains( edgeKey ) )
                    {
                        continue;
                    }
                    else
                    {
                        edgeSet.add( edgeKey );
                    }

                    //
                    // Only process non loopback nodes
                    // This is a multigraph after all...
                    //
                    if(source.equals(sink)==false)
                    {
                        //
                        // Add both vertices
                        //
                        graph.addVertex( source );
                        graph.addVertex( sink );

                        //
                        // And connect
                        //
                        graph.addEdge( source, sink );
                    }
                    else
                    {
                        //
                        // Data for debug
                        //
                            /*
                        if( nodeHeap.contains( source ) == false )
                        {
                            loopCount++;
                        }
                        nodeHeap.add(source);
                            */
                    }
                }
            }
            
        }
        catch(Exception e)
        {
            CLogger.logger().info("Exception [%s]",e.toString() );
        }

        
        //
        // And send home graph name
        //
        return( graphName );
    }


    //
    // Take a GraphML file and load the initial
    // database structures
    //
    public String loadGraphML(String path)
    {
        //
        // Consume the graphml
        //
        Document graphDoc = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringElementContentWhitespace(true);
        File file = new File( path );
        try 
        {
            //
            // Read and parse the document
            //
            DocumentBuilder builder = factory.newDocumentBuilder();
            graphDoc = builder.parse(file);
        } 
        catch(ParserConfigurationException e) 
        {
        } 
        catch(SAXException e) 
        {
        }
        catch(IOException e) 
        {
         
        }

        //
        // Get the graph name
        // 
        String baseFile=file.getName();
        String [] components=baseFile.split( "\\." );
        String graphName=components[0];
        CLogger.logger().info("Graph name = [%s]", graphName);

        //
        // We need to determine the label attribute for nodes
        //
        String id=null;
        NodeList tags=graphDoc.getElementsByTagName( "key" );
        int length=tags.getLength();
        for( int idx=0;idx<length;idx++)
        {
            Node node=tags.item( idx );
    
            //
            // Extract the attributes
            //
            NamedNodeMap attrs=node.getAttributes();
            Node idNode=attrs.getNamedItem("id");
            Node nameNode=attrs.getNamedItem("attr.name");
            Node forNode=attrs.getNamedItem("for");
            if( idNode != null & nameNode != null && forNode != null )
            {
                String label=nameNode.getNodeValue();
                String forLabel=forNode.getNodeValue();
                if( label.compareTo( "label" ) == 0 && forLabel.compareTo( "node" ) == 0 )
                {
                    id=idNode.getNodeValue();
                    CLogger.logger().info("Label Node value [%s=%s]",nameNode.getNodeValue(),idNode.getTextContent() );
                }
            }
        }

        //
        // Ok We now have the right id value for the node label
        // get list of nodes
        //
        Map<String,String> nodes=new HashMap<String,String>();
        NodeList allNodes=graphDoc.getElementsByTagName( "node" );
        length=allNodes.getLength();
        for( int idx=0;idx<length;idx++)
        {
            Node node=allNodes.item( idx );

            //
            // Extract unique id
            //
            NamedNodeMap nAttrs=node.getAttributes();
            Node idNode=nAttrs.getNamedItem( "id" );
            String nodeId=idNode.getNodeValue();

            //
            // Now walk children for label of node
            //
            NodeList childNodes=node.getChildNodes();
            int dlen=childNodes.getLength();
            for( int didx=0; didx< dlen; didx ++ )
            {
                Node dNode=childNodes.item( didx );
                NamedNodeMap attrs=dNode.getAttributes();
                if( attrs != null )
                {
                    //
                    // Check that key is the label id
                    // 
                    Node    keyNode=attrs.getNamedItem( "key" );
                    String  key=keyNode.getNodeValue();
                    if( key.compareTo( id ) == 0 )
                    {
                        if( dNode.hasChildNodes() )
                        {
                            String nodeLabel=dNode.getFirstChild().getNodeValue();
                            CLogger.logger().info("Label Node value [%s] [%s]",nodeId,nodeLabel);
                            //
                            // Ok, we add the node to our set
                            //
                            nodes.put( nodeId, nodeLabel );
                        }
                    }
                }
            }
        }

        //
        // And get all of the edges
        //
        NodeList allEdges=graphDoc.getElementsByTagName( "edge" );
        length=allEdges.getLength();
        boolean createGraph=true;
        for( int idx=0;idx<length;idx++)
        {
            //
            // Pull the edge
            //
            Node edge=allEdges.item( idx );

            //
            // And get the crucial
            //
            NamedNodeMap attrs=edge.getAttributes();
            Node sourceNode=attrs.getNamedItem( "source" );
            Node targetNode=attrs.getNamedItem( "target" );
            CLogger.logger().info("Connecting [%s]<->[%s]",sourceNode,targetNode);

            //
            // Ok, we have enough now to populate the database
            //
            this.addEdgeToDb( sourceNode.getNodeValue(), targetNode.getNodeValue(), nodes, graphName, path, createGraph );

            //
            // And reset graph create flag
            //
            createGraph=false;
        }

        CLogger.logger().info("Graph contains [%d] nodes",nodes.size());

        //
        // All done
        //
        return( graphName );
    }

    //
    // Take a GraphML file and load the initial
    // database structures
    //
    public String loadGraphML2(String path)
    {
        //
        // Prevent Multigraph processing duplicate edges
        //
        Set<String> edgeSet=new HashSet<String>();
        StringBuilder edgeBuilder=null;

        //
        // Process each line
        //
        Multigraph<String, CEdge> graph=new Multigraph<String, CEdge>(CEdge.class);        

        //
        // Add the graph
        //
        mGraphs.add( graph );

        //
        // Consume the graphml
        //
        Document graphDoc = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringElementContentWhitespace(true);
        File file = new File( path );
        try 
        {
            //
            // Read and parse the document
            //
            DocumentBuilder builder = factory.newDocumentBuilder();
            graphDoc = builder.parse(file);
        } 
        catch(ParserConfigurationException e) 
        {
        } 
        catch(SAXException e) 
        {
        }
        catch(IOException e) 
        {
         
        }

        //
        // Get the graph name
        // 
        String baseFile=file.getName();
        String [] components=baseFile.split( "\\." );
        String graphName=components[0];
        CLogger.logger().info("Graph name = [%s]", graphName);

        //
        // We need to determine the label attribute for nodes
        //
        String id=null;
        NodeList tags=graphDoc.getElementsByTagName( "key" );
        int length=tags.getLength();
        for( int idx=0;idx<length;idx++)
        {
            Node node=tags.item( idx );
    
            //
            // Extract the attributes
            //
            NamedNodeMap attrs=node.getAttributes();
            Node idNode=attrs.getNamedItem("id");
            Node nameNode=attrs.getNamedItem("attr.name");
            Node forNode=attrs.getNamedItem("for");
            if( idNode != null & nameNode != null && forNode != null )
            {
                String label=nameNode.getNodeValue();
                String forLabel=forNode.getNodeValue();
                if( label.compareTo( "label" ) == 0 && forLabel.compareTo( "node" ) == 0 )
                {
                    id=idNode.getNodeValue();
                    CLogger.logger().info("Label Node value [%s=%s]",nameNode.getNodeValue(),idNode.getTextContent() );
                }
            }
        }

        //
        // Ok We now have the right id value for the node label
        // get list of nodes
        //
        Map<String,String> nodes=new HashMap<String,String>();
        NodeList allNodes=graphDoc.getElementsByTagName( "node" );
        length=allNodes.getLength();
        for( int idx=0;idx<length;idx++)
        {
            Node node=allNodes.item( idx );

            //
            // Extract unique id
            //
            NamedNodeMap nAttrs=node.getAttributes();
            Node idNode=nAttrs.getNamedItem( "id" );
            String nodeId=idNode.getNodeValue();

            //
            // Now walk children for label of node
            //
            NodeList childNodes=node.getChildNodes();
            int dlen=childNodes.getLength();
            for( int didx=0; didx< dlen; didx ++ )
            {
                Node dNode=childNodes.item( didx );
                NamedNodeMap attrs=dNode.getAttributes();
                if( attrs != null )
                {
                    //
                    // Check that key is the label id
                    // 
                    Node    keyNode=attrs.getNamedItem( "key" );
                    String  key=keyNode.getNodeValue();
                    if( key.compareTo( id ) == 0 )
                    {
                        if( dNode.hasChildNodes() )
                        {
                            String nodeLabel=dNode.getFirstChild().getNodeValue();
                            CLogger.logger().info("Label Node value [%s] [%s]",nodeId,nodeLabel);
                            //
                            // Ok, we add the node to our set
                            //
                            nodes.put( nodeId, nodeLabel );
                        }
                    }
                }
            }
        }

        //
        // And get all of the edges
        //
        NodeList allEdges=graphDoc.getElementsByTagName( "edge" );
        length=allEdges.getLength();
        boolean createGraph=true;
        for( int idx=0;idx<length;idx++)
        {
            //
            // Pull the edge
            //
            Node edge=allEdges.item( idx );

            //
            // And get the crucial
            //
            NamedNodeMap attrs=edge.getAttributes();
            String source=attrs.getNamedItem( "source" ).getNodeValue();
            String sink=attrs.getNamedItem( "target" ).getNodeValue();
            CLogger.logger().info("Connecting [%s]<->[%s]",source,sink);

            //
            // Only process non loopback nodes
            // This is a multigraph after all...
            //
            if(source.equals(sink)==false)
            {
                //
                // Add both vertices
                //
                graph.addVertex( source );
                graph.addVertex( sink );

                //
                // And connect
                //
                graph.addEdge( source, sink );
            }
        }

        CLogger.logger().info("Graph contains [%d] nodes",nodes.size());

        //
        // All done
        //
        return( graphName );
    }

    //
    // For graphml stuff add and edge to the db
    //
    public void addEdgeToDb(String source, String target, Map<String,String> labels, String graphName, String path, boolean createGraph)
    {
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
            // Do I create the graph
            //
            if( createGraph )
            {
                CLogger.logger().info("Creating graph [%s]",graphName);
                //
                // Reset the stats
                //
                cStmnt=db_con.prepareCall( "{ call ifatdb.create_graph( ?, ? ) }" );
                cStmnt.setString( "grph", graphName );
                cStmnt.setString( "dsc", path );
    
                //
                // Run it
                //
                ResultSet results=cStmnt.executeQuery();
    
                //
                // Close the CallableStatement
                //
                cStmnt.close();
            }

            //
            // And build the edge
            //
            String srcLabel = graphName+source+labels.get(source);
            String targLabel = graphName+target+labels.get(target);

            //
            // Reset the stats
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.connect_vertices( ?, ?, ? ) }" );
            cStmnt.setString( "lhs", srcLabel );
            cStmnt.setString( "rhs", targLabel );
            cStmnt.setString( "grp", graphName );

            //
            // Run it
            //
            ResultSet results=cStmnt.executeQuery();

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
    // Simple build of the multigraphs
    //
    public void load(String graphSource)
    {
        //
        // Do some debug on how long this takes
        //
        Long startTime=System.currentTimeMillis();

        int createCount=0;
        int nodeCount=0;
        int loopCount=0;
        Set<String> nodeHeap=new HashSet<String>();
 
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
            cStmnt=db_con.prepareCall( "{ call ifatdb.fetch_topology( ? ) }" );
            cStmnt.setString( "grp", graphSource );

            //
            // Run it
            //
            ResultSet results=cStmnt.executeQuery();

            //
            // And now get the edges
            //
            boolean rc=cStmnt.getMoreResults();
            if(rc!=true)
            {
                CLogger.logger().warning("Failed to retrieve edges - no topo");
                return;
            }
        
            //
            // Edge set...
            //
            results=cStmnt.getResultSet();
            while(results.next())
            {
                //
                // Get the id and name
                //
                String  source=results.getString("source_node");
                String  sink=results.getString("sink_node");

                //
                // Only process non loopback nodes
                // This is a multigraph after all...
                //
                if(source.equals(sink)==false)
                {
                    //
                    // So do we have an entry to map either of the nodes to a graph?
                    //
                    Multigraph<String, CEdge> srcGraph=mNodeToGraph.get( source );
                    Multigraph<String, CEdge> sinkGraph=mNodeToGraph.get( sink );

                    //
                    // Now, either:
                    // 1. srcGraph and sinkGraph are null
                    // 2. One is null - which means we are adding an edge
                    // 3. Both exist in which case:
                    //    a. srcGraph == sinkGraph and this is a duplicate
                    //    b. srcGraph != sinkGraph and this is a merge graph moment. We always merge smallest to
                    //       largest
                    //

                    //
                    // Ok, determine action
                    //
                    EResolveType type=EResolveType.E_Ignore;
                    Multigraph<String, CEdge> targetGraph=null;
                    if( srcGraph == null && sinkGraph == null )
                    {
                        type=EResolveType.E_Creation;
                        createCount++;
                    }
                    else
                    {
                        //
                        // So...
                        //
                        if( sinkGraph != null && srcGraph != null )
                        {
                            int sinkCount   = sinkGraph.vertexSet().size();
                            int srcCount    = srcGraph.vertexSet().size();
                            //
                            // Duplicate edge?
                            //
                            if( sinkGraph == srcGraph )
                            {
                                //type=EResolveType.E_Ignore;
                                type=EResolveType.E_AddEdge;
                                targetGraph=sinkGraph;
                            }
                            else
                            {
                                //
                                // We're merging - which one..
                                //
                                if( sinkCount <= srcCount )
                                {
                                    type=EResolveType.E_MergeSink;
                                }
                                else
                                {
                                    type=EResolveType.E_MergeSrc;
                                }
                            }
                        }
                        else
                        {
                            type=EResolveType.E_AddEdge;
                            if( sinkGraph != null )
                            {
                                targetGraph=sinkGraph;
                            }
                            else
                            {
                                targetGraph=srcGraph;
                            }
                        }
                    }

                    //
                    // Ok, lets perform actions
                    //
                    switch( type )
                    {
                        case E_Creation:
                            {
                                //
                                // Build a new graph
                                //
                                Multigraph<String, CEdge> graph=new Multigraph<String, CEdge>(CEdge.class);        

                                //
                                // Add both vertices
                                //
                                graph.addVertex( source );
                                graph.addVertex( sink );
                                nodeHeap.add(source);
                                nodeHeap.add(sink);

                                //
                                // And connect
                                //
                                graph.addEdge( source, sink );
    
                                // 
                                //  And add twice to the set
                                // 
                                mNodeToGraph.put( source, graph );
                                mNodeToGraph.put( sink, graph );

                                //
                                // Add the graph
                                //
                                mGraphs.add( graph );
                            }
                            break;
                        case E_AddEdge:
                            {
                                //
                                // Add both vertices
                                //
                                targetGraph.addVertex( source );
                                targetGraph.addVertex( sink );

                                //
                                // And connect
                                //
                                targetGraph.addEdge( source, sink );

                                // 
                                //  And add sink node mapping
                                // 
                                mNodeToGraph.put( sink, targetGraph );
                                nodeHeap.add(source);
                                nodeHeap.add(sink);
                            }
                            break;
                        case E_MergeSink:
                            {
                                //
                                // Ok, strategy is to merge all sink nodes and edges into source
                                //
                                Set<String> sinkNodes=sinkGraph.vertexSet();
                                Set<CEdge>  sinkEdges=sinkGraph.edgeSet();

                                //
                                // Merge in the graph
                                //
                                Iterator<String> nItr=sinkNodes.iterator();
                                while( nItr.hasNext() )
                                {
                                    String sVert=nItr.next();
    
                                    //
                                    // Add the node to the src graph and change reference in map
                                    //
                                    srcGraph.addVertex( sVert );
                                    mNodeToGraph.remove( sVert );
                                    mNodeToGraph.put( sVert, srcGraph );
                                    nodeHeap.add(sVert);
                                }
    
                                //
                                // Add in the edges
                                //
                                Iterator<CEdge> eItr=sinkEdges.iterator();
                                while( eItr.hasNext() )
                                {
                                    CEdge edge=eItr.next();

                                    srcGraph.addEdge( edge.getSource(), edge.getTarget() );
                                }

                                //
                                // And connect the two new nodes.
                                //
                                srcGraph.addEdge( source, sink );

                                //
                                // And remove sink
                                //
                                mGraphs.remove( sinkGraph );
                            }
                            break;
                        case E_MergeSrc:
                            {
                                //
                                // Ok, strategy is to merge all source nodes and edges into sink
                                //
                                Set<String> srcNodes=srcGraph.vertexSet();
                                Set<CEdge> srcEdges=srcGraph.edgeSet();

                                //
                                // Merge in the graph
                                //
                                Iterator<String> nItr=srcNodes.iterator();
                                while( nItr.hasNext() )
                                {
                                    String sVert=nItr.next();
    
                                    //
                                    // Add the node to the src graph and change reference in map
                                    //
                                    sinkGraph.addVertex( sVert );
                                    mNodeToGraph.remove( sVert );
                                    mNodeToGraph.put( sVert, sinkGraph );
                                    nodeHeap.add(sVert);
                                }
    
                                //
                                // Add in the edges
                                //
                                Iterator<CEdge> eItr=srcEdges.iterator();
                                while( eItr.hasNext() )
                                {
                                    CEdge edge=eItr.next();

                                    sinkGraph.addEdge( edge.getSource(), edge.getTarget() );
                                }

                                //
                                // And connect the two new nodes.
                                //
                                sinkGraph.addEdge( source, sink );

                                //
                                // And remove source
                                //
                                mGraphs.remove( srcGraph );
                            }
                            break;
                        case E_Ignore:
                            {
                                //
                                // Nop
                                //
                            }
                            break;
                    }

                }
                else
                {
                    //
                    // Data for debug
                    //
                    if( nodeHeap.contains( source ) == false )
                    {
                        loopCount++;
                    }
                    nodeHeap.add(source);
                }
            }  


            CLogger.logger().info("Loop count [%d] createCount [%d], processed nodes [%d]",loopCount,createCount,nodeHeap.size());

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
        // And output duration
        //
        Long endTime=System.currentTimeMillis();
        double duration=(endTime.doubleValue() - startTime.doubleValue() ) / 1000.0;
        CLogger.logger().info("Loaded graph in [%f] seconds",duration);

        CLogger.logger().info("Resolved [%d] graphs with [%d] nodes",mGraphs.size(),mNodeToGraph.size());

        //
        // All done
        //
        return;
    }
}
