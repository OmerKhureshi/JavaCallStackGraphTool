package com.application.fxgraph.graph;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class RectangleCell extends Cell {
    private Rectangle rectangle;
    private Label idLabel;

    private int elementId;
    private int id;

    public RectangleCell(int id, int elementId, float startX, float startY, float width, float height) {
        super(String.valueOf(elementId));
        this.elementId = elementId;

        // Uncomment to see yellow background on the whole rectangle pane.
        setStyle("-fx-background-color: yellow");

        rectangle = new Rectangle(width, height);
        rectangle.setStroke(Color.BLACK);

        idLabel = new Label(String.valueOf(elementId) + ",id:" + id);

        getChildren().addAll(rectangle, idLabel);
        this.relocate(startX, startY);
    }

    public void setColor(String color) {
        rectangle.setFill(Color.web(color));
    }

    public void setArcHeight(float height) {
        rectangle.setArcHeight(height);
    }

    public void setArcWidth(float width) {
        rectangle.setArcWidth(width);
    }

    public int getElementId() {
        return elementId;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setHeight(double height) {
        this.setPrefHeight(height);
        rectangle.setHeight(height);
    }

    public void setWidth(double width) {
        this.setPrefWidth(width);
        rectangle.setWidth(width);
    }
}
