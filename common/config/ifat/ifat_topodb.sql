############################################################
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
# Create user ermintrude and allow login and proc usage from any host
#
grant all privileges on ifatdb.* to 'ermintrude'@'%' identified by 'm00' with grant option;
grant select on `mysql`.`proc` to 'ermintrude'@'%';

#
# Create user ermintrude and allow login and proc usage from localhost
#
grant all privileges on ifatdb.* to 'ermintrude'@'localhost' identified by 'm00' with grant option;
grant select on `mysql`.`proc` to 'ermintrude'@'localhost';

drop database if exists ifatdb;
create database ifatdb character set "UTF8";

use ifatdb;

#
# List of active graphs
#
drop table if exists graphs;
create table graphs
(
    graph       VARCHAR(255),   # The graph name
    description TEXT,           # Description
    created     BIGINT,         # Creation timestamp

    UNIQUE INDEX(graph)
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Connected subgraphs of graphs (may be a complete subset)
# with data calculated
#
drop table if exists subgraphs;
create table subgraphs
(
    graph       VARCHAR(255),           # The graph name
    gid         INT AUTO_INCREMENT,     # Description
    vertex_cnt  INT,                    # Num vertices
    edge_cnt    INT,                    # Num edges
    entropy     DOUBLE,                 # Korner Entropy
    chrom_ent   DOUBLE,                 # Chromatic Entropy
    vne_ent     DOUBLE,                 # Von Neumann Entropy
    total_VE1   DOUBLE,                 # Total Degree Entropy
    total_VE2   DOUBLE,                 # Total fractional degree Entropy
    total_VEN1  DOUBLE,                 # VE1 Normalized for loops
    total_VEN2  DOUBLE,                 # VE2 Normalized for loops
    total_CVEN2  DOUBLE,                 # VE2 Normalized for loops
    total_VEN3  DOUBLE,                 # VE2 Normalized for loops
    created     BIGINT,                 # Creation timestamp

    UNIQUE INDEX(gid)
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# create a sub graph
#
drop procedure if exists create_subgraph;
delimiter DTR
create procedure create_subgraph(IN grph VARCHAR(255), IN ec INT,IN vc INT)
    begin
        #
        # Simple insert
        #
        INSERT INTO subgraphs VALUES ( grph, 0, vc, ec, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,0.0,0.0, UNIX_TIMESTAMP() )
            ON DUPLICATE KEY UPDATE 
                entropy=0.0,
                chrom_ent=0.0,
                vne_ent=0.0,
                total_VE1=0.0,
                total_VE2=0.0,
                total_VEN1=0.0,
                total_VEN2=0.0,
                total_CVEN2=0.0,
                total_VEN3=0.0;

        #
        # And return the inserted id
        #
        SELECT LAST_INSERT_ID() AS gid;
    end
DTR
delimiter ;

#
# And some updates
#
drop procedure if exists update_subgraph_entropy;
delimiter DTR
create procedure update_subgraph_entropy(IN id INT, IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE subgraphs SET entropy=val WHERE gid=id;
    end
DTR
delimiter ;
drop procedure if exists update_subgraph_chrom;
delimiter DTR
create procedure update_subgraph_chrom(IN id INT, IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE subgraphs SET chrom_ent=val WHERE gid=id;
    end
DTR
delimiter ;
drop procedure if exists update_subgraph_vne;
delimiter DTR
create procedure update_subgraph_vne(IN id INT, IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE subgraphs SET vne_ent=val WHERE gid=id;
    end
DTR
delimiter ;
drop procedure if exists update_subgraph_VE1;
delimiter DTR
create procedure update_subgraph_VE1(IN id INT, IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE subgraphs SET total_VE1=val WHERE gid=id;
    end
DTR
delimiter ;
drop procedure if exists update_subgraph_VE2;
delimiter DTR
create procedure update_subgraph_VE2(IN id INT, IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE subgraphs SET total_VE2=val WHERE gid=id;
    end
DTR
delimiter ;
drop procedure if exists update_subgraph_VEN1;
delimiter DTR
create procedure update_subgraph_VEN1(IN id INT, IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE subgraphs SET total_VEN1=val WHERE gid=id;
    end
DTR
delimiter ;
drop procedure if exists update_subgraph_VEN2;
delimiter DTR
create procedure update_subgraph_VEN2(IN id INT, IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE subgraphs SET total_VEN2=val WHERE gid=id;
    end
DTR
delimiter ;
drop procedure if exists update_subgraph_VEN3;
delimiter DTR
create procedure update_subgraph_VEN3(IN id INT, IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE subgraphs SET total_VEN3=val WHERE gid=id;
    end
DTR
delimiter ;
drop procedure if exists update_subgraph_CVEN2;
delimiter DTR
create procedure update_subgraph_CVEN2(IN id INT, IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE subgraphs SET total_CVEN2=val WHERE gid=id;
    end
DTR
delimiter ;



#
# Used to store nodes loaded from an edges file.
#
drop table if exists vertices;
create table vertices
(
    node     VARCHAR(255),  # The node name
    graph    VARCHAR(255),  # Which graph do I belong to
    created  BIGINT,        # Creation timestamp

    UNIQUE INDEX(node)
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;


#
# Vertex entropy calcs
#
drop table if exists vertex_ent;
create table vertex_ent
(
    node    VARCHAR(255),   # The node name
    graph   VARCHAR(255),   # Which graph do I belong to
    degree  INT,            # degree of the node
    cluster DOUBLE,         # Clustering coefficient
    VE1     DOUBLE,         # Degree Entropy
    VE2     DOUBLE,         # Fractional degree Entropy
    VEN1    DOUBLE,         # VE1 Normalized for loops
    VEN2    DOUBLE,         # VE2 Normalized for loops
    CE      DOUBLE,         # Cluster Coeffient Entropy
    CVEN2   DOUBLE,         # Cluster Fract VE2 Normalized for loops
    VEN3    DOUBLE,         # Linear normalized VE2
    BC      DOUBLE,         # Betweeness Centrality
    created BIGINT,         # Creation timestamp

    UNIQUE INDEX(node)
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;


#
# Degree distribution
#
drop table if exists degree_dist;
create table degree_dist 
( 
    graph   VARCHAR(255),   # Graph
    degree  INT,            # Break Point
    freq    INT             # Count     
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Update the node degree
#
drop procedure if exists update_degree;
delimiter DTR
create procedure update_degree(IN nd VARCHAR(255), IN grp VARCHAR(255),IN dgr INT)
    begin
        INSERT INTO vertex_ent VALUES ( nd, grp, dgr, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,  UNIX_TIMESTAMP() ) 
        ON DUPLICATE KEY UPDATE degree=dgr;
    end
DTR
delimiter ;

#
# Add fixed degree
#
drop procedure if exists add_static;
delimiter DTR
create procedure add_static(IN nd VARCHAR(255), IN dgr INT)
    begin
        INSERT INTO vertex_ent VALUES ( nd, "static", dgr, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,  UNIX_TIMESTAMP() ) 
        ON DUPLICATE KEY UPDATE degree=dgr;
    end
DTR
delimiter ;

#
# And one a piece for individual stats
#
drop procedure if exists reset_degree_stats;
delimiter DTR
create procedure reset_degree_stats(IN grp VARCHAR(255))
    begin
        DELETE FROM degree_dist WHERE graph=grp;
    end
DTR
delimiter ;

#
# Get degree bounds
#
drop procedure if exists get_degree_bounds;
delimiter DTR
create procedure get_degree_bounds(IN grp VARCHAR(255))
    begin
        SELECT max(degree) as MaxDeg, min(degree) as MinDeg
            FROM vertex_ent WHERE graph=grp;
    end
DTR
delimiter ;

#
# Get degree bounds for all graphs
#
drop procedure if exists get_all_degree_bounds;
delimiter DTR
create procedure get_all_degree_bounds()
    begin
        SELECT max(degree) as MaxDeg, min(degree) as MinDeg
            FROM vertex_ent;
    end
DTR
delimiter ;

#
# Populate the degree dist table
#
drop procedure if exists populate_degree_dist;
delimiter DTR
create procedure populate_degree_dist(IN grp VARCHAR(255),IN floor INT, IN ceiling INT)
    begin
        #
        # Depending on this being a zero data point, >= or >
        #
        IF floor = 0
        THEN
            IF grp = "--Totalizer--"
            THEN
                INSERT INTO degree_dist 
                    SELECT grp,ceiling,count(*) FROM vertex_ent WHERE degree >= floor AND degree <= ceiling;
            ELSE
                INSERT INTO degree_dist 
                    SELECT grp,ceiling,count(*) FROM vertex_ent WHERE degree >= floor AND degree <= ceiling AND graph=grp;
            END IF;
        ELSE
            IF grp = "--Totalizer--"
            THEN
                INSERT INTO degree_dist 
                    SELECT grp,ceiling,count(*) FROM vertex_ent WHERE degree > floor AND degree <= ceiling;
            ELSE
                INSERT INTO degree_dist 
                    SELECT grp,ceiling,count(*) FROM vertex_ent WHERE degree > floor AND degree <= ceiling AND graph=grp;
            END IF;
        END IF;
    end
DTR
delimiter ;

#
# Clustering coefficient stats
#
drop table if exists cluster_dist;
create table cluster_dist 
( 
    graph   VARCHAR(255),   # Graph
    cluster  DOUBLE,            # Break Point
    freq    INT             # Count     
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# And one a piece for individual stats
#
drop procedure if exists reset_cluster_stats;
delimiter DTR
create procedure reset_cluster_stats(IN grp VARCHAR(255))
    begin
        DELETE FROM cluster_dist WHERE graph=grp;
    end
DTR
delimiter ;

#
# Get cluster bounds
#
drop procedure if exists get_cluster_bounds;
delimiter DTR
create procedure get_cluster_bounds(IN grp VARCHAR(255))
    begin
        SELECT max(cluster) as MaxCls, min(cluster) as MinCls
            FROM vertex_ent WHERE graph=grp;
    end
DTR
delimiter ;

#
# Get degree bounds for all graphs
#
drop procedure if exists get_all_cluster_bounds;
delimiter DTR
create procedure get_all_cluster_bounds()
    begin
        SELECT max(cluster) as MaxCls, min(cluster) as MinCls
            FROM vertex_ent;
    end
DTR
delimiter ;

#
# Populate the cluster dist table
#
drop procedure if exists populate_cluster_dist;
delimiter DTR
create procedure populate_cluster_dist(IN grp VARCHAR(255),IN floor DOUBLE, IN ceiling DOUBLE)
    begin
        #
        # Depending on this being a zero data point, >= or >
        #
        IF floor = 0.0
        THEN
            IF grp = "--Totalizer--"
            THEN
                INSERT INTO cluster_dist 
                    SELECT grp,ceiling,count(*) FROM vertex_ent WHERE cluster >= floor AND cluster <= ceiling;
            ELSE
                INSERT INTO cluster_dist 
                    SELECT grp,ceiling,count(*) FROM vertex_ent WHERE cluster >= floor AND cluster <= ceiling AND graph=grp;
            END IF;
        ELSE
            IF grp = "--Totalizer--"
            THEN
                INSERT INTO cluster_dist 
                    SELECT grp,ceiling,count(*) FROM vertex_ent WHERE cluster > floor AND cluster <= ceiling;
            ELSE
                INSERT INTO cluster_dist 
                    SELECT grp,ceiling,count(*) FROM vertex_ent WHERE cluster > floor AND cluster <= ceiling AND graph=grp;
            END IF;
        END IF;
    end
DTR
delimiter ;

#
# And some updates
#
drop procedure if exists update_vertex_cluster_coeff;
delimiter DTR
create procedure update_vertex_cluster_coeff(IN nd VARCHAR(255), IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE vertex_ent SET cluster=val WHERE node=nd;
    end
DTR
delimiter ;
drop procedure if exists update_vertex_VE1;
delimiter DTR
create procedure update_vertex_VE1(IN nd VARCHAR(255), IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE vertex_ent SET VE1=val WHERE node=nd;
    end
DTR
delimiter ;
drop procedure if exists update_vertex_VE2;
delimiter DTR
create procedure update_vertex_VE2(IN nd VARCHAR(255), IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE vertex_ent SET VE2=val WHERE node=nd;
    end
DTR
delimiter ;
drop procedure if exists update_vertex_VEN1;
delimiter DTR
create procedure update_vertex_VEN1(IN nd VARCHAR(255), IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE vertex_ent SET VEN1=val WHERE node=nd;
    end
DTR
delimiter ;
drop procedure if exists update_vertex_VEN2;
delimiter DTR
create procedure update_vertex_VEN2(IN nd VARCHAR(255), IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE vertex_ent SET VEN2=val WHERE node=nd;
    end
DTR
delimiter ;
drop procedure if exists update_vertex_VEN3;
delimiter DTR
create procedure update_vertex_VEN3(IN nd VARCHAR(255), IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE vertex_ent SET VEN3=val WHERE node=nd;
    end
DTR
delimiter ;
drop procedure if exists update_vertex_CE;
delimiter DTR
create procedure update_vertex_CE(IN nd VARCHAR(255), IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE vertex_ent SET CE=val WHERE node=nd;
    end
DTR
delimiter ;
drop procedure if exists update_vertex_BC;
delimiter DTR
create procedure update_vertex_BC(IN nd VARCHAR(255), IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE vertex_ent SET BC=val WHERE node=nd;
    end
DTR
delimiter ;

drop procedure if exists update_vertex_CVEN2;
delimiter DTR
create procedure update_vertex_CVEN2(IN nd VARCHAR(255), IN val DOUBLE)
    begin
        #
        # Simple insert
        #
        UPDATE vertex_ent SET CVEN2=val WHERE node=nd;
    end
DTR
delimiter ;


#
# Connectivity table. List of links in the topo
#
drop table if exists edges;
create table edges
(
    graph           VARCHAR(255),       # Name of the graph.
    source_node     VARCHAR(255),       # Left hand side
    sink_node       VARCHAR(255),       # Right hand side
    count           INT,                # In case of double entry - how many times has this link been entered
    weight          FLOAT DEFAULT 1.0,  # Future proofing, what is the strength of this link
    created         BIGINT,             # Timestamp link was last updated

    INDEX(source_node,sink_node)        # Index on source and destination
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# create graph
#
drop procedure if exists create_graph;
delimiter DTR
create procedure create_graph(IN grph VARCHAR(255), IN dsc TEXT)
    begin
        #
        # Simple insert
        #
        INSERT INTO graphs VALUES ( grph, dsc, UNIX_TIMESTAMP() );
    end
DTR
delimiter ;

#
# Connect two vertices.
#
drop procedure if exists connect_vertices;
delimiter DTR
create procedure connect_vertices(IN lhs VARCHAR(255),IN rhs VARCHAR(255), IN grp VARCHAR(255))
    begin
        DECLARE src VARCHAR(255) DEFAULT NULL;
        DECLARE snk VARCHAR(255) DEFAULT NULL;
        DECLARE nd1 VARCHAR(255) DEFAULT NULL;
        DECLARE nd2 VARCHAR(255) DEFAULT NULL;
        DECLARE cnt INT;

        SELECT source_node,sink_node,count FROM edges WHERE source_node=lhs AND sink_node=rhs AND graph=grp
                INTO src,snk,cnt;

        #
        # Create node?
        #
        SELECT node FROM vertices WHERE graph=grp and node=lhs INTO nd1;
        IF nd1 IS NULL
        THEN
            INSERT INTO vertices VALUES ( lhs, grp, UNIX_TIMESTAMP() );
            INSERT INTO vertex_ent VALUES ( lhs, grp, 1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, UNIX_TIMESTAMP() );
        END IF;
        SELECT node FROM vertices WHERE graph=grp and node=rhs INTO nd2;
        IF nd2 IS NULL
        THEN
            INSERT INTO vertices VALUES ( rhs, grp, UNIX_TIMESTAMP() );
            INSERT INTO vertex_ent VALUES ( rhs, grp, 1, 0.0,  0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, UNIX_TIMESTAMP() );
        END IF;

        IF src IS NULL AND snk IS NULL
        THEN
            #
            # And add, with count of 1
            #
            INSERT INTO edges VALUES (grp,lhs,rhs,1,DEFAULT,UNIX_TIMESTAMP());
        ELSE
            UPDATE edges 
                    SET count=count+1,created=UNIX_TIMESTAMP()
                    WHERE source_node=lhs AND sink_node=rhs
                    AND graph=grp;
        END IF;
    end
DTR
delimiter ;

#
# Connect two vertices with a weighting.
#
drop procedure if exists connect_vertices_weighted;
delimiter DTR
create procedure connect_vertices_weighted(IN lhs VARCHAR(255),IN rhs VARCHAR(255),IN wgt FLOAT, grp VARCHAR(255))
    begin
        DECLARE src VARCHAR(255) DEFAULT NULL;
        DECLARE snk VARCHAR(255) DEFAULT NULL;
        DECLARE cnt INT;

        SELECT source_node,sink_node,count FROM edges 
            WHERE source_node=lhs AND sink_node=rhs  AND graph=grp INTO src,snk,cnt;

        IF src IS NULL AND snk IS NULL
        THEN
            #
            # And add, with count of 1
            #
            INSERT INTO edges VALUES (grp,lhs,rhs,1,wgt,UNIX_TIMESTAMP());
        ELSE
            UPDATE edges 
                    SET count=count+1,created=UNIX_TIMESTAMP()
                    WHERE source_node=lhs AND sink_node=rhs
                    AND graph=grp;
        END IF;
    end
DTR
delimiter ;

#
# Fetch all the information required to build a topology for Nexus.
#
drop procedure if exists fetch_topology;
delimiter DTR
create procedure fetch_topology(IN grp VARCHAR(255))
    begin
        #
        # Nodes, then edges
        #
        SELECT node FROM vertices WHERE graph=grp;
        SELECT source_node,sink_node,count,weight FROM edges WHERE graph=grp;
    end
DTR
delimiter ;

##############################################
# Distribution and plot support
##############################################

#
# VE1
#
drop table if exists VE1_dist;
create table VE1_dist 
( 
    graph   VARCHAR(255),   # Graph
    val     DOUBLE,         # Break Point
    freq    INT             # Count     
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Populate the VE1 dist table
#
drop procedure if exists populate_VE1;
delimiter DTR
create procedure populate_VE1(IN grp VARCHAR(255),IN floor DOUBLE, IN ceiling DOUBLE)
    begin
        DECLARE partial_cnt INT;
        #
        # Depending on this being a zero data point, >= or >
        #
        IF floor = 0.0
        THEN
            SELECT count(*) FROM vertex_ent WHERE VE1 >= floor AND VE1 <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        ELSE
            SELECT count(*) FROM vertex_ent WHERE VE1 > floor AND VE1 <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        END IF;

        #
        # And insert
        #
        INSERT INTO VE1_dist VALUES ( grp, ceiling, partial_cnt );
    end
DTR
delimiter ;

#
# VE2
#
drop table if exists VE2_dist;
create table VE2_dist 
( 
    graph   VARCHAR(255),   # Graph
    val     DOUBLE,         # Break Point
    freq    INT             # Count     
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Populate the VE2 dist table
#
drop procedure if exists populate_VE2;
delimiter DTR
create procedure populate_VE2(IN grp VARCHAR(255),IN floor DOUBLE, IN ceiling DOUBLE)
    begin
        DECLARE partial_cnt INT;
        #
        # Depending on this being a zero data point, >= or >
        #
        IF floor = 0.0
        THEN
            SELECT count(*) FROM vertex_ent WHERE VE2 >= floor AND VE2 <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        ELSE
            SELECT count(*) FROM vertex_ent WHERE VE2 > floor AND VE2 <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        END IF;

        #
        # And insert
        #
        INSERT INTO VE2_dist VALUES ( grp, ceiling, partial_cnt );
    end
DTR
delimiter ;


#
# VEN1
#
drop table if exists VEN1_dist;
create table VEN1_dist 
( 
    graph   VARCHAR(255),   # Graph
    val     DOUBLE,         # Break Point
    freq    INT             # Count     
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Populate the VEN1 dist table
#
drop procedure if exists populate_VEN1;
delimiter DTR
create procedure populate_VEN1(IN grp VARCHAR(255),IN floor DOUBLE, IN ceiling DOUBLE)
    begin
        DECLARE partial_cnt INT;
        #
        # Depending on this being a zero data point, >= or >
        #
        IF floor = 0.0
        THEN
            SELECT count(*) FROM vertex_ent WHERE VEN1 >= floor AND VEN1 <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        ELSE
            SELECT count(*) FROM vertex_ent WHERE VEN1 > floor AND VEN1 <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        END IF;

        #
        # And insert
        #
        INSERT INTO VEN1_dist VALUES ( grp, ceiling, partial_cnt );
    end
DTR
delimiter ;


#
# VEN2
#
drop table if exists VEN2_dist;
create table VEN2_dist 
( 
    graph   VARCHAR(255),   # Graph
    val     DOUBLE,         # Break Point
    freq    INT             # Count     
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Populate the VEN2 dist table
#
drop procedure if exists populate_VEN2;
delimiter DTR
create procedure populate_VEN2(IN grp VARCHAR(255),IN floor DOUBLE, IN ceiling DOUBLE)
    begin
        DECLARE partial_cnt INT;
        #
        # Depending on this being a zero data point, >= or >
        #
        IF floor = 0.0
        THEN
            SELECT count(*) FROM vertex_ent WHERE VEN2 >= floor AND VEN2 <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        ELSE
            SELECT count(*) FROM vertex_ent WHERE VEN2 > floor AND VEN2 <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        END IF;

        #
        # And insert
        #
        INSERT INTO VEN2_dist VALUES ( grp, ceiling, partial_cnt );
    end
DTR
delimiter ;

#
# CE
#
drop table if exists CE_dist;
create table CE_dist 
( 
    graph   VARCHAR(255),   # Graph
    val     DOUBLE,         # Break Point
    freq    INT             # Count     
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# BC
#
drop table if exists BC_dist;
create table BC_dist 
( 
    graph   VARCHAR(255),   # Graph
    val     DOUBLE,         # Break Point
    freq    INT             # Count     
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Populate the VEN2 dist table
#

#
# Betweeness Centrality
#
drop procedure if exists populate_BC;
delimiter DTR
create procedure populate_BC(IN grp VARCHAR(255),IN floor DOUBLE, IN ceiling DOUBLE)
    begin
        DECLARE partial_cnt INT;
        #
        # Depending on this being a zero data point, >= or >
        #
        IF floor = 0.0
        THEN
            SELECT count(*) FROM vertex_ent WHERE BC >= floor AND BC <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        ELSE
            SELECT count(*) FROM vertex_ent WHERE BC > floor AND BC <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        END IF;

        #
        # And insert
        #
        INSERT INTO BC_dist VALUES ( grp, ceiling, partial_cnt );
    end
DTR
delimiter ;

#
# Cluster Entropy
#
drop procedure if exists populate_CE;
delimiter DTR
create procedure populate_CE(IN grp VARCHAR(255),IN floor DOUBLE, IN ceiling DOUBLE)
    begin
        DECLARE partial_cnt INT;
        #
        # Depending on this being a zero data point, >= or >
        #
        IF floor = 0.0
        THEN
            SELECT count(*) FROM vertex_ent WHERE CE >= floor AND CE <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        ELSE
            SELECT count(*) FROM vertex_ent WHERE CE > floor AND CE <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        END IF;

        #
        # And insert
        #
        INSERT INTO CE_dist VALUES ( grp, ceiling, partial_cnt );
    end
DTR
delimiter ;

#
# CVEN2
#
drop table if exists CVEN2_dist;
create table CVEN2_dist 
( 
    graph   VARCHAR(255),   # Graph
    val     DOUBLE,         # Break Point
    freq    INT             # Count     
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Populate the CVEN2 dist table
#
drop procedure if exists populate_CVEN2;
delimiter DTR
create procedure populate_CVEN2(IN grp VARCHAR(255),IN floor DOUBLE, IN ceiling DOUBLE)
    begin
        DECLARE partial_cnt INT;
        #
        # Depending on this being a zero data point, >= or >
        #
        IF floor = 0.0
        THEN
            SELECT count(*) FROM vertex_ent WHERE CVEN2 >= floor AND CVEN2 <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        ELSE
            SELECT count(*) FROM vertex_ent WHERE CVEN2 > floor AND CVEN2 <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        END IF;

        #
        # And insert
        #
        INSERT INTO CVEN2_dist VALUES ( grp, ceiling, partial_cnt );
    end
DTR
delimiter ;

#
# VEN3
#
drop table if exists VEN3_dist;
create table VEN3_dist 
( 
    graph   VARCHAR(255),   # Graph
    val     DOUBLE,         # Break Point
    freq    INT             # Count     
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Populate the VEN3 dist table
#
drop procedure if exists populate_VEN3;
delimiter DTR
create procedure populate_VEN3(IN grp VARCHAR(255),IN floor DOUBLE, IN ceiling DOUBLE)
    begin
        DECLARE partial_cnt INT;
        #
        # Depending on this being a zero data point, >= or >
        #
        IF floor = 0.0
        THEN
            SELECT count(*) FROM vertex_ent WHERE VEN3 >= floor AND VEN3 <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        ELSE
            SELECT count(*) FROM vertex_ent WHERE VEN3 > floor AND VEN3 <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        END IF;

        #
        # And insert
        #
        INSERT INTO VEN3_dist VALUES ( grp, ceiling, partial_cnt );
    end
DTR
delimiter ;

#
# Reset all distrib data
#
drop procedure if exists reset_stats;
delimiter DTR
create procedure reset_stats(IN grp VARCHAR(255))
    begin
        DELETE FROM VE1_dist WHERE graph=grp;
        DELETE FROM VE2_dist WHERE graph=grp;
        DELETE FROM VEN1_dist WHERE graph=grp;
        DELETE FROM VEN2_dist WHERE graph=grp;
        DELETE FROM VEN3_dist WHERE graph=grp;
        DELETE FROM CE_dist WHERE graph=grp;
        DELETE FROM BC_dist WHERE graph=grp;
        DELETE FROM CVEN2_dist WHERE graph=grp;
    end
DTR
delimiter ;

#
# And one a piece for individual stats
#
drop procedure if exists reset_VE1_stats;
delimiter DTR
create procedure reset_VE1_stats(IN grp VARCHAR(255))
    begin
        DELETE FROM VE1_dist WHERE graph=grp;
    end
DTR
delimiter ;

drop procedure if exists reset_VE2_stats;
delimiter DTR
create procedure reset_VE2_stats(IN grp VARCHAR(255))
    begin
        DELETE FROM VE2_dist WHERE graph=grp;
    end
DTR
delimiter ;
drop procedure if exists reset_VEN1_stats;
delimiter DTR
create procedure reset_VEN1_stats(IN grp VARCHAR(255))
    begin
        DELETE FROM VEN1_dist WHERE graph=grp;
    end
DTR
delimiter ;
drop procedure if exists reset_VEN2_stats;
delimiter DTR
create procedure reset_VEN2_stats(IN grp VARCHAR(255))
    begin
        DELETE FROM VEN2_dist WHERE graph=grp;
    end
DTR
delimiter ;
drop procedure if exists reset_VEN3_stats;
delimiter DTR
create procedure reset_VEN3_stats(IN grp VARCHAR(255))
    begin
        DELETE FROM VEN3_dist WHERE graph=grp;
    end
DTR
delimiter ;

drop procedure if exists reset_CE_stats;
delimiter DTR
create procedure reset_CE_stats(IN grp VARCHAR(255))
    begin
        DELETE FROM CE_dist WHERE graph=grp;
    end
DTR
delimiter ;

drop procedure if exists reset_BC_stats;
delimiter DTR
create procedure reset_BC_stats(IN grp VARCHAR(255))
    begin
        DELETE FROM BC_dist WHERE graph=grp;
    end
DTR
delimiter ;

drop procedure if exists reset_CVEN2_stats;
delimiter DTR
create procedure reset_CVEN2_stats(IN grp VARCHAR(255))
    begin
        DELETE FROM CVEN2_dist WHERE graph=grp;
    end
DTR
delimiter ;

#
# Get all VE bounds
#
drop procedure if exists get_entropy_bounds;
delimiter DTR
create procedure get_entropy_bounds(IN grp VARCHAR(255))
    begin
        SELECT max(VE1) as MaxVE1, min(VE1) as MinVE1,
                max(VE2) as MaxVE2, min(VE2) as MinVE2,
                max(VEN1) as MaxVEN1, min(VEN1) as MinVEN1,
                max(VEN2) as MaxVEN2, min(VEN2) as MinVEN2,
                max(VEN3) as MaxVEN3, min(VEN3) as MinVEN3,
                max(CE) as MaxCE, min(CE) as MinCE,
                max(BC) as MaxBC, min(BC) as MinBC,
                max(CVEN2) as MaxCVEN2, min(CVEN2) as MinCVEN2
            FROM vertex_ent WHERE graph=grp;
    end
DTR
delimiter ;

#
# Get all VE bounds for whole graphs
#
drop procedure if exists get_all_entropy_bounds;
delimiter DTR
create procedure get_all_entropy_bounds()
    begin
        SELECT max(VE1) as MaxVE1, min(VE1) as MinVE1,
                max(VE2) as MaxVE2, min(VE2) as MinVE2,
                max(VEN1) as MaxVEN1, min(VEN1) as MinVEN1,
                max(VEN2) as MaxVEN2, min(VEN2) as MinVEN2,
                max(VEN3) as MaxVEN3, min(VEN3) as MinVEN3,
                max(CE) as MaxCE, min(CE) as MinCE,
                max(BC) as MaxBC, min(BC) as MinBC,
                max(CVEN2) as MaxCVEN2, min(CVEN2) as MinCVEN2
            FROM vertex_ent;
    end
DTR
delimiter ;

#
# Get all VE bounds for whole graphs
#
drop procedure if exists get_all_incident_bounds;
delimiter DTR
create procedure get_all_incident_bounds()
    begin
        SELECT max(VE1) as MaxVE1, min(VE1) as MinVE1,
                max(VE2) as MaxVE2, min(VE2) as MinVE2,
                max(VEN1) as MaxVEN1, min(VEN1) as MinVEN1,
                max(VEN2) as MaxVEN2, min(VEN2) as MinVEN2,
                max(VEN3) as MaxVEN3, min(VEN3) as MinVEN3,
                max(CE) as MaxCE, min(CE) as MinCE,
                max(BC) as MaxBC, min(BC) as MinBC,
                max(CVEN2) as MaxCVEN2, min(CVEN2) as MinCVEN2
            FROM incidents_by_node;
    end
DTR
delimiter ;

drop procedure if exists get_all_event_bounds;
delimiter DTR
create procedure get_all_event_bounds()
    begin
        SELECT max(VE1) as MaxVE1, min(VE1) as MinVE1,
                max(VE2) as MaxVE2, min(VE2) as MinVE2,
                max(VEN1) as MaxVEN1, min(VEN1) as MinVEN1,
                max(VEN2) as MaxVEN2, min(VEN2) as MinVEN2,
                max(VEN3) as MaxVEN3, min(VEN3) as MinVEN3,
                max(CE) as MaxCE, min(CE) as MinCE,
                max(BC) as MaxBC, min(BC) as MinBC,
                max(CVEN2) as MaxCVEN2, min(CVEN2) as MinCVEN2
            FROM events_by_node;
    end
DTR
delimiter ;

##################################################################
#
# Events portion
#
##################################################################

#
# Core events table, used to accumulate events data
#
drop table if exists events;
create table events
(
    signature   VARBINARY( 767 ), # Unique hash key
    count       INT,            # How many times has this occurred
    node        VARCHAR( 255 ), # The node name
    VE1         DOUBLE,         # Entropy value VE1
    VE2         DOUBLE,         # Entropy value VE2
    VEN1        DOUBLE,         # Entropy value VEN1
    VEN2        DOUBLE,         # Entropy value VEN2
    VEN3        DOUBLE,         # Entropy value VEN3
    CE          DOUBLE,         # Entropy value VEN2
    CVEN2       DOUBLE,         # Entropy value CVEN2
    BC          DOUBLE,         # Entropy value CVEN2
    description TEXT,           # Description of the event

    PRIMARY KEY( signature )
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# And a pivot to roll up the data by node
#
drop table if exists events_by_node;
create table events_by_node
(
    node        VARCHAR( 255 ), # The node name
    count       INT,            # How many times has this occurred
    VE1         DOUBLE,         # Entropy value VE1
    VE2         DOUBLE,         # Entropy value VE2
    VEN1        DOUBLE,         # Entropy value VEN1
    VEN2        DOUBLE,         # Entropy value VEN2
    VEN3        DOUBLE,         # Entropy value VEN3
    CE          DOUBLE,         # Entropy value VEN2
    CVEN2       DOUBLE,         # Entropy value CVEN2
    BC          DOUBLE,         # Entropy value CVEN2
    degree      INT,            # Degree of node
    cluster     DOUBLE,         # Clustering coefficient of node

    PRIMARY KEY( node )
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Add an event
#
DROP PROCEDURE IF EXISTs new_event;
delimiter DTR
CREATE PROCEDURE new_event(IN sig VARCHAR(767),IN host VARCHAR(255),IN dsc TEXT)
    BEGIN
        DECLARE ent1 DOUBLE;     # A value for the entropy
        DECLARE ent2 DOUBLE;     # A value for the entropy
        DECLARE ent3 DOUBLE;     # A value for the entropy
        DECLARE ent4 DOUBLE;     # A value for the entropy
        DECLARE ent5 DOUBLE;     # A value for the entropy
        DECLARE ent6 DOUBLE;     # A value for the entropy
        DECLARE ent7 DOUBLE;     # A value for the entropy
        DECLARE ent8 DOUBLE;     # A value for the entropy
        DECLARE dgr  INT;     # A value for the entropy
        DECLARE clstr DOUBLE;     # A value for the clustering coefficient

        #
        # Grab the entropies of the node
        #
        SELECT VE1 FROM vertex_ent WHERE node=host INTO ent1;
        SELECT VE2 FROM vertex_ent WHERE node=host INTO ent2;
        SELECT VEN1 FROM vertex_ent WHERE node=host INTO ent3;
        SELECT VEN2 FROM vertex_ent WHERE node=host INTO ent4;
        SELECT VEN3 FROM vertex_ent WHERE node=host INTO ent5;
        SELECT CE FROM vertex_ent WHERE node=host INTO ent6;
        SELECT CVEN2 FROM vertex_ent WHERE node=host INTO ent7;
        SELECT BC FROM vertex_ent WHERE node=host INTO ent8;
        SELECT degree FROM vertex_ent WHERE node=host INTO dgr;
        SELECT cluster FROM vertex_ent WHERE node=host INTO clstr;

        #
        # Now add the event if valid
        #
        IF ent1 IS NOT NULL THEN
            #
            # Insert the event
            #
            INSERT INTO events VALUES ( sig, 1, host, ent1, ent2, ent3, ent4, ent5, ent6, ent7, ent8,  dsc )
                ON DUPLICATE KEY UPDATE count=count+1;

            #
            # And duplicate into the node roll up
            #
            INSERT INTO events_by_node VALUES ( host, 1,  ent1, ent2, ent3, ent4, ent5, ent6, ent7,  ent8, dgr,clstr )
                ON DUPLICATE KEY UPDATE count=count+1;
        END IF;
    END
DTR
delimiter ;

#---------------------------------------------
# Now distribution tables for the nodes stuff
#---------------------------------------------

#
# Event Count Distribution
#
drop table if exists event_count_by_node_distribution;
create table event_count_by_node_distribution
( 
    count       DOUBLE,         # Break Point
    freq        INT             # Count of nodes to this count
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Reset all event distrib data
#
drop procedure if exists reset_event_stats;
delimiter DTR
create procedure reset_event_stats()
    begin
        DELETE FROM event_count_by_node_distribution;
    end
DTR
delimiter ;

#
# Get node event count bounds
#
drop procedure if exists get_count_bounds;
delimiter DTR
create procedure get_count_bounds()
    begin
        SELECT max(count) as MaxCount, min(count) as MinCount
            FROM events_by_node;
    end
DTR
delimiter ;

#
# Populate the event_count dist table
#
drop procedure if exists populate_event_node_count_dist;
delimiter DTR
create procedure populate_event_node_count_dist(IN floor INT, IN ceiling INT)
    begin
        DECLARE partial_cnt INT;
        #
        # Depending on this being a zero data point, >= or >
        #
        IF floor = 1
        THEN
            SELECT sum(count) FROM events_by_node WHERE count >= floor AND count <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        ELSE
            SELECT sum(count) FROM events_by_node WHERE count > floor AND count <= ceiling INTO partial_cnt;
            IF partial_cnt IS NULL
            THEN
                SET partial_cnt=0;
            END IF;
        END IF;
    
        #
        # And insert
        #
        INSERT INTO event_count_by_node_distribution VALUES ( ceiling, partial_cnt );
    end
DTR
delimiter ;

#-----------------------------------------------------------------
# And event to entropy distributions
#-----------------------------------------------------------------

#
# Grab all of the statistics
#
drop table if exists event_entropy_dist;
create table event_entropy_dist 
( 
    val         DOUBLE,         # Break Point
    freq        INT,            # Count of all events in this break point
    percent     FLOAT,          # As a percentage
    cumulative  FLOAT,          # sum of percent
    type        VARCHAR(16)     # Type, "VE1","VE2","VEN1","VEN2", "CE", "CVEN2" "VEN3" "BC"
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Grab all of the event stats
#
drop table if exists event_degree_dist;
create table event_degree_dist 
( 
    val         INT,            # Break Point
    freq        INT,            # Count of all incidents in this break point
    percent     FLOAT,           # Normalized version
    cumulative  FLOAT          # sum of percent
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Grab all of the event stats
#
drop table if exists event_cluster_dist;
create table event_cluster_dist 
( 
    val         DOUBLE,            # Break Point
    freq        INT,            # Count of all incidents in this break point
    percent     FLOAT           # Normalized version
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;


#
# Reset entropy event distrib data
#
drop procedure if exists reset_event_ent_stats;
delimiter DTR
create procedure reset_event_ent_stats()
    begin
        DELETE FROM event_entropy_dist;
        DELETE FROM event_degree_dist;
    end
DTR
delimiter ;

#
# Populate the event entropy table
#
drop procedure if exists populate_event_entropy;
delimiter DTR
create procedure populate_event_entropy(IN tp VARCHAR(16),IN floor DOUBLE, IN ceiling DOUBLE)
    begin
        DECLARE total_events    INT;
        DECLARE partial_cnt     INT;
        DECLARE cum             FLOAT;

        #
        # Get the total
        #
        SELECT SUM(count) FROM events INTO total_events;
        SELECT SUM(percent) FROM event_entropy_dist WHERE type = tp INTO cum;
        IF cum IS NULL
        THEN
            SET cum=0.0;
        END IF;

        #
        # One SP to rule them all so choose which
        #
        IF tp = "VE1"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM events_by_node WHERE VE1 >= floor AND VE1 <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM events_by_node WHERE VE1 > floor AND VE1 <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            END IF;
            #
            # AND INSERT
            #
            SET cum=cum+(partial_cnt/total_events);
            INSERT INTO event_entropy_dist VALUES ( ceiling, partial_cnt, (partial_cnt/total_events),cum,  tp );
        END IF;
        IF tp = "VE2"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM events_by_node WHERE VE2 >= floor AND VE2 <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM events_by_node WHERE VE2 > floor AND VE2 <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            END IF;
            #
            # AND INSERT
            #
            SET cum=cum+(partial_cnt/total_events);
            INSERT INTO event_entropy_dist VALUES ( ceiling, partial_cnt, (partial_cnt/total_events), cum, tp );
        END IF;
        IF tp = "VEN1"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM events_by_node WHERE VEN1 >= floor AND VEN1 <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM events_by_node WHERE VEN1 > floor AND VEN1 <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            END IF;
            #
            # AND INSERT
            #
            SET cum=cum+(partial_cnt/total_events);
            INSERT INTO event_entropy_dist VALUES ( ceiling, partial_cnt, (partial_cnt/total_events), cum, tp );
        END IF;
        IF tp = "VEN2"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM events_by_node WHERE VEN2 >= floor AND VEN2 <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM events_by_node WHERE VEN2 > floor AND VEN2 <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            END IF;
            #
            # AND INSERT
            #
            SET cum=cum+(partial_cnt/total_events);
            INSERT INTO event_entropy_dist VALUES ( ceiling, partial_cnt, (partial_cnt/total_events), cum, tp );
        END IF;
        IF tp = "VEN3"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM events_by_node WHERE VEN3 >= floor AND VEN3 <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM events_by_node WHERE VEN3 > floor AND VEN3 <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            END IF;
            #
            # AND INSERT
            #
            SET cum=cum+(partial_cnt/total_events);
            INSERT INTO event_entropy_dist VALUES ( ceiling, partial_cnt, (partial_cnt/total_events), cum, tp );
        END IF;
        IF tp = "CE"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM events_by_node WHERE CE >= floor AND CE <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM events_by_node WHERE CE > floor AND CE <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            END IF;
            #
            # AND INSERT
            #
            SET cum=cum+(partial_cnt/total_events);
            INSERT INTO event_entropy_dist VALUES ( ceiling, partial_cnt, (partial_cnt/total_events), cum, tp );
        END IF;
        IF tp = "CVEN2"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM events_by_node WHERE CVEN2 >= floor AND CVEN2 <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM events_by_node WHERE CVEN2 > floor AND CVEN2 <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            END IF;
            #
            # AND INSERT
            #
            SET cum=cum+(partial_cnt/total_events);
            INSERT INTO event_entropy_dist VALUES ( ceiling, partial_cnt, (partial_cnt/total_events), cum, tp );
        END IF;
        IF tp = "BC"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM events_by_node WHERE BC >= floor AND BC <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM events_by_node WHERE BC > floor AND BC <= ceiling INTO partial_cnt;
                IF partial_cnt IS NULL
                THEN
                    SET partial_cnt=0;
                END IF;
            END IF;
            #
            # AND INSERT
            #
            SET cum=cum+(partial_cnt/total_events);
            INSERT INTO event_entropy_dist VALUES ( ceiling, partial_cnt, (partial_cnt/total_events), cum, tp );
        END IF;
    end
DTR
delimiter ;

#
# Populate the event degree table
#
drop procedure if exists populate_event_degree;
delimiter DTR
create procedure populate_event_degree(IN floor INT, IN ceiling INT)
    begin
        DECLARE total_events   INT;
        DECLARE cnt               INT;
        DECLARE cum             FLOAT;

        #
        # Get the total
        #
        SELECT SUM(count) FROM events_by_node INTO total_events;
        SELECT SUM(percent) FROM event_degree_dist INTO cum;
        IF cum IS NULL
        THEN
            SET cum=0.0;
        END IF;

        #
        # Depending on this being a zero data point, >= or >
        #
        IF floor = 0
        THEN
            SELECT sum(count) FROM events_by_node WHERE degree >= floor AND degree <= ceiling INTO cnt;
            IF cnt IS NULL
            THEN
                SET cnt=0;
            END IF;
        ELSE
            SELECT sum(count) FROM events_by_node WHERE degree > floor AND degree <= ceiling INTO cnt;
            IF cnt IS NULL
            THEN
                SET cnt=0;
            END IF;
        END IF;
        #
        # Do insert
        #
        SET cum=cum+(cnt/total_events);
        INSERT INTO event_degree_dist VALUES ( ceiling, cnt, (cnt/total_events),cum);
    end
DTR
delimiter ;

#
# Populate the event cluster table
#
drop procedure if exists populate_event_cluster;
delimiter DTR
create procedure populate_event_cluster(IN floor DOUBLE, IN ceiling DOUBLE)
    begin
        DECLARE total_events   INT;
        DECLARE cnt               INT;

        #
        # Get the total
        #
        SELECT SUM(count) FROM events_by_node INTO total_events;

        #
        # Depending on this being a zero data point, >= or >
        #
        IF floor = 0
        THEN
            SELECT sum(count) FROM events_by_node WHERE cluster >= floor AND cluster <= ceiling INTO cnt;
            IF cnt IS NULL
            THEN
                SET cnt=0;
            END IF;
        ELSE
            SELECT sum(count) FROM events_by_node WHERE cluster > floor AND cluster <= ceiling INTO cnt;
            IF cnt IS NULL
            THEN
                SET cnt=0;
            END IF;
        END IF;
        #
        # Do insert
        #
        INSERT INTO event_cluster_dist VALUES ( ceiling, cnt, (cnt/total_events));
    end
DTR
delimiter ;

##################################################################
#
# Incidents portion
#
##################################################################

#
# Core incidents table, used to accumulate events data
#
drop table if exists incidents;
create table incidents
(
    signature   VARBINARY( 767 ), # Unique hash key
    count       INT,            # How many times has this occurred
    node        VARCHAR( 255 ), # The node name
    ticket      TEXT,           # Ticket numner
    type        TEXT,           # Type of incident
    VE1         DOUBLE,         # Entropy value VE1
    VE2         DOUBLE,         # Entropy value VE2
    VEN1        DOUBLE,         # Entropy value VEN1
    VEN2        DOUBLE,         # Entropy value VEN2
    VEN3        DOUBLE,         # Entropy value VEN3
    CE          DOUBLE,         # Entropy value VEN2
    CVEN2       DOUBLE,         # Entropy value CVEN2
    BC          DOUBLE,         # Entropy value CVEN2
    description TEXT,           # Description of the event

    PRIMARY KEY( signature )
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# And a pivot to roll up the data by node
#
drop table if exists incidents_by_node;
create table incidents_by_node
(
    node        VARCHAR( 255 ), # The node name
    count       INT,            # How many times has this occurred
    ticket      TEXT,           # Ticket numner
    VE1         DOUBLE,         # Entropy value VE1
    VE2         DOUBLE,         # Entropy value VE2
    VEN1        DOUBLE,         # Entropy value VEN1
    VEN2        DOUBLE,         # Entropy value VEN2
    VEN3        DOUBLE,         # Entropy value VEN3
    CE          DOUBLE,         # Entropy value VEN2
    CVEN2       DOUBLE,         # Entropy value CVEN2
    BC          DOUBLE,         # Entropy value CVEN2
    degree      INT,            # Degree of node
    cluster     DOUBLE,         # Cluster Coefficient of node

    PRIMARY KEY( node )
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Add an event
#
DROP PROCEDURE IF EXISTs new_incident;
delimiter DTR
CREATE PROCEDURE new_incident(IN sig VARCHAR(767),IN host VARCHAR(255),IN dsc TEXT,IN tkt TEXT, IN tp TEXT)
    BEGIN
        DECLARE ent1 DOUBLE;     # A value for the entropy
        DECLARE ent2 DOUBLE;     # A value for the entropy
        DECLARE ent3 DOUBLE;     # A value for the entropy
        DECLARE ent4 DOUBLE;     # A value for the entropy
        DECLARE ent5 DOUBLE;     # A value for the entropy
        DECLARE ent6 DOUBLE;     # A value for the entropy
        DECLARE ent7 DOUBLE;     # A value for the entropy
        DECLARE ent8 DOUBLE;     # A value for the entropy
        DECLARE dgr  INT;        # Degree of node
        DECLARE clstr DOUBLE;        # Cluster of node

        #
        # Grab the entropies of the node
        #
        SELECT VE1 FROM vertex_ent WHERE node=host INTO ent1;
        SELECT VE2 FROM vertex_ent WHERE node=host INTO ent2;
        SELECT VEN1 FROM vertex_ent WHERE node=host INTO ent3;
        SELECT VEN2 FROM vertex_ent WHERE node=host INTO ent4;
        SELECT VEN3 FROM vertex_ent WHERE node=host INTO ent5;
        SELECT CE FROM vertex_ent WHERE node=host INTO ent6;
        SELECT CVEN2 FROM vertex_ent WHERE node=host INTO ent7;
        SELECT BC FROM vertex_ent WHERE node=host INTO ent8;
        SELECT cluster FROM vertex_ent WHERE node=host INTO clstr;
        SELECT degree FROM vertex_ent WHERE node=host INTO dgr;

        #
        # Now add the incident valid
        #
        IF ent1 IS NOT NULL THEN
            #
            # Insert the event
            #
            INSERT INTO incidents VALUES ( sig, 1, host, tkt, tp, ent1, ent2, ent3, ent4, ent5, ent6, ent7, ent8, dsc )
                ON DUPLICATE KEY UPDATE count=count+1;

            #
            # And duplicate into the node roll up
            #
            INSERT INTO incidents_by_node VALUES ( host, 1,  tkt, ent1, ent2, ent3, ent4, ent5, ent6, ent7, ent8, dgr, clstr )
                ON DUPLICATE KEY UPDATE count=count+1;
        END IF;
    END
DTR
delimiter ;


#-----------------------------------------------------------------
# And incident to entropy distributions
#-----------------------------------------------------------------

#
# Grab all of the statistics
#
drop table if exists incident_entropy_dist;
create table incident_entropy_dist 
( 
    val         DOUBLE,         # Break Point
    freq        INT,            # Count of all events in this break point
    percent     FLOAT,          # Normalized version
    cumulative  FLOAT,          # Cumulative percent
    type        VARCHAR(16)     # Type, "VE1","VE2","VEN1","VEN2", "CE", "CVEN2" "VEN3" "BC"
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Grab all of the incident stats
#
drop table if exists incident_degree_dist;
create table incident_degree_dist 
( 
    val         INT,            # Break Point
    freq        INT,            # Count of all incidents in this break point
    percent     FLOAT,           # Normalized version
    cumulative  FLOAT          # sum of percent
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Incident cluster dist
#
drop table if exists incident_cluster_dist;
create table incident_cluster_dist 
( 
    val         DOUBLE,            # Break Point
    freq        INT,            # Count of all incidents in this break point
    percent     FLOAT           # Normalized version
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;


#
# Reset entropy event distrib data
#
drop procedure if exists reset_incident_ent_stats;
delimiter DTR
create procedure reset_incident_ent_stats()
    begin
        DELETE FROM incident_entropy_dist;
        DELETE FROM incident_degree_dist;
    end
DTR
delimiter ;

#
# Prune collateral nodes for a ticket (ie less causal nodes)
#
drop procedure if exists prune_collateral_nodes;
delimiter DTR
create procedure prune_collateral_nodes(IN tkt TEXT)
    begin
        DECLARE maxVE1 DOUBLE;     # A value for the entropy
        DECLARE maxVE2 DOUBLE;     # A value for the entropy
        DECLARE maxVEN1 DOUBLE;     # A value for the entropy
        DECLARE maxVEN2 DOUBLE;     # A value for the entropy
        DECLARE maxVEN3 DOUBLE;     # A value for the entropy
        DECLARE maxCE DOUBLE;     # A value for the entropy
        DECLARE maxCVEN2 DOUBLE;     # A value for the entropy
        DECLARE maxBC DOUBLE;     # A value for the entropy

        # 
        #  Get the maximums
        # 
        SELECT max(VE1),max(VE2),max(VEN1),max(VEN2),max(CE),max(CVEN2),max(VEN3),max(BC) FROM incidents_by_node WHERE ticket=tkt INTO maxVE1,maxVE2,maxVEN1,maxVEN2,maxCE,maxCVEN2,maxVEN3,maxBC;

        #
        # And prune
        #
        DELETE FROM incidents WHERE ticket=tkt AND 
            VE1 < maxVE1 AND VE2 < maxVE2 AND
                VEN1 < maxVEN1 AND VEN2 < maxVEN2
                AND CVEN2 < maxCVEN2 
                AND VEN3 < maxVEN3 
                AND BC < maxBC 
                AND CE < maxCE;

        DELETE FROM incidents_by_node WHERE ticket=tkt AND 
            VE1 < maxVE1 AND VE2 < maxVE2 AND
                VEN1 < maxVEN1 AND VEN2 < maxVEN2
                AND CVEN2 < maxCVEN2 
                AND VEN3 < maxVEN3 
                AND BC < maxBC 
                AND CE < maxCE;
    end
DTR
delimiter ;

#
# Populate the incident entropy table
#
drop procedure if exists populate_incident_entropy;
delimiter DTR
create procedure populate_incident_entropy(IN tp VARCHAR(16),IN floor DOUBLE, IN ceiling DOUBLE)
    begin
        DECLARE total_incidents   INT;
        DECLARE cnt               INT;
        DECLARE cum               FLOAT;

        #
        # Get the total
        #
        SELECT SUM(count) FROM incidents_by_node INTO total_incidents;
        SELECT SUM(percent) FROM incident_entropy_dist WHERE type = tp INTO cum;
        IF cum IS NULL
        THEN
            SET cum=0.0;
        END IF;
        #
        # One SP to rule them all so choose which
        #
        IF tp = "VE1"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM incidents_by_node WHERE VE1 >= floor AND VE1 <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM incidents_by_node WHERE VE1 > floor AND VE1 <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            END IF;
            #
            # Do insert
            #
            SET cum=cum+(cnt/total_incidents);
            INSERT INTO incident_entropy_dist VALUES ( ceiling, cnt, (cnt/total_incidents),cum,tp);
        END IF;
        IF tp = "VE2"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM incidents_by_node WHERE VE2 >= floor AND VE2 <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM incidents_by_node WHERE VE2 > floor AND VE2 <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            END IF;
            #
            # Do insert
            #
            SET cum=cum+(cnt/total_incidents);
            INSERT INTO incident_entropy_dist VALUES ( ceiling, cnt, cnt/total_incidents,cum,tp);
        END IF;
        IF tp = "VEN1"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM incidents_by_node WHERE VEN1 >= floor AND VEN1 <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM incidents_by_node WHERE VEN1 > floor AND VEN1 <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            END IF;
            #
            # Do insert
            #
            SET cum=cum+(cnt/total_incidents);
            INSERT INTO incident_entropy_dist VALUES ( ceiling, cnt, cnt/total_incidents,cum,tp);
        END IF;
        IF tp = "VEN2"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM incidents_by_node WHERE VEN2 >= floor AND VEN2 <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM incidents_by_node WHERE VEN2 > floor AND VEN2 <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            END IF;
            #
            # Do insert
            #
            SET cum=cum+(cnt/total_incidents);
            INSERT INTO incident_entropy_dist VALUES ( ceiling, cnt, cnt/total_incidents,cum,tp);
        END IF;
        IF tp = "VEN3"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM incidents_by_node WHERE VEN3 >= floor AND VEN3 <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM incidents_by_node WHERE VEN3 > floor AND VEN3 <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            END IF;
            #
            # Do insert
            #
            SET cum=cum+(cnt/total_incidents);
            INSERT INTO incident_entropy_dist VALUES ( ceiling, cnt, cnt/total_incidents,cum,tp);
        END IF;
        IF tp = "CE"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM incidents_by_node WHERE CE >= floor AND CE <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM incidents_by_node WHERE CE > floor AND CE <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            END IF;
            #
            # Do insert
            #
            SET cum=cum+(cnt/total_incidents);
            INSERT INTO incident_entropy_dist VALUES ( ceiling, cnt, cnt/total_incidents,cum,tp);
        END IF;
        IF tp = "CVEN2"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM incidents_by_node WHERE CVEN2 >= floor AND CVEN2 <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM incidents_by_node WHERE CVEN2 > floor AND CVEN2 <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            END IF;
            #
            # Do insert
            #
            SET cum=cum+(cnt/total_incidents);
            INSERT INTO incident_entropy_dist VALUES ( ceiling, cnt, cnt/total_incidents,cum,tp);
        END IF;
        IF tp = "BC"
        THEN
            #
            # Depending on this being a zero data point, >= or >
            #
            IF floor = 0.0
            THEN
                SELECT sum(count) FROM incidents_by_node WHERE BC >= floor AND BC <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            ELSE
                SELECT sum(count) FROM incidents_by_node WHERE BC > floor AND BC <= ceiling INTO cnt;
                IF cnt IS NULL
                THEN
                    SET cnt=0;
                END IF;
            END IF;
            #
            # Do insert
            #
            SET cum=cum+(cnt/total_incidents);
            INSERT INTO incident_entropy_dist VALUES ( ceiling, cnt, cnt/total_incidents,cum,tp);
        END IF;
    end
DTR
delimiter ;


#
# Populate the incident degree table
#
drop procedure if exists populate_incident_degree;
delimiter DTR
create procedure populate_incident_degree(IN floor INT, IN ceiling INT)
    begin
        DECLARE total_incidents   INT;
        DECLARE cnt               INT;
        DECLARE cum             FLOAT;

        #
        # Get the total
        #
        SELECT SUM(count) FROM incidents_by_node INTO total_incidents;
        SELECT SUM(percent) FROM incident_degree_dist INTO cum;
        IF cum IS NULL
        THEN
            SET cum=0.0;
        END IF;

        #
        # Depending on this being a zero data point, >= or >
        #
        IF floor = 0
        THEN
            SELECT sum(count) FROM incidents_by_node WHERE degree >= floor AND degree <= ceiling INTO cnt;
            IF cnt IS NULL
            THEN
                SET cnt=0;
            END IF;
        ELSE
            SELECT sum(count) FROM incidents_by_node WHERE degree > floor AND degree <= ceiling INTO cnt;
            IF cnt IS NULL
            THEN
                SET cnt=0;
            END IF;
        END IF;
        #
        # Do insert
        #
        SET cum=cum+(cnt/total_incidents);
        INSERT INTO incident_degree_dist VALUES ( ceiling, cnt, (cnt/total_incidents),cum);
    end
DTR
delimiter ;

#
# Populate the incident degree table
#
drop procedure if exists populate_incident_cluster;
delimiter DTR
create procedure populate_incident_cluster(IN floor DOUBLE, IN ceiling DOUBLE)
    begin
        DECLARE total_incidents   INT;
        DECLARE cnt               INT;

        #
        # Get the total
        #
        SELECT SUM(count) FROM incidents_by_node INTO total_incidents;

        #
        # Depending on this being a zero data point, >= or >
        #
        IF floor = 0
        THEN
            SELECT sum(count) FROM incidents_by_node WHERE cluster >= floor AND cluster <= ceiling INTO cnt;
            IF cnt IS NULL
            THEN
                SET cnt=0;
            END IF;
        ELSE
            SELECT sum(count) FROM incidents_by_node WHERE cluster > floor AND cluster <= ceiling INTO cnt;
            IF cnt IS NULL
            THEN
                SET cnt=0;
            END IF;
        END IF;
        #
        # Do insert
        #
        INSERT INTO incident_cluster_dist VALUES ( ceiling, cnt, (cnt/total_incidents));
    end
DTR
delimiter ;

#
# Simulation
#
drop table if exists delta;
create table delta
(
    step        INT,        #   Timestep
    delta       DOUBLE,     #   delta
    closure     DOUBLE      #   closure
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# add delta
#
drop procedure if exists add_delta;
delimiter DTR
create procedure add_delta(IN st INT, IN dl DOUBLE,IN cl DOUBLE)
    begin
        #
        # Simple insert
        #
        INSERT INTO delta VALUES ( st, dl, cl );
    end
DTR
delimiter ;

##################################################################
#
# Section for calc scoping
#
##################################################################

drop table if exists node_scope;
create table node_scope
(
    node        VARCHAR(255),   # The node name
    count       INT,        #   Timestep
    UNIQUE INDEX(node)
)
CHARACTER SET "UTF8"
ENGINE = InnoDB
;

#
# Add a node to node scope
#
drop procedure if exists add_node;
delimiter DTR
create procedure add_node(IN nd VARCHAR(255))
    begin
        INSERT INTO node_scope VALUES ( nd, 1 )
        ON DUPLICATE KEY UPDATE count=count + 1;
    end
DTR
delimiter ;

#
# Get scope
#
drop procedure if exists fetch_scope;
delimiter DTR
create procedure fetch_scope()
    begin
        #
        # Nodes, then edges
        #
        SELECT node,count FROM node_scope;
    end
DTR
delimiter ;
