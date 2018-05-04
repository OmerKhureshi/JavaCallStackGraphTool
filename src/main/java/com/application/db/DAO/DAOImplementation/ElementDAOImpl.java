package com.application.db.DAO.DAOImplementation;

import com.application.controller.ControllerLoader;
import com.application.db.DTO.ElementDTO;
import com.application.db.DatabaseUtil;
import com.application.db.TableNames;
import com.application.fxgraph.ElementHelpers.Element;
import javafx.geometry.BoundingBox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.application.db.TableNames.ELEMENT_TABLE;

public class ElementDAOImpl {

    private  static Map<String, Integer> lowestCellInThreadMap = new HashMap<>();

    public static boolean isTableCreated() {
        return DatabaseUtil.isTableCreated(ELEMENT_TABLE);
    }

    public static void createTable() {
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
                            elementDTO.getDeltaY() + ", " +
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

    public static void updateCollapse(ElementDTO elementDTO) {
        String updateClickedElement = "UPDATE " + TableNames.ELEMENT_TABLE + " " +
                "SET COLLAPSED = " + elementDTO.getCollapsed() + " " +
                "WHERE ID = " + elementDTO.getId();

        DatabaseUtil.executeUpdate(updateClickedElement);
    }

    public static void updateCollapseAndDelta(ElementDTO elementDTO) {
        String updateClickedElement = "UPDATE " + TableNames.ELEMENT_TABLE + " " +
                "SET COLLAPSED = 2, " +
                "DELTA = " + elementDTO.getDeltaY() + ", " +
                "DELTA_X = " + elementDTO.getDeltaX() + " " +
                "WHERE ID = " + elementDTO.getId();


        DatabaseUtil.executeUpdate(updateClickedElement);

    }

    public static ElementDTO getElementDTO(String id) {
        ElementDTO elementDTO = null;

        String sql = "SELECT * FROM " + TableNames.ELEMENT_TABLE + " WHERE ID = " + id;

        // System.out.println();
        // System.out.println("ElementDAOImpl.getElementDTOsInViewport query: " + sql);
        try (ResultSet rs = DatabaseUtil.select(sql)) {
            if (rs.next()) {
                elementDTO = processElementDTO(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }

        return elementDTO;
    }

    private static ElementDTO processElementDTO(ResultSet rs) {
        ElementDTO elementDTO = new ElementDTO();

        try {
            elementDTO.setId(String.valueOf(rs.getInt("ID")));
            elementDTO.setParentId(rs.getInt("parent_id"));
            elementDTO.setIdEnterCallTrace(rs.getInt("id_enter_call_trace"));
            elementDTO.setIdExitCallTrace(rs.getInt("id_exit_call_trace"));

            // top left
            elementDTO.setBoundBoxXTopLeft(rs.getFloat("bound_box_x_top_left"));
            elementDTO.setBoundBoxYTopLeft(rs.getFloat("bound_box_y_top_left"));

            // bottom left
            elementDTO.setBoundBoxXBottomLeft(rs.getFloat("bound_box_x_bottom_left"));
            elementDTO.setBoundBoxYBottomLeft(rs.getFloat("bound_box_y_bottom_left"));

            // top right
            elementDTO.setBoundBoxXTopRight(rs.getFloat("bound_box_x_top_right"));
            elementDTO.setBoundBoxYTopRight(rs.getFloat("bound_box_y_top_right"));

            // bottom right
            elementDTO.setBoundBoxXBottomRight(rs.getFloat("bound_box_x_bottom_right"));
            elementDTO.setBoundBoxYBottomRight(rs.getFloat("bound_box_y_bottom_right"));

            // coordinates
            elementDTO.setBoundBoxXCoordinate(rs.getFloat("bound_box_x_coordinate"));
            elementDTO.setBoundBoxYCoordinate(rs.getFloat("bound_box_y_coordinate"));

            elementDTO.setIndexInParent(rs.getInt("index_in_parent"));
            elementDTO.setLeafCount(rs.getInt("leaf_count"));
            elementDTO.setLevelCount(rs.getInt("level_count"));
            elementDTO.setCollapsed(rs.getInt("collapsed"));

            elementDTO.setDeltaY(rs.getFloat("delta"));
            elementDTO.setDeltaX(rs.getFloat("delta_x"));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return elementDTO;
    }

    /**
     * This method fetches the rows for elements that should be drawn next on the UI.
     */
    public static List<ElementDTO> getElementDTOsInViewport(BoundingBox viewPort) {
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
                "LEFT JOIN " + TableNames.METHOD_DEFINITION_TABLE + " AS M ON CT.METHOD_ID = M.ID " +
                "WHERE CT.THREAD_ID = " + ControllerLoader.centerLayoutController.getCurrentThreadId() +
                " AND E.bound_box_x_coordinate >= " + (viewPort.getMinX()) +
                " AND E.bound_box_x_coordinate <= " + (viewPort.getMaxX()) +
                " AND E.bound_box_y_coordinate >= " + (viewPort.getMinY()) +
                " AND E.bound_box_y_coordinate <= " + (viewPort.getMaxY()) +
                " AND E.LEVEL_COUNT > 1" +
                " AND (E.COLLAPSED = 0" +
                " OR E.COLLAPSED = 2)";

        // System.out.println();
        // System.out.println("ElementDAOImpl.getElementDTOsInViewport query: " + sql);
        try (ResultSet rs = DatabaseUtil.select(sql)) {
            while (rs != null && rs.next()) {
                // System.out.println("ElementDAOImpl.getElementDTOsInViewport: while in loop : "  + rs.getInt("EID"));
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
            System.out.println("ElementDAOImpl.getElementDTOsInViewport Exception in this method....");
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

    public static int getLowestCellInThread(String threadId) {
        if (lowestCellInThreadMap.containsKey(threadId)) {
            return lowestCellInThreadMap.get(threadId);
        }

        String maxEleIdQuery = "SELECT MAX(E.ID) AS MAXID " +
                "FROM " + TableNames.ELEMENT_TABLE + " AS E JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT " +
                "ON E.ID_ENTER_CALL_TRACE = CT.ID " +
                "WHERE CT.THREAD_ID = " + threadId;

        try (ResultSet eleIdRS = DatabaseUtil.select(maxEleIdQuery)){
            if (eleIdRS.next()) {
                int eleId = eleIdRS.getInt("MAXID");
                lowestCellInThreadMap.put(threadId, eleId);
                return eleId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }

        return Integer.MAX_VALUE;
    }

    public static int getNextLowerSiblingOrAncestorNode(ElementDTO elementDTO, String threadId) {
        String clickedCellId = elementDTO.getId();
        double x = elementDTO.getBoundBoxXTopLeft();
        double y = elementDTO.getBoundBoxYTopLeft();

        int lastCellId = getLowestCellInThread(threadId) + 1;

        String getNextQuery = "SELECT " +
                "CASE " +
                "WHEN MIN(E.ID) IS NULL THEN " + lastCellId + " " +
                "ELSE MIN(E.ID) " +
                "END " +
                "AS MINID " +
                "FROM " + TableNames.ELEMENT_TABLE + " AS E JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT " +
                "ON E.ID_ENTER_CALL_TRACE = CT.ID " +
                "WHERE E.BOUND_BOX_Y_TOP_LEFT > " + y + " " +
                "AND E.BOUND_BOX_X_TOP_LEFT <= " + x + " " +
                "AND E.ID > " + clickedCellId + " " +
                "AND CT.THREAD_ID = " + threadId;

        try (ResultSet rs = DatabaseUtil.select(getNextQuery)) {
            if (rs.next()) {
                return rs.getInt("MINID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }

        return Integer.MAX_VALUE;
    }

    public static float calculateNewDeltaX(ElementDTO elementDTO, String nextCellId) {
        String getDeltaXQuery = "SELECT MAX(BOUND_BOX_X_TOP_RIGHT) AS MAX_X FROM " + TableNames.ELEMENT_TABLE + " " +
                "WHERE ID >= " + elementDTO.getId() + " AND ID < " + nextCellId + " " +
                "AND COLLAPSED IN (0, 2)";

        try (ResultSet rs = DatabaseUtil.select(getDeltaXQuery)) {
            if (rs.next()) {
                return rs.getFloat("MAX_X") - elementDTO.getBoundBoxXTopRight();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }

        return 0;
    }

    public static List<ElementDTO> getAllParentElementDTOs(ElementDTO elementDTO, String threadId) {
        List<ElementDTO> elementDTOs = new ArrayList<>();

        String getAllParentIDsQuery = "SELECT MAX(ID) AS ID " +
                "FROM " + TableNames.ELEMENT_TABLE + " AS E " +
                "WHERE E.ID < " + elementDTO.getId() + " " +
                "AND E.BOUND_BOX_X_COORDINATE < (SELECT BOUND_BOX_X_COORDINATE " +
                "FROM " + TableNames.ELEMENT_TABLE + " AS E1 " +
                "WHERE E1.ID = " + elementDTO.getId() + ") " +
                "AND EXISTS (SELECT * FROM " + TableNames.CALL_TRACE_TABLE + " AS CT " +
                "WHERE CT.ID = E.ID_ENTER_CALL_TRACE AND " +
                "CT.THREAD_ID = " + threadId + ")" +
                "AND E.PARENT_ID > 1 " +
                "AND E.COLLAPSED <> 0 " +
                "GROUP BY E.BOUND_BOX_X_COORDINATE " +
                "ORDER BY ID ASC ";

        try (ResultSet rs = DatabaseUtil.select(getAllParentIDsQuery)) {
            while (rs.next()) {
                elementDTOs.add(processElementDTO(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }

        return elementDTOs;
    }

    public static String getUpdateElementQueryAfterCollapse(double y, double delta, int nextCellId, int lastCellId) {
        return "UPDATE " + TableNames.ELEMENT_TABLE + " " +
                "SET bound_box_y_top_left = bound_box_y_top_left - " + delta + ", " +
                "bound_box_y_top_right = bound_box_y_top_right - " + delta + ", " +
                "bound_box_y_bottom_left = bound_box_y_bottom_left - " + delta + ", " +
                "bound_box_y_bottom_right = bound_box_y_bottom_right - " + delta + ", " +
                "bound_box_y_coordinate = bound_box_y_coordinate - " + delta + " " +
                "WHERE bound_box_y_coordinate >= " + y + " " +
                "AND ID >= " + nextCellId + " " +
                "AND ID <= " + lastCellId;

    }
}
