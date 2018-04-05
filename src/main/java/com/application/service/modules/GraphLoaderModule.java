package com.application.service.modules;

import com.application.controller.CenterLayoutController;
import com.application.db.DAO.DAOImplementation.CallTraceDAOImpl;
import com.application.db.DAO.DAOImplementation.ElementDAOImpl;
import com.application.db.DAO.DAOImplementation.HighlightDAOImpl;
import com.application.db.DAO.DAOImplementation.MethodDefnDAOImpl;
import com.application.db.DTO.ElementDTO;
import com.application.db.DatabaseUtil;
import com.application.db.TableNames;
import com.application.db.model.Bookmark;
import com.application.fxgraph.cells.CircleCell;
import com.application.fxgraph.graph.*;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class GraphLoaderModule {

    private int currentSelectedThread = 0;
    private CenterLayoutController centerLayoutController;

    public int getCurrentSelectedThread() {
        return CallTraceDAOImpl.getCurrentSelectedThread();
    }

    public void setCurrentSelectedThread(int currentSelectedThread) {
        CallTraceDAOImpl.setCurrentSelectedThread(currentSelectedThread);
    }

    public int computePlaceHolderWidth(String threadId) {
        return ElementDAOImpl.getMaxLeafCount(threadId);
    }

    public int computePlaceHolderHeight(String threadId) {
         return ElementDAOImpl.getMaxLevelCount(threadId);
    }

/*
    public void update() {
        loadUIComponentsInsideVisibleViewPort();
        removeUIComponentsFromInvisibleViewPort();
    }


    public void loadUIComponentsInsideVisibleViewPort(Graph graph) {
        // System.out.println("ElementTreeModule:loadUIComponentsInsideVisibleViewPort: Method started");

        // BoundingBox viewPortDims = graph.getViewPortDims();
        // if (!isUIDrawingRequired(viewPortDims)) {
        //     // System.out.println("ElementTreeModule:loadUIComponentsInsideVisibleViewPort: UI redrawing not required.");
        //     return;
        // }


        // System.out.println("ElementTreeModule::loadUIComponentsInsideVisibleViewPort: before drawing, existing contents of cellLayer: ");
        // System.out.println("mapCircleCellsOnUI: size: " + graph.getModel().getCircleCellsOnUI().size());
        // graph.getModel().getCircleCellsOnUI().keySet().stream().sorted().forEach(id -> {
        //     System.out.print(id + ", ");
        // });

        // System.out.println("mapEdgesOnUI: ");
        // graph.getModel().getEdgesOnUI().keySet().stream().sorted().forEach(id -> {
        //     System.out.print(id + ", ");
        // });

        // System.out.println("highlightsOnUI: elementId : ");
        // graph.getModel().getHighlightsOnUI().values().stream().sorted().forEach(rect -> {
        //     System.out.print(rect.getElementId()+ ", ");
        // });
        addComponents();
        // inBackground(true);
        // System.out.println("ElementTreeModule::loadUIComponentsInsideVisibleViewPort: after drawing, contents of cellLayer: ");
        // System.out.println("mapCircleCellsOnUI: size: " + graph.getModel().getCircleCellsOnUI().size());
        // graph.getModel().getCircleCellsOnUI().keySet().stream().sorted().forEach(id -> {
        //     System.out.print(id + ", ");
        // });
    }

 public void addComponents() {
        addCircleCells();
        addBookmarks();
        addEdges();
        addHighlights();
        graph.updateCellLayer();
    }*/

    public List<ElementDTO> addCircleCellsNew(BoundingBox viewPort) {
        return ElementDAOImpl.getElementDTOs(viewPort);
        /*
            C 1. get view port with buffer region
            D 2. get element rows that go in the view port
            C 3. create circle cells from element rows
            C 4. resolve label for cell.
            C 4. populate circle cells on ui
        */

    }
/*

    private void addCircleCells() {
        Map<String, CircleCell> mapCircleCellsOnUI = centerLayoutController.getCircleCellsOnUI();

        // Calculate the expanded region around viewport that needs to be loaded.
        BoundingBox viewPortDims = graph.getViewPortDims();

        double viewPortMinX = viewPortDims.getMinX();
        double viewPortMaxX = viewPortDims.getMaxX();
        double viewPortMinY = viewPortDims.getMinY();
        double viewPortMaxY = viewPortDims.getMaxY();
        double widthOffset = viewPortDims.getWidth() * multiplierForVisibleViewPort;
        double heightOffset = viewPortDims.getHeight() * multiplierForVisibleViewPort;


        // Get element properties for those elements that are inside the expanded region calculated above.
        String sql = "SELECT E.ID AS EID, parent_id, collapsed, bound_box_x_coordinate, bound_box_y_coordinate, message, id_enter_call_trace, method_id " +
                "FROM " + TableNames.CALL_TRACE_TABLE + " AS CT JOIN " + TableNames.ELEMENT_TABLE + " AS E ON CT.ID = E.ID_ENTER_CALL_TRACE " +
                "WHERE CT.THREAD_ID = " + CallTraceDAOImpl.getCurrentSelectedThread() +
                " AND E.bound_box_x_coordinate > " + (viewPortMinX - widthOffset) +
                " AND E.bound_box_x_coordinate < " + (viewPortMaxX + widthOffset) +
                " AND E.bound_box_y_coordinate > " + (viewPortMinY - heightOffset) +
                " AND E.bound_box_y_coordinate < " + (viewPortMaxY + heightOffset) +
                " AND E.LEVEL_COUNT > 1" +
                " AND (E.COLLAPSED = 0" +
                " OR E.COLLAPSED = 2)";

        // System.out.println("ElementTreeModule::addCircleCells: sql: " + sql);
        CircleCell curCircleCell;
        CircleCell parentCircleCell;


        try (ResultSet rs = DatabaseUtil.select(sql)) {
            while (rs.next()) {

                String id = String.valueOf(rs.getInt("EID"));
                String parentId = String.valueOf(rs.getInt("parent_id"));
                int collapsed = rs.getInt("collapsed");
                float xCoordinate = rs.getFloat("bound_box_x_coordinate");
                float yCoordinate = rs.getFloat("bound_box_y_coordinate");
                int idEnterCallTrace = rs.getInt("id_enter_call_trace");
                int methodId = rs.getInt("method_id");
                String methodName = "";

                // If method is wait, notify or notify all, get the message (wait-enter, wait-exit etc).
                if (methodId == 0) {
                    methodName = rs.getString("message");
                }
                // else get method name from Method def table.
                else {
                    try (ResultSet rsMethod = MethodDefnDAOImpl.selectWhere("id = " + methodId)) {
                        while (rsMethod.next()) {
                            methodName = rsMethod.getString("method_name");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                String eventType = "";
                eventType = rs.getString("message");

                // Collapsed value -> description
                // 0               -> visible     AND  uncollapsed
                // 2               -> visible     AND  collapsed
                // >2              -> not visible AND  collapsed
                // <0              -> not visible AND  collapsed

                */
/*
                 * collapsed - actions
                 *     0     - Show cell on UI
                 *     1     - parent of this cell was minimized. Don't show on UI
                 *     2     - this cell was minimized. Show on UI.
                 *     3     - parent of this cell was minimized. this cell was minimized. Don't expand this cell's children. Don't show on UI.
                 *//*


                // Add circle cell to model and UI only if they are not already present on UI and if collapsed value is 0 or 2
                if (!mapCircleCellsOnUI.containsKey(id) && (collapsed == 0 || collapsed == 2)) {
                    // System.out.println("ElementTreeModule::addCircleCells: adding new cells to UI: cell id: " + id);
                    curCircleCell = new CircleCell(id, xCoordinate, yCoordinate);
                    curCircleCell.setMethodNameLabel(methodName);
                    model.addCell(curCircleCell);
                    // SimplifiedElement ele = new SimplifiedElement(id, methodName);
                    // model.addSimplifiedElementToMap(ele);

                    String label = "";
                    switch (eventType.toUpperCase()) {
                        case "WAIT-ENTER":
                            label = "WAIT";
                            break;
                        case "NOTIFY-ENTER":
                            label = "NOTIFY";
                            break;
                        case "NOTIFYALL-ENTER":
                            label = "NOTIFY\nALL";
                            break;
                    }

                    curCircleCell.setLabel(label);

                    // Add parent circle cell if not already added earlier.
                    parentCircleCell = mapCircleCellsOnUI.get(parentId);
                    if (!mapCircleCellsOnUI.containsKey(parentId)) {
                        try (ResultSet rsTemp = ElementDAOImpl.selectWhere("id = " + parentId)) {
                            if (rsTemp.next() && rsTemp.getInt("LEVEL_COUNT") > 1) {
                                float xCoordinateTemp = rsTemp.getFloat("bound_box_x_coordinate");
                                float yCoordinateTemp = rsTemp.getFloat("bound_box_y_coordinate");
                                parentCircleCell = new CircleCell(parentId, xCoordinateTemp, yCoordinateTemp);
                                // System.out.println("ElementTreeModule::addCircleCells: adding new parent cells to UI: cell id: " + parentId);
                                model.addCell(parentCircleCell);
                            }
                        }
                    }


                    // // 1 -> 2 -> 3
                    // String pId = String.valueOf(rs.getInt("PARENT_ID"));
                    // SimplifiedElement childSE = ele, parentSE;
                    // try {
                    //     while (Integer.valueOf(pId) != -1) {
                    //         String q = "SELECT * FROM " + TableNames.ELEMENT_TABLE + " AS E " +
                    //                 "JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT " +
                    //                 "ON E.ID_ENTER_CALL_TRACE = CT.ID " +
                    //                 "WHERE E.ID = " + pId;
                    //         ResultSet pRS = DatabaseUtil.select(q);
                    //         if (pRS.next()) {
                    //             String mName = pRS.getString("MESSAGE");
                    //             parentSE = new SimplifiedElement(pId, mName);
                    //             childSE.setParentElement(parentSE);
                    //             pId = String.valueOf(pRS.getInt("PARENT_ID"));
                    //             childSE = parentSE;
                    //         } else {
                    //             pId = "-1";
                    //         }
                    //     }
                    // } catch (SQLException e) {
                    //     e.printStackTrace();
                    // }
                }
                // else {
                //     curCircleCell = mapCircleCellsOnUI.get(id);
                //     parentCircleCell = mapCircleCellsOnUI.get(parentId);
                // }
                // if (curCircleCell != null && !model.getEdgesOnUI().containsKey(curCircleCell.getCellId()) && parentCircleCell != null) {
                //     Edge curEdge = new Edge(parentCircleCell, curCircleCell);
                //     model.addEdge(curEdge);
                // }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addHighlights() {
        // System.out.println("ElementTreeModule::addHighlights: method started");

        Map<Integer, RectangleCell> highlightsOnUI = model.getHighlightsOnUI();

        BoundingBox viewPortDims = graph.getViewPortDims();

        double viewPortMinX = viewPortDims.getMinX();
        double viewPortMaxX = viewPortDims.getMaxX();
        double viewPortMinY = viewPortDims.getMinY();
        double viewPortMaxY = viewPortDims.getMaxY();
        double widthOffset = viewPortDims.getWidth() * multiplierForVisibleViewPort;
        double heightOffset = viewPortDims.getHeight() * multiplierForVisibleViewPort;

        // Query to fetches highlight boxes that are contained within the bounds of outermost preload box.

        String sql = (viewPortMinX - widthOffset) + " <= (start_x + width) " + " " +
                "AND " + (viewPortMaxX + widthOffset) + " >= start_x " +
                "AND " + (viewPortMinY - heightOffset) + " <= (start_y + height) " + " " +
                "AND " + (viewPortMaxY + heightOffset) + " >= start_Y " +
                "AND thread_id = " + currentThreadId + " " +
                "AND COLLAPSED = 0";

        ResultSet rs = HighlightDAOImpl.selectWhere(sql);

        try {
            while (rs.next()) {
                int id = rs.getInt("ID");
                int elementId = rs.getInt("ELEMENT_ID");
                float startX = rs.getFloat("START_X");
                float startY = rs.getFloat("START_Y");
                float width = rs.getFloat("WIDTH");
                float height = rs.getFloat("HEIGHT");
                String color = rs.getString("COLOR");

                // If the rectangle highlight is not on UI then create a new rectangle and show on UI.
                if (!highlightsOnUI.containsKey(id)) {
                    // System.out.println("Drawing rectangle: " + id + " elementId: " + elementId);
                    // Rectangle rectangle = new Rectangle(startX, startY, width, height);
                    // rectangle.setFill(Color.web(color));
                    // rectangle.setArcHeight(20);
                    // rectangle.setArcWidth(20);

                    RectangleCell rect = new RectangleCell(id, elementId, startX, startY, width, height);
                    rect.setColor(color);
                    rect.setArcHeight(20);
                    rect.setArcWidth(20);

                    // model.addHighlight(id, rectangle);
                    model.addHighlight(id, rect);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // System.out.println("ElementTreeModule::addHighlights: method ended");
    }

    private void addBarMarks() {
        graph.getModel().updateBookmarkMap();
        Map<String, Rectangle> barMarkMap = graph.getModel().getBarMarkMap();

    }

    private void addBookmarks() {
        Map<String, Bookmark> bookmarkMap = graph.getModel().updateAndGetBookmarkMap();
        Map<String, CircleCell> mapCircleCellsOnUI = model.getCircleCellsOnUI();

        bookmarkMap.forEach((cellId, bookmark) -> {
            if (mapCircleCellsOnUI.containsKey(cellId)) {
                mapCircleCellsOnUI.get(cellId).bookmarkCell(bookmark.getColor());
            }
        });
    }


    private void addEdges() {
        BoundingBox viewPortDims = graph.getViewPortDims();
        double viewPortMinX = viewPortDims.getMinX();
        double viewPortMaxX = viewPortDims.getMaxX();
        double viewPortMinY = viewPortDims.getMinY();
        double viewPortMaxY = viewPortDims.getMaxY();
        double widthOffset = viewPortDims.getWidth() * multiplierForVisibleViewPort;
        double heightOffset = viewPortDims.getHeight() * multiplierForVisibleViewPort;

        String sql = "SELECT * FROM EDGE_ELEMENT " +
                "INNER JOIN ELEMENT ON FK_SOURCE_ELEMENT_ID = ELEMENT.ID " +
                "INNER JOIN CALL_TRACE ON ELEMENT.ID_ENTER_CALL_TRACE = CALL_TRACE.ID " +
                "WHERE CALL_TRACE.THREAD_ID = " + currentThreadId + " ";

        String commonWhereClausForEdges = "AND EDGE_ELEMENT.collapsed = " + CollapseType.EDGE_VISIBLE + " AND " + "end_x >= " + (viewPortMinX - widthOffset) + " AND start_x <= " + (viewPortMaxX + widthOffset);
        String whereClauseForUpwardEdges = " AND end_Y >= " + (viewPortMinY - heightOffset) + " AND start_y <= " + (viewPortMaxY + heightOffset);
        String whereClauseForDownwardEdges = " AND start_y >= " + (viewPortMinY - heightOffset) + " AND end_Y <= " + (viewPortMaxY + heightOffset);

        // System.out.println("ElementTreeModule::loadUIComponentsInsideVisibleViewPort: sql: " + sql + commonWhereClausForEdges + whereClauseForUpwardEdges);
        try (ResultSet rsUpEdges = DatabaseUtil.select(sql + commonWhereClausForEdges + whereClauseForUpwardEdges)) {
            getEdgesFromResultSet(rsUpEdges);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // System.out.println("ElementTreeModule::loadUIComponentsInsideVisibleViewPort: sql: " + sql + commonWhereClausForEdges + whereClauseForDownwardEdges);
        try (ResultSet rsDownEdges = DatabaseUtil.select(sql + commonWhereClausForEdges + whereClauseForDownwardEdges)) {
            getEdgesFromResultSet(rsDownEdges);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void getEdgesFromResultSet(ResultSet rs) {
        Edge curEdge;
        try {
            while (rs.next()) {
                String targetEdgeId = String.valueOf(rs.getInt("fk_target_element_id"));
                double startX = rs.getFloat("start_x");
                double endX = rs.getFloat("end_x");
                double startY = rs.getFloat("start_y");
                double endY = rs.getFloat("end_y");
                // System.out.println("ElementTreeModule::getEdgesFromResultSet: adding edge with target id: " + targetEdgeId);
                curEdge = new Edge(targetEdgeId, startX, endX, startY, endY);
                model.addEdge(curEdge);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeCircleCells(BoundingBox preloadBox) {
        CellLayer cellLayer = (CellLayer) graph.getCellLayer();

        Map<String, CircleCell> mapCircleCellsOnUI = model.getCircleCellsOnUI();
        List<String> removeCircleCells = new ArrayList<>();
        List<CircleCell> listCircleCellsOnUI = model.getListCircleCellsOnUI();


        Iterator i = mapCircleCellsOnUI.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, CircleCell> entry = (Map.Entry) i.next();
            CircleCell cell = entry.getValue();
            if (!preloadBox.contains(cell.getLayoutX(), cell.getLayoutY())) {
                removeCircleCells.add(cell.getCellId());

                // model.removeSimplifiedElementFromMap(cell.getCellId());
            }
        }

        removeCircleCells.forEach(cellId -> {
            CircleCell circleCell = mapCircleCellsOnUI.get(cellId);
            Platform.runLater(() -> cellLayer.getChildren().remove(circleCell));
            mapCircleCellsOnUI.remove(cellId);
            listCircleCellsOnUI.remove(circleCell);
        });
    }

    private void removeEdges(BoundingBox preloadBox) {
        CellLayer cellLayer = (CellLayer) graph.getCellLayer();

        Map<String, Edge> mapEdgesOnUI = model.getEdgesOnUI();

        List<String> removeEdges = new ArrayList<>();
        List<Edge> listEdgesOnUI = model.getListEdgesOnUI();

        Iterator j = mapEdgesOnUI.entrySet().iterator();

        while (j.hasNext()) {
            Map.Entry<String, Edge> entry = (Map.Entry) j.next();
            Edge edge = entry.getValue();
            Line line = (Line) edge.getChildren().get(0);
            BoundingBox lineBB = new BoundingBox(
                    line.getStartX(),
                    Math.min(line.getStartY(), line.getEndY()),
                    Math.abs(line.getEndX() - line.getStartX()),
                    Math.abs(line.getEndY() - line.getStartY()));
            // if (!preloadBox.contains(line.getEndX(), line.getEndY())) {
            //     removeEdges.add(edge.getEdgeId());
            // }
            if (!preloadBox.intersects(lineBB)) {
                removeEdges.add(edge.getEdgeId());
            }
        }

        removeEdges.forEach(edgeId -> {
            Edge edge = mapEdgesOnUI.get(edgeId);
            Platform.runLater(() -> cellLayer.getChildren().remove(edge));
            mapEdgesOnUI.remove(edgeId);
            listEdgesOnUI.remove(edge);
        });

        // clearUI();
    }

    private void removeHighlights(BoundingBox preloadBox) {
        // System.out.println("ElementTreeModule::removeHighlights: method started");
        CellLayer cellLayer = (CellLayer) graph.getCellLayer();

        // This is the global HashMap that stores rectangle highlights currently on the UI.
        Map<Integer, RectangleCell> highlightsOnUI = model.getHighlightsOnUI();

        // Temporary list to aid in removal of HashMap elements.
        List<Integer> removeHighlights = new ArrayList<>();

        Iterator j = highlightsOnUI.entrySet().iterator();

        while (j.hasNext()) {
            Map.Entry<Integer, RectangleCell> entry = (Map.Entry) j.next();
            RectangleCell rectangle = entry.getValue();
            int rectId = entry.getKey();

            // if (!preloadBox.intersects(rectangle.getBoundsInLocal())) {
            if (!preloadBox.intersects(rectangle.getBoundsInParent())) {
                // ------------------   FOR DEBUGGING     ------------------
                // System.out.println("ElementTreeModule::removeHighlights: adding to removeHighlights because of 1: " + rectId + " elementid: " + rectangle.getElementId());
                // System.out.println("  minY: " + rectangle.getBoundsInLocal().getMinY() + " " + rectangle.getBoundsInLocal().getMinY() + " " + rectangle.getLayoutY());
                // System.out.println("  maxY: " + rectangle.getBoundsInLocal().getMaxY() + " " + rectangle.getBoundsInLocal().getMaxY());
                // System.out.println("  height: " + rectangle.getBoundsInLocal().getHeight() + " " + rectangle.getBoundsInLocal().getHeight());
                // System.out.println("---------------------------------------------------");
                // System.out.println("  minY: " + rectangle.getRectangle().getBoundsInLocal().getMinY() + " " + rectangle.getRectangle().getBoundsInLocal().getMinY() + " " + rectangle.getRectangle().getLayoutY());
                // System.out.println("  maxY: " + rectangle.getRectangle().getBoundsInLocal().getMaxY() + " " + rectangle.getRectangle().getBoundsInLocal().getMaxY());
                // System.out.println("  height: " + rectangle.getRectangle().getBoundsInLocal().getHeight() + " " + rectangle.getRectangle().getBoundsInLocal().getHeight());
                // System.out.println("---------------------------------------------------");
                // System.out.println("preloadbox bounds: " + preloadBox);
                // System.out.println("rectangleCell: boundsinlocal: " + rectangle.getBoundsInLocal());
                // System.out.println("rectangleCell: boundsInParent: " + rectangle.getBoundsInParent());
                // System.out.println("rectangleCell: layoutBounds: " + rectangle.getLayoutBounds());
                // System.out.println("rectangleCell.getRectangle: boundsinlocal: " + rectangle.getRectangle().getBoundsInLocal());
                // System.out.println("rectangleCell.getRectangle: boundsinparent: " + rectangle.getRectangle().getBoundsInParent());
                // System.out.println("rectangleCell.getRectangle: layoutBounds: " + rectangle.getRectangle().getLayoutBounds());
                // System.out.println("---------------------------------------------------");

                // Removes those highlights that are not visible.
                removeHighlights.add(rectId);

            } else {
                // Removes those highlights that are not in HIGHLIGHT_ELEMENT Table.
                String deleteQuery = "SELECT COUNT(*) AS COUNT FROM HIGHLIGHT_ELEMENT WHERE ID = " + rectId;
                ResultSet rs = DatabaseUtil.select(deleteQuery);
                try {
                    while (rs.next()) {
                        if (rs.getInt("COUNT") == 0) {
                            if (!removeHighlights.contains(rectId)) {
                                removeHighlights.add(rectId);
                                // System.out.println("ElementTreeModule::removeHighlights: adding to removeHighlights because of 2: " + rectId + " elementId: " + rs.getInt("element_id"));
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }


        // Removing elements from HashMap is a two step process, this is to avoid concurrent modification exception
        // Remove rectangle highlights from UI and HashMap.
        removeHighlights.forEach(rectId -> {
            RectangleCell rectangle = highlightsOnUI.get(rectId);
            int elementId = rectangle.getElementId();
            // System.out.println("ElementTreeModule::removeHighlights: removing rectangle: " + rectId + " elementId: " + elementId);
            Platform.runLater(() -> cellLayer.getChildren().remove(rectangle));
            highlightsOnUI.remove(rectId);
        });
        // System.out.println("ElementTreeModule::removeHighlights: method ended");
    }

    public void removeUIComponentsFromInvisibleViewPort(Graph graph) {
        // System.out.println("ElementTreeModule::removeUIComponentsFromInvisibleViewPort: method started");
        this.graph = graph;
        this.model = graph.getModel();

        BoundingBox viewPortDims = graph.getViewPortDims();

        // if (!isUIDrawingRequired(viewPortDims)) {
        //     return;
        // }

        double minX = viewPortDims.getMinX();
        double minY = viewPortDims.getMinY();
        double width = viewPortDims.getWidth();
        double height = viewPortDims.getHeight();
        double widthOffset = viewPortDims.getWidth() * 3;
        double heightOffset = viewPortDims.getHeight() * 3;

        BoundingBox preloadBox = new BoundingBox(
                minX - widthOffset,
                minY - heightOffset,
                width + widthOffset * 6,
                height + heightOffset * 6);
//        BoundingBox preloadBox = new BoundingBox(minX , minY, width, height);

        removeCircleCells(preloadBox);
        removeEdges(preloadBox);
        removeHighlights(preloadBox);

        graph.updateCellLayer();

    }
*/

    public void inject(CenterLayoutController centerLayoutController) {
        this.centerLayoutController = centerLayoutController;
    }
}
