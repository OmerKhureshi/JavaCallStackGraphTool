package com.application.fxgraph.cells;

import com.application.fxgraph.ElementHelpers.Element;
import com.application.fxgraph.graph.Cell;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class CircleCell extends Cell {

    Label label;
    Label methodName;
    Circle circle;

    public CircleCell(String id) {
        super(id);

        // Uncomment to see yellow background on the whole circle cell stack pane.
        // setStyle("-fx-background-color: yellow");

        circle = new Circle(20);
        label = new Label("This is a long string");
        methodName = new Label("");
        circle.setStroke(Color.GRAY);
        circle.setFill(Color.RED);
        circle.relocate(0,0);

        // getChildren().setAll(circle, label);
        getChildren().add(circle);
        getChildren().add(methodName);
        // setView(group);
    }

    public CircleCell (String id, Element element) {
        this(id);
        this.relocate(
                element.getBoundBox().xCoordinate,
                element.getBoundBox().yCoordinate
        );
    }

    public CircleCell (String id, float xCoordinate, float yCoordinate) {
        this(id);
        this.relocate(xCoordinate , yCoordinate);
    }

    public String getLabel() {
        return label.getText();
    }

    public void setLabel(String text) {
        this.label.setText(text);
    }

    public String getMethodName() {
        return methodName.getText();
    }

    public void setMethodName(String methodName) {
        this.methodName.setText(methodName);

        // Center the method name label below the circle.
        this.methodName.setMinWidth(this.methodName.getText().length()*2);
        this.methodName.relocate(-this.methodName.getMinWidth()/2, 40);//-this.methodName.getMinHeight()/2);

    }

    public void setColor(Color color) {
        circle.setFill(color);
    }
}