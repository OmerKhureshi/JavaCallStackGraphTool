package com.csgt;

import com.csgt.controller.ControllerUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


/**
 * This is the main Class for the Call Stack Graph Tool.
 * It is responsible for loading the main.fxml layout and all the associated layout files.
 */
public class Main extends Application {

    private static final String mainFXML = "/fxml/main.fxml";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(mainFXML));
            Parent content = loader.load();
            Scene scene = new Scene(content, 1000, 500);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Call Stack Graph Tool");
            primaryStage.show();

            ControllerUtil.setPrimaryStage(primaryStage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}