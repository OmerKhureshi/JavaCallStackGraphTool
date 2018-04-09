package com.application.db.DAO.DAOImplementation;

import com.application.controller.ControllerLoader;
import com.application.db.DTO.ElementDTO;
import com.application.db.DatabaseUtil;
import com.application.db.TableNames;
import com.application.fxgraph.ElementHelpers.Element;
import com.application.fxgraph.graph.Graph;
import javafx.geometry.BoundingBox;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.application.db.DAO.DAOImplementation.BookmarksDAOImpl.createTable;
import static com.application.db.TableNames.ELEMENT_TABLE;

public class ElementDAOImpl {
    // public static boolean isTableCreated = false;

    public static boolean isTableCreated() {
        //        System.out.println("starting isTableCreated");
        // if (!isTableCreated) {// No need to call DatabaseUtil method every time. Save time this way.
        //            System.out.println("ElementDAOImpl:isTableCreated: " + isTableCreated);
        // isTableCreated = DatabaseUtil.isTableCreated(ELEMENT_TABLE);
        //            System.out.println("ElementDAOImpl:isTableCreated: " + isTableCreated);
        // }
        //        System.out.println("ending isTableCreated");
        // return isTableCreated;
        return DatabaseUtil.isTableCreated(ELEMENT_TABLE);
    }

