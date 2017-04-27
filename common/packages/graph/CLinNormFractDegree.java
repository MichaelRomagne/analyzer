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

import org.jgrapht.graph.Multigraph;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

//-----------------------------------------------------------------
// CLinNormFractDegree class
//
// Calculate the vertex and total normalized fractional entropy of a graph
//
//-----------------------------------------------------------------
public class CLinNormFractDegree extends CAbstractEntropyCalculator
{
    //---------------------------------------------
    // Members
    //---------------------------------------------

    private Multigraph<String,CEdge>    mGraph=null;

    //---------------------------------------------
    // Implementation
    //---------------------------------------------

    //
    // For now, empty constructor
    //
    public CLinNormFractDegree( CGraphLoader loader )
    {
        super( loader );
    }

    //
    // Initialise with a multigraph
    //
    public void initialise( Multigraph<String,CEdge> graph )
    {
        mGraph=graph;
    
        //
        // All done
        //
        return;
    }

    //
    // Calculate entropy of whole graph
    //
    public Double calculateEntropy( Integer gid, CDbPool pool , Set<String> scope)
    {
        Double result=0.0;

        //
        // Get the full monty
        //
        Set<String> vertices=mGraph.vertexSet();
        Set<CEdge>  edges=mGraph.edgeSet();

        Integer edgeSize=edges.size();
        Integer vertexSize=vertices.size();

        CLogger.logger().info("Normalized Fractional Degree Analysing graph with [%d] vertices and [%d] edges",vertexSize,edgeSize);

        //
        // Convert to base 2 logs
        //
        Double log2=Math.log( 2 );

        //
        // Now calculate the running sum
        //
        Double tve=0.0;

        //
        // Ok, iterater over the vertices
        //
        Iterator<String> itr=vertices.iterator();
        while( itr.hasNext() )
        {
            String vertex=itr.next();

            //
            // In scope?
            //
            if( scope != null )
            {
                if( scope.contains( vertex ) == false )
                {
                    CLogger.logger().info("Skipping vertex [%s]", vertex);
                    continue;
                }
            }

            //
            // Get the degree
            //
            Integer degree=mGraph.degreeOf( vertex );

            //
            // And calculate
            //
            Double probV=degree.doubleValue() / ( 2.0 * edgeSize );

            //
            // Entropy
            //
            Double entropy=-1.0 * probV * Math.log( probV ) / log2;

            //
            // In cache? if not make it so
            //
            Double normFactor= mLoader.clusterCoefficient( vertex );
            if( normFactor == null )
            {
                normFactor= ( 2.0-this.clusteringCoefficient( vertex ) );
                CLogger.logger().info("Failed to retrueve value for [%s]",vertex);
            }
            else
            {
                //
                // Invert the clustering coefficient
                //
                normFactor = 2.0-normFactor;
            }

            //
            // And scale
            //
            entropy=entropy * normFactor;

            //
            // Calculate the new value
            //
            tve+=entropy;
            
            CLogger.logger().info("Normalized Fractional Degree Entropy of vertex is [%f], degree [%d]",entropy,degree );

            //
            // And store in the database
            //
            this.updateVertexDb( vertex, entropy, pool );
        }

        //
        // And entropy of whole graph
        //
        CLogger.logger().info("Normalized Fractional Degree Entropy of graph is [%f]",tve );

        //
        // Save result
        //
        this.updateGraphDb( gid, tve, pool );

        //
        // And home
        //
        return( result );
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
    public Double clusteringCoefficient( String vertex )
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
        Set<CEdge> edges=mGraph.edgesOf( vertex );
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
                Set<CEdge> internalCons=mGraph.getAllEdges( member, candVertex );
                
                //
                // Each of these count to the cycleCount
                //
                subgraphEdges += (double )internalCons.size();
            }
        }

        //
        // Account for double counting
        //
        subgraphEdges=subgraphEdges / 2;

        //
        // Get degree
        //
        Double degree=( double )( mGraph.degreeOf( vertex ) );

        //
        // And calculate
        //
        clustCoeff = ( 2.0 * subgraphEdges ) / ( degree * (degree + 1 ) );
        if( clustCoeff == 0.0 )
        {
            clustCoeff=1.0;
            CLogger.logger().warning("Clustering coefficient of [%s] is nil - adjusting to 1",vertex);
        }

        CLogger.logger().info("Clustering coefficient of [%s] degree [%f] is [%f]",vertex,degree,clustCoeff);

        //
        // Send it home
        //
        return( clustCoeff );
    }

    //
    // For a given vertex, which neighbors connect to one other neighbor
    // Ie what is the |C3| value
    //
    public int threeCycleDetector( String vertex )
    {
        int cycleCount=0;

        //
        // Neighbor set
        //
        Set<String> neighbors=new HashSet<String>();

        //
        // Grab all edges
        //
        Set<CEdge> edges=mGraph.edgesOf( vertex );
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
                neighbors.add( target );
            }
            if( source.compareTo( vertex ) != 0 )
            {
                neighbors.add( source );
            }
        }

        //
        // Ok, the messy bit!!!
        //
        Iterator<String> nitr=neighbors.iterator();
        while( nitr.hasNext() )
        {
            String neighbor=nitr.next();

            //
            // And check all others!
            //
            Iterator<String> citr=neighbors.iterator();
            while( citr.hasNext() )
            {
                String candVertex=citr.next();

                //
                // Get all connections
                //
                Set<CEdge> internalCons=mGraph.getAllEdges( neighbor, candVertex );
                
                //
                // Each of these count to the cycleCount
                //
                cycleCount += internalCons.size();
            }
        }

        //
        // Account for double counting
        //
        cycleCount=cycleCount / 2;

        //
        // Send it home
        //
        return( cycleCount );
    }

    //
    // Update database record of graph
    //
    public void updateGraphDb( Integer gid, Double val, CDbPool pool )
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
            db_con=pool.checkOut();

            //
            // Build & run the sql
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.update_subgraph_VEN3( ?, ? ) }" );
            cStmnt.setInt( "id", gid );
            cStmnt.setDouble( "val", val );
    
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

            pool.checkIn(db_con);
        }

        //
        // All done
        //
        return;
    }

    //
    // Update database record of vertex
    //
    public void updateVertexDb( String node, Double val, CDbPool pool )
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
            db_con=pool.checkOut();

            //
            // Build & run the sql
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.update_vertex_VEN3( ?, ? ) }" );
            cStmnt.setString( "nd", node );
            cStmnt.setDouble( "val", val );
    
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

            pool.checkIn(db_con);
        }
        //
        // All done
        //
        return;
    }

    //
    // Build the distribution table for calculation
    //
    public void buildDistribution( String graph, Integer points,  CDbPool pool, Double upperLimit )
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
            db_con=pool.checkOut();

            //
            // Build & run the sql
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.reset_VEN3_stats( ? ) }" );
            cStmnt.setString( "grp", graph );
    
            //
            // Run it
            //
            ResultSet results=cStmnt.executeQuery();

            //
            // Close the CallableStatement
            //
            cStmnt.close();

            //
            // Now get the bounds for VEN3
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.get_entropy_bounds( ? ) }" );
            cStmnt.setString( "grp", graph );
    
            //
            // Run it
            //
            double maxVal=0.0;
            double minVal=0.0;
            results=cStmnt.executeQuery();
            while(results.next())
            {
                //
                // Get the spread
                //
                maxVal=results.getDouble( "MaxVEN3" );
                minVal=results.getDouble( "MinVEN3" );
            }

            //
            // Close the CallableStatement
            //
            cStmnt.close();

            //
            // Override if we have a limit
            //
            if( upperLimit != null )
            {
                maxVal=upperLimit;
            }

            //
            // So calculate the step
            //
            double step=(maxVal - minVal)/points.doubleValue();

            // 
            // And run through updating the distrib
            //
            Double floor=minVal;
            for(int idx=0;idx < points;idx++)
            {
                //
                // And calculate the distribution
                //
                cStmnt=db_con.prepareCall( "{ call ifatdb.populate_VEN3( ?, ?, ? ) }" );
                cStmnt.setString( "grp", graph );
                cStmnt.setDouble( "floor", floor );
                cStmnt.setDouble( "ceiling", (floor+step) );
        
                //
                // Run it
                //
                results=cStmnt.executeQuery();

                //
                // Close the CallableStatement
                //
                cStmnt.close();

                //
                // Increment the floor
                //
                floor+=step;
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

            pool.checkIn(db_con);
        }

        //
        // All done
        //
        return;
    }

    //
    // Build the distribution table for calculation
    // for all graphs
    //
    public void buildAllGraphDistribution( String graph, Integer points,  CDbPool pool )
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
            db_con=pool.checkOut();

            //
            // Build & run the sql
            //
            cStmnt=db_con.prepareCall( "{ call ifatdb.reset_VEN3_stats( ? ) }" );
            cStmnt.setString( "grp", graph );
    
            //
            // Run it
            //
            ResultSet results=cStmnt.executeQuery();

            //
            // Close the CallableStatement
            //
            cStmnt.close();

            //
            // Now get the bounds for VEN3
            //
            //cStmnt=db_con.prepareCall( "{ call ifatdb.get_all_entropy_bounds( ? ) }" );
            //cStmnt.setString( "grp", graph );
            cStmnt=db_con.prepareCall( "{ call ifatdb.get_all_entropy_bounds(  ) }" );
    
            //
            // Run it
            //
            double maxVal=0.0;
            double minVal=0.0;
            results=cStmnt.executeQuery();
            while(results.next())
            {
                //
                // Get the spread
                //
                maxVal=results.getDouble( "MaxVEN3" );
                minVal=results.getDouble( "MinVEN3" );
            }

            //
            // Close the CallableStatement
            //
            cStmnt.close();

            //
            // So calculate the step
            //
            double step=(maxVal - minVal)/points.doubleValue();

            // 
            // And run through updating the distrib
            //
            Double floor=minVal;
            for(int idx=0;idx < points;idx++)
            {
                //
                // And calculate the distribution
                //
                cStmnt=db_con.prepareCall( "{ call ifatdb.populate_VEN3( ?, ?, ? ) }" );
                cStmnt.setString( "grp", graph );
                cStmnt.setDouble( "floor", floor );
                cStmnt.setDouble( "ceiling", (floor+step) );
        
                //
                // Run it
                //
                results=cStmnt.executeQuery();

                //
                // Close the CallableStatement
                //
                cStmnt.close();

                //
                // Increment the floor
                //
                floor+=step;
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

            pool.checkIn(db_con);
        }

        //
        // All done
        //
        return;
    }

}
