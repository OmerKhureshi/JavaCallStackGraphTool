package com.csgt.controller;

import com.csgt.controller.tasks.DBFetchTask;
import com.csgt.dataaccess.DAO.EdgeDAOImpl;
import com.csgt.dataaccess.DAO.ElementDAOImpl;
import com.csgt.dataaccess.DAO.HighlightDAOImpl;
import com.csgt.dataaccess.DTO.BookmarkDTO;
import com.csgt.dataaccess.DTO.EdgeDTO;
import com.csgt.dataaccess.DTO.ElementDTO;
import com.csgt.dataaccess.DTO.HighlightDTO;
import com.csgt.presentation.graph.*;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller class for canvas.fxml
 * This class handles all the actions on the canvas including the graph.
 */
public class CanvasController {

    @FXML
    AnchorPane canvasAnchorPane;

    public Pane canvas;

    public CustomScrollPane scrollPane;
    public Map<String, NodeCell> nodeCellsOnUI = new HashMap<>();
    private Map<String, Edge> edgesOnUI = new HashMap<>();
    private Map<Integer, HighlightCell> highlightsOnUI = new HashMap<>();

    // Region where UI components are loaded.
    private static BoundingBox activeRegion = null;
    // Trigger UI components to be reloaded when visible viewport is outside this region. triggerRegion < activeRegion
    private static BoundingBox triggerRegion = null;

    private Map<String, Double> vScrollBarPos = new HashMap<>();
    private Map<String, Double> hScrollBarPos = new HashMap<>();



    @FXML
    private void initialize() {
        ControllerLoader.register(this);
    }

    public void setUp() {
        setUpCenterLayout();
    }

    private void setUpCenterLayout() {
        canvas = new Pane();
        scrollPane = new CustomScrollPane(canvas);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        canvas.prefHeightProperty().bind(scrollPane.heightProperty());
        canvas.prefWidthProperty().bind(scrollPane.widthProperty());

        AnchorPane.setTopAnchor(scrollPane, 0.0);
        AnchorPane.setBottomAnchor(scrollPane, 0.0);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setRightAnchor(scrollPane, 0.0);

        canvasAnchorPane.getChildren().add(scrollPane);

        drawPlaceHolderLines();
        setListeners();


        scrollPane.setStyle("-fx-background: WHITE;");
    }


    /**
     * This method is invoked to check and draw any UI components on the graph if needed.
     */
    public void updateIfNeeded() {
        if (isUIDrawingRequired()) {
        System.out.println(Thread.currentThread().getId() + ": CanvasController.updateIfNeeded: ");
            DBFetchTask.initiateTask(true);
        }
    }

    /**
     * This methods creates and draws circle cells and Edges on the active region of the viewport.
     */
    private void addCanvasComponentsFromDB() {
        System.out.println(Thread.currentThread().getId() + ": CanvasController.addCanvasComponentsFromDB");
        DBFetchTask.initiateTask(false);
    }

    private void addUIComponents() {
        if (ControllerLoader.centerLayoutController.getCurrentThreadId() == null) {
            return;
        }
        addNodeCellsToUI();
        addEdgesToUI();
        addHighlightsToUI();
        addBookmarks();

        stackRectangles();
    }

    public void removeUIComponents() {
        removeNodeCellsFromUI();
        removeEdgesFromUI();
        removeHighlightsFromUI();
    }

    public List<NodeCell>  getNodeCell() {
        BoundingBox viewPort = getPrefetchViewPortDims();

        List<ElementDTO> elementDTOList = ElementDAOImpl.getElementDTOsInViewport(viewPort);
        return ControllerUtil.convertElementDTOTOCell(elementDTOList);
    }


    public void addElementsToUI(List<NodeCell> nodeCells, List<Edge> edges, List<HighlightCell> highlightRectList, Map<String, BookmarkDTO> bookmarkDTOMap) {
        nodeCells.forEach(this::addNewCellToUI);

        edges.forEach(edge -> {
            if (!edgesOnUI.containsKey(edge.getEdgeId())) {
                edgesOnUI.put(edge.getEdgeId(), edge);
                canvas.getChildren().add(edge);
                edge.toBack();
            }
        });

        highlightRectList.forEach(this::addNewHighlightsToUI);

        bookmarkDTOMap.forEach((cellId, bookmark) -> {
            if (nodeCellsOnUI.containsKey(cellId)) {
                nodeCellsOnUI.get(cellId).bookmarkCell(bookmark.getColor());
            }
        });
    }

    /**
     * Determines all the element nodes that should be drawn in the current viewport and draws them if not already present.
     */
    private void addNodeCellsToUI() {
        List<NodeCell> nodeCells = getNodeCellsFromDB();
        nodeCells.forEach(this::addNewCellToUI);
    }

    public List<NodeCell> getNodeCellsFromDB() {
        BoundingBox viewPort = getPrefetchViewPortDims();

        List<ElementDTO> elementDTOList = ElementDAOImpl.getElementDTOsInViewport(viewPort);
        List<NodeCell> nodeCells = ControllerUtil.convertElementDTOTOCell(elementDTOList);

        return nodeCells;
    }

    /**
     * Draws a new element node on UI if not already present.
     *
     * @param nodeCell
     */
    private void addNewCellToUI(NodeCell nodeCell) {
        if (!nodeCellsOnUI.containsKey(nodeCell.getCellId())) {
            nodeCellsOnUI.put(nodeCell.getCellId(), nodeCell);
            canvas.getChildren().add(nodeCell);
            nodeCell.toFront();
            ControllerLoader.getEventHandlers().setCustomMouseEventHandlers(nodeCell);
            System.out.println("CanvasController.addNewCellToUI added nodeCell = " + nodeCell);
        }
    }

    private void removeNodeCellsFromUI() {
        List<NodeCell> removeNodeCellsList = nodeCellsOnUI.values().stream()
                .filter(nodeCell -> !activeRegion.contains(nodeCell.getBoundsInParent()))
                .collect(Collectors.toList());

        removeNodeCellsList.forEach(this::removeCellFromUI);
    }

    private void removeCellFromUI(NodeCell nodeCell) {
        if (nodeCell != null && nodeCellsOnUI.containsKey(nodeCell.getCellId())) {
            nodeCellsOnUI.remove(nodeCell.getCellId());
            canvas.getChildren().remove(nodeCell);
            System.out.println("CanvasController.removeCellFromUI removed nodeCell = " + nodeCell);
        }
    }

    private void addEdgesToUI() {
        List<Edge> edges = getEdgesFromDB();
        edges.forEach(edge -> {
            if (!edgesOnUI.containsKey(edge.getEdgeId())) {
                edgesOnUI.put(edge.getEdgeId(), edge);
                canvas.getChildren().add(edge);
                edge.toBack();
            }
        });
    }

    public List<Edge> getEdgesFromDB() {
        BoundingBox viewPort = getPrefetchViewPortDims();

        List<EdgeDTO> edgeDTOList = EdgeDAOImpl.getEdgeDTO(viewPort);
        List<Edge> edges = ControllerUtil.convertEdgeDTOToEdges(edgeDTOList);

        return edges;
    }
    private void removeEdgesFromUI() {
        List<Edge> removeEdgeList = edgesOnUI.values().stream()
                .filter(edge -> !activeRegion.contains(edge.line.getEndX(), edge.line.getEndY()))
                .collect(Collectors.toList());

        removeEdgeList.forEach(this::removeEdgeFromUI);
    }

    public void removeEdgeFromUI(Edge edge) {
        if (edge != null && edgesOnUI.containsKey(edge.getEdgeId())) {
            edgesOnUI.remove(edge.getEdgeId());
            canvas.getChildren().remove(edge);
        }
    }

