package com.csgt.presentation;

import com.csgt.presentation.graph.SizeProp;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CustomProgressBar {

    private ProgressBar progressBar;
    private Stage stage;
    private VBox vBox;
    private Label titleLabel;
    private Label progressLabel;

    private static final int PROGRESS_BAR_WIDTH = 676;

    public CustomProgressBar(String titleText, String progressText) {
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(PROGRESS_BAR_WIDTH);

        titleLabel = new Label(titleText);
        progressLabel = new Label(progressText);

        // taskList.forEach(task -> {
        //     progressBar.progressProperty().bind(task.progressProperty());
        //     titleLabel.textProperty().bind(task.titleProperty());
        //     progressLabel.textProperty().bind(task.messageProperty());
        // });

        vBox = new VBox();
        vBox.getChildren().addAll(titleLabel, progressLabel, progressBar);
        vBox.setSpacing(SizeProp.SPACING);
        vBox.setPadding(new Insets(10, 5, 5, 5));
        vBox.setAlignment(Pos.CENTER);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(null);
        stage.setScene(new Scene(vBox));
        stage.setTitle("Please wait while we crunch the logs");
        stage.show();
    }

    public void close() {
        stage.close();
    }

    public void bind(Task task) {
        progressBar.progressProperty().bind(task.progressProperty());
        titleLabel.textProperty().bind(task.titleProperty());
        progressLabel.textProperty().bind(task.messageProperty());
    }

}
