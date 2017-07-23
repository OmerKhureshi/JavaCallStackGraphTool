package com.application.db.DAOImplementation;

import com.application.db.DatabaseUtil;
import com.application.db.TableNames;
import com.application.fxgraph.ElementHelpers.EdgeElement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.application.db.TableNames.EDGE_TABLE;

public class EdgeDAOImpl {
    // public static boolean isTableCreated = false;

    public static boolean isTableCreated() {
        //        System.out.println("starting isTableCreated");
        // if (!isTableCreated) {// No need to call DatabaseUtil method every time. Save time this way.
            //            System.out.println("ElementDAOImpl:isTableCreated: " + isTableCreated);
            // isTableCreated = DatabaseUtil.isTableCreated(EDGE_TABLE);
            //            System.out.println("ElementDAOImpl:isTableCreated: " + isTableCreated);
        // }
        //        System.out.println("ending isTableCreated");
        // return isTableCreated;
        return DatabaseUtil.isTableCreated(TableNames.EDGE_TABLE);
    }

    public static void createTable() {
        //        System.out.println("starting createTable");
        //        System.out.println("ElementDAOImpl:createTable: " + isTableCreated());
        if (!isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                String sql = "CREATE TABLE " + EDGE_TABLE + " (" +
                        "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                        "fk_source_element_id INTEGER, " +
                        "fk_target_element_id INTEGER, " +
                        "start_x FLOAT, " +
                        "start_y FLOAT, " +
                        "end_x FLOAT, " +
                        "end_y FLOAT, " +
                        "collapsed INTEGER" +
                        ")";
                ps.execute(sql);
                System.out.println("** Creating table " + TableNames.EDGE_TABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void insert(EdgeElement edge) {
        if (!isTableCreated())
            createTable();

        String sql = null;
        try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
            sql = "INSERT INTO " + EDGE_TABLE + " (" +
                    "fk_source_element_id, " +
                    "fk_target_element_id, " +
                    "start_x, " +
                    "start_y, " +
                    "end_x, " +
                    "end_y, " +
                    "collapsed) " +
                    " VALUES (" +
                    edge.getSourceElement().getElementId() + ", " +
                    edge.getTargetElement().getElementId() + ", " +
                    edge.getStartX() + ", " +
                    edge.getStartY() + ", " +
                    edge.getEndX() + ", " +
                    edge.getEndY() + ", " +
                    edge.getCollpased() + "" +
                    ")";

            ps.execute(sql);
        } catch (SQLException e) {
            System.out.println(" Exception caused by: " + sql);
            e.printStackTrace();
        }
    }

    public static void dropTable() {
        //        System.out.println("starting dropTable");
        if (isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                String sql= "Drop table " + EDGE_TABLE;
                System.out.println(">> Dropping table " + TableNames.EDGE_TABLE);

                ps.execute(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //        System.out.println("ending dropTable");
    }

    static Connection conn;
    static Statement ps;
    static String sql;
    public static ResultSet selectWhere(String where) {
        if (isTableCreated()) {
            try  {
                conn = DatabaseUtil.getConnection();
                ps = conn.createStatement();
                sql = "SELECT * FROM " + EDGE_TABLE + " WHERE " + where;
                //                System.out.println(">>> we got " + sql);
                ResultSet resultSet = ps.executeQuery(sql);
                // System.out.println("Where: " + sql);
                //                resultSet.next();
                //                System.out.println(resultSet.getInt("id"));
                return resultSet;
            } catch (SQLException e) {
                System.out.println("Line that threw error: " + sql);
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("No results for the select query. " + sql);
    }

    public static void updateWhere(String columnName, String columnValue, String where) {
        if (isTableCreated()) {
            try  {
                conn = DatabaseUtil.getConnection();
                ps = conn.createStatement();
                sql = "UPDATE " + TableNames.EDGE_TABLE +
                        " SET " + columnName + " = " + columnValue +
                        " WHERE " + where;
                //                System.out.println(">>> we got " + sql);
                ps.executeUpdate(sql);
                return;
                //                resultSet.next();
                //                System.out.println(resultSet.getInt("id"));
            } catch (SQLException e) {
                System.out.println("Line that threw error: " + sql);
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("Table does not exist. Hence cannot fetch any rows from it.");

    }
}
