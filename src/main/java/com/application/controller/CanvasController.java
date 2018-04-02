package com.application.controller;

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
import javafx.scene.Group;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CanvasController {

    @FXML
    AnchorPane centerAnchorPane;

    private GraphLoaderModule graphLoaderModule;
    private Group canvasContainer;
    private Pane canvas;
    private ZoomableScrollPane scrollPane;
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
        System.out.println("CanvasController.initialize");
        graphLoaderModule = ModuleLocator.getGraphLoaderModule();
        setUpCenterLayout();
    }

    private void setUpCenterLayout() {
        canvas = new Pane();
        canvasContainer = new Group();
        canvasContainer.getChildren().add(canvas);
        scrollPane = new ZoomableScrollPane(canvasContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        centerAnchorPane.getChildren().add(scrollPane);

        update();
        drawPlaceHolderLines();
        // setListeners();


        Rectangle rect = new Rectangle(30, 60, Color.BLACK);
        canvas.getChildren().add(rect);
    }


    /**
     * This method is invoked to check and draws any UI components on the graph if needed.
     */
    public void update() {
        if (isUIDrawingRequired()) {
            loadCircles();
        }
    }

    /**
     * This methods creates and draws circle cells on the active region of the viewport.
     */
    private void loadCircles() {
        System.out.println("CanvasController.loadCircles");
        List<ElementDTO> elementDTOList = graphLoaderModule.addCircleCellsNew(getViewPortDims());
        List<CircleCell> circleCells = ControllerUtil.convertElementDTOTOCell(elementDTOList);
        drawCircles(circleCells);
        System.out.println("CanvasController.loadCircles ended");
    }

    private void drawCircles(List<CircleCell> circleCells) {
        circleCells.forEach(circleCell -> {
            if (!circleCellsOnUI.containsKey(circleCell.getCellId())) {
                circleCellsOnUI.put(circleCell.getCellId(), circleCell);
                canvas.getChildren().add(circleCell);
                circleCell.toFront();
            }
        });
    }

    private BoundingBox getViewPortDims() {
        double scale = ZoomableScrollPane.getScaleValue();

        double hValue = scrollPane.getHvalue();
        double scaledContentWidth = scrollPane.getContent().getLayoutBounds().getWidth();// * scale;
        double scaledViewportWidth = scrollPane.getViewportBounds().getWidth() / scale;

        double vValue = scrollPane.getVvalue();
        double scaledContentHeight = scrollPane.getContent().getLayoutBounds().getHeight();// * scale;
        double scaledViewportHeight = scrollPane.getViewportBounds().getHeight() / scale;

        double minX = hValue * (scaledContentWidth - scaledViewportWidth);
        double minY = vValue * (scaledContentHeight - scaledViewportHeight);

        return new BoundingBox(minX, minY, scaledViewportWidth, scaledViewportHeight);
    }


    private boolean isUIDrawingRequired() {
        // if (firstLoad) {
        //     firstLoad = false;
        //     return true;
        // }
        System.out.println("CanvasController.isUIDrawingRequired");
        System.out.println("active region: " + activeRegion + " ; trigger region: " + triggerRegion
                + " ; contains? " + triggerRegion.contains(getViewPortDims()));
        if (activeRegion == null)
            setActiveRegion();

        if (triggerRegion == null)
            setTriggerRegion();

        if (!triggerRegion.contains(getViewPortDims())) {
            setActiveRegion();
            setTriggerRegion();
            System.out.println("CanvasController.isUIDrawingRequired return true");
            return true;
        }

        // if (graph.getModel().uiUpdateRequired) {
        //     // System.out.println("ElementTreeModule::UiUpdateRequired: passed true");
        //     return true;
        // }

        System.out.println("CanvasController.isUIDrawingRequired return false");
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

        // System.out.println();
        // System.out.println("------------- New active region -------------");
        // System.out.println("Viewport: " + viewPort);
        // System.out.println("activeRegion: " + activeRegion);
        // System.out.println("triggerRegion: " + triggerRegion);
        // System.out.println("------------------");
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

        // System.out.println();
        // System.out.println("------------- New Triggering region -------------");
        // System.out.println("Viewport: " + viewPort);
        // System.out.println("activeRegion: " + activeRegion);
        // System.out.println("triggerRegion: " + triggerRegion);
        // System.out.println("------------------");
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

    private ChangeListener valuePropListener = (observable, oldValue, newValue) -> update();

    private void removeListeners() {
        scrollPane.vvalueProperty().removeListener(valuePropListener);
        scrollPane.hvalueProperty().removeListener(valuePropListener);
        scrollPane.viewportBoundsProperty().removeListener(valuePropListener);
    }



    public void drawPlaceHolderLines(String currentThreadId) {
        int height = graphLoaderModule.computePlaceHolderHeight(currentThreadId);
        int width = graphLoaderModule.computePlaceHolderWidth(currentThreadId);

        Line hPlaceHolderLine = new Line(0, 0, (width + 2) * BoundBox.unitWidthFactor, 0);
        hPlaceHolderLine.setStrokeWidth(.001);
        canvas.getChildren().add(hPlaceHolderLine);

        Line vPlaceHolderLine = new Line(0, 0, 0, height * BoundBox.unitHeightFactor);
        vPlaceHolderLine.setStrokeWidth(.001);
        canvas.getChildren().add(vPlaceHolderLine);
    }
}
