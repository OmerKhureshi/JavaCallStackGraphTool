package com.application.db.DAO.DAOImplementation;

import com.application.db.DatabaseUtil;
import com.application.db.TableNames;
import com.application.fxgraph.graph.BoundBox;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

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

    public static void insert(double startXOffset, double startYOffset, double widthOffset, double heightOffset, String methodName, String packageName,
                              String highlightType, Map<String, Color> colorsMap, String fullName, Statement statement) {
        String sqlSingle = "INSERT INTO " + TableNames.HIGHLIGHT_ELEMENT + " " +
                "(ELEMENT_ID, METHOD_ID, THREAD_ID, HIGHLIGHT_TYPE, START_X, START_Y, WIDTH, HEIGHT, COLOR, COLLAPSED) " +

                "SELECT " +

                // ELEMENT_ID
                TableNames.ELEMENT_TABLE + ".ID, " +

                // METHOD_ID
                TableNames.METHOD_DEFINITION_TABLE + ".ID, " +

                // THREAD_ID
                TableNames.CALL_TRACE_TABLE + ".THREAD_ID, " +

                // HIGHLIGHT_TYPE
                "'" + highlightType + "', " +

                // START_X
                TableNames.ELEMENT_TABLE + ".BOUND_BOX_X_TOP_LEFT + " + startXOffset + ", " +

                // START_Y
                TableNames.ELEMENT_TABLE + ".BOUND_BOX_Y_TOP_LEFT + " + startYOffset + ", " +

                // WIDTH
                (BoundBox.unitWidthFactor + widthOffset) + ", " +

                // HEIGHT
                (BoundBox.unitHeightFactor + heightOffset) + ", " +
                // "(" + TableNames.ELEMENT_TABLE + ".BOUND_BOX_X_TOP_RIGHT - " + TableNames.ELEMENT_TABLE + ".BOUND_BOX_X_TOP_LEFT), " +
                // "(" + TableNames.ELEMENT_TABLE + ".BOUND_BOX_Y_BOTTOM_LEFT - " + TableNames.ELEMENT_TABLE + ".BOUND_BOX_Y_TOP_LEFT), " +

                // HIGHLIGHT COLOR
                "'" + colorsMap.getOrDefault(fullName, Color.AQUAMARINE) + "'," +

                // COLLAPSED
                "0 " +

                "FROM " + TableNames.ELEMENT_TABLE + " " +
                "JOIN " + TableNames.CALL_TRACE_TABLE + " ON " + TableNames.ELEMENT_TABLE + ".ID_ENTER_CALL_TRACE = " + TableNames.CALL_TRACE_TABLE + ".ID " +
                "JOIN " + TableNames.METHOD_DEFINITION_TABLE + " ON " + TableNames.CALL_TRACE_TABLE + ".METHOD_ID = " + TableNames.METHOD_DEFINITION_TABLE + ".ID " +
                "WHERE " + TableNames.METHOD_DEFINITION_TABLE + ".METHOD_NAME = '" + methodName + "' " +
                "AND " + TableNames.METHOD_DEFINITION_TABLE + ".PACKAGE_NAME = '" + packageName + "' " +
                "AND NOT EXISTS " +
                "(SELECT * FROM " + TableNames.HIGHLIGHT_ELEMENT + " " +
                "WHERE " + TableNames.HIGHLIGHT_ELEMENT + ".METHOD_ID = " + TableNames.METHOD_DEFINITION_TABLE + ".ID " +
                "AND " + TableNames.HIGHLIGHT_ELEMENT + ".HIGHLIGHT_TYPE = '" + highlightType + "')";


        // Get thread id for the method. There can only be a single thread.
        // If method with same name was invoked by another thread, then its package name would different.
        int threadId = CallTraceDAOImpl.getThreadIdByMethodNameAndPackageName(methodName, packageName);

        String sqlFull = "INSERT INTO " + TableNames.HIGHLIGHT_ELEMENT + " " +
                "(ELEMENT_ID, METHOD_ID, THREAD_ID, HIGHLIGHT_TYPE, START_X, START_Y, WIDTH, HEIGHT, COLOR, COLLAPSED) " +
                "SELECT " +

                // ELEMENT_ID
                TableNames.ELEMENT_TABLE + ".ID, " +

                // METHOD_ID
                TableNames.METHOD_DEFINITION_TABLE + ".ID, " +

                // THREAD_ID
                TableNames.CALL_TRACE_TABLE + ".THREAD_ID, " +

                // HIGHLIGHT_TYPE
                "'" + highlightType + "', " +

                // START_X
                TableNames.ELEMENT_TABLE + ".BOUND_BOX_X_TOP_LEFT + " + startXOffset + ", " +

                // START_Y
                TableNames.ELEMENT_TABLE + ".BOUND_BOX_Y_TOP_LEFT + " + startYOffset + ", " +

                // WIDTH
                "CASE " +
                "WHEN " + TableNames.ELEMENT_TABLE + ".COLLAPSED IN (0, 2) THEN " +
                "((SELECT MAX(E1.BOUND_BOX_X_TOP_RIGHT) FROM " + TableNames.ELEMENT_TABLE + " AS E1 " +
                "JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT ON E1.ID_ENTER_CALL_TRACE = CT.ID " +
                "WHERE E1.BOUND_BOX_Y_COORDINATE >= " + TableNames.ELEMENT_TABLE + ".BOUND_BOX_Y_TOP_LEFT " +
                "AND E1.BOUND_BOX_Y_COORDINATE < " + TableNames.ELEMENT_TABLE + ".BOUND_BOX_Y_BOTTOM_LEFT " +
                "AND CT.THREAD_ID = " + threadId + " " +
                "AND (E1.COLLAPSED IN (0, 2)  OR E1.ID = ELEMENT.ID)" +
                // ") - " + TableNames.ELEMENT_TABLE + ".BOUND_BOX_X_TOP_LEFT + " + widthOffset + ") " +
                ") - " + TableNames.ELEMENT_TABLE + ".BOUND_BOX_X_TOP_LEFT) " +
                "ELSE " + (BoundBox.unitWidthFactor + widthOffset) + " " +
                "END " +
                ", " +
                // TableNames.ELEMENT_TABLE + ".BOUND_BOX_X_BOTTOM_RIGHT - " + TableNames.ELEMENT_TABLE + ".BOUND_BOX_X_BOTTOM_LEFT + " + endOffset + "," +

                // HEIGHT
                // "(SELECT MAX(E1.BOUND_BOX_Y_BOTTOM_RIGHT) FROM " + TableNames.ELEMENT_TABLE + " AS E1 " +
                // "JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT ON E1.ID_ENTER_CALL_TRACE = CT.ID " +
                // "WHERE E1.BOUND_BOX_Y_COORDINATE >= " + TableNames.ELEMENT_TABLE + ".BOUND_BOX_Y_TOP_LEFT " +
                // "AND E1.BOUND_BOX_Y_COORDINATE <= " + TableNames.ELEMENT_TABLE + ".BOUND_BOX_Y_BOTTOM_LEFT " +
                // "AND E1.BOUND_BOX_X_COORDINATE >= " + TableNames.ELEMENT_TABLE + ".BOUND_BOX_X_TOP_LEFT " +
                // "AND CT.THREAD_ID = " + threadId + ") - " + TableNames.ELEMENT_TABLE + ".BOUND_BOX_Y_TOP_LEFT + " + endOffset + ", " +
                TableNames.ELEMENT_TABLE + ".BOUND_BOX_Y_BOTTOM_LEFT - " + TableNames.ELEMENT_TABLE + ".BOUND_BOX_Y_TOP_LEFT + " + heightOffset + "," +

                // COLOR
                "'" + colorsMap.getOrDefault(fullName, Color.AQUAMARINE) + "', " +

                // COLLAPSED
                "(SELECT " +
                "CASE " +
                "WHEN E1.COLLAPSED = 0 THEN 0 " +
                "WHEN E1.COLLAPSED = 2 THEN 0 " +
                "ELSE 1 " +
                "END " +
                "FROM " + TableNames.ELEMENT_TABLE + " AS E1 WHERE E1.ID = " + TableNames.ELEMENT_TABLE + ".ID) " +


                "FROM " + TableNames.ELEMENT_TABLE + " " +
                "JOIN " + TableNames.CALL_TRACE_TABLE + " ON " + TableNames.ELEMENT_TABLE + ".ID_ENTER_CALL_TRACE = " + TableNames.CALL_TRACE_TABLE + ".ID " +
                "JOIN " + TableNames.METHOD_DEFINITION_TABLE + " ON " + TableNames.CALL_TRACE_TABLE + ".METHOD_ID = " + TableNames.METHOD_DEFINITION_TABLE + ".ID " +
                "WHERE " + TableNames.METHOD_DEFINITION_TABLE + ".METHOD_NAME = '" + methodName + "' " +
                "AND " + TableNames.METHOD_DEFINITION_TABLE + ".PACKAGE_NAME = '" + packageName + "' " +
                "AND NOT EXISTS " +
                "(SELECT * FROM " + TableNames.HIGHLIGHT_ELEMENT + " " +
                "WHERE " + TableNames.HIGHLIGHT_ELEMENT + ".METHOD_ID = " + TableNames.METHOD_DEFINITION_TABLE + ".ID " +
                "AND " + TableNames.HIGHLIGHT_ELEMENT + ".HIGHLIGHT_TYPE = '" + highlightType + "')";

        String sql = highlightType.equalsIgnoreCase("SINGLE") ? sqlSingle : sqlFull;

        try {
            statement.addBatch(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
