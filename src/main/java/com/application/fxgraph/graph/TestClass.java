package com.application.fxgraph.graph;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class TestClass extends Application {

    List listObj = new ArrayList<String>();
    @Override
    public void start(Stage stage) {
        listObj.add("list item one");
        listObj.add("list item one");
        listObj.add("list item one");
        listObj.add("list item one");
        listObj.add("list item one");
        listObj.add("list item one");
        listObj.add("list item one");
        listObj.add("list item one");
        listObj.add("list item one");
        listObj.add("list item one");
        listObj.add("list item one");
        listObj.add("list item one");
        listObj.add("list item one");
        listObj.add("list item one");
        listObj.add("list item one");
        listObj.add("list item one");
        listObj.add("list item one");
        Rectangle content = new Rectangle(1000, 700, Color.GREEN);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPrefSize(400, 300);

//        ChangeListener<Object> changeListener = new ChangeListener<Object>() {
//            @Override
//            public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
//                Bounds bounds = scrollPane.getViewportBounds();
//                int left = (int) bounds.getMinX();
//                int right = left + (int) bounds.getWidth();
//                System.out.println("hval:" + scrollPane.getHvalue() + " left:" + left + " right:" + right);
//            }
//        };

        ChangeListener<Object> changeListener = new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
                double hmin = scrollPane.getHmin();
                double hmax = scrollPane.getHmax();
                double hvalue = scrollPane.getHvalue();
                double contentWidth = content.getLayoutBounds().getWidth();
                double viewportWidth = scrollPane.getViewportBounds().getWidth();

                double hoffset =
                        Math.max(0, contentWidth - viewportWidth) * (hvalue - hmin) / (hmax - hmin);

                double vmin = scrollPane.getVmin();
                double vmax = scrollPane.getVmax();
                double vvalue = scrollPane.getVvalue();
                double contentHeight = content.getLayoutBounds().getHeight();
                double viewportHeight = scrollPane.getViewportBounds().getHeight();

                double voffset =
                        Math.max(0,  contentHeight - viewportHeight) * (vvalue - vmin) / (vmax - vmin);

//                System.out.printf("Offset: [%.1f, %.1f] width: %.1f height: %.1f %n",
//                        hoffset, voffset, viewportWidth, viewportHeight);

                double minX = hvalue * (contentWidth - viewportWidth);
                double maxX = minX + viewportHeight;
            }
        };
        scrollPane.viewportBoundsProperty().addListener(changeListener);
        scrollPane.hvalueProperty().addListener(changeListener);
        scrollPane.vvalueProperty().addListener(changeListener);

        Scene scene = new Scene(scrollPane, 400, 480);
        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}