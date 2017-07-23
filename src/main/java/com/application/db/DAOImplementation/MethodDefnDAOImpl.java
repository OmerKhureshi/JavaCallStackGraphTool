package com.application.db.DAOImplementation;

import com.application.db.DatabaseUtil;
import com.application.db.TableNames;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static com.application.db.TableNames.METHOD_DEFINITION_TABLE;

public class MethodDefnDAOImpl {
    // TODO: Create abstract classes for all these Impl classes. All common funtionality is implement by the default class. Rest of the methods are abstract.
    // public static boolean isTableCreated = false;

    public static boolean isTableCreated() {
        //        System.out.println("starting isTableCreated");
        // if (!isTableCreated) {// No need to call DatabaseUtil method every time. Save time this way.
            //            System.out.println("MethodDefnDAOImpl:isTableCreated: " + isTableCreated);
            // isTableCreated = DatabaseUtil.isTableCreated(METHOD_DEFINITION_TABLE);
            return DatabaseUtil.isTableCreated(METHOD_DEFINITION_TABLE);
            //            System.out.println("MethodDefnDAOImpl:isTableCreated: " + isTableCreated);
        // }
        //        System.out.println("ending isTableCreated");
        // return isTableCreated;
    }

    public static void createTable() {
        //        System.out.println("starting createTable");
        if (!isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                sql = "CREATE TABLE " + METHOD_DEFINITION_TABLE + " (" +
                        "id INTEGER NOT NULL PRIMARY KEY , " +
                        "package_name VARCHAR(200), " +
                        "method_name VARCHAR(50), " +
                        "parameter_types VARCHAR(200)" +
                        ")";
                ps.execute(sql);
                System.out.println("** Creating table " + TableNames.METHOD_DEFINITION_TABLE);
            } catch (SQLException e) {
                System.err.println("MethodDefnDAOImpl::createTable: SQL Exception on create table");
                e.printStackTrace();
            }
        }
        //        System.out.println("ending createTable");
    }

    public static void insert(List<String> vals) {
        if (!isTableCreated())
            createTable();

        int methodID = Integer.parseInt(vals.get(0));
        String packageName = vals.get(1);
        String methodName = vals.get(2);
        String arguments = vals.get(3);
        String sql = null;
        try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
            sql = "INSERT INTO " + METHOD_DEFINITION_TABLE + " VALUES(\n"+
                    methodID + ", '" +
                    packageName + "', '" +
                    methodName + "', '" +
                    arguments + "'" +
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
                String sql= "Drop table " + METHOD_DEFINITION_TABLE;
                System.out.println(">> Dropping table " + TableNames.METHOD_DEFINITION_TABLE);

                ps.execute(sql);
                ps.close();
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
        if (isTableCreated()) try {
            conn = DatabaseUtil.getConnection();
            ps = conn.createStatement();
            sql = "SELECT * FROM " + METHOD_DEFINITION_TABLE + " WHERE " + where;
            ResultSet resultSet = ps.executeQuery(sql);
            //                resultSet.next();
            //                System.out.println(resultSet.getInt("id"));
            return resultSet;
        } catch (SQLException e) {
            System.out.println("Line that threw error: " + sql);
            e.printStackTrace();
        }
        throw new IllegalStateException("Table does not exist. Hence cannot fetch any rows from it.");
    }
}
