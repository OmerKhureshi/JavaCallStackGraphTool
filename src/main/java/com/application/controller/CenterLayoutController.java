package com.application.controller;

import com.application.db.DAO.DAOImplementation.CallTraceDAOImpl;
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
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;

@SuppressWarnings("ALL")
public class CenterLayoutController {
    @FXML private ToggleButton settingsToggleButton;
    @FXML private ToggleButton bookmarksToggleButton;
    @FXML private SplitPane verticalSplitPane;
    @FXML private SplitPane horizontalSplitPane;

    @FXML private ListView<String> threadListView;

    @FXML
    private AnchorPane canvas;

    @FXML
    private CanvasController canvasController;

    private DoubleProperty verticalSplitPanePosProperty;
    private DoubleProperty horizontalSplitPanePosProperty;
    private ObservableList<String> threadsObsList;
    private GraphLoaderModule graphLoaderModule;

    private String currentThreadId = "0";


    @FXML
    private void initialize() {
        System.out.println("CenterLayoutController.initialize");
        setUpPaneButtonsActions();
        graphLoaderModule = ModuleLocator.getGraphLoaderModule();
        if (canvasController == null) {
            System.out.println("CenterLayoutController.initialize canvasController is null.");
        }
        canvasController.setUp(this);
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
                currentThreadId = threadId;
                System.out.println("CenterLayoutController.setUpThreadsListView: changed thread to : " + threadId);
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
