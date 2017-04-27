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
import java.util.Iterator;

//-----------------------------------------------------------------
// CInverseDegreeEntropy class
//
// Calculate the vertex and total inverse degree entropy of a graph
//
//-----------------------------------------------------------------
public class CInverseDegreeEntropy extends CAbstractEntropyCalculator
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
    public CInverseDegreeEntropy( CGraphLoader loader )
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

        CLogger.logger().info("Inverse Degree : Analysing graph with [%d] vertices and [%d] edges",vertexSize,edgeSize);

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
                else
                {
                    CLogger.logger().info("Calculating vertex [%s]", vertex);
                }
            }
            //
            // Get the degree
            //
            Integer degree=mGraph.degreeOf( vertex );

            //
            // And calculate
            //
            Double probV=1.0 / degree.doubleValue();

            //
            // Entropy
            //
            Double entropy=-1.0 * probV * Math.log( probV ) / log2;

            //
            // Calculate the new value
            //
            tve+=entropy;
            
            CLogger.logger().info("Inverse Degree Entropy of vertex is [%f], degree [%d]",entropy,degree );

            //
            // And store in the database
            //
            this.updateVertexDb( vertex, entropy, pool );
        }

        //
        // And entropy of whole graph
        //
        CLogger.logger().info("Inverse Degree Entropy of graph is [%f]",tve );

        //
        // Save result
        //
        this.updateGraphDb( gid, tve, pool );

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
            cStmnt=db_con.prepareCall( "{ call ifatdb.update_subgraph_VE1( ?, ? ) }" );
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
            cStmnt=db_con.prepareCall( "{ call ifatdb.update_vertex_VE1( ?, ? ) }" );
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
    public void buildDistribution( String graph, Integer points,  CDbPool pool , Double upperLimit)
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
            cStmnt=db_con.prepareCall( "{ call ifatdb.reset_VE1_stats( ? ) }" );
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
            // Now get the bounds for VE1
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
                maxVal=results.getDouble( "MaxVE1" );
                minVal=results.getDouble( "MinVE1" );
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
                cStmnt=db_con.prepareCall( "{ call ifatdb.populate_VE1( ?, ?, ? ) }" );
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
            cStmnt=db_con.prepareCall( "{ call ifatdb.reset_VE1_stats( ? ) }" );
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
            // Now get the bounds for VE1
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
                maxVal=results.getDouble( "MaxVE1" );
                minVal=results.getDouble( "MinVE1" );
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
                cStmnt=db_con.prepareCall( "{ call ifatdb.populate_VE1( ?, ?, ? ) }" );
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
