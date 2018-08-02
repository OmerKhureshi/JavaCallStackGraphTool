package com.csgt;

import com.csgt.controller.ControllerUtil;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;
import javafx.util.Duration;

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