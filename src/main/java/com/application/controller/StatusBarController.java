package com.application.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;



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
}
