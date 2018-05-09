package com.csgt.controller;

import com.csgt.dataaccess.DAO.CallTraceDAOImpl;
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
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class CenterLayoutController {
    @FXML
    private ToggleButton threadsToggleButton;

    // @FXML
    // private ToggleButton bookmarksToggleButton;

    @FXML
    private SplitPane verticalSplitPane;

    // @FXML
    // private SplitPane horizontalSplitPane;


    @FXML private ListView<String> threadListView;

    // @FXML private ListView<String> bookmarkListView;

    @FXML
    private AnchorPane canvas;

    @FXML
    private CanvasController canvasController;

    private DoubleProperty verticalSplitPanePosProperty;
    private DoubleProperty horizontalSplitPanePosProperty;
    private ObservableList<String> threadsObsList;
    private ObservableList<String> bookmarkObsList;


    public void setCurrentThreadId(String currentThreadId) {
        CenterLayoutController.currentThreadId = currentThreadId;
    }

    private static String currentThreadId;

    private double sidePaneAnimationDuration = .01;


    @FXML
    private void initialize() {
        ControllerLoader.register(this);

        setUpPaneButtonsActions();
        setUpThreadsListView();
        canvasController.setUp();
    }

    private void setUpPaneButtonsActions() {
        threadsToggleButton.setSelected(false);
        threadsToggleButton.setStyle("-fx-focus-color: transparent; " +
                "-fx-background-color: -fx-outer-border, -fx-inner-border, -fx-body-color; " +
                "-fx-background-insets: 0, 1, 2; " +
                "-fx-background-radius: 5, 4, 3;");
        verticalSplitPanePosProperty = verticalSplitPane.getDividers().get(0).positionProperty();
        verticalSplitPanePosProperty.setValue(0);

        // horizontalSplitPanePosProperty = horizontalSplitPane.getDividers().get(0).positionProperty();

        threadsToggleButton.setOnAction(event -> paneActionsForBookmarksButton());
        // bookmarksToggleButton.setOnAction(event -> paneActions());
    }

    // private void paneActions() {
    //     KeyValue vSplitPaneKeyVal = null;
    //     KeyValue hSplitPaneKeyVal = null;
    //
    //     if (settingsToggleButton.isSelected() && bookmarksToggleButton.isSelected()) {
    //         System.out.println("both selected");
    //         vSplitPaneKeyVal = new KeyValue(verticalSplitPanePosProperty, 0.3);
    //         hSplitPaneKeyVal = new KeyValue(horizontalSplitPanePosProperty, 0.5);
    //     } else if (settingsToggleButton.isSelected()) {
    //         System.out.println("settings selected");
    //         vSplitPaneKeyVal = new KeyValue(verticalSplitPanePosProperty, 0.3);
    //         hSplitPaneKeyVal = new KeyValue(horizontalSplitPanePosProperty, 1.0);
    //     } else if (bookmarksToggleButton.isSelected()) {
    //         System.out.println("bookmarks selected");
    //         vSplitPaneKeyVal = new KeyValue(verticalSplitPanePosProperty, 0.3);
    //         hSplitPaneKeyVal = new KeyValue(horizontalSplitPanePosProperty, 0);
    //     } else {
    //         System.out.println("none selected");
    //         vSplitPaneKeyVal = new KeyValue(verticalSplitPanePosProperty, 0);
    //         hSplitPaneKeyVal = new KeyValue(horizontalSplitPanePosProperty, 0);
    //     }
    //     new Timeline(new KeyFrame(Duration.seconds(sidePaneAnimationDuration), vSplitPaneKeyVal)).play();
    //     new Timeline(new KeyFrame(Duration.seconds(sidePaneAnimationDuration), hSplitPaneKeyVal)).play();
    // }


    private void paneActionsForBookmarksButton() {
        KeyValue vSplitPaneKeyVal = null;

         if (threadsToggleButton.isSelected()) {
            vSplitPaneKeyVal = new KeyValue(verticalSplitPanePosProperty, 0.15);
        } else {
            vSplitPaneKeyVal = new KeyValue(verticalSplitPanePosProperty, 0);
        }

        new Timeline(new KeyFrame(Duration.seconds(sidePaneAnimationDuration), vSplitPaneKeyVal)).play();
    }

    public void setUpThreadsListView() {
        threadsObsList = FXCollections.observableArrayList();
        threadListView.setItems(threadsObsList);

        threadListView.setOnMouseClicked((e) -> {
            if (threadsObsList.size() == 0) {
                return;
            }
            String threadId = threadListView.getSelectionModel().getSelectedItem().split(" ")[1];
            switchCurrentThread(threadId);
        });

        final String[] maxLenThreadName = {};
        List<Integer> threadIds = CallTraceDAOImpl.getDistinctThreadIds();

        threadsObsList.addAll(threadIds.stream().map(id -> "Thread: " + id).collect(Collectors.toList()));

        // highlight first thread
        if (threadsObsList.size() > 0) {
            currentThreadId = threadsObsList.get(0).split(" ")[1];
            threadListView.getSelectionModel().select(0);
        }

        // set width of threads pane based on text width.
        // Text text = new Text(String.valueOf(threadIds.get(threadIds.size() - 1)));
        // double maxWidth = text.getLayoutBounds().getWidth();
        //
        // threadListView.setMaxWidth(maxWidth + 30);
        // threadListView.setPrefWidth(maxWidth + 30);
    }

    public void switchCurrentThread(String threadId) {
        if (!currentThreadId.equalsIgnoreCase(threadId)) {
            canvasController.saveScrollBarPos();
            currentThreadId = threadId;
            canvasController.onThreadSelect();
            int ind = threadsObsList.indexOf("Thread: " + threadId);
            threadListView.getSelectionModel().select(ind);
        }
    }

    public String getCurrentThreadId() {
        return currentThreadId;
    }

}
