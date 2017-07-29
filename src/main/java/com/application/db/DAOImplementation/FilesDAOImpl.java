package com.application.db.DAOImplementation;

import com.application.db.DatabaseUtil;
import com.application.db.TableNames;
import jdk.nashorn.internal.ir.ReturnNode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.application.db.TableNames.FILES_TABLE;

public class FilesDAOImpl {

    public static boolean isTableCreated() {
        return DatabaseUtil.isTableCreated(FILES_TABLE);
    }


    public static void createTable() {
        if (!isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                String sql = "CREATE TABLE " + FILES_TABLE + " (" +
                        "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                        "FILE_TYPE VARCHAR(10), " +  // MEHTODDEFN or CALLTRACE
                        "FILE_PATH VARCHAR(200) " +
                        ")";
                ps.execute(sql);
                System.out.println("** Creating table " + TableNames.FILES_TABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void insert(String fileType, String filePath) {
        if (!isTableCreated()) {
            System.out.println("Since table is not created, we are creating it now.");
            createTable();
        }
        String sql = null;
        try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
            sql = "INSERT INTO " + TableNames.FILES_TABLE + " (FILE_TYPE, FILE_PATH) VALUES (" +
                    "'" + fileType + "', " +
                    "'" + filePath + "'" +
                    ")";

            ps.execute(sql);
        } catch (SQLException e) {
            System.out.println("Exception caused by: " + sql);
            e.printStackTrace();
        }
    }

    public static void dropTable() {
        if (isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                String sql= "Drop table " + TableNames.FILES_TABLE;
                System.out.println(">> Dropping table " + TableNames.FILES_TABLE);

                ps.execute(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    static Connection conn;
    static Statement ps;
    static String sql;
    public static ResultSet selectWhere(String where) {
        if (!isTableCreated()) {
            createTable();
        }

        try  {
            conn = DatabaseUtil.getConnection();
            ps = conn.createStatement();
            sql = "SELECT * FROM " + FILES_TABLE + " WHERE " + where;
            ResultSet resultSet = ps.executeQuery(sql);
            return resultSet;
        } catch (SQLException e) {
            System.out.println("Line that threw error: " + sql);
            e.printStackTrace();
        }

        throw new IllegalStateException("Table does not exist. Hence cannot fetch any rows from it.");

    }

    public static void updateWhere(String columnName, String columnValue, String where) {
        if (!isTableCreated()) {
            createTable();
        } else {
            try {
                conn = DatabaseUtil.getConnection();
                ps = conn.createStatement();
                sql = "UPDATE " + FILES_TABLE +
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