    public static void createTable() {
        //        System.out.println("starting createTable");
        //        System.out.println("ElementDAOImpl:createTable: " + isTableCreated());
        if (!isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                String sql = "CREATE TABLE " + ELEMENT_TABLE + " (" +
                        "id INTEGER NOT NULL, " +
                        "parent_id INTEGER, " +  // todo define foreign key
                        "id_enter_call_trace INTEGER, " +  // not a foreign key.
                        "id_exit_call_trace INTEGER, " +  // not a foreign key.
                        "bound_box_x_top_left FLOAT, " +
                        "bound_box_y_top_left FLOAT, " +
                        "bound_box_x_top_right FLOAT, " +
                        "bound_box_y_top_right FLOAT, " +
                        "bound_box_x_bottom_right FLOAT, " +
                        "bound_box_y_bottom_right FLOAT, " +
                        "bound_box_x_bottom_left FLOAT, " +
                        "bound_box_y_bottom_left FLOAT, " +
                        "bound_box_x_coordinate FLOAT, " +
                        "bound_box_y_coordinate FLOAT, " +
                        "index_in_parent INTEGER, " +
                        "leaf_count INTEGER, " +
                        "level_count INTEGER, " +
                        "collapsed INTEGER, " +
                        "delta FLOAT, " +
                        "delta_x FLOAT" +
                        ")";
                ps.execute(sql);
                System.out.println("** Creating table " + TableNames.ELEMENT_TABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //        System.out.println("ending createTable");
    }

    public static void insert(Element element) {
        if (!isTableCreated())
            createTable();
        String sql = null;
        try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
            sql = "INSERT INTO " + TableNames.ELEMENT_TABLE + " VALUES (" +
                    element.getElementId() + ", " +
                    (element.getParent() == null? -1 : element.getParent().getElementId()) + ", " +
                    element.getFkEnterCallTrace() + ", " +
                    element.getFkExitCallTrace() + ", " +
                    element.getBoundBox().xTopLeft + ", " +
                    element.getBoundBox().yTopLeft + ", " +
                    element.getBoundBox().xTopRight + ", " +
                    element.getBoundBox().yTopRight + ", " +
                    element.getBoundBox().xBottomRight + ", " +
                    element.getBoundBox().yBottomRight + ", " +
                    element.getBoundBox().xBottomLeft + ", " +
                    element.getBoundBox().yBottomLeft + ", " +
                    element.getBoundBox().xCoordinate + ", " +
                    element.getBoundBox().yCoordinate + ", " +
                    element.getIndexInParent() + ", " +
                    element.getLeafCount() + ", " +
                    element.getLevelCount() +  ", " +
                    element.getIsCollapsed() + ", " +
                    element.getDelta() + ", " +
                    element.getDeltaX() +
                    ")";

            ps.execute(sql);
            //            System.out.println(TableNames.ELEMENT_TABLE + ": Inserted: " + sql);
        } catch (SQLException e) {
            System.out.println(" Exception caused by: " + sql);
            e.printStackTrace();
        }
        //        System.out.println("ending insert");
    }

    public static void insert(List<ElementDTO> elementDTOList) {
        if (!isTableCreated())
            createTable();

        List<String> queryList = getQueryList(elementDTOList);
        DatabaseUtil.addAndExecuteBatch(queryList);
    }

    private static List<String> getQueryList(List<ElementDTO> elementDTOList) {
        return elementDTOList
                .stream()
                .map(elementDTO -> {
                    return  "INSERT INTO " + TableNames.ELEMENT_TABLE + " VALUES (" +
                            elementDTO.getId() + ", " +
                            elementDTO.getParentId() + ", " +
                            elementDTO.getIdEnterCallTrace() + ", " +
                            elementDTO.getIdExitCallTrace() + ", " +
                            elementDTO.getBoundBoxXTopLeft() + ", " +
                            elementDTO.getBoundBoxYTopLeft() + ", " +
                            elementDTO.getBoundBoxXTopRight() + ", " +
                            elementDTO.getBoundBoxYTopRight() + ", " +
                            elementDTO.getBoundBoxXBottomRight() + ", " +
                            elementDTO.getBoundBoxYBottomRight() + ", " +
                            elementDTO.getBoundBoxXBottomLeft() + ", " +
                            elementDTO.getBoundBoxYBottomLeft() + ", " +
                            elementDTO.getBoundBoxXCoordinate() + ", " +
                            elementDTO.getBoundBoxYCoordinate() + ", " +
                            elementDTO.getIndexInParent() + ", " +
                            elementDTO.getLeafCount() + ", " +
                            elementDTO.getLevelCount() + ", " +
                            elementDTO.getCollapsed() + ", " +
                            elementDTO.getDelta() + ", " +
                            elementDTO.getDeltaX() +
                            ")"; })
                .collect(Collectors.toList());
    }

    public static void dropTable() {
        //        System.out.println("starting dropTable");
        if (isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                String sql= "Drop table " + TableNames.ELEMENT_TABLE;
                System.out.println(">> Dropping table " + TableNames.ELEMENT_TABLE);

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
                sql = "SELECT * FROM " + ELEMENT_TABLE + " WHERE " + where;
                //                System.out.println(">>> we got " + sql);
                ResultSet resultSet = ps.executeQuery(sql);
                //                resultSet.next();
                //                System.out.println(resultSet.getInt("id"));
                return resultSet;
            } catch (SQLException e) {
                System.out.println("Line that threw error: " + sql);
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("Table does not exist. Hence cannot fetch any rows from it.");
    }

    public static void updateWhere(String columnName, String columnValue, String where) {
        if (isTableCreated()) {
            try  {
                conn = DatabaseUtil.getConnection();
                ps = conn.createStatement();
                sql = "UPDATE " + ELEMENT_TABLE +
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

    /**
     * This method fetches the rows for elements that should be drawn next on the UI.
     */
    public static List<ElementDTO> getElementDTOs(BoundingBox viewPort) {
        List<ElementDTO> elementDTOList = new ArrayList<>();
        // Get element properties for those elements that are inside the expanded region calculated above.
        String sql = "SELECT E.ID AS EID, parent_id, collapsed, " +
                "bound_box_x_coordinate, bound_box_y_coordinate, " +
                "message, id_enter_call_trace, method_id, " +
                "(CASE " +
                "   WHEN M.METHOD_NAME IS null THEN MESSAGE " +
                "   ELSE M.METHOD_NAME " +
                "END) AS method_name " +
                "FROM " + TableNames.CALL_TRACE_TABLE + " AS CT " +
                "JOIN " + TableNames.ELEMENT_TABLE + " AS E ON CT.ID = E.ID_ENTER_CALL_TRACE " +
                "INNER JOIN " + TableNames.METHOD_DEFINITION_TABLE + " AS M ON CT.METHOD_ID = M.ID " +
                "WHERE CT.THREAD_ID = " + ControllerLoader.centerLayoutController.getCurrentThreadId() +
                " AND E.bound_box_x_coordinate >= " + (viewPort.getMinX()) +
                " AND E.bound_box_x_coordinate <= " + (viewPort.getMaxX()) +
                " AND E.bound_box_y_coordinate >= " + (viewPort.getMinY()) +
                " AND E.bound_box_y_coordinate <= " + (viewPort.getMaxY()) +
                " AND E.LEVEL_COUNT > 1" +
                " AND (E.COLLAPSED = 0" +
                " OR E.COLLAPSED = 2)";

        // System.out.println();
        // System.out.println("ElementDAOImpl.getElementDTOs query: " + sql);
        try (ResultSet rs = DatabaseUtil.select(sql)) {
            while (rs.next()) {
                ElementDTO elementDTO = new ElementDTO();
                elementDTO.setId(String.valueOf(rs.getInt("EID")));
                elementDTO.setParentId(rs.getInt("parent_id"));
                elementDTO.setCollapsed(rs.getInt("collapsed"));
                elementDTO.setBoundBoxXCoordinate(rs.getFloat("bound_box_x_coordinate"));
                elementDTO.setBoundBoxYCoordinate(rs.getFloat("bound_box_y_coordinate"));
                elementDTO.setIdEnterCallTrace(rs.getInt("id_enter_call_trace"));
                elementDTO.setMethodId(rs.getInt("method_id"));
                elementDTO.setMethodName(rs.getString("method_name"));

                elementDTOList.add(elementDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }
        return elementDTOList;
    }

    public static int getMaxLevelCount(String threadId) {
        if (!ElementDAOImpl.isTableCreated()) {
            ElementDAOImpl.createTable();
        }

        if (!CallTraceDAOImpl.isTableCreated()) {
            CallTraceDAOImpl.createTable();
        }

        // Get the width for placeholder line.
        String SQLMaxLevelCount = "select max(LEVEL_COUNT) from ELEMENT " +
                "where ID_ENTER_CALL_TRACE in " +
                "(SELECT  CALL_TRACE.ID from CALL_TRACE where THREAD_ID  = " + threadId + ")";

        return DatabaseUtil.executeSelectForInt(SQLMaxLevelCount);
    }

    public static int getMaxLeafCount(String threadId) {
        if (!ElementDAOImpl.isTableCreated()) {
            ElementDAOImpl.createTable();
        }

        if (!CallTraceDAOImpl.isTableCreated()) {
            CallTraceDAOImpl.createTable();
        }

        if (!ElementToChildDAOImpl.isTableCreated()) {
            ElementToChildDAOImpl.createTable();
        }

        // Get the height for placeholder line.
        String SQLMaxLeafCount = "select LEAF_COUNT from ELEMENT " +
                "where LEVEL_COUNT = 1 AND ID = " +
                "(SELECT PARENT_ID from ELEMENT_TO_CHILD " +
                "where CHILD_ID = " +
                "(SELECT id from ELEMENT " +
                "where ID_ENTER_CALL_TRACE = " +
                "(SELECT  min(CALL_TRACE.ID) from CALL_TRACE " +
                "where THREAD_ID  = " + threadId + ")))";
        // System.out.println("ElementDAOImpl.getMaxLeafCount query: " + SQLMaxLeafCount);

        return DatabaseUtil.executeSelectForInt(SQLMaxLeafCount);
    }

}
