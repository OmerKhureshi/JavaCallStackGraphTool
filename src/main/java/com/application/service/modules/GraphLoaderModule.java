package com.application.service.modules;

import com.application.db.DAO.DAOImplementation.CallTraceDAOImpl;
import com.application.db.DAO.DAOImplementation.EdgeDAOImpl;
import com.application.db.DAO.DAOImplementation.ElementDAOImpl;
import com.application.db.DTO.EdgeDTO;
import com.application.db.DTO.ElementDTO;
import javafx.geometry.BoundingBox;

import java.util.List;

@SuppressWarnings("ALL")
public class GraphLoaderModule {

    private int currentSelectedThread = 0;

    public int getCurrentSelectedThread() {
        return CallTraceDAOImpl.getCurrentSelectedThread();
    }

    public void setCurrentSelectedThread(int currentSelectedThread) {
        CallTraceDAOImpl.setCurrentSelectedThread(currentSelectedThread);
    }

    public int computePlaceHolderWidth(String threadId) {
        return ElementDAOImpl.getMaxLevelCount(threadId);
    }

    public int computePlaceHolderHeight(String threadId) {
        return ElementDAOImpl.getMaxLeafCount(threadId);
    }


    // public void updateIfNeeded() {
    //     loadUIComponentsInsideVisibleViewPort();
    //     removeUIComponentsFromInvisibleViewPort();
    // }


    // public void loadUIComponentsInsideVisibleViewPort(Graph graph) {
    //     // System.out.println("ElementTreeModule:loadUIComponentsInsideVisibleViewPort: Method started");
    //
    //     // BoundingBox viewPortDims = graph.getViewPortDims();
    //     // if (!isUIDrawingRequired(viewPortDims)) {
    //     //     // System.out.println("ElementTreeModule:loadUIComponentsInsideVisibleViewPort: UI redrawing not required.");
    //     //     return;
    //     // }
    //
    //
    //     // System.out.println("ElementTreeModule::loadUIComponentsInsideVisibleViewPort: before drawing, existing contents of cellLayer: ");
    //     // System.out.println("mapCircleCellsOnUI: size: " + graph.getModel().getCircleCellsOnUI().size());
    //     // graph.getModel().getCircleCellsOnUI().keySet().stream().sorted().forEach(id -> {
    //     //     System.out.print(id + ", ");
    //     // });
    //
    //     // System.out.println("mapEdgesOnUI: ");
    //     // graph.getModel().getEdgesOnUI().keySet().stream().sorted().forEach(id -> {
    //     //     System.out.print(id + ", ");
    //     // });
    //
    //     // System.out.println("highlightsOnUI: elementId : ");
    //     // graph.getModel().getHighlightsOnUI().values().stream().sorted().forEach(rect -> {
    //     //     System.out.print(rect.getElementId()+ ", ");
    //     // });
    //     addComponents();
    //     // inBackground(true);
    //     // System.out.println("ElementTreeModule::loadUIComponentsInsideVisibleViewPort: after drawing, contents of cellLayer: ");
    //     // System.out.println("mapCircleCellsOnUI: size: " + graph.getModel().getCircleCellsOnUI().size());
    //     // graph.getModel().getCircleCellsOnUI().keySet().stream().sorted().forEach(id -> {
    //     //     System.out.print(id + ", ");
    //     // });
    // }

    // public void addComponents() {
    //     addCircleCells();
    //     addBookmarks();
    //     addEdges();
    //     addHighlights();
    //     graph.updateCellLayer();
    // }



