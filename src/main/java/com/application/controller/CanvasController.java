package com.application.controller;

import com.application.db.DAO.DAOImplementation.EdgeDAOImpl;
import com.application.db.DAO.DAOImplementation.ElementDAOImpl;
import com.application.db.DAO.DAOImplementation.HighlightDAOImpl;
import com.application.db.DTO.BookmarkDTO;
import com.application.db.DTO.EdgeDTO;
import com.application.db.DTO.ElementDTO;
import com.application.db.DTO.HighlightDTO;
import com.application.fxgraph.cells.CircleCell;
import com.application.fxgraph.graph.BoundBox;
import com.application.fxgraph.graph.Edge;
import com.application.fxgraph.graph.RectangleCell;
import com.application.presentation.graph.ZoomableScrollPane;
import com.application.service.modules.GraphLoaderModule;
import com.application.service.modules.ModuleLocator;
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

    private GraphLoaderModule graphLoaderModule;
    public Pane canvas;
    public ZoomableScrollPane scrollPane;

    public Map<String, CircleCell> getCircleCellsOnUI() {
        return circleCellsOnUI;
    }

    private Map<String, CircleCell> circleCellsOnUI = new HashMap<>();

    public Map<String, Edge> getEdgesOnUI() {
        return edgesOnUI;
    }

    private Map<String, Edge> edgesOnUI = new HashMap<>();

    public Map<Integer, RectangleCell> getHighlightsOnUI() {
        return highlightsOnUI;
    }

    private Map<Integer, com.application.fxgraph.graph.RectangleCell> highlightsOnUI = new HashMap<>();

    // Region where UI components are loaded.
    private static BoundingBox activeRegion = null;
    // Trigger UI components to be reloaded when visible viewport is outside this region. triggerRegion < activeRegion
    private static BoundingBox triggerRegion = null;

    private Map<String, Double> vScrollBarPos = new HashMap<>();
    private Map<String, Double> hScrollBarPos = new HashMap<>();


    @FXML
    private void initialize() {
        graphLoaderModule = ModuleLocator.getGraphLoaderModule();
        ControllerLoader.register(this);
    }

    public void setUp() {
        setUpCenterLayout();
    }

    private void setUpCenterLayout() {
        canvas = new Pane();
        scrollPane = new ZoomableScrollPane(canvas);
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

        // System.out.println("CanvasController.addCirclesToUI prefetch viewport: "+ viewPort);
        List<ElementDTO> elementDTOList = ElementDAOImpl.getElementDTOsInViewport(viewPort);
        List<CircleCell> circleCells = ControllerUtil.convertElementDTOTOCell(elementDTOList);

        // System.out.println();
        circleCells.forEach(this::addNewCellToUI);
    }

    /**
     * Draws a new element node on UI if not already present.
     *
     * @param circleCell
     */
    private void addNewCellToUI(CircleCell circleCell) {
        if (!circleCellsOnUI.containsKey(circleCell.getCellId())) {
            circleCellsOnUI.put(circleCell.getCellId(), circleCell);
            canvas.getChildren().add(circleCell);
            circleCell.toFront();
            ControllerLoader.getEventHandlers().setCustomMouseEventHandlers(circleCell);
            // System.out.print("+" + circleCell.getCellId() + ", ");
        }
    }

    private void removeCirclesFromUI() {
        List<CircleCell> removeCircleCellsList = circleCellsOnUI.values().stream()
                .filter(circleCell -> !activeRegion.contains(circleCell.getBoundsInParent()))
                .collect(Collectors.toList());

        removeCircleCellsList.forEach(this::removeCellFromUI);
    }

    private void removeCellFromUI(CircleCell circleCell) {
        if (circleCell != null && circleCellsOnUI.containsKey(circleCell.getCellId())) {
            circleCellsOnUI.remove(circleCell.getCellId());
            canvas.getChildren().remove(circleCell);
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
        // System.out.println("CanvasController.removeUIComponentsBetween");
        int startCellId = Integer.valueOf(elementDTO.getId());
        float clickedCellTopRightX =  elementDTO.getBoundBoxXTopRight();
        float clickedCellTopY = elementDTO.getBoundBoxYTopLeft();
        float leafCount = elementDTO.getLeafCount();
        float clickedCellHeight = leafCount * BoundBox.unitHeightFactor;
        float clickedCellBottomY = clickedCellTopY + BoundBox.unitHeightFactor;
        float clickedCellBoundBottomY = elementDTO.getBoundBoxYBottomLeft();

        List<CircleCell> removeCircleCells = new ArrayList<>();
        List<Edge> removeEdges = new ArrayList<>();

        Map<Integer, RectangleCell> highlightsOnUi = ControllerLoader.canvasController.getHighlightsOnUI();
        List<RectangleCell> removeHighlights = new ArrayList<>();

        circleCellsOnUI.forEach((id, circleCell) -> {
            int intId = Integer.parseInt(id);
            if (intId > startCellId && intId < endCellId) {
                removeCircleCells.add(circleCell);
            }

            // Remove all children cells and edges that end at these cells from UI

            double thisCellTopLeftX = circleCell.getLayoutX();
            double thisCellTopY = circleCell.getLayoutY();

            if (!removeCircleCells.contains(circleCell) && thisCellTopY >= clickedCellBottomY && thisCellTopY < clickedCellBoundBottomY && thisCellTopLeftX > clickedCellTopRightX) {
                // if (thisCellTopY >= clickedCellTopY ) {
                // System.out.println(" -" + id);
                // System.out.println(" --" + id);
                removeCircleCells.add(circleCell);
                removeEdges.add(edgesOnUI.get(id));
            } else if (!removeCircleCells.contains(circleCell) && thisCellTopY == clickedCellTopY && thisCellTopLeftX >= clickedCellTopRightX) {
                // System.out.println(" -" + id);
                // System.out.println(" --" + id);
                removeCircleCells.add(circleCell);
                removeEdges.add(edgesOnUI.get(id));
            }
        });

        removeCircleCells.forEach((circleCell) -> {
            ControllerLoader.canvasController.removeCellFromUI(circleCell);
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

        removeHighlights.forEach((rectangleCell) -> {
            if (highlightsOnUi.containsKey(rectangleCell)) {
                highlightsOnUi.remove(rectangleCell);
                canvas.getChildren().remove(rectangleCell);
            }
        });
    }

    public void moveLowerTreeByDelta(ElementDTO elementDTO) {

        float clickedCellBottomY = elementDTO.getBoundBoxYTopLeft() + BoundBox.unitHeightFactor;
        double delta = elementDTO.getDeltaY();

        // For each circle cell on UI that is below the clicked cell, move up by delta
        circleCellsOnUI.forEach((thisCellID, thisCircleCell) -> {
            double thisCellTopY = thisCircleCell.getLayoutY();

            if (thisCellTopY >= clickedCellBottomY) {
                thisCircleCell.relocate(thisCircleCell.getLayoutX(), thisCellTopY - delta);
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
        highlightsOnUI.forEach((id, rectangleCell) -> {
            double y = rectangleCell.getLayoutY();
            if (y >= clickedCellBottomY - BoundBox.unitHeightFactor/2) {
                rectangleCell.relocate(rectangleCell.getLayoutX(), y - delta);
            }
        });

    }


    private void addHighlightsToUI() {
        BoundingBox viewPort = getPrefetchViewPortDims();

        List<HighlightDTO> highlightDTOs = HighlightDAOImpl.getHighlightDTOsInViewPort(viewPort);
        List<RectangleCell> highlightRectList = ControllerUtil.convertHighlightDTOsToHighlights(highlightDTOs);

        highlightRectList.forEach(this::addNewHighlightsToUI);
    }

    private void addNewHighlightsToUI(RectangleCell rectangleCell) {
        if (!highlightsOnUI.containsKey(rectangleCell.getElementId())) {
            highlightsOnUI.putIfAbsent(rectangleCell.getElementId(), rectangleCell);
            canvas.getChildren().add(rectangleCell);
        }
    }

    private void removeHighlightsFromUI() {
        List<RectangleCell> removeCircleCellsList = highlightsOnUI.values().stream()
                .filter(rectangleCell -> !activeRegion.intersects(rectangleCell.getBoundsInParent()))
                .collect(Collectors.toList());

        removeCircleCellsList.forEach(this::removeHighlightFromUI);
    }

    private void removeHighlightFromUI(RectangleCell rectangleCell) {
        if (rectangleCell != null && highlightsOnUI.containsKey(rectangleCell.getCellId())) {
            highlightsOnUI.remove(rectangleCell.getCellId());
            canvas.getChildren().remove(rectangleCell);
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

        circleCellsOnUI.forEach((id, circleCell) -> {
            if (bookmarkDTOs.containsKey(id)) {
                circleCell.removeBookmark();
            }
        });
    }


    /**
     * Clears all UI components on canvas except the placeholder lines.
     */
    private void clear() {
        circleCellsOnUI.forEach((id, circleCell) -> {
            canvas.getChildren().remove(circleCell);
        });
        circleCellsOnUI.clear();
        // bookmarks are part of circle cells, so no need to remove them explicitly.

        edgesOnUI.forEach((id, edge) -> {
            canvas.getChildren().remove(edge);
        });
        edgesOnUI.clear();

        highlightsOnUI.forEach((id, rectangleCell) -> {
            canvas.getChildren().remove(rectangleCell);
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
        double scale = ZoomableScrollPane.getScaleValue();

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
        int height = graphLoaderModule.computePlaceHolderHeight(currentThreadId);
        int width = graphLoaderModule.computePlaceHolderWidth(currentThreadId);

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

        List<com.application.fxgraph.graph.RectangleCell> list = new ArrayList<>(highlightsOnUI.values());

        // Sort the list of rectangles according to area.
        list.sort((o1, o2) -> (int) (o1.getBoundsInParent().getWidth() * o1.getBoundsInParent().getHeight()
                - o2.getBoundsInParent().getWidth() * o2.getBoundsInParent().getHeight()));
        list.forEach(Node::toBack);

        edgesOnUI.forEach((id, edge) -> edge.toFront());
        circleCellsOnUI.forEach((id, circleCell) -> circleCell.toFront());
    }

    public void jumpTo(String cellId, String threadId, int collapsed) {
        // collapsed value might have changes since the last time event handlers were set on the the bookmarks buttons.
        // get new collapsed value.
        ElementDTO elementDTO = ElementDAOImpl.getElementDTO(cellId);
        ControllerLoader.eventHandlers.jumpTo(cellId, threadId, elementDTO.getCollapsed());
    }

}