    public void removeUIComponentsBetween(ElementDTO elementDTO, int endCellId) {
        int startCellId = Integer.valueOf(elementDTO.getId());
        float clickedCellTopRightX =  elementDTO.getBoundBoxXTopRight();
        float clickedCellTopY = elementDTO.getBoundBoxYTopLeft();
        float leafCount = elementDTO.getLeafCount();
        float clickedCellHeight = leafCount * BoundBox.unitHeightFactor;
        float clickedCellBottomY = clickedCellTopY + BoundBox.unitHeightFactor;
        float clickedCellBoundBottomY = elementDTO.getBoundBoxYBottomLeft();

        List<NodeCell> removeNodeCells = new ArrayList<>();
        List<Edge> removeEdges = new ArrayList<>();

        Map<Integer, HighlightCell> highlightsOnUi = ControllerLoader.canvasController.getHighlightsOnUI();
        List<HighlightCell> removeHighlights = new ArrayList<>();

        nodeCellsOnUI.forEach((id, nodeCell) -> {
            int intId = Integer.parseInt(id);
            if (intId > startCellId && intId < endCellId) {
                removeNodeCells.add(nodeCell);
            }

            // Remove all children cells and edges that end at these cells from UI
            double thisCellTopLeftX = nodeCell.getLayoutX();
            double thisCellTopY = nodeCell.getLayoutY();

            if (!removeNodeCells.contains(nodeCell) && thisCellTopY >= clickedCellBottomY && thisCellTopY < clickedCellBoundBottomY && thisCellTopLeftX > clickedCellTopRightX) {
                removeNodeCells.add(nodeCell);
                removeEdges.add(edgesOnUI.get(id));
            } else if (!removeNodeCells.contains(nodeCell) && thisCellTopY == clickedCellTopY && thisCellTopLeftX >= clickedCellTopRightX) {
                removeNodeCells.add(nodeCell);
                removeEdges.add(edgesOnUI.get(id));
            }
        });

        removeNodeCells.forEach((nodeCell) -> {
            ControllerLoader.canvasController.removeCellFromUI(nodeCell);
        });

        edgesOnUI.forEach((id, edge) -> {
            int intId = Integer.parseInt(id);
            if (intId > startCellId && intId < endCellId) {
                // System.out.print(" .--" + id);
                removeEdges.add(edge);
            }

            // Get edges that don't have a target circle rendered on UI.
            // Get edges to right and not extending height of the clicked cell bound box.
            double thisLineEndY = edge.line.getEndY();
            double thisLineStartY = edge.line.getStartY();
            double thisLineStartX = edge.line.getStartX();

            if (thisLineEndY >= clickedCellTopY
                    && thisLineEndY <= clickedCellBoundBottomY
                    && thisLineStartY >= clickedCellTopY
                    && thisLineStartX >= (clickedCellTopRightX - BoundBox.unitWidthFactor)) {
                // System.out.print(" --" + id);
                removeEdges.add(edge);
            }
        });

        removeEdges.forEach((edge) -> {
            ControllerLoader.canvasController.removeEdgeFromUI(edge);
        });

        highlightsOnUi.forEach((id, rectangle) -> {
            if (id> startCellId && id < endCellId) {
                removeHighlights.add(rectangle);
            }
        });

        removeHighlights.forEach((highlightCell) -> {
            if (highlightsOnUi.containsKey(highlightCell)) {
                highlightsOnUi.remove(highlightCell);
                canvas.getChildren().remove(highlightCell);
            }
        });
    }

    public void moveLowerTreeByDelta(ElementDTO elementDTO) {
        float clickedCellBottomY = elementDTO.getBoundBoxYTopLeft() + BoundBox.unitHeightFactor;
        double delta = elementDTO.getDeltaY();
        System.out.println("CanvasController.moveLowerTreeByDelta for cell : " + elementDTO.getId() + " by : " + delta);
        System.out.println("moving cells down:");
        // For each circle cell on UI that is below the clicked cell, move up by delta
        nodeCellsOnUI.forEach((thisCellID, thisNodeCell) -> {
            double thisCellTopY = thisNodeCell.getLayoutY();

            if (thisCellTopY >= clickedCellBottomY) {
                System.out.print(thisNodeCell.getCellId() + ", ");
                thisNodeCell.relocate(thisNodeCell.getLayoutX(), thisCellTopY - delta);
            }

        });

        // For each edge on UI whose endY or startY is below the clicked cell, relocate that edge appropriately
        edgesOnUI.forEach((id, edge) -> {
            double thisEdgeEndY = edge.line.getEndY();
            double thisEdgeStartY = edge.line.getStartY();

            if (thisEdgeEndY >= clickedCellBottomY) {
                edge.line.setEndY(thisEdgeEndY - delta);
            }

            if (thisEdgeStartY >= clickedCellBottomY) {
                edge.line.setStartY(thisEdgeStartY - delta);
            }
        });

        // For each highlight rectangle whose startY is below the clicked cell, relocate that rectangle appropriately
        highlightsOnUI.forEach((id, highlightCell) -> {
            double y = highlightCell.getLayoutY();
            if (y >= clickedCellBottomY - BoundBox.unitHeightFactor/2) {
                highlightCell.relocate(highlightCell.getLayoutX(), y - delta);
            }
        });

    }


    private void addHighlightsToUI() {
        List<HighlightCell> highlightRectList = getHighlightsFromDB();
        highlightRectList.forEach(this::addNewHighlightsToUI);
    }

    public List<HighlightCell> getHighlightsFromDB() {
        BoundingBox viewPort = getPrefetchViewPortDims();

        List<HighlightDTO> highlightDTOs = HighlightDAOImpl.getHighlightDTOsInViewPort(viewPort);
        List<HighlightCell> highlightRectList = ControllerUtil.convertHighlightDTOsToHighlights(highlightDTOs);

        return highlightRectList;
    }

    private void addNewHighlightsToUI(HighlightCell highlightCell) {
        if (!highlightsOnUI.containsKey(highlightCell.getElementId())) {
            highlightsOnUI.putIfAbsent(highlightCell.getElementId(), highlightCell);
            canvas.getChildren().add(highlightCell);
        }
    }

    private void removeHighlightsFromUI() {
        List<HighlightCell> removeCircleCellsList = highlightsOnUI.values().stream()
                .filter(highlightCell -> !activeRegion.intersects(highlightCell.getBoundsInParent()))
                .collect(Collectors.toList());

        removeCircleCellsList.forEach(this::removeHighlightFromUI);
    }

    private void removeHighlightFromUI(HighlightCell highlightCell) {
        if (highlightCell != null && highlightsOnUI.containsKey(highlightCell.getCellId())) {
            highlightsOnUI.remove(highlightCell.getCellId());
            canvas.getChildren().remove(highlightCell);
        }
    }

    public void addBookmarks() {
        Map<String, BookmarkDTO> bookmarkMap = getBookmarksFromDB();

        bookmarkMap.forEach((cellId, bookmark) -> {
            if (nodeCellsOnUI.containsKey(cellId)) {
                nodeCellsOnUI.get(cellId).bookmarkCell(bookmark.getColor());
            }
        });
    }

    public Map<String, BookmarkDTO> getBookmarksFromDB() {
        return ControllerLoader.menuController.getBookmarkDTOs();
    }

    public void removeBookmarkFromUI(String circleCellId) {
        if (nodeCellsOnUI.containsKey(circleCellId)) {
            nodeCellsOnUI.get(circleCellId).removeBookmark();
        }
    }

    public void removeAllBookmarksFromUI() {
        Map<String, BookmarkDTO> bookmarkDTOs = getBookmarksFromDB();

        nodeCellsOnUI.forEach((id, nodeCell) -> {
            if (bookmarkDTOs.containsKey(id)) {
                nodeCell.removeBookmark();
            }
        });
    }


    /**
     * Clears all UI components on canvas except the placeholder lines.
     */
    private void clear() {
        nodeCellsOnUI.forEach((id, nodeCell) -> {
            canvas.getChildren().remove(nodeCell);
        });
        nodeCellsOnUI.clear();
        // bookmarks are part of circle cells, so no need to remove them explicitly.

        edgesOnUI.forEach((id, edge) -> {
            canvas.getChildren().remove(edge);
        });
        edgesOnUI.clear();

        highlightsOnUI.forEach((id, highlightCell) -> {
            canvas.getChildren().remove(highlightCell);
        });
        highlightsOnUI.clear();
    }

    /**
     * Clears all UI components on canvas.
     */
    private void clearAll() {
        canvas.getChildren().clear();
        nodeCellsOnUI.clear();
        edgesOnUI.clear();
        highlightsOnUI.clear();
    }

    public void clearAndUpdate() {
        System.out.println(Thread.currentThread().getId() + ": CanvasController.clearAndUpdate");
        clear();
        addCanvasComponentsFromDB();
    }

    public void onThreadSelect() {
        clearAll();

        drawPlaceHolderLines();
        positionScrollBarFromHistory();
        addCanvasComponentsFromDB();
    }

    public void showGraphForThread(String threadId) {
        ControllerLoader.centerLayoutController.setCurrentThreadId(threadId);
        onThreadSelect();
    }


    public BoundingBox getViewPortDims() {
        double scale = CustomScrollPane.getScaleValue();

        double hValue = scrollPane.getHvalue();
        double scaledContentWidth = scrollPane.getContent().getLayoutBounds().getWidth();// * scale;
        double scaledViewportWidth = scrollPane.getViewportBounds().getWidth() / scale;

        double vValue = scrollPane.getVvalue();
        double scaledContentHeight = scrollPane.getContent().getLayoutBounds().getHeight();// * scale;
        double scaledViewportHeight = scrollPane.getViewportBounds().getHeight() / scale;

        double minX = hValue * (scaledContentWidth - scaledViewportWidth);
        double minY = vValue * (scaledContentHeight - scaledViewportHeight);

        BoundingBox box = new BoundingBox(minX, minY, scaledViewportWidth, scaledViewportHeight);

        return box;
    }

    public BoundingBox getPrefetchViewPortDims() {
        BoundingBox viewPort = getViewPortDims();

        return new BoundingBox(
                viewPort.getMinX() - viewPort.getWidth() * 3,
                viewPort.getMinY() - viewPort.getHeight() * 3,
                viewPort.getWidth() * 7,
                viewPort.getHeight() * 7
        );
    }


    private void setRegions() {
        setActiveRegion();
        setTriggerRegion();
    }

    private boolean isUIDrawingRequired() {
        if (triggerRegion == null || activeRegion == null) {
            System.out.println("one of region is null");
            setRegions();
        }

        if (!triggerRegion.contains(getViewPortDims())) {
            System.out.println("trigger region doesnot contain viewport");
            System.out.println("triggerRegion = " + triggerRegion);
            System.out.println("getViewPortDims() = " + getViewPortDims());
            setRegions();
            System.out.println("after setting regions");
            System.out.println("triggerRegion = " + triggerRegion);
            System.out.println("getViewPortDims() = " + getViewPortDims());
            return true;
        }

        return false;
    }

    private void setActiveRegion() {
        BoundingBox viewPort = getViewPortDims();

        activeRegion = new BoundingBox(
                viewPort.getMinX() - viewPort.getWidth() * 2,
                viewPort.getMinY() - viewPort.getHeight() * 2,
                viewPort.getWidth() * 5,
                viewPort.getHeight() * 5
        );
        //        activeRegion = new BoundingBox(
        //         viewPort.getMinX() - viewPort.getWidth() * 3,
        //         viewPort.getMinY() - viewPort.getHeight() * 3,
        //         viewPort.getWidth() * 7,
        //         viewPort.getHeight() * 7
        // );
    }

    public static BoundingBox getActiveRegion() {
        return activeRegion;
    }

    private void setTriggerRegion() {
        BoundingBox viewPort = getViewPortDims();

        // triggerRegion = new BoundingBox(
        //         activeRegion.getMinX() + viewPort.getWidth(),
        //         activeRegion.getMinY() + viewPort.getHeight(),
        //         viewPort.getWidth() * 5,
        //         viewPort.getHeight() * 5
        // );
        triggerRegion = new BoundingBox(
                activeRegion.getMinX() + viewPort.getWidth(),
                activeRegion.getMinY() + viewPort.getHeight(),
                viewPort.getWidth() * 3,
                viewPort.getHeight() * 3
        );
    }

    public void setListeners() {
        scrollPane.vvalueProperty().addListener(valuePropListener);
        scrollPane.hvalueProperty().addListener(valuePropListener);
        scrollPane.viewportBoundsProperty().addListener(viewportChangeListener);
        // scrollPane.widthProperty().addListener(viewportChangeListener);
        // scrollPane.heightProperty().addListener(viewportChangeListener);

        // scrollPane.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
        //     System.out.println("scrollPane.viewportBoundsProperty()");
        //     System.out.println("oldValue = " + oldValue);
        //     System.out.println("newValue = " + newValue);
        //     System.out.println();
        // });
    }

    private ChangeListener valuePropListener = (observable, oldValue, newValue) -> {
        System.out.println(Thread.currentThread().getId() + ": CanvasController.valuePropListener");
        updateIfNeeded();
    };

    private ChangeListener viewportChangeListener = (observable, oldValue, newValue) -> {
        System.out.println(Thread.currentThread().getId() + ": CanvasController.viewportChangeListener");
        addCanvasComponentsFromDB();
    };

    private void setScrollBarPos(double hVal, double vVal) {
        // System.out.println(Thread.currentThread().getId() + ": CanvasController.setScrollBarPos");
        removeListeners();
        scrollPane.setHvalue(hVal);
        scrollPane.setVvalue(vVal);
        updateIfNeeded();
        setListeners();
    }


