package com.csgt.dataaccess.DAO;

import com.csgt.controller.ControllerLoader;
import com.csgt.dataaccess.DTO.EdgeDTO;
import com.csgt.dataaccess.DatabaseUtil;
import com.csgt.dataaccess.TableNames;
import com.csgt.dataaccess.model.EdgeElement;
import javafx.geometry.BoundingBox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.csgt.dataaccess.TableNames.EDGE_TABLE;

public class EdgeDAOImpl {
    public static boolean isTableCreated() {
        return DatabaseUtil.isTableCreated(TableNames.EDGE_TABLE);
    }

    public static void createTable() {
        String sql = "";
        if (!isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                sql = "CREATE TABLE " + EDGE_TABLE + " (" +
                        "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                        "fk_source_element_id INTEGER, " +
                        "fk_target_element_id INTEGER, " +
                        "start_x FLOAT, " +
                        "start_y FLOAT, " +
                        "end_x FLOAT, " +
                        "end_y FLOAT, " +
                        "collapsed INTEGER, " +
                        "FOREIGN KEY(fk_source_element_id) REFERENCES " + TableNames.ELEMENT_TABLE + "(ID), " +
                        "FOREIGN KEY(fk_target_element_id) REFERENCES " + TableNames.ELEMENT_TABLE + "(ID)" +
                        ")";
                ps.execute(sql);
                // System.out.println("** Creating table " + TableNames.EDGE_TABLE);
            } catch (SQLException e) {
                System.out.println("EdgeDAOImpl.createTable: exception caused by query: " + sql);
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
                    edge.getCollapsed() + "" +
                    ")";

            ps.execute(sql);
        } catch (SQLException e) {
            System.out.println(" Exception caused by: " + sql);
            e.printStackTrace();
        }
    }

    public static void insert(List<EdgeDTO> edgeDTOList) {
        if (!isTableCreated())
            createTable();

        DatabaseUtil.addAndExecuteBatch(getQueryList(edgeDTOList));
    }


