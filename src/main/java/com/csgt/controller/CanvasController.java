package com.csgt.controller;

import com.csgt.dataaccess.DAO.EdgeDAOImpl;
import com.csgt.dataaccess.DAO.ElementDAOImpl;
import com.csgt.dataaccess.DAO.HighlightDAOImpl;
import com.csgt.dataaccess.DTO.BookmarkDTO;
import com.csgt.dataaccess.DTO.EdgeDTO;
import com.csgt.dataaccess.DTO.ElementDTO;
import com.csgt.dataaccess.DTO.HighlightDTO;
import com.csgt.presentation.graph.NodeCell;
import com.csgt.presentation.graph.BoundBox;
import com.csgt.presentation.graph.Edge;
import com.csgt.presentation.graph.HighlightCell;
import com.csgt.presentation.graph.CustomScrollPane;
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

public class CanvasController {

    @FXML
    AnchorPane canvasAnchorPane;

    public Pane canvas;

    public CustomScrollPane scrollPane;
    private Map<String, NodeCell> circleCellsOnUI = new HashMap<>();
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
            addUIComponents();
            removeUIComponents();
        }
    }

    /**
     * This methods creates and draws circle cells and Edges on the active region of the viewport.
     */
    private void addCanvasComponentsFromDB() {
        addUIComponents();
    }

    private void addUIComponents() {
        if (ControllerLoader.centerLayoutController.getCurrentThreadId() == null) {
            return;
        }
        addCirclesToUI();
        addEdgesToUI();
        addHighlightsToUI();
        addBookmarks();

        stackRectangles();
    }

    private void removeUIComponents() {
        removeCirclesFromUI();
        removeEdgesFromUI();
        removeHighlightsFromUI();
    }


    /**
     * Determines all the element nodes that should be drawn in the current viewport and draws them if not already present.
     */
    private void addCirclesToUI() {
        BoundingBox viewPort = getPrefetchViewPortDims();

        List<ElementDTO> elementDTOList = ElementDAOImpl.getElementDTOsInViewport(viewPort);
        List<NodeCell> nodeCells = ControllerUtil.convertElementDTOTOCell(elementDTOList);

        nodeCells.forEach(this::addNewCellToUI);
    }

    /**
     * Draws a new element node on UI if not already present.
     *
     * @param nodeCell
     */
    private void addNewCellToUI(NodeCell nodeCell) {
        if (!circleCellsOnUI.containsKey(nodeCell.getCellId())) {
            circleCellsOnUI.put(nodeCell.getCellId(), nodeCell);
            canvas.getChildren().add(nodeCell);
            nodeCell.toFront();
            ControllerLoader.getEventHandlers().setCustomMouseEventHandlers(nodeCell);
            // System.out.print("+" + nodeCell.getCellId() + ", ");
        }
    }

    private void removeCirclesFromUI() {
        List<NodeCell> removeNodeCellsList = circleCellsOnUI.values().stream()
                .filter(nodeCell -> !activeRegion.contains(nodeCell.getBoundsInParent()))
                .collect(Collectors.toList());

        removeNodeCellsList.forEach(this::removeCellFromUI);
    }

    private void removeCellFromUI(NodeCell nodeCell) {
        if (nodeCell != null && circleCellsOnUI.containsKey(nodeCell.getCellId())) {
            circleCellsOnUI.remove(nodeCell.getCellId());
            canvas.getChildren().remove(nodeCell);
        }
    }

    private void addEdgesToUI() {
        BoundingBox viewPort = getPrefetchViewPortDims();

        List<EdgeDTO> edgeDTOList = EdgeDAOImpl.getEdgeDTO(viewPort);
        List<Edge> edges = ControllerUtil.convertEdgeDTOToEdges(edgeDTOList);

        edges.forEach(edge -> {
            if (!edgesOnUI.containsKey(edge.getEdgeId())) {
                edgesOnUI.put(edge.getEdgeId(), edge);
                canvas.getChildren().add(edge);
                edge.toBack();
            }
        });
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

        circleCellsOnUI.forEach((id, nodeCell) -> {
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

        // For each circle cell on UI that is below the clicked cell, move up by delta
        circleCellsOnUI.forEach((thisCellID, thisNodeCell) -> {
            double thisCellTopY = thisNodeCell.getLayoutY();

            if (thisCellTopY >= clickedCellBottomY) {
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
        BoundingBox viewPort = getPrefetchViewPortDims();

        List<HighlightDTO> highlightDTOs = HighlightDAOImpl.getHighlightDTOsInViewPort(viewPort);
        List<HighlightCell> highlightRectList = ControllerUtil.convertHighlightDTOsToHighlights(highlightDTOs);

        highlightRectList.forEach(this::addNewHighlightsToUI);
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
        Map<String, BookmarkDTO> bookmarkMap = ControllerLoader.menuController.getBookmarkDTOs();

        bookmarkMap.forEach((cellId, bookmark) -> {
            if (circleCellsOnUI.containsKey(cellId)) {
                circleCellsOnUI.get(cellId).bookmarkCell(bookmark.getColor());
            }
        });
    }

    public void removeBookmarkFromUI(String circleCellId) {
        if (circleCellsOnUI.containsKey(circleCellId)) {
            circleCellsOnUI.get(circleCellId).removeBookmark();
        }
    }

    public void removeAllBookmarksFromUI() {
        Map<String, BookmarkDTO> bookmarkDTOs = ControllerLoader.menuController.getBookmarkDTOs();

        circleCellsOnUI.forEach((id, nodeCell) -> {
            if (bookmarkDTOs.containsKey(id)) {
                nodeCell.removeBookmark();
            }
        });
    }


    /**
     * Clears all UI components on canvas except the placeholder lines.
     */
    private void clear() {
        circleCellsOnUI.forEach((id, nodeCell) -> {
            canvas.getChildren().remove(nodeCell);
        });
        circleCellsOnUI.clear();
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
        circleCellsOnUI.clear();
        edgesOnUI.clear();
        highlightsOnUI.clear();
    }

    public void clearAndUpdate() {
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
            setRegions();
        }

        if (!triggerRegion.contains(getViewPortDims())) {
            // System.out.println("CanvasController.isUIDrawingRequired drawing IS required.   YYYYYYYYYYYYYY");
            setRegions();
            return true;
        }

        return false;
    }

    private void setActiveRegion() {
        BoundingBox viewPort = getViewPortDims();

        activeRegion = new BoundingBox(
                viewPort.getMinX() - viewPort.getWidth() * 3,
                viewPort.getMinY() - viewPort.getHeight() * 3,
                viewPort.getWidth() * 7,
                viewPort.getHeight() * 7
        );
    }

    public static BoundingBox getActiveRegion() {
        return activeRegion;
    }

    private void setTriggerRegion() {
        BoundingBox viewPort = getViewPortDims();

        triggerRegion = new BoundingBox(
                activeRegion.getMinX() + viewPort.getWidth(),
                activeRegion.getMinY() + viewPort.getHeight(),
                viewPort.getWidth() * 5,
                viewPort.getHeight() * 5
        );
    }

    private void setListeners() {
        scrollPane.vvalueProperty().addListener(valuePropListener);
        scrollPane.hvalueProperty().addListener(valuePropListener);
        scrollPane.viewportBoundsProperty().addListener(viewportChangeListener);
    }

    private ChangeListener valuePropListener = (observable, oldValue, newValue) -> {
        updateIfNeeded();
    };

    private ChangeListener viewportChangeListener = (observable, oldValue, newValue) -> {
        addCanvasComponentsFromDB();
    };


    private void removeListeners() {
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

        scrollPane.setVvalue(vScrollBarPos.getOrDefault(threadId, 0.0));
        scrollPane.setHvalue(hScrollBarPos.getOrDefault(threadId, 0.0));
    }

    public void moveScrollPane(double xCord, double yCord){
        scrollPane.setHvalue(getHValue(xCord));
        scrollPane.setVvalue(getVValue(yCord));
    }

    private double getHValue(double xCoordinate) {
        double scaledContentWidth = scrollPane.getContent().getLayoutBounds().getWidth();// * scale;
        double scaledViewportWidth = scrollPane.getViewportBounds().getWidth(); // / scale;

        return xCoordinate / (scaledContentWidth - scaledViewportWidth);
    }

    private double getVValue(double yCoordinate) {
        double scaledContentHeight = scrollPane.getContent().getLayoutBounds().getHeight();// * scale;
        double scaledViewportHeight = scrollPane.getViewportBounds().getHeight(); // / scale;

        return yCoordinate / (scaledContentHeight - scaledViewportHeight);
    }

    public void stackRectangles() {

        List<HighlightCell> list = new ArrayList<>(highlightsOnUI.values());

        // Sort the list of rectangles according to area.
        list.sort((o1, o2) -> (int) (o1.getBoundsInParent().getWidth() * o1.getBoundsInParent().getHeight()
                - o2.getBoundsInParent().getWidth() * o2.getBoundsInParent().getHeight()));
        list.forEach(Node::toBack);

        edgesOnUI.forEach((id, edge) -> edge.toFront());
        circleCellsOnUI.forEach((id, nodeCell) -> nodeCell.toFront());
    }

    public void jumpTo(String cellId, String threadId, int collapsed) {
        // collapsed value might have changes since the last time event handlers were set on the the bookmarks buttons.
        // get new collapsed value.
        ElementDTO elementDTO = ElementDAOImpl.getElementDTO(cellId);
        ControllerLoader.eventHandlers.jumpTo(cellId, threadId, elementDTO.getCollapsed());
    }

    public void onReset() {
        clearAll();

        circleCellsOnUI = new HashMap<>();
        edgesOnUI = new HashMap<>();
        highlightsOnUI = new HashMap<>();
    }

    public Map<Integer, HighlightCell> getHighlightsOnUI() {
        return highlightsOnUI;
    }
}
