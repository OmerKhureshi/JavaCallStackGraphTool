package com.application.db.DAO.DAOImplementation;

import com.application.controller.ControllerLoader;
import com.application.db.DTO.ElementDTO;
import com.application.db.DTO.HighlightDTO;
import com.application.db.DatabaseUtil;
import com.application.db.TableNames;
import com.application.fxgraph.graph.BoundBox;
import javafx.geometry.BoundingBox;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public static void close() {
        try {
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public static List<HighlightDTO> getHighlightDTOsInViewPort(BoundingBox viewPortDims) {
        if (!isTableCreated())
            createTable();

        List<HighlightDTO> highlightDTOs = new ArrayList<>();

        double viewPortMinX = viewPortDims.getMinX();
        double viewPortMaxX = viewPortDims.getMaxX();
        double viewPortMinY = viewPortDims.getMinY();
        double viewPortMaxY = viewPortDims.getMaxY();
        double widthOffset = viewPortDims.getWidth();
        double heightOffset = viewPortDims.getHeight();

        // Query to fetches highlight boxes that are contained within the bounds of outermost preload box.

        String sql = "SELECT * FROM " + TableNames.HIGHLIGHT_ELEMENT + " WHERE " +
                (viewPortMinX - widthOffset) + " <= (start_x + width) " + " " +
                "AND " + (viewPortMaxX + widthOffset) + " >= start_x " +
                "AND " + (viewPortMinY - heightOffset) + " <= (start_y + height) " + " " +
                "AND " + (viewPortMaxY + heightOffset) + " >= start_Y " +
                "AND thread_id = " + ControllerLoader.centerLayoutController.getCurrentThreadId() + " " +
                "AND COLLAPSED = 0";

        try (ResultSet rs = DatabaseUtil.select(sql)) {
            while (rs.next()) {
                HighlightDTO highlightDTO = new HighlightDTO();
                highlightDTO.setId(rs.getInt("ID"));
                highlightDTO.setElementId(rs.getInt("ELEMENT_ID"));
                highlightDTO.setStartX(rs.getFloat("START_X"));
                highlightDTO.setStartY(rs.getFloat("START_Y"));
                highlightDTO.setWidth(rs.getFloat("WIDTH"));
                highlightDTO.setHeight(rs.getFloat("HEIGHT"));
                highlightDTO.setColor(rs.getString("COLOR"));

                highlightDTOs.add(highlightDTO);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }

        return highlightDTOs;
    }

    public static List<String> getParentHighlightResizeQueries(ElementDTO clickedEleDTO, int threadId) {
        List<String> queries = new ArrayList<>();

        double startXOffset = 30;
        double widthOffset = 35;
        double startYOffset = -10;
        double heightOffset = -20;

        double y = clickedEleDTO.getBoundBoxYTopLeft();
        double x = clickedEleDTO.getBoundBoxXTopLeft();
        String cellId = clickedEleDTO.getId();
        double deltaY = clickedEleDTO.getDeltaY();

        // get ids of all 'full' highlights that contain the current cell and cells of parent chain.
        // String getHighlightIdsToResize =  "SELECT ID " +
        //         "FROM HIGHLIGHT_ELEMENT " +
        //         "WHERE START_X <= " + (x + startXOffset) + " " +
        //         "AND START_X + WIDTH >=  " + (x + startXOffset) + " " +
        //         "AND START_Y <= " + (y + startYOffset) + " " +
        //         "AND START_Y + HEIGHT >= " + (y + startYOffset) + " " +
        //         "AND ELEMENT_ID <= " + cellId + " " +
        //         "AND COLLAPSED = 0 " +
        //         "AND HIGHLIGHT_TYPE = 'FULL' " +
        //         "AND THREAD_ID = " + threadId;

        double clickedCellX = clickedEleDTO.getBoundBoxXCoordinate();
        double clickedCellY = clickedEleDTO.getBoundBoxYCoordinate();


        String getHighlightIdsToResize =  "SELECT ID " +
                "FROM HIGHLIGHT_ELEMENT " +
                "WHERE START_X <= " + (clickedCellX) + " " +
                "AND START_X + WIDTH >=  " + (clickedCellX) + " " +
                "AND START_Y <= " + (clickedCellY) + " " +
                "AND START_Y + HEIGHT >= " + (clickedCellY) + " " +
                "AND ELEMENT_ID <= " + cellId + " " +
                "AND COLLAPSED = 0 " +
                "AND HIGHLIGHT_TYPE = 'FULL' " +
                "AND THREAD_ID = " + threadId;


        try (ResultSet rs = DatabaseUtil.select(getHighlightIdsToResize)) {
            while (rs.next()) {

                // For all the highlights obtained above, adjust their width and height
                // so that the highlights cover only the visible cells.
                queries.add(
                        "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " AS H " +
                        "SET H.HEIGHT = HEIGHT - " + deltaY + " " +
                        "WHERE H.ID = " + rs.getInt("ID"));

                queries.add(
                        "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " AS H " +
                                "SET " +
                                // "H.HEIGHT = HEIGHT - " + delta + ", " +
                                "H.WIDTH = " +
                                "((SELECT MAX(E1.BOUND_BOX_X_TOP_RIGHT) FROM " + TableNames.ELEMENT_TABLE + " AS E1 " +
                                "JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT ON E1.ID_ENTER_CALL_TRACE = CT.ID " +
                                "WHERE E1.BOUND_BOX_Y_COORDINATE >= H.START_Y " +
                                "AND E1.BOUND_BOX_Y_COORDINATE <= (H.START_Y + H.HEIGHT) " +
                                "AND E1.BOUND_BOX_X_COORDINATE >= H.START_X " +
                                "AND CT.THREAD_ID = " + threadId + " " +
                                "AND (E1.COLLAPSED = 0 OR E1.COLLAPSED = 2)" +
                                ") - H.START_X + " + widthOffset + ") " +
                                "WHERE H.ID = " + rs.getInt("ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }

        return queries;
    }

    public static List<String> getChildrenHighlightResizeQueries(ElementDTO clickedEleDTO, boolean isCollapsed, int nextCellId, int threadId) {
        List<String> queries = new ArrayList<>();

        String cellId = clickedEleDTO.getId();

        double startX = 0, startY = 0, width = 0, height = 0;

        double startXOffset = 30;
        double widthOffset = 30;
        double startYOffset = -10;
        double heightOffset = -20;

        if (!isCollapsed) {
            // get all highlights that are contained within the expanded subtree.
            String getHighlightsToResize = "SELECT * " +
                    "FROM " + TableNames.HIGHLIGHT_ELEMENT + " " +
                    "WHERE  ELEMENT_ID > " + cellId + " " +
                    "AND ELEMENT_ID < " + nextCellId + " " +
                    "AND COLLAPSED IN (0, 2) " +
                    "AND HIGHLIGHT_TYPE = 'FULL' " +
                    "AND THREAD_ID = " + threadId;

            // System.out.println(" getHighlightsToResize: " + getHighlightsToResize);

            try (ResultSet rs = DatabaseUtil.select(getHighlightsToResize)) {
                while (rs.next()) {

                    startX = rs.getDouble("START_X");
                    startY = rs.getDouble("START_Y");
                    width = rs.getDouble("WIDTH");
                    height = rs.getDouble("HEIGHT");

                    // For all the highlights obtained above, adjust their widht so that the highlights cover only the visible cells.
                    String updatChildrenHighlightsQuery = "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " AS H " +
                            "SET H.WIDTH = " +
                            "((SELECT MAX(E1.BOUND_BOX_X_TOP_RIGHT) FROM " + TableNames.ELEMENT_TABLE + " AS E1 " +
                            "JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT ON E1.ID_ENTER_CALL_TRACE = CT.ID " +
                            "WHERE E1.BOUND_BOX_Y_COORDINATE >= " + startY + " " +
                            "AND E1.BOUND_BOX_Y_COORDINATE <= " + ( startY + height) + " " +
                            "AND E1.BOUND_BOX_X_COORDINATE >= " + startX + " " +
                            "AND CT.THREAD_ID = " + threadId + " " +
                            "AND E1.COLLAPSED IN (0, 2)" +
                            ") - H.START_X + " + widthOffset + ") " +
                            "WHERE H.ID = " + rs.getInt("ID");

                    queries.add(updatChildrenHighlightsQuery);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DatabaseUtil.close();
            }
        }

        return queries;
    }


    public static String getUpdateHighlightQuery(double y, double delta, int nextCellId, int lastCellId, int threadId) {
        return "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " " +
                "SET START_Y = START_Y - " + delta + " " +
                "WHERE ELEMENT_ID >= " + nextCellId + " " +
                "AND THREAD_ID = " + threadId;
    }
}