    static List<String> getQueryList(List<EdgeDTO> edgeDTOList) {
        return edgeDTOList.stream()
                .map(edgeDTO -> {
                    return "INSERT INTO " + EDGE_TABLE + " (" +
                            "fk_source_element_id, " +
                            "fk_target_element_id, " +
                            "start_x, " +
                            "start_y, " +
                            "end_x, " +
                            "end_y, " +
                            "collapsed) " +
                            " VALUES (" +
                            edgeDTO.getSourceElementId() + ", " +
                            edgeDTO.getTargetElementId() + ", " +
                            edgeDTO.getStartX() + ", " +
                            edgeDTO.getStartY() + ", " +
                            edgeDTO.getEndX() + ", " +
                            edgeDTO.getEndY() + ", " +
                            edgeDTO.getCollapsed() + ")";
                })
                .collect(Collectors.toList());
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
                ResultSet resultSet = ps.executeQuery(sql);
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

    public static List<EdgeDTO> getEdgeDTO(BoundingBox viewPort) {
        List<EdgeDTO> edgeDTOList = new ArrayList<>();

        double viewPortMinX = viewPort.getMinX();
        double viewPortMaxX = viewPort.getMaxX();
        double viewPortMinY = viewPort.getMinY();
        double viewPortMaxY = viewPort.getMaxY();

        double widthOffset = viewPort.getWidth() * 3;
        double heightOffset = viewPort.getHeight() * 3;

        String sql = "SELECT * FROM EDGE_ELEMENT " +
                "INNER JOIN ELEMENT ON FK_SOURCE_ELEMENT_ID = ELEMENT.ID " +
                "INNER JOIN CALL_TRACE ON ELEMENT.ID_ENTER_CALL_TRACE = CALL_TRACE.ID " +
                "WHERE CALL_TRACE.THREAD_ID = " + ControllerLoader.centerLayoutController.getCurrentThreadId() + " ";

        String commonWhereClausForEdges = "AND EDGE_ELEMENT.collapsed = 0 AND " + "end_x >= " + (viewPortMinX - widthOffset) + " AND start_x <= " + (viewPortMaxX + widthOffset);
        String whereClauseForUpwardEdges = " AND end_Y >= " + (viewPortMinY - heightOffset) + " AND start_y <= " + (viewPortMaxY + heightOffset);
        String whereClauseForDownwardEdges = " AND start_y >= " + (viewPortMinY - heightOffset) + " AND end_Y <= " + (viewPortMaxY + heightOffset);

        try (ResultSet rsUpEdges = DatabaseUtil.select(sql + commonWhereClausForEdges + whereClauseForUpwardEdges)) {
            getEdgesFromResultSet(rsUpEdges, edgeDTOList);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }

        try (ResultSet rsDownEdges = DatabaseUtil.select(sql + commonWhereClausForEdges + whereClauseForDownwardEdges)) {
            getEdgesFromResultSet(rsDownEdges, edgeDTOList);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }

        return edgeDTOList;
    }




    private static void getEdgesFromResultSet(ResultSet rs, List<EdgeDTO> edgeDTOList) {
        try {
            while (rs.next()) {
                String targetEdgeId = String.valueOf(rs.getInt("fk_target_element_id"));
                float startX = rs.getFloat("start_x");
                float endX = rs.getFloat("end_x");
                float startY = rs.getFloat("start_y");
                float endY = rs.getFloat("end_y");

                EdgeDTO edgeDTO = new EdgeDTO(targetEdgeId, startX, startY, endX, endY);
                edgeDTOList.add(edgeDTO);
            }
        } catch (SQLException e) {
            System.out.println("EdgeDAOImpl.getEdgesFromResultSet exception in this method...");
            e.printStackTrace();
        }
    }

    public static String getUpdateEdgeStartPointQuery(double y, double delta, int nextCellId, int lastCellId, int threadId) {
        return "UPDATE " + TableNames.EDGE_TABLE + " " +
                "SET START_Y =  START_Y - " + delta + " " +
                "WHERE START_Y >= " + y + " " +
                "AND FK_SOURCE_ELEMENT_ID >= " + nextCellId + " " +
//                "AND FK_SOURCE_ELEMENT_ID <= " + lastCellId;
                "AND FK_SOURCE_ELEMENT_ID <= " + lastCellId + " " +
                "and " +
                "(Exists (select * from " + TableNames.ELEMENT_TABLE + " as E " +
                "join " + TableNames.CALL_TRACE_TABLE + " as CT on E.ID_ENTER_CALL_TRACE = CT.ID " +
                "where (E.ID = FK_TARGET_ELEMENT_ID OR E.ID = FK_TARGET_ELEMENT_ID) and CT.THREAD_ID = " + threadId + "))";
//                "Exists (select * from " + TableNames.ELEMENT_TABLE + " as E " +
//                "join " + TableNames.CALL_TRACE_TABLE + " as CT on E.ID_ENTER_CALL_TRACE = CT.ID " +
//                "where E.ID = " + lastCellId + " and CT.THREAD_ID = " + threadId + "))";

    }

    public static String getUpdateEdgeEndPointQuery(double y, double delta, int nextCellId, int lastCellId, int threadId) {
        return "UPDATE " + TableNames.EDGE_TABLE + " " +
                "SET END_Y =  END_Y - " + delta + " " +
                "WHERE END_Y >= " + y + " " +
                "AND FK_TARGET_ELEMENT_ID >= " + nextCellId + " " +
//                "AND FK_TARGET_ELEMENT_ID <= " + lastCellId;
                "AND FK_TARGET_ELEMENT_ID <= " + lastCellId + " " +
                "and " +
                "(Exists (select * from " + TableNames.ELEMENT_TABLE + " as E " +
                "join " + TableNames.CALL_TRACE_TABLE + " as CT on E.ID_ENTER_CALL_TRACE = CT.ID " +
                "where (E.ID = FK_TARGET_ELEMENT_ID OR E.ID = FK_TARGET_ELEMENT_ID) and CT.THREAD_ID = " + threadId + "))";
//                "OR " +
//                "Exists (select * from " + TableNames.ELEMENT_TABLE + " as E " +
//                "join " + TableNames.CALL_TRACE_TABLE + " as CT on E.ID_ENTER_CALL_TRACE = CT.ID " +
//                "where E.ID = " + lastCellId + " and CT.THREAD_ID = " + threadId + "))";

    }

}
