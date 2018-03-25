package com.application.controller;

import com.application.db.DAO.DAOImplementation.CallTraceDAOImpl;
import com.application.db.model.Bookmark;
import com.application.fxgraph.cells.CircleCell;
import com.application.fxgraph.graph.Edge;
import com.application.fxgraph.graph.RectangleCell;
import com.application.presentation.graph.ZoomableScrollPane;
import com.application.service.modules.ElementTreeModule;
import com.application.service.modules.GraphLoaderModule;
import com.application.service.modules.ModuleLocator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import sun.security.pkcs11.Secmod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class CenterLayoutController {
    @FXML private ToggleButton settingsToggleButton;
    @FXML private ToggleButton bookmarksToggleButton;
    @FXML private SplitPane verticalSplitPane;
    @FXML private SplitPane horizontalSplitPane;

    @FXML private ListView<String> threadListView;
    @FXML private AnchorPane centerAnchorPane;

    private DoubleProperty verticalSplitPanePosProperty;
    private DoubleProperty horizontalSplitPanePosProperty;
    private ObservableList<String> threadsObsList;
    private GraphLoaderModule graphLoaderModule;

    private ZoomableScrollPane scrollPane;
    private Group canvasContainer;
    private Pane canvas;

    private Map<String, CircleCell> circleCellsOnUI = new HashMap<>();

    private Map<String, Edge> edgesOnUI = new HashMap<>();
    private Map<Integer, com.application.fxgraph.graph.RectangleCell> highlightsOnUI = new HashMap<>();
    private Map<String, Bookmark> bookmarkMap = new HashMap<>();

    @FXML
    private void initialize() {
        setUpPaneButtonsActions();
        graphLoaderModule = ModuleLocator.getGraphLoaderModule();
    }

    public void injectController() {
        ModuleLocator.getGraphLoaderModule().inject(this);
    }

    private void setUpCenterLayout() {
        canvasContainer = new Group();
        canvas = new Pane();
        scrollPane = new ZoomableScrollPane(canvasContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        centerAnchorPane.getChildren().add(scrollPane);
    }

    private void resetCenterLayout() {

    }


    private void setUpPaneButtonsActions() {
        verticalSplitPanePosProperty = verticalSplitPane.getDividers().get(0).positionProperty();
        horizontalSplitPanePosProperty = horizontalSplitPane.getDividers().get(0).positionProperty();

        settingsToggleButton.setOnAction(event -> paneActions());
        bookmarksToggleButton.setOnAction(event -> paneActions());
    }

    private void paneActions() {
        KeyValue vSplitPaneKeyVal = null;
        KeyValue hSplitPaneKeyVal = null;
        if (settingsToggleButton.isSelected() && bookmarksToggleButton.isSelected()) {
            System.out.println("both selected");
            vSplitPaneKeyVal = new KeyValue(verticalSplitPanePosProperty, 0.3);
            hSplitPaneKeyVal = new KeyValue(horizontalSplitPanePosProperty, 0.5);
        } else if (settingsToggleButton.isSelected()) {
            System.out.println("settings selected");
            vSplitPaneKeyVal = new KeyValue(verticalSplitPanePosProperty, 0.3);
            hSplitPaneKeyVal = new KeyValue(horizontalSplitPanePosProperty, 1.0);
        } else if (bookmarksToggleButton.isSelected()) {
            System.out.println("bookmarks selected");
            vSplitPaneKeyVal = new KeyValue(verticalSplitPanePosProperty, 0.3);
            hSplitPaneKeyVal = new KeyValue(horizontalSplitPanePosProperty, 0);
        } else {
            System.out.println("none selected");
            vSplitPaneKeyVal = new KeyValue(verticalSplitPanePosProperty, 0);
            hSplitPaneKeyVal = new KeyValue(horizontalSplitPanePosProperty, 0);
        }
        new Timeline(new KeyFrame(Duration.seconds(0.3), vSplitPaneKeyVal)).play();
        new Timeline(new KeyFrame(Duration.seconds(0.3), hSplitPaneKeyVal)).play();
    }

    public void setUpThreadsListView() {
        threadsObsList = FXCollections.observableArrayList();
        threadListView.setItems(threadsObsList);

        threadListView.setOnMouseClicked((e) -> {
            String threadId = threadListView.getSelectionModel().getSelectedItem().split(" ")[1];
            ElementTreeModule.resetRegions();
            if (!String.valueOf(graphLoaderModule.getCurrentSelectedThread()).equalsIgnoreCase(threadId)) {
                showThread(threadId);
            }
        });

        final String[] maxLenThreadName = {};
        List<Integer> threadIds = CallTraceDAOImpl.getDistinctThreadIds();

        threadIds.forEach(id -> {
            String val = "Thread: " + id;
            maxLenThreadName[0] = maxLenThreadName[0].length() < val.length()? val : maxLenThreadName[0];
            threadsObsList.add("Thread: " + id);
        });

        Text text = new Text(maxLenThreadName[0]);
        double maxWidth = text.getLayoutBounds().getWidth();

        threadListView.setMaxWidth(maxWidth + 30);
    }

    public void update() {
        if (isUIDrawingRequired()) {
            graphLoaderModule.update();
        }
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

    // Region where UI components are loaded.

    private static BoundingBox activeRegion;
    // Trigger UI components to be reloaded when visible viewport is outside this region. triggerRegion < activeRegion

    private static BoundingBox triggerRegion;
    private static boolean firstLoad = true;

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
