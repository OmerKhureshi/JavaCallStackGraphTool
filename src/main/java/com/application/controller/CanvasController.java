package com.application.controller;

import com.application.db.DTO.EdgeDTO;
import com.application.db.DTO.ElementDTO;
import com.application.db.model.Bookmark;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CanvasController {

    @FXML
    AnchorPane canvasAnchorPane;

    private GraphLoaderModule graphLoaderModule;
    public Pane canvas;
    public ZoomableScrollPane scrollPane;

    private Map<String, CircleCell> circleCellsOnUI = new HashMap<>();
    private Map<String, Edge> edgesOnUI = new HashMap<>();
    private Map<Integer, com.application.fxgraph.graph.RectangleCell> highlightsOnUI = new HashMap<>();
    private Map<String, Bookmark> bookmarkMap = new HashMap<>();

    // Region where UI components are loaded.
    private static BoundingBox activeRegion = null;

    // Trigger UI components to be reloaded when visible viewport is outside this region. triggerRegion < activeRegion
    private static BoundingBox triggerRegion = null;

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
        // Group canvasContainer = new Group();
        // canvasContainer.getChildren().add(canvas);
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

        // canvas.setStyle("-fx-background-color: blue;");
    }


    /**
     * This method is invoked to check and draw any UI components on the graph if needed.
     * @param checkIsDrawingRequired
     */
    void updateIfNeeded(boolean checkIsDrawingRequired) {
        if (checkIsDrawingRequired && isUIDrawingRequired()) {
            update();
        } else {
            update();
        }
    }

    /**
     * This methods creates and draws circle cells and Edges on the active region of the viewport.
     */
    private void update() {
        BoundingBox viewPort = getViewPortDims();
        updateCircles(viewPort);
        updateEdges(viewPort);
    }

    private void updateCircles(BoundingBox viewPort) {
        List<ElementDTO> elementDTOList = graphLoaderModule.addCircleCellsNew(viewPort);
        List<CircleCell> circleCells = ControllerUtil.convertElementDTOTOCell(elementDTOList);

        System.out.println();
        System.out.println("finally adding to UI.");
        circleCells.forEach(circleCell -> {
            if (!circleCellsOnUI.containsKey(circleCell.getCellId())) {
                circleCellsOnUI.put(circleCell.getCellId(), circleCell);
                canvas.getChildren().add(circleCell);
                circleCell.toFront();
                System.out.print(circleCell.getCellId() + ", ");
            }
        });
    }

    private void updateEdges(BoundingBox viewPort) {
        List<EdgeDTO> edgeDTOList = graphLoaderModule.addEdgesNew(viewPort);
        List<Edge> edges = ControllerUtil.convertEdgeDTOToEdges(edgeDTOList);

        edges.forEach(edge -> {
            if (!edgesOnUI.containsKey(edge.getEdgeId())) {
                edgesOnUI.put(edge.getEdgeId(), edge);
                canvas.getChildren().add(edge);
                edge.toBack();
            }
        });
    }

    private void clear() {
        canvas.getChildren().clear();
        circleCellsOnUI.clear();
        edgesOnUI.clear();
    }

    public void onThreadSelect() {
        clear();
        drawPlaceHolderLines();
        updateIfNeeded(false);
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


    private boolean isUIDrawingRequired() {
        if (activeRegion == null) {
            setActiveRegion();
        }

        if (triggerRegion == null) {
            setTriggerRegion();
        }

        if (!triggerRegion.contains(getViewPortDims())) {
            setActiveRegion();
            setTriggerRegion();
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

    /*
    public static void resetRegions() {
        activeRegion = null;
        triggerRegion = null;
        firstLoad = true;
    }*/

    public Map<String, CircleCell> getCircleCellsOnUI() {
        return circleCellsOnUI;
    }

    public Map<String, Edge> getEdgesOnUI() {
        return edgesOnUI;
    }

    public Map<Integer, RectangleCell> getHighlightsOnUI() {
        return highlightsOnUI;
    }

    public Map<String, Bookmark> getBookmarkMap() {
        return bookmarkMap;
    }

    private void setListeners() {
        scrollPane.vvalueProperty().addListener(valuePropListener);
        scrollPane.hvalueProperty().addListener(valuePropListener);
        scrollPane.viewportBoundsProperty().addListener(valuePropListener);
    }

    private ChangeListener valuePropListener = (observable, oldValue, newValue) -> updateIfNeeded(true);

    private void removeListeners() {
        scrollPane.vvalueProperty().removeListener(valuePropListener);
        scrollPane.hvalueProperty().removeListener(valuePropListener);
        scrollPane.viewportBoundsProperty().removeListener(valuePropListener);
    }

    private void drawPlaceHolderLines() {
        String currentThreadId = ControllerLoader.centerLayoutController.getCurrentThreadId();;
        int height = graphLoaderModule.computePlaceHolderHeight(currentThreadId);
        int width = graphLoaderModule.computePlaceHolderWidth(currentThreadId);

        Line hPlaceHolderLine = new Line(0, 0, (width + 2) * BoundBox.unitWidthFactor, 0);
        hPlaceHolderLine.setStrokeWidth(5);
        canvas.getChildren().add(hPlaceHolderLine);

        Line vPlaceHolderLine = new Line(0, 0, 0, height * BoundBox.unitHeightFactor);
        vPlaceHolderLine.setStrokeWidth(5);
        canvas.getChildren().add(vPlaceHolderLine);

        getViewPortDims();
    }

}
