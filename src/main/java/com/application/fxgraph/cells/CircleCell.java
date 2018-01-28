package com.application.fxgraph.cells;

import com.application.fxgraph.ElementHelpers.Element;
import com.application.fxgraph.graph.Cell;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

public class CircleCell extends Cell {

    private Label label;
    private Label methodName;
    private Circle circle;

    private Group minMaxGroup;
    private Group infoGroup;

    public CircleCell(String id) {
        super(id);

        // Uncomment to see a colored background on the whole circle cell stack pane.
        // setStyle("-fx-background-color: mediumslateblue");

        circle = new Circle(20);

        label = new Label("This is a long string");

        // id label
        Label idLabel = new Label(id);
        idLabel.relocate(12, 10);
        idLabel.setFont(Font.font(15));

        // method name label
        methodName = new Label("");
        methodName.setPrefWidth(85);
        methodName.setWrapText(true);
        methodName.setStyle("-fx-background-color: papayawhip; -fx-background-radius: 7; -fx-border-color: burlywood; -fx-border-radius: 7; -fx-border-width: 2");
        methodName.setAlignment(Pos.CENTER);
        methodName.setTextAlignment(TextAlignment.CENTER);
        circle.setStroke(Color.web("#003366"));
        circle.setFill(Color.web("#6699CC"));
        circle.relocate(0,0);

        // Min-Max button
        Arc minMaxArc = new Arc();
        minMaxArc.setCenterX(20);
        minMaxArc.setCenterY(20);
        minMaxArc.setRadiusX(20);
        minMaxArc.setRadiusY(20);
        minMaxArc.setStartAngle(270);
        minMaxArc.setLength(180);
        minMaxArc.setType(ArcType.ROUND);
        minMaxArc.setFill(Color.TRANSPARENT);

        Glyph minMaxGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.EXPAND);
        minMaxGlyph.setColor(Color.TRANSPARENT);
        minMaxGlyph.relocate(25, 13);

        minMaxGroup = new Group(minMaxArc, minMaxGlyph);

        // info button
        Arc infoArc = new Arc();
        infoArc.setCenterX(20);
        infoArc.setCenterY(20);
        infoArc.setRadiusX(20);
        infoArc.setRadiusY(20);
        infoArc.setStartAngle(90);
        infoArc.setLength(180);
        infoArc.setType(ArcType.ROUND);
        infoArc.setFill(Color.TRANSPARENT);

        Glyph infoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.INFO_CIRCLE);
        infoGlyph.setColor(Color.TRANSPARENT);
        infoGlyph.relocate(5, 13);
        infoGroup = new Group(infoArc, infoGlyph);

        getChildren().addAll(circle, methodName, idLabel, minMaxGroup, infoGroup);

        // setView(group);
        this.toFront();
    }

    public CircleCell (String id, Element element) {
        this(id);
        this.relocate(
                element.getBoundBox().xCoordinate,
                element.getBoundBox().yCoordinate
        );
        this.toFront();
    }

    public CircleCell (String id, float xCoordinate, float yCoordinate) {
        this(id);
        this.relocate(xCoordinate , yCoordinate);
        this.toFront();
    }

    public void setLabel(String text) {
        this.label.setText(text);
    }

    public void setMethodName(String methodName) {
        this.methodName.setText(methodName);

        // Center the method name label below the circle.
        // this.methodName.setMinWidth(this.methodName.getText().length()*2);
        this.methodName.relocate(-this.methodName.getPrefWidth() * .25, 45);//-this.methodName.getMinHeight()/2);

    }

    public void setColor(Color color) {
        circle.setFill(color);
    }

    public Group getMinMaxGroup() {
        return minMaxGroup;
    }

    public Group getInfoGroup() {
        return infoGroup;
    }

    @Override
    public String toString() {
        return "CircleCell: id: " + getCellId() + "; x: " + getLayoutX() + "; y: " + getLayoutY();
    }
}