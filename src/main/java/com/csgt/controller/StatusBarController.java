package com.csgt.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;


public class StatusBarController {
    @FXML
    private Label leftStatusText;

    @FXML
    private void initialize() {
        ControllerLoader.register(this);
    }

    public void setStatusText(String text) {
        leftStatusText.setText(text);
    }

    public void setTimedStatusText(String initialText, String laterText, double ms) {
        Platform.runLater(() -> {
            ControllerLoader.statusBarController.setStatusText(initialText);

            Timeline idleWait = new Timeline(new KeyFrame(Duration.millis(ms),
                    event -> ControllerLoader.statusBarController.setStatusText(laterText)));

            idleWait.setCycleCount(1);
            idleWait.play();
        });
    }
}