    // private void addHighlights() {
    //     // System.out.println("ElementTreeModule::addHighlights: method started");
    //
    //     Map<Integer, RectangleCell> highlightsOnUI = model.getHighlightsOnUI();
    //
    //     BoundingBox viewPortDims = graph.getViewPortDims();
    //
    //     double viewPortMinX = viewPortDims.getMinX();
    //     double viewPortMaxX = viewPortDims.getMaxX();
    //     double viewPortMinY = viewPortDims.getMinY();
    //     double viewPortMaxY = viewPortDims.getMaxY();
    //     double widthOffset = viewPortDims.getWidth() * multiplierForVisibleViewPort;
    //     double heightOffset = viewPortDims.getHeight() * multiplierForVisibleViewPort;
    //
    //     // Query to fetches highlight boxes that are contained within the bounds of outermost preload box.
    //
    //     String sql = (viewPortMinX - widthOffset) + " <= (start_x + width) " + " " +
    //             "AND " + (viewPortMaxX + widthOffset) + " >= start_x " +
    //             "AND " + (viewPortMinY - heightOffset) + " <= (start_y + height) " + " " +
    //             "AND " + (viewPortMaxY + heightOffset) + " >= start_Y " +
    //             "AND thread_id = " + currentThreadId + " " +
    //             "AND COLLAPSED = 0";
    //
    //     ResultSet rs = HighlightDAOImpl.selectWhere(sql);
    //
    //     try {
    //         while (rs.next()) {
    //             int id = rs.getInt("ID");
    //             int elementId = rs.getInt("ELEMENT_ID");
    //             float startX = rs.getFloat("START_X");
    //             float startY = rs.getFloat("START_Y");
    //             float width = rs.getFloat("WIDTH");
    //             float height = rs.getFloat("HEIGHT");
    //             String color = rs.getString("COLOR");
    //
    //             // If the rectangle highlight is not on UI then create a new rectangle and show on UI.
    //             if (!highlightsOnUI.containsKey(id)) {
    //                 // System.out.println("Drawing rectangle: " + id + " elementId: " + elementId);
    //                 // Rectangle rectangle = new Rectangle(startX, startY, width, height);
    //                 // rectangle.setFill(Color.web(color));
    //                 // rectangle.setArcHeight(20);
    //                 // rectangle.setArcWidth(20);
    //
    //                 RectangleCell rect = new RectangleCell(id, elementId, startX, startY, width, height);
    //                 rect.setColor(color);
    //                 rect.setArcHeight(20);
    //                 rect.setArcWidth(20);
    //
    //                 // model.addHighlight(id, rectangle);
    //                 model.addHighlight(id, rect);
    //             }
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    //
    // }


    // private void addBarMarks() {
    //     graph.getModel().updateBookmarkMap();
    //     Map<String, Rectangle> barMarkMap = graph.getModel().getBarMarkMap();
    //
    // }

    // private void addBookmarks() {
    //     Map<String, Bookmark> bookmarkMap = graph.getModel().updateAndGetBookmarkMap();
    //     Map<String, CircleCell> mapCircleCellsOnUI = model.getCircleCellsOnUI();
    //
    //     bookmarkMap.forEach((cellId, bookmark) -> {
    //         if (mapCircleCellsOnUI.containsKey(cellId)) {
    //             mapCircleCellsOnUI.get(cellId).bookmarkCell(bookmark.getColor());
    //         }
    //     });
    // }


