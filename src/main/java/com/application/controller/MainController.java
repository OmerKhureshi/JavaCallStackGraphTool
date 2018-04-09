package com.application.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainController {
    @FXML private BorderPane borderPane;

    @FXML private MenuController menuBarController;
    @FXML private CenterLayoutController centerLayoutController;

    @FXML protected void initialize() {
        menuBarController.setParentController(this);
    }

    void showInstructionsPane() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/fxml/instructionsPane.fxml"));
            Node content = fxmlLoader.load();
            borderPane.setCenter(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void loadGraphPane() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/fxml/centerLayout.fxml"));
            Node content = fxmlLoader.load();
            borderPane.setCenter(content);

            centerLayoutController = fxmlLoader.getController();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
