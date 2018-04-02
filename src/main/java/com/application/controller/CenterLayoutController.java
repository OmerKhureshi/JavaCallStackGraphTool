package com.application.controller;

import com.application.db.DAO.DAOImplementation.CallTraceDAOImpl;
import com.application.db.DTO.ElementDTO;
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

    private DoubleProperty verticalSplitPanePosProperty;
    private DoubleProperty horizontalSplitPanePosProperty;
    private ObservableList<String> threadsObsList;
    private GraphLoaderModule graphLoaderModule;

    private String currentThreadId = "0";


    @FXML
    private void initialize() {
        setUpPaneButtonsActions();
        graphLoaderModule = ModuleLocator.getGraphLoaderModule();
    }

    public void injectController() {
        ModuleLocator.getGraphLoaderModule().inject(this);
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
                // showThread(threadId);
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

    public String getCurrentThreadId() {
        return currentThreadId;
    }

}