    // private void addEdgesNew(BoundingBox viewport) {
    //     BoundingBox viewPortDims = graph.getViewPortDims();
    //     double viewPortMinX = viewPortDims.getMinX();
    //     double viewPortMaxX = viewPortDims.getMaxX();
    //     double viewPortMinY = viewPortDims.getMinY();
    //     double viewPortMaxY = viewPortDims.getMaxY();
    //     double widthOffset = viewPortDims.getWidth() * multiplierForVisibleViewPort;
    //     double heightOffset = viewPortDims.getHeight() * multiplierForVisibleViewPort;
    //
    //     String sql = "SELECT * FROM EDGE_ELEMENT " +
    //             "INNER JOIN ELEMENT ON FK_SOURCE_ELEMENT_ID = ELEMENT.ID " +
    //             "INNER JOIN CALL_TRACE ON ELEMENT.ID_ENTER_CALL_TRACE = CALL_TRACE.ID " +
    //             "WHERE CALL_TRACE.THREAD_ID = " + currentThreadId + " ";
    //
    //     String commonWhereClausForEdges = "AND EDGE_ELEMENT.collapsed = " + CollapseType.EDGE_VISIBLE + " AND " + "end_x >= " + (viewPortMinX - widthOffset) + " AND start_x <= " + (viewPortMaxX + widthOffset);
    //     String whereClauseForUpwardEdges = " AND end_Y >= " + (viewPortMinY - heightOffset) + " AND start_y <= " + (viewPortMaxY + heightOffset);
    //     String whereClauseForDownwardEdges = " AND start_y >= " + (viewPortMinY - heightOffset) + " AND end_Y <= " + (viewPortMaxY + heightOffset);
    //
    //     // System.out.println("ElementTreeModule::loadUIComponentsInsideVisibleViewPort: sql: " + sql + commonWhereClausForEdges + whereClauseForUpwardEdges);
    //     try (ResultSet rsUpEdges = DatabaseUtil.select(sql + commonWhereClausForEdges + whereClauseForUpwardEdges)) {
    //         getEdgesFromResultSet(rsUpEdges);
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    //
    //     // System.out.println("ElementTreeModule::loadUIComponentsInsideVisibleViewPort: sql: " + sql + commonWhereClausForEdges + whereClauseForDownwardEdges);
    //     try (ResultSet rsDownEdges = DatabaseUtil.select(sql + commonWhereClausForEdges + whereClauseForDownwardEdges)) {
    //         getEdgesFromResultSet(rsDownEdges);
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }


    // private void getEdgesFromResultSet(ResultSet rs) {
    //     Edge curEdge;
    //     try {
    //         while (rs.next()) {
    //             String targetEdgeId = String.valueOf(rs.getInt("fk_target_element_id"));
    //             double startX = rs.getFloat("start_x");
    //             double endX = rs.getFloat("end_x");
    //             double startY = rs.getFloat("start_y");
    //             double endY = rs.getFloat("end_y");
    //             // System.out.println("ElementTreeModule::getEdgesFromResultSet: adding edge with target id: " + targetEdgeId);
    //             curEdge = new Edge(targetEdgeId, startX, endX, startY, endY);
    //             model.addEdge(curEdge);
    //
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }

    // private void removeCircleCells(BoundingBox preloadBox) {
    //     CellLayer cellLayer = (CellLayer) graph.getCellLayer();
    //
    //     Map<String, CircleCell> mapCircleCellsOnUI = model.getCircleCellsOnUI();
    //     List<String> removeCircleCells = new ArrayList<>();
    //     List<CircleCell> listCircleCellsOnUI = model.getListCircleCellsOnUI();
    //
    //
    //     Iterator i = mapCircleCellsOnUI.entrySet().iterator();
    //     while (i.hasNext()) {
    //         Map.Entry<String, CircleCell> entry = (Map.Entry) i.next();
    //         CircleCell cell = entry.getValue();
    //         if (!preloadBox.contains(cell.getLayoutX(), cell.getLayoutY())) {
    //             removeCircleCells.add(cell.getCellId());
    //
    //             // model.removeSimplifiedElementFromMap(cell.getCellId());
    //         }
    //     }
    //
    //     removeCircleCells.forEach(cellId -> {
    //         CircleCell circleCell = mapCircleCellsOnUI.get(cellId);
    //         Platform.runLater(() -> cellLayer.getChildren().remove(circleCell));
    //         mapCircleCellsOnUI.remove(cellId);
    //         listCircleCellsOnUI.remove(circleCell);
    //     });
    // }

    // private void removeEdges(BoundingBox preloadBox) {
    //     CellLayer cellLayer = (CellLayer) graph.getCellLayer();
    //
    //     Map<String, Edge> mapEdgesOnUI = model.getEdgesOnUI();
    //
    //     List<String> removeEdges = new ArrayList<>();
    //     List<Edge> listEdgesOnUI = model.getListEdgesOnUI();
    //
    //     Iterator j = mapEdgesOnUI.entrySet().iterator();
    //
    //     while (j.hasNext()) {
    //         Map.Entry<String, Edge> entry = (Map.Entry) j.next();
    //         Edge edge = entry.getValue();
    //         Line line = (Line) edge.getChildren().get(0);
    //         BoundingBox lineBB = new BoundingBox(
    //                 line.getStartX(),
    //                 Math.min(line.getStartY(), line.getEndY()),
    //                 Math.abs(line.getEndX() - line.getStartX()),
    //                 Math.abs(line.getEndY() - line.getStartY()));
    //         // if (!preloadBox.contains(line.getEndX(), line.getEndY())) {
    //         //     removeEdges.add(edge.getEdgeId());
    //         // }
    //         if (!preloadBox.intersects(lineBB)) {
    //             removeEdges.add(edge.getEdgeId());
    //         }
    //     }
    //
    //     removeEdges.forEach(edgeId -> {
    //         Edge edge = mapEdgesOnUI.get(edgeId);
    //         Platform.runLater(() -> cellLayer.getChildren().remove(edge));
    //         mapEdgesOnUI.remove(edgeId);
    //         listEdgesOnUI.remove(edge);
    //     });
    //
    //     // clearUI();
    // }


    // private void removeHighlights(BoundingBox preloadBox) {
    //     // System.out.println("ElementTreeModule::removeHighlights: method started");
    //     CellLayer cellLayer = (CellLayer) graph.getCellLayer();
    //
    //     // This is the global HashMap that stores rectangle highlights currently on the UI.
    //     Map<Integer, RectangleCell> highlightsOnUI = model.getHighlightsOnUI();
    //
    //     // Temporary list to aid in removal of HashMap elements.
    //     List<Integer> removeHighlights = new ArrayList<>();
    //
    //     Iterator j = highlightsOnUI.entrySet().iterator();
    //
    //     while (j.hasNext()) {
    //         Map.Entry<Integer, RectangleCell> entry = (Map.Entry) j.next();
    //         RectangleCell rectangle = entry.getValue();
    //         int rectId = entry.getKey();
    //
    //         // if (!preloadBox.intersects(rectangle.getBoundsInLocal())) {
    //         if (!preloadBox.intersects(rectangle.getBoundsInParent())) {
    //             // ------------------   FOR DEBUGGING     ------------------
    //             // System.out.println("ElementTreeModule::removeHighlights: adding to removeHighlights because of 1: " + rectId + " elementid: " + rectangle.getElementId());
    //             // System.out.println("  minY: " + rectangle.getBoundsInLocal().getMinY() + " " + rectangle.getBoundsInLocal().getMinY() + " " + rectangle.getLayoutY());
    //             // System.out.println("  maxY: " + rectangle.getBoundsInLocal().getMaxY() + " " + rectangle.getBoundsInLocal().getMaxY());
    //             // System.out.println("  height: " + rectangle.getBoundsInLocal().getHeight() + " " + rectangle.getBoundsInLocal().getHeight());
    //             // System.out.println("---------------------------------------------------");
    //             // System.out.println("  minY: " + rectangle.getRectangle().getBoundsInLocal().getMinY() + " " + rectangle.getRectangle().getBoundsInLocal().getMinY() + " " + rectangle.getRectangle().getLayoutY());
    //             // System.out.println("  maxY: " + rectangle.getRectangle().getBoundsInLocal().getMaxY() + " " + rectangle.getRectangle().getBoundsInLocal().getMaxY());
    //             // System.out.println("  height: " + rectangle.getRectangle().getBoundsInLocal().getHeight() + " " + rectangle.getRectangle().getBoundsInLocal().getHeight());
    //             // System.out.println("---------------------------------------------------");
    //             // System.out.println("preloadbox bounds: " + preloadBox);
    //             // System.out.println("rectangleCell: boundsinlocal: " + rectangle.getBoundsInLocal());
    //             // System.out.println("rectangleCell: boundsInParent: " + rectangle.getBoundsInParent());
    //             // System.out.println("rectangleCell: layoutBounds: " + rectangle.getLayoutBounds());
    //             // System.out.println("rectangleCell.getRectangle: boundsinlocal: " + rectangle.getRectangle().getBoundsInLocal());
    //             // System.out.println("rectangleCell.getRectangle: boundsinparent: " + rectangle.getRectangle().getBoundsInParent());
    //             // System.out.println("rectangleCell.getRectangle: layoutBounds: " + rectangle.getRectangle().getLayoutBounds());
    //             // System.out.println("---------------------------------------------------");
    //
    //             // Removes those highlights that are not visible.
    //             removeHighlights.add(rectId);
    //
    //         } else {
    //             // Removes those highlights that are not in HIGHLIGHT_ELEMENT Table.
    //             String deleteQuery = "SELECT COUNT(*) AS COUNT FROM HIGHLIGHT_ELEMENT WHERE ID = " + rectId;
    //             ResultSet rs = DatabaseUtil.select(deleteQuery);
    //             try {
    //                 while (rs.next()) {
    //                     if (rs.getInt("COUNT") == 0) {
    //                         if (!removeHighlights.contains(rectId)) {
    //                             removeHighlights.add(rectId);
    //                             // System.out.println("ElementTreeModule::removeHighlights: adding to removeHighlights because of 2: " + rectId + " elementId: " + rs.getInt("element_id"));
    //                         }
    //                     }
    //                 }
    //             } catch (SQLException e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     }
    //
    //
    //     // Removing elements from HashMap is a two step process, this is to avoid concurrent modification exception
    //     // Remove rectangle highlights from UI and HashMap.
    //     removeHighlights.forEach(rectId -> {
    //         RectangleCell rectangle = highlightsOnUI.get(rectId);
    //         int elementId = rectangle.getElementId();
    //         // System.out.println("ElementTreeModule::removeHighlights: removing rectangle: " + rectId + " elementId: " + elementId);
    //         Platform.runLater(() -> cellLayer.getChildren().remove(rectangle));
    //         highlightsOnUI.remove(rectId);
    //     });
    //     // System.out.println("ElementTreeModule::removeHighlights: method ended");
    // }

//     public void removeUIComponentsFromInvisibleViewPort(Graph graph) {
//         // System.out.println("ElementTreeModule::removeUIComponentsFromInvisibleViewPort: method started");
//         this.graph = graph;
//         this.model = graph.getModel();
//
//         BoundingBox viewPortDims = graph.getViewPortDims();
//
//         // if (!isUIDrawingRequired(viewPortDims)) {
//         //     return;
//         // }
//
//         double minX = viewPortDims.getMinX();
//         double minY = viewPortDims.getMinY();
//         double width = viewPortDims.getWidth();
//         double height = viewPortDims.getHeight();
//         double widthOffset = viewPortDims.getWidth() * 3;
//         double heightOffset = viewPortDims.getHeight() * 3;
//
//         BoundingBox preloadBox = new BoundingBox(
//                 minX - widthOffset,
//                 minY - heightOffset,
//                 width + widthOffset * 6,
//                 height + heightOffset * 6);
// //        BoundingBox preloadBox = new BoundingBox(minX , minY, width, height);
//
//         removeCircleCells(preloadBox);
//         removeEdges(preloadBox);
//         removeHighlights(preloadBox);
//
//         graph.updateCellLayer();
//
//     }

}
