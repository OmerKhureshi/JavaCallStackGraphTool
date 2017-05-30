package com.application.fxgraph.cells;

import com.application.fxgraph.graph.Cell;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class RectangleCell extends Cell {

    public RectangleCell( String id) {
        super( id);

        Rectangle view = new Rectangle( 50,50);

        view.setStroke(Color.DODGERBLUE);
        view.setFill(Color.DODGERBLUE);

        setView( view);

    }

}