package com.csgt.presentation.graph;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


/**
 * This call represents the Background highlight for the nodes on UI.
 */
public class HighlightCell extends Cell {
    private Rectangle rectangle;

    private int elementId;

    public HighlightCell(int id, int elementId, float startX, float startY, float width, float height) {
        super(String.valueOf(elementId));
        this.elementId = elementId;

        // Uncomment to see yellow background on the whole rectangle pane.
        // setStyle("-fx-background-color: yellow");

        rectangle = new Rectangle(width, height);
        rectangle.setStroke(Color.BLACK);

        // for debugging, show element id.
        // Label idLabel = new Label(String.valueOf(elementId) + ");
        // getChildren().addAll(rectangle, idLabel);

        getChildren().add(rectangle);
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
