package com.application.fxgraph.graph;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class RectangleCell extends Pane {
    private Rectangle rectangle;
    private int elementId;

    public RectangleCell(int elementId, float startX, float startY, float width, float height) {
        rectangle = new Rectangle(startX, startY, width, height);
        this.elementId = elementId;
        getChildren().add(rectangle);
    }

    public void setColor(String color) {
        rectangle.setFill(Color.web(color));
    }

    public void setArcHeight(float height) {
        rectangle.setArcHeight(height);
    }

    public void setArcWidht(float width) {
        rectangle.setArcWidth(width);
    }


    public int getElementId() {
        return elementId;
    }
}
