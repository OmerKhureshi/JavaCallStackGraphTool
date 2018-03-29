package com.application.controller;

import com.application.db.DTO.ElementDTO;
import com.application.db.model.Bookmark;
import com.application.fxgraph.cells.CircleCell;
import com.application.fxgraph.graph.Edge;
import com.application.fxgraph.graph.RectangleCell;
import com.application.presentation.graph.ZoomableScrollPane;
import com.application.service.modules.GraphLoaderModule;
import com.application.service.modules.ModuleLocator;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

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
    private static BoundingBox activeRegion;

    // Trigger UI components to be reloaded when visible viewport is outside this region. triggerRegion < activeRegion
    private static BoundingBox triggerRegion;
    private static boolean firstLoad = true;


    private void initialize() {
        graphLoaderModule = ModuleLocator.getGraphLoaderModule();
    }

    private void setUpCenterLayout() {
        canvasContainer = new Group();
        canvas = new Pane();
        scrollPane = new ZoomableScrollPane(canvasContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        centerAnchorPane.getChildren().add(scrollPane);
    }


    public void update() {
        if (isUIDrawingRequired()) {
            graphLoaderModule.update();
        }
    }

    /**
     * This methods creates and draws circle cells on the active region of the viewport.
     */
    public void loadCircles() {
        List<ElementDTO> elementDTOList = graphLoaderModule.addCircleCellsNew(getViewPortDims());
        List<CircleCell> circleCells = ControllerUtil.convertElementDTOTOCell(elementDTOList);
        drawCircles(circleCells);
    }

    private void drawCircles(List<CircleCell> circleCells) {
        circleCells.forEach(circleCell -> circleCellsOnUI.put(circleCell.getCellId(), circleCell));
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

        return new BoundingBox(minX, minY, scaledViewportWidth, scaledViewportHeight);
    }


    public boolean isUIDrawingRequired() {
        BoundingBox viewPort = getViewPortDims();
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

        // if (graph.getModel().uiUpdateRequired) {
        //     // System.out.println("ElementTreeModule::UiUpdateRequired: passed true");
        //     return true;
        // }

        return false;
    }

    private void setActiveRegion(BoundingBox viewPort) {
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
}
