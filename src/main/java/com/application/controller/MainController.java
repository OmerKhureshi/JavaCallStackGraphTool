package com.application.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.IOException;

public class MainController {
    @FXML private BorderPane borderPane;

    @FXML private MenuController menuBarController;
    @FXML private CenterLayoutController centerLayoutController;

    @FXML protected void initialize() {
        System.out.println("MainController.initialize started.");
        menuBarController.setParentController(this);
    }

    void showInstructionsPane() {
        System.out.println("MainController.showInstructionsPane");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            System.out.println("MainController.showInstructionsPane: ");
            System.out.println("setting fxmlLoader locations: " + getClass().getResource("/fxml/instructionsPane.fxml"));
            fxmlLoader.setLocation(getClass().getResource("/fxml/instructionsPane.fxml"));
            Node content = fxmlLoader.load();
            borderPane.setCenter(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void showGraphPane() {
        System.out.println("MainController.showGraphPane");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            System.out.println("setting fxmlLoader locations: " + getClass().getResource("/fxml/centerLayout.fxml"));
            fxmlLoader.setLocation(getClass().getResource("/fxml/centerLayout.fxml"));
            Node content = fxmlLoader.load();
            borderPane.setCenter(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
