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

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

//-----------------------------------------------------------------
// CBetweenEntropy class
//
// Calculates the betweenes centrailty of each node.
//
//-----------------------------------------------------------------
public class CBetweenEntropy extends CAbstractEntropyCalculator
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
    public CBetweenEntropy( CGraphLoader loader )
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
    // Calculate centrality of whole graph
    // Implements the Brandes algorithm
    //
    public Double calculateEntropy( Integer gid, CDbPool pool, Set<String> scope )
    {
        Double result=0.0;

        //
        // Get the full monty
        //
        Set<String> vertices=mGraph.vertexSet();
        Set<CEdge>  edges=mGraph.edgeSet();

        Integer edgeSize=edges.size();
        Integer vertexSize=vertices.size();

        Integer maxEdges=(vertexSize-1)*(vertexSize-2)/2;

        CLogger.logger().info("Betweenesss Brandes Centrality Analysing graph with [%d] vertices and [%d] edges, max [%d]",vertexSize,edgeSize, maxEdges);

        //
        // Create and initialize the centrality list for this graph
        //
        Map<String,Double> centrality=new HashMap<String,Double>();
        Iterator<String> itr=vertices.iterator();
        while( itr.hasNext() )
        {
            centrality.put( itr.next(), 0.0 );
        }

        //
        // Now calculate the running sum
        //
        Double tve=0.0;

        //
        // Ok, iterater over the vertices
        //
        itr=vertices.iterator();
        while( itr.hasNext() )
        {
            String vertex=itr.next();                                               // Brandes var s

            //
            // Do we have a scope?
            //
            if( scope != null )
            {
                if( scope.contains( vertex ) == false )
                {
                    //
                    // Not in scope skip
                    //
                    CLogger.logger().info("Skipping this sucker [%s]",vertex);
                    continue;
                }
            }

            Stack<String>                   neighbors=new Stack<String>();          // Brandes var S
            Map<String,List<String>>        paths=new HashMap<String,List<String>>();      // Brandes var P[w]


            //
            // Partial centrality
            //
            Map<String,Double>  sigma=new HashMap<String,Double>(); // Brandes var \sigma[t] 
            Iterator<String> initItr=vertices.iterator();
            while( initItr.hasNext() )
            {
                sigma.put( initItr.next(), 0.0 );
            }
            sigma.put( vertex, 1.0 );

            //
            // Partial distance
            //
            Map<String,Double>  distance=new HashMap<String,Double>(); // Brandes var d[t] 
            initItr=vertices.iterator();
            while( initItr.hasNext() )
            {
                distance.put( initItr.next(), -1.0 );
            }
            distance.put( vertex, 0.0 );

            //
            // The queue
            //
            Queue<String> shells=new PriorityQueue<String>( vertexSize );               // Brandes var Q
            if( shells.offer( vertex ) != true )
            {
                CLogger.logger().fatal("Could not store in queue vertex [%s]",vertex);
            }
            CLogger.logger().info("Offered [%s] sigma [%f]",vertex,sigma.get( vertex ) );

            //
            // And iterate through
            //
            while( shells.peek() != null )
            {
                //
                // Get the sucker
                //
                String vNode=shells.poll(); // Brandes v

                CLogger.logger().info("Processing [%s] sigma [%f]",vNode,sigma.get( vNode ) );

                //
                // Pop onto neighbors stack
                //
                neighbors.push( vNode );

                //
                // Walk all neighbors of vNode
                //
                Set<CEdge> vEdges=mGraph.edgesOf( vNode );
                Iterator<CEdge> vItr=vEdges.iterator();
                while( vItr.hasNext() )
                {
                    //
                    // Grab the edge
                    //
                    CEdge ed=vItr.next();
                    
                    String vNeighbor=ed.getTarget();                // Brandes w
                    if( vNeighbor.compareTo( vNode ) == 0 )
                    {
                        //
                        // Edge oriented incorrectly flip it
                        //
                        vNeighbor=ed.getSource();
                    }

                    //
                    // First time?
                    //
                    if( distance.get( vNeighbor ) < 0.0 )
                    {
                        //
                        // Queue it
                        //
                        if( shells.offer( vNeighbor ) != true )
                        {
                            CLogger.logger().fatal("Could not store in queue vertex [%s]",vNeighbor);
                        }
                        Double vNdist=distance.get( vNode ) + 1.0;
                        distance.put( vNeighbor, vNdist );
                        CLogger.logger().info("Distnace of [%s] from [%s] now [%f]",vNeighbor,vNode, vNdist );
                    }

                    //
                    // Shortesr path to vNeighbor via vNode?
                    //
                    if( distance.get( vNeighbor ) == ( distance.get( vNode ) + 1.0 ) )
                    {
                        //
                        // Update sigma
                        //
                        Double sigVNeighbor=sigma.get( vNeighbor ) + sigma.get( vNode );
                        sigma.put( vNeighbor, sigVNeighbor );
                        if( sigVNeighbor > 0.0 )
                        {
                            CLogger.logger().info("Sigma of [%s] from [%s] now [%f]",vNeighbor,vNode, sigVNeighbor );
                        }

                        //
                        // And add to list
                        //
                        List<String> vNPath=paths.get( vNeighbor );
                        if( vNPath == null )
                        {
                            vNPath=new LinkedList<String>();
                            vNPath.add( vNode );
                            paths.put( vNeighbor, vNPath );
                        }
                        else
                        {
                            vNPath.add( vNode );
                        }
                    }
                }
            }

            //
            // And compute centrality
            //

            //
            // deltas
            //
            Map<String,Double>  delta=new HashMap<String,Double>(); // Brandes var \delta[v] 
            initItr=vertices.iterator();
            while( initItr.hasNext() )
            {
                delta.put( initItr.next(), 0.0 );
            }

            //
            // Away we go...
            //
            while( neighbors.empty() != true )
            {
                //
                // Grab em
                //
                String node=neighbors.pop();     // Brandes w

                List<String> nodePath=paths.get( node );
                if( nodePath != null )
                {
                    Iterator<String> pItr=nodePath.iterator();
                    while( pItr.hasNext() )
                    {
                        String hop=pItr.next();     // Brandes v

                        Double delta_hop=delta.get( hop );
                        Double delta_node=delta.get( node );
                        Double sigma_hop=sigma.get( hop );      // \sigma[v]
                        Double sigma_node=sigma.get( node );    // \sigma[w]

                        CLogger.logger().info("sigma_hop [%f] sigma_node [%f]",sigma_hop,sigma_node);

                        //
                        // Compute and store
                        //
                        Double newDelta = delta_hop + ( (sigma_hop/sigma_node)*(1.0 + delta_node) );
                        delta.put( hop, newDelta );

                    }

                    //
                    // Are we computing a finallzed value?
                    // Brandes w != s
                    //
                    if( node.compareTo( vertex ) != 0 )
                    {
                        Double newCentrality = centrality.get( node ) + delta.get( node );
                        centrality.put( node, newCentrality );
                        CLogger.logger().info("Betweeness for [%s] now [%f]",node,newCentrality);
                    }
                }
            }

        }

        //
        // Ok store...
        //
        Iterator<String> valItr=centrality.keySet().iterator();
        while( valItr.hasNext() )
        {
            String vtx=valItr.next();

            //
            // Entropy
            //
            Double nodeCentrality=centrality.get( vtx )/(2*maxEdges.doubleValue());

            //
            // Calculate the new value
            //
            tve+=nodeCentrality;
            
            CLogger.logger().info("Betweenness Centrality of vertex [%s]  is [%f]",vtx,nodeCentrality );

            //
            // And store in the database
            //
            this.updateVertexDb( vtx, nodeCentrality, pool );
        }

        //
        // And entropy of whole graph
        //
        CLogger.logger().info("Cluster Entropy of graph is [%f]",tve );

        //
        // And home
        //
        return( result );
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
            cStmnt=db_con.prepareCall( "{ call ifatdb.update_subgraph_VEN2( ?, ? ) }" );
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
            cStmnt=db_con.prepareCall( "{ call ifatdb.update_vertex_BC( ?, ? ) }" );
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
    public void buildDistribution( String graph, Integer points,  CDbPool pool, Double upperLimit)
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
            cStmnt=db_con.prepareCall( "{ call ifatdb.reset_BC_stats( ? ) }" );
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
            // Now get the bounds for VEN2
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
                maxVal=results.getDouble( "MaxBC" );
                minVal=results.getDouble( "MinBC" );
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
                cStmnt=db_con.prepareCall( "{ call ifatdb.populate_BC( ?, ?, ? ) }" );
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
            cStmnt=db_con.prepareCall( "{ call ifatdb.reset_BC_stats( ? ) }" );
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
            // Now get the bounds for VEN2
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
                maxVal=results.getDouble( "MaxBC" );
                minVal=results.getDouble( "MinBC" );
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
                cStmnt=db_con.prepareCall( "{ call ifatdb.populate_BC( ?, ?, ? ) }" );
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
