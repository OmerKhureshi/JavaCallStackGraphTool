package com.csgt.controller;

import com.csgt.dataaccess.DatabaseUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

import java.io.IOException;

/**
 * This is the main controller which handles displaying the instructions during application startup and loading canvas after running analysis.
 */
public class MainController {
    @FXML private BorderPane borderPane;

    public boolean alertShown = false;

    @FXML protected void initialize() {
        ControllerLoader.register(this);

        showInstructionsPane();
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
        if (!DatabaseUtil.isDBValid()) {
            System.out.println("MainController.loadGraphPane. dataaccess not valid.");
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/fxml/centerLayout.fxml"));
            Node content = fxmlLoader.load();
            borderPane.setCenter(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showErrorPopup(String title, String header, String message) {
        Platform.runLater(() -> {
            System.out.println("MainController.showErrorPopup.");

            if (alertShown) {
                System.out.println("MainController.showErrorPopup. alert shown.");
                return;
            }


            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(message);
            ButtonType resetButtonType = new ButtonType("Close");
            alert.getButtonTypes().setAll(resetButtonType);

            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

            alertShown = true;
            alert.show();
        });
    }

}