    public void removeListeners() {
        scrollPane.vvalueProperty().removeListener(valuePropListener);
        scrollPane.hvalueProperty().removeListener(valuePropListener);
        scrollPane.viewportBoundsProperty().removeListener(valuePropListener);
    }

    private void drawPlaceHolderLines() {
        String currentThreadId = ControllerLoader.centerLayoutController.getCurrentThreadId();
        if (currentThreadId == null) {
            System.out.println("CanvasController.drawPlaceHolderLines. currentThreadId is null. Returning without loading.");
            return;
        }
        int height = ElementDAOImpl.getMaxLeafCount(currentThreadId);
        int width = ElementDAOImpl.getMaxLevelCount(currentThreadId);

        Line hPlaceHolderLine = new Line(0, 0, (width + 2) * BoundBox.unitWidthFactor, 0);
        hPlaceHolderLine.setStrokeWidth(.0005);
        canvas.getChildren().add(hPlaceHolderLine);

        Line vPlaceHolderLine = new Line(0, 0, 0, height * BoundBox.unitHeightFactor);
        vPlaceHolderLine.setStrokeWidth(.0005);
        canvas.getChildren().add(vPlaceHolderLine);
    }

    public void saveScrollBarPos() {
        String threadId = ControllerLoader.centerLayoutController.getCurrentThreadId();

        vScrollBarPos.put(threadId, scrollPane.getVvalue());
        hScrollBarPos.put(threadId, scrollPane.getHvalue());
    }

    private void positionScrollBarFromHistory() {
        String threadId = ControllerLoader.centerLayoutController.getCurrentThreadId();

        setScrollBarPos(
                hScrollBarPos.getOrDefault(threadId, 0.0),
                vScrollBarPos.getOrDefault(threadId, 0.0)
        );
    }

    public void moveScrollPane(double xCord, double yCord) {
        double hVal = getHValue(xCord);
        double vVal = getVValue(yCord);
        setScrollBarPos(hVal, vVal);
    }

    private double getHValue(double xCoordinate) {
        double scaledContentWidth = scrollPane.getContent().getLayoutBounds().getWidth();// * scale;
        double scaledViewportWidth = scrollPane.getViewportBounds().getWidth(); // / scale;

        return xCoordinate / (scaledContentWidth - scaledViewportWidth * 0.5);
    }

    private double getVValue(double yCoordinate) {
        double scaledContentHeight = scrollPane.getContent().getLayoutBounds().getHeight();// * scale;
        double scaledViewportHeight = scrollPane.getViewportBounds().getHeight(); // / scale;

        return yCoordinate / (scaledContentHeight - scaledViewportHeight * 0.5);
    }

    public void stackRectangles() {

        List<HighlightCell> list = new ArrayList<>(highlightsOnUI.values());

        // Sort the list of rectangles according to area.
        list.sort((o1, o2) -> (int) (o1.getBoundsInParent().getWidth() * o1.getBoundsInParent().getHeight()
                - o2.getBoundsInParent().getWidth() * o2.getBoundsInParent().getHeight()));
        list.forEach(Node::toBack);

        edgesOnUI.forEach((id, edge) -> edge.toFront());
        nodeCellsOnUI.forEach((id, nodeCell) -> nodeCell.toFront());
    }

    public void jumpTo(String cellId, String threadId, int collapsed) {
        // collapsed value might have changes since the last time event handlers were set on the the bookmarks buttons.
        // get new collapsed value.
        ElementDTO elementDTO = ElementDAOImpl.getElementDTO(cellId);
        ControllerLoader.eventHandlers.jumpTo(cellId, threadId, elementDTO.getCollapsed());
    }

    public void onReset() {
        clearAll();

        nodeCellsOnUI = new HashMap<>();
        edgesOnUI = new HashMap<>();
        highlightsOnUI = new HashMap<>();
    }

    Map<Integer, HighlightCell> getHighlightsOnUI() {
        return highlightsOnUI;
    }

    public void processTaskResults(List<NodeCell> nodeCells,
                                   List<Edge> edges,
                                   List<HighlightCell> highlightRectList,
                                   Map<String, BookmarkDTO> bookmarkDTOMap,
                                   boolean isRemovalRequired) {
        addElementsToUI(nodeCells, edges, highlightRectList, bookmarkDTOMap);
        stackRectangles();
        if (isRemovalRequired) {
            removeUIComponents();
        }
    }
}
