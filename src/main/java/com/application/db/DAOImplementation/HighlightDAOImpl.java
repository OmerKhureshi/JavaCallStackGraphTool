package com.application.db.DAOImplementation;

import com.application.db.DatabaseUtil;
import com.application.db.TableNames;
import javafx.geometry.BoundingBox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HighlightDAOImpl {

    public static boolean isTableCreated() {
        return DatabaseUtil.isTableCreated(TableNames.HIGHLIGHT_ELEMENT);
    }

    public static void createTable() {
        if (!isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                String sql = "CREATE TABLE " + TableNames.HIGHLIGHT_ELEMENT + " (" +
                        "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                        "element_id INT, " +
                        "method_id INT, " +
                        "thread_id INT, " +
                        "highlight_type VARCHAR(6), " + // valid values may be "SINGLE" or "FuLL".
                        "start_x FLOAT, " +
                        "start_y FLOAT, " +
                        "width FLOAT, " +
                        "height FLOAT, " +
                        "color VARCHAR(10), " +
                        "collapsed INT" +
                        ")";
                ps.execute(sql);
                System.out.println("** Creating table " + TableNames.HIGHLIGHT_ELEMENT);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void dropTable() {
        if (isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                String sql= "Drop table " + TableNames.HIGHLIGHT_ELEMENT;
                System.out.println(">> Dropping table " + TableNames.HIGHLIGHT_ELEMENT);

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
        if (!isTableCreated())
            createTable();

        try  {
            conn = DatabaseUtil.getConnection();
            ps = conn.createStatement();
            sql = "SELECT * FROM " + TableNames.HIGHLIGHT_ELEMENT + " WHERE " + where;
            ResultSet resultSet = ps.executeQuery(sql);
            return resultSet;
        } catch (SQLException e) {
            System.out.println("Line that threw error: " + sql);
            e.printStackTrace();
        }
        throw new IllegalStateException("No results for the select query. " + sql);
    }

    public static void updateWhere(String columnName, String columnValue, String where) {
        if (isTableCreated()) {
            try  {
                conn = DatabaseUtil.getConnection();
                ps = conn.createStatement();
                sql = "UPDATE " + TableNames.HIGHLIGHT_ELEMENT +
                        " SET " + columnName + " = " + columnValue +
                        " WHERE " + where;
                ps.executeUpdate(sql);
                return;
            } catch (SQLException e) {
                System.out.println("Line that threw error: " + sql);
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("Table does not exist. Hence cannot fetch any rows from it.");

    }
}
