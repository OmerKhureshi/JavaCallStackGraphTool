package com.application.service.modules;

import com.application.db.DAO.DAOImplementation.*;
import com.application.db.DTO.EdgeDTO;
import com.application.db.DatabaseUtil;
import com.application.db.TableNames;
import com.application.db.model.Bookmark;
import com.application.fxgraph.ElementHelpers.EdgeElement;
import com.application.fxgraph.ElementHelpers.Element;
import com.application.fxgraph.cells.CircleCell;
import com.application.fxgraph.graph.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.BoundingBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ElementTreeModule {
    public static Element greatGrandParent;
    private Map<Integer, Element> threadMapToRoot;
    public ArrayList<Element> rootsList;
    Element grandParent, parent, cur;
    Map<Integer, Element> currentMap;
    Graph graph;
    Model model;
    private String currentThreadId = "0";

    private boolean showAllThreads = true;

    public int multiplierForVisibleViewPort = 3;
    public int multiplierForPreLoadedViewPort = 2;

    public ElementTreeModule() {
        Element.clearAutoIncrementId();
        greatGrandParent = new Element(null, -2);
        rootsList = new ArrayList<>();
        currentMap = new HashMap<>();
        threadMapToRoot = new LinkedHashMap<>();
    }

    /**
     * This method converts the string to an element tree.
     */
    public void StringToElementList(List<String> line, int fkCallTrace) {
        String msg = line.get(3);
        Integer threadId = Integer.valueOf(line.get(2));

        switch (msg.toUpperCase()) {
            case "WAIT-ENTER":
            case "NOTIFY-ENTER":
            case "NOTIFYALL-ENTER":
            case "ENTER":
                if (!threadMapToRoot.containsKey(threadId)) {
                    // new thread
                    parent = null;
                } else if (currentMap.containsKey(threadId)) {
                    parent = currentMap.get(threadId);
                    // parent = cur;
                }
                cur = new Element(parent, fkCallTrace);
                currentMap.put(threadId, cur);
                break;

            case "WAIT-EXIT":
            case "NOTIFY-EXIT":
            case "NOTIFYALL-EXIT":
            case "EXIT":
                cur = currentMap.get(threadId);
                cur.setFkExitCallTrace(fkCallTrace);
                cur = cur.getParent();
                currentMap.put(threadId, cur);
                // cur = cur.getParent();
                break;

            default:
                IllegalStateException up = new IllegalStateException("EventType should be either ENTER OR EXIT. This line caused exception: " + line);
                throw up;  // Yuck! Not having any of that :(
        }

        if (parent == null &&
                (!msg.equalsIgnoreCase("EXIT") &&
                        !msg.equalsIgnoreCase("WAIT-EXIT") &&
                        !msg.equalsIgnoreCase("NOTIFY-EXIT") &&
                        !msg.equalsIgnoreCase("NOTIFYALL-EXIT"))) {
            if (!threadMapToRoot.containsKey(threadId)) {
                grandParent = new Element(greatGrandParent, -1);
                grandParent.setChildren(new ArrayList<>(Arrays.asList(cur)));
                cur.setParent(grandParent);
                threadMapToRoot.put(threadId, grandParent);
                /*defaultInitialize(grandParent);
                ElementDAOImpl.insert(grandParent);*/
            } else {
                Element grandparent = threadMapToRoot.get(threadId);   // Get grandParent root for the current threadId
                grandparent.setChildren(new ArrayList<>(Collections.singletonList(cur)));       // set the current element as the child of the grandParent element.
                cur.setParent(grandparent);
            }
        }

        /*if ( msg.equalsIgnoreCase("ENTER")) {
            defaultInitialize(cur);
            ElementDAOImpl.insert(cur);
        }*/
    }

    private void defaultInitialize(Element element) {
        cur.setLeafCount(-1);
        cur.setLevelCount(-1);
        cur.getBoundBox().xTopLeft = -1;
        cur.getBoundBox().yTopLeft = -1;
        cur.getBoundBox().xTopRight = -1;
        cur.getBoundBox().yTopRight = -1;
        cur.getBoundBox().xBottomRight = -1;
        cur.getBoundBox().yBottomRight = -1;
        cur.getBoundBox().xBottomLeft = -1;
        cur.getBoundBox().yBottomLeft = -1;
    }

    public Map<Integer, Element> getThreadMapToRoot() {
        return threadMapToRoot;
    }


    /**
     * Calculates the Element properties on all direct and indirect children of current element.
     * Ensure that the sub tree is fully constructed before invoking this method.
     */
    public void calculateElementProperties() {
        greatGrandParent.calculateLeafCount();
        greatGrandParent.calculateLevelCount(0);

        greatGrandParent.getChildren().stream().forEach(element -> {
            element.setBoundBoxOnAll(element);
        });
        // greatGrandParent.setBoundBoxOnAll(greatGrandParent);

    }

    public void recursivelyInsertElementsIntoDB(Element root) {
        if (root == null)
            return;
        ElementDAOImpl.insert(root);
        ElementToChildDAOImpl.insert(
                root.getParent() == null ? -1 : root.getParent().getElementId(),
                root.getElementId());
        // // Create and insert Edges.
        // Edge edge = new Edge(root.getParent(), root);
        // edge.setStartX();

        if (root.getChildren() != null)
            root.getChildren().stream().forEachOrdered(this::recursivelyInsertElementsIntoDB);
    }

    public void recursivelyInsertEdgeElementsIntoDB(Element root) {
        if (root == null)
            return;

        if (root.getChildren() != null)
            root.getChildren().stream().forEachOrdered(targetElement -> {
                EdgeElement edgeElement = new EdgeElement(root, targetElement);
                edgeElement.calculateEndPoints();
                EdgeDAOImpl.insert(edgeElement);

                recursivelyInsertEdgeElementsIntoDB(targetElement);
            });
    }

    public void recursivelyInsertEdgeElementsIntoDB(Element root, List<EdgeElement> edgeElementList) {
        if (root == null)
            return;

        if (root.getChildren() != null)
            root.getChildren().stream().forEachOrdered(targetElement -> {
                EdgeElement edgeElement = new EdgeElement(root, targetElement);
                edgeElement.calculateEndPoints();
                // EdgeDAOImpl.insert(edgeElement);

                edgeElementList.add(edgeElement);

                recursivelyInsertEdgeElementsIntoDB(targetElement);
            });
    }


    /**
     * Loads circle on UI if they are not present when they should be.
     */
    public void loadUIComponentsInsideVisibleViewPort(Graph graph) {
        // System.out.println("ElementTreeModule:loadUIComponentsInsideVisibleViewPort: Method started");
        this.graph = graph;
        this.model = graph.getModel();


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

        addUIComponents();
        // inBackground(true);
        // System.out.println("ElementTreeModule::loadUIComponentsInsideVisibleViewPort: after drawing, contents of cellLayer: ");
        // System.out.println("mapCircleCellsOnUI: size: " + graph.getModel().getCircleCellsOnUI().size());
        // graph.getModel().getCircleCellsOnUI().keySet().stream().sorted().forEach(id -> {
        //     System.out.print(id + ", ");
        // });
    }

    public void addUIComponents() {
        // Add circle cells
        addCircleCells();
        // Add bookmarks to bookmarked cells
        addBookmarks();
        // Add edges
        addEdges();
        // Add highlights
        addHighlights();
        graph.updateCellLayer();
    }

    public void forceUiRendering() {
        addUIComponents();
        // inBackground(false);
    }

    public void inBackground(boolean removeUIComponents) {
        System.out.println("ElementTreeModule.inBackground: started");
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Add circle cells
                addCircleCells();
                // Add bookmarks to bookmarked cells
                addBookmarks();
                // Add edges
                addEdges();
                // Add highlights
                addHighlights();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                // graph.updateCellLayer();
                if (removeUIComponents) {
                    inBackgroundRemoveUIComponents();
                }
                System.out.println("ElementTreeModule.succeeded:");
            }
        };

        new Thread(task).start();

        System.out.println("ElementTreeModule.inBackground: ended");
    }

    public void inBackgroundRemoveUIComponents() {
        System.out.println("ElementTreeModule.inBackgroundRemoveUIComponents: started task method");
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                removeUIComponentsFromInvisibleViewPort(graph);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                graph.updateCellLayer();
                System.out.println("ElementTreeModule.succeeded");
            }
        };
        System.out.println("ElementTreeModule.inBackgroundRemoveUIComponents: ended task method");

        new Thread(task).start();
    }


    public void clearAndUpdateCellLayer() {
        // remove all circles cells from UI
        graph.getCellLayer().getChildren().removeAll(model.getCircleCellsOnUI().values());
        model.getCircleCellsOnUI().clear();

        // remove all Edges from UI
        graph.getCellLayer().getChildren().removeAll(model.getEdgesOnUI().values());
        model.getEdgesOnUI().clear();

        // remove all Highlights from UI
        graph.getCellLayer().getChildren().removeAll(model.getHighlightsOnUI().values());
        model.getHighlightsOnUI().clear();

        forceUiRendering();
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


    private void addCircleCells() {
        Map<String, CircleCell> mapCircleCellsOnUI = model.getCircleCellsOnUI();

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
                "WHERE CT.THREAD_ID = " + currentThreadId +
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

                if (methodId == 0) {
                    methodName = rs.getString("message");
                } else {
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

                /*
                 * collapsed - actions
                 *     0     - Show cell on UI
                 *     1     - parent of this cell was minimized. Don't show on UI
                 *     2     - this cell was minimized. Show on UI.
                 *     3     - parent of this cell was minimized. this cell was minimized. Don't expand this cell's children. Don't show on UI.
                 */

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

    public void clearUI() {

        // CellLayer cellLayer = (CellLayer) graph.getCellLayer();
        // cellLayer.getChildren().clear();
        // synchronized (Main.getLock()) {

        if (graph == null) {
            System.out.println("------> graph is null.");

        }
        // System.out.println("ElementTreeModule::clearUI");
        graph.clearCellLayer();
        // System.out.println("ElementTreeModule::clearUI: getHighlightsOnUI.size() " + graph.getModel().getHighlightsOnUI().size());

        if (graph.getModel() != null) {
            graph.getModel().clearMaps();

            // System.out.println("ElementTreeModule::clearUI: getHighlightsOnUI.size() " + graph.getModel().getHighlightsOnUI().size());
        }

        // }

        // if (model != null && model.getCircleCellsOnUI() != null)
        //     model.getCircleCellsOnUI().clear();
        //
        // if (model != null && model.getEdgesOnUI() != null)
        //     model.getEdgesOnUI().clear();

        // Get the width for placeholder line.
        String SQLMaxLevelCount = "select max(LEVEL_COUNT) from ELEMENT " +
                "where ID_ENTER_CALL_TRACE in " +
                "(SELECT  CALL_TRACE.ID from CALL_TRACE where THREAD_ID  = " + currentThreadId + ")";

        int width = 0;
        ResultSet rs = DatabaseUtil.select(SQLMaxLevelCount);
        try {
            if (rs.next()) {
                width = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        // Get the height for placeholder line.
        String SQLMaxLeafCount = "select LEAF_COUNT from ELEMENT " +
                "where LEVEL_COUNT = 1 AND ID = " +
                "(SELECT PARENT_ID from ELEMENT_TO_CHILD " +
                "where CHILD_ID = " +
                "(SELECT id from ELEMENT " +
                "where ID_ENTER_CALL_TRACE = " +
                "(SELECT  min(CALL_TRACE.ID) from CALL_TRACE " +
                "where THREAD_ID  = " + currentThreadId + ")))";

        int height = 0;
        rs = DatabaseUtil.select(SQLMaxLeafCount);
        try {
            if (rs.next()) {
                height = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Graph.drawPlaceHolderLines(height, width);

        // System.out.println("ElementTreeModule::clearUI: END");
    }

    public void setCurrentThreadId(String currentThreadId) {
        this.currentThreadId = currentThreadId;
    }

    public boolean isShowAllThreads() {
        return showAllThreads;
    }

    public void setShowAllThreads(boolean showAllThreads) {
        this.showAllThreads = showAllThreads;
    }

    // Region where UI components are loaded.
    private static BoundingBox activeRegion;

    // Trigger UI components to be reloaded when visible viewport is outside this region. triggerRegion < activeRegion
    private static BoundingBox triggerRegion;

    static boolean firstLoad = true;

    public boolean isUIDrawingRequired(BoundingBox viewPort) {
        // System.out.println("ElementTreeModule::UiUpdateRequired:");

        if (firstLoad) {
            firstLoad = false;
            return true;
        }

        if (activeRegion == null)
            setActiveRegion(viewPort);

        if (triggerRegion == null)
            setTriggerRegion(viewPort);

        if (!triggerRegion.contains(viewPort)) {
            setActiveRegion(viewPort);
            setTriggerRegion(viewPort);
            return true;
        }

        if (graph.getModel().uiUpdateRequired) {
            // System.out.println("ElementTreeModule::UiUpdateRequired: passed true");
            return true;
        }

        return false;
    }

    private void setActiveRegion(BoundingBox viewPort) {
        this.activeRegion = new BoundingBox(
                viewPort.getMinX() - viewPort.getWidth() * 3,
                viewPort.getMinY() - viewPort.getHeight() * 3,
                viewPort.getWidth() * 7,
                viewPort.getHeight() * 7
        );

        // System.out.println();
        // System.out.println("------------- New active region -------------");
        // System.out.println("Viewport: " + viewPort);
        // System.out.println("activeRegion: " + activeRegion);
        // System.out.println("triggerRegion: " + triggerRegion);
        // System.out.println("------------------");
    }

    private void setTriggerRegion(BoundingBox viewPort) {
        triggerRegion = new BoundingBox(
                activeRegion.getMinX() + viewPort.getWidth(),
                activeRegion.getMinY() + viewPort.getHeight(),
                viewPort.getWidth() * 5,
                viewPort.getHeight() * 5
        );

        // System.out.println();
        // System.out.println("------------- New Triggering region -------------");
        // System.out.println("Viewport: " + viewPort);
        // System.out.println("activeRegion: " + activeRegion);
        // System.out.println("triggerRegion: " + triggerRegion);
        // System.out.println("------------------");
    }

    public static void resetRegions() {
        activeRegion = null;
        triggerRegion = null;
        firstLoad = true;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
}

