package com.csgt.presentation.graph;

import com.csgt.controller.ControllerLoader;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class Edge extends Group {
    protected Cell source;
    protected Cell target;

    // EdgeId is also the CircleCellId for the target/end circle cell.
    private String edgeId;
    public Line line;

    public Edge(String edgeId, double startX, double endX, double startY, double endY) {
        this.edgeId = edgeId;
        boolean isLite = ControllerLoader.menuController.isLiteModeEnabled;

        // this.source = source;
        // this.target = target;
        //
        // source.addCellChild(target);
        // target.addCellParent(source);
        Label label = new Label(edgeId);

        line = new Line();

        line.setStartX(startX + Cell.prefWidth * .5 ) ;
        line.setStartY(startY + Cell.prefHeight * .5 );
        line.setEndX(endX + Cell.prefWidth * .5 );
        line.setEndY(endY + Cell.prefHeight * .5 );


        if (isLite) {
            line.setStroke(Color.valueOf("#bab2f0"));
            line.setStrokeWidth(0.8);
            // line.setFill(Color.valueOf("#bab2f0"));
        } else {
            // line.setFill(Color.valueOf("#bab2f0"));
            line.setStroke(Color.valueOf("#dab2f0"));

            Glow glow = new Glow();
            glow.setLevel(0.5);
            line.setEffect(glow);
        }

        // line.startXProperty().bind( source.layoutXProperty().add(source.getLayoutX()));
        // line.startYProperty().bind( source.layoutYProperty().add(source.getLayoutY()));
        // line.endXProperty().bind( target.layoutXProperty().add( target.getLayoutX()));
        // line.endYProperty().bind( target.layoutYProperty().add( target.getLayoutY()));

        // Bind a line to the source and target cells.
        // line.startXProperty().bind( source.layoutXProperty().add(source.getBoundsInParent().getWidth() / 2.0));
        // line.startYProperty().bind( source.layoutYProperty().add(source.getBoundsInParent().getHeight() / 2.0));
        // line.endXProperty().bind( target.layoutXProperty().add( target.getBoundsInParent().getWidth() / 2.0));
        // line.endYProperty().bind( target.layoutYProperty().add( target.getBoundsInParent().getHeight() / 2.0));

        // System.out.println( source.getCellId() + ": layoutX: " + source.getLayoutX() + "; layoutY: " + source.getLayoutY() + "; width: " + source.getWidth() + "; height: " + source.getHeight());
        // System.out.println( target.getCellId() + ": layoutX: " + target.getLayoutX() + "; layoutY: " + target.getLayoutY() + "; width: " + target.getWidth() + "; height: " + target.getHeight());
        // System.out.println(getEdgeId() + ": Line: " + line.getStartX() + "; end: " + line.getEndX());
        label.setLayoutX(line.getStartX() + 50);
        label.setLayoutY((line.getStartY() + line.getEndY()) /2);

        getChildren().add(line);
        // getChildren().add(label);
        this.toFront();
    }

    public void createLine() {

    }

    public String getEdgeId() {
        return edgeId;
    }

    public void setEdgeId(String edgeId) {
        this.edgeId = edgeId;
    }

}