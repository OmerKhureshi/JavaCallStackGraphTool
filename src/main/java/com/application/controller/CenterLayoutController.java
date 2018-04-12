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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class CenterLayoutController {
    @FXML
    private ToggleButton settingsToggleButton;

    @FXML
    private ToggleButton bookmarksToggleButton;

    @FXML
    private SplitPane verticalSplitPane;

    @FXML
    private SplitPane horizontalSplitPane;


    @FXML private ListView<String> threadListView;

    @FXML private ListView<String> bookmarkListView;

    @FXML
    private AnchorPane canvas;

    @FXML
    private CanvasController canvasController;

    private DoubleProperty verticalSplitPanePosProperty;
    private DoubleProperty horizontalSplitPanePosProperty;
    private ObservableList<String> threadsObsList;
    private ObservableList<String> bookmarkObsList;

    private GraphLoaderModule graphLoaderModule;

    private static String currentThreadId;

    private double sidePaneAnimationDuration = .05;


    @FXML
    private void initialize() {
        ControllerLoader.register(this);
        setUpPaneButtonsActions();
        graphLoaderModule = ModuleLocator.getGraphLoaderModule();
        setUpThreadsListView();
        canvasController.setUp();
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
        new Timeline(new KeyFrame(Duration.seconds(sidePaneAnimationDuration), vSplitPaneKeyVal)).play();
        new Timeline(new KeyFrame(Duration.seconds(sidePaneAnimationDuration), hSplitPaneKeyVal)).play();
    }

    public void setUpThreadsListView() {
        threadsObsList = FXCollections.observableArrayList();
        threadListView.setItems(threadsObsList);

        threadListView.setOnMouseClicked((e) -> {
            String threadId = threadListView.getSelectionModel().getSelectedItem().split(" ")[1];
            ElementTreeModule.resetRegions();
            if (!String.valueOf(graphLoaderModule.getCurrentSelectedThread()).equalsIgnoreCase(threadId)) {
                canvasController.saveScrollBarPos();
                currentThreadId = threadId;
                canvasController.onThreadSelect();
            }
        });

        final String[] maxLenThreadName = {};
        List<Integer> threadIds = CallTraceDAOImpl.getDistinctThreadIds();

        threadsObsList.addAll(threadIds.stream().map(id -> "Thread: " + id).collect(Collectors.toList()));

        if (threadsObsList.size() > 0) {
            currentThreadId = threadsObsList.get(0).split(" ")[1];
            threadListView.getSelectionModel().select(0);
        }

        Text text = new Text(String.valueOf(threadIds.get(threadIds.size() - 1)));
        double maxWidth = text.getLayoutBounds().getWidth();

        threadListView.setMaxWidth(maxWidth + 30);
        threadListView.setPrefWidth(maxWidth + 30);
    }


    public void setUpBookmarkListView() {

        bookmarkListView.setCellFactory(bookmarkView -> new ListCell<String>() {

            private ImageView imageView = new ImageView();

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // true makes this load in background
                    // see other constructors if you want to control the size, etc
                    Image image = new Image(item, true) ;
                    imageView.setImage(image);
                    setGraphic(imageView);
                }
            }
        });
    }

    public String getCurrentThreadId() {
        return currentThreadId;
    }

}
