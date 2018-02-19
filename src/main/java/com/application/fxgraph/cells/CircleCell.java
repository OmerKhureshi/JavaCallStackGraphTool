package com.application.fxgraph.cells;

import com.application.fxgraph.graph.Cell;
import com.application.fxgraph.graph.CustomColors;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

public class CircleCell extends Cell {

    private Label label;
    private StackPane idBubble;
    private Label methodName;
    private Shape nodeShape;

    private StackPane minMaxStackPane;
    private StackPane infoStackPane;

    private StackPane rootStackPane;

    private double bookmarkStrokeWidth = 3;
    private double firstPortion = 0.7;
    private double secondPortion = 1 - firstPortion;

    public CircleCell(String id) {
        super(id);

        // Uncomment to see a colored background on the whole circle cell stack pane.
        // setStyle("-fx-background-color: mediumslateblue");

        nodeShape = createRectangle();

        label = new Label("This is a long string");


        // id label
        setUpIdLabel(id);

        // method name label
        setUpMethodName();

        // setup info and min/max buttons
        setUpButtons();

        // rootStackPane = new StackPane(nodeShape, methodName, idBubble, minMaxStackPane, infoStackPane);
        // getChildren().addAll(rootStackPane);

        getChildren().addAll(nodeShape, methodName, idBubble, minMaxStackPane, infoStackPane);

        // setView(group);
        this.toFront();
    }

    private void setUpIdLabel(String id) {
        double x = 0, y = 0;
        Label idLabel = new Label(id);
        idLabel.setFont(Font.font(10));
        idLabel.setTextFill(CustomColors.DARK_BLUE.getColor());

        Shape background = new Rectangle();
        ((Rectangle) background).setWidth(idLabel.getText().length() * 6 + 6);
        ((Rectangle) background).setHeight(15);
        ((Rectangle) background).setArcHeight(10);
        ((Rectangle) background).setArcWidth(10);
        background.setFill(CustomColors.LIGHT_TURQUOISE.getColor());
        background.setStroke(CustomColors.DARK_GREY.getColor());

        idBubble = new StackPane();
        idBubble.getChildren().addAll(background, idLabel);
        idBubble.relocate(-((Rectangle) background).getWidth() * 0.5, -((Rectangle) background).getHeight() * 0.5);
    }

    private void setUpButtons() {
        setUpMinMaxButton();
        setUpInfoButton();
    }

    private void setUpMinMaxButton() {
        // Min-Max button
        Arc minMaxArc = new Arc();
        minMaxArc.setCenterX(20.5);
        minMaxArc.setCenterY(20.5);
        minMaxArc.setRadiusX(20);
        minMaxArc.setRadiusY(20);
        minMaxArc.setStartAngle(270);
        minMaxArc.setLength(180);
        minMaxArc.setType(ArcType.ROUND);
        minMaxArc.setFill(Color.TRANSPARENT);

        Rectangle minMaxButton = new Rectangle();
        minMaxButton.setWidth(((Rectangle) nodeShape).getWidth() * 0.5);
        minMaxButton.setHeight(((Rectangle) nodeShape).getHeight() * secondPortion);
        minMaxButton.setArcHeight(10);
        minMaxButton.setArcWidth(10);
        minMaxButton.setFill(CustomColors.TRANSPARENT.getColor());
        // minMaxButton.setStroke(CustomColors.DARK_GREY.getColor());

        Glyph minMaxGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.EXPAND);
        minMaxGlyph.setColor(Color.WHITESMOKE);

        minMaxStackPane = new StackPane(minMaxButton, minMaxGlyph);
        minMaxStackPane.relocate(((Rectangle) nodeShape).getWidth() * 0.5, ((Rectangle) nodeShape).getHeight() * firstPortion);

    }

    private void setUpInfoButton() {
        Arc infoArc = new Arc();
        infoArc.setCenterX(20.5);
        infoArc.setCenterY(20.5);
        infoArc.setRadiusX(20);
        infoArc.setRadiusY(20);
        infoArc.setStartAngle(90);
        infoArc.setLength(180);
        infoArc.setType(ArcType.ROUND);
        infoArc.setFill(Color.TRANSPARENT);

        // info button
        Rectangle infoButton = new Rectangle();
        infoButton.setWidth(((Rectangle) nodeShape).getWidth() * 0.5);
        infoButton.setHeight(((Rectangle) nodeShape).getHeight() * secondPortion);
        infoButton.setArcHeight(10);
        infoButton.setArcWidth(10);
        infoButton.setFill(CustomColors.TRANSPARENT.getColor());
        // infoButton.setStroke(CustomColors.DARK_GREY.getColor());

        Glyph infoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.INFO_CIRCLE);
        infoGlyph.setColor(Color.WHITESMOKE);

        infoStackPane = new StackPane(infoButton, infoGlyph);
        infoStackPane.relocate(((Rectangle) nodeShape).getWidth() * 0, ((Rectangle) nodeShape).getHeight() * firstPortion);
    }
/*
    // Used?
    public CircleCell (String id, Element element) {
        this(id);
        this.relocate(
                element.getBoundBox().xCoordinate,
                element.getBoundBox().yCoordinate
        );
        this.toFront();
    }*/

    public CircleCell (String id, float xCoordinate, float yCoordinate) {
        this(id);
        this.relocate(xCoordinate , yCoordinate);
        this.toFront();
    }

    public void setLabel(String text) {
        this.label.setText(text);
    }

    private void setUpMethodName() {
        methodName = new Label("");
        methodName.setPrefWidth(85);
        methodName.setWrapText(true);
        methodName.setTextFill(CustomColors.LIGHTEST_BLUE.getColor());
        // methodName.setStyle("-fx-background-color: papayawhip; -fx-background-radius: 7; -fx-border-color: burlywood; -fx-border-radius: 7; -fx-border-width: 2");
        methodName.setAlignment(Pos.CENTER);
        methodName.setTextAlignment(TextAlignment.CENTER);
        methodName.setFont(Font.font(12));
        methodName.setMaxWidth(((Rectangle) nodeShape).getWidth() - 5);
        methodName.setMinWidth(((Rectangle) nodeShape).getWidth() - 5);
        methodName.setMaxHeight(((Rectangle) nodeShape).getHeight() * firstPortion);
        methodName.setMinHeight(((Rectangle) nodeShape).getHeight() * firstPortion);

        // align the method name to top of the square.
        methodName.relocate(2.5, 0);//-this.methodName.getMinHeight()/2);
        // Center the method name label below the circle.
        // this.methodName.setMinWidth(this.methodName.getText().length()*2);
        // this.methodName.relocate(-this.methodName.getPrefWidth() * .25, 45);//-this.methodName.getMinHeight()/2);

    }

    public void setMethodName(String methodName) {
        this.methodName.setText(methodName);
    }
/*
    // Used?
    public void setColor(Color color) {
        circle.setFill(color);
    }*/

    public StackPane getMinMaxStackPane() {
        return minMaxStackPane;
    }

    public StackPane getInfoStackPane() {
        return infoStackPane;
    }

    public void bookmarkCell(String color) {
        nodeShape.setStroke(Paint.valueOf(color));
        nodeShape.setStrokeWidth(bookmarkStrokeWidth);
    }

    @Override
    public String toString() {
        return "CircleCell: id: " + getCellId() + "; x: " + getLayoutX() + "; y: " + getLayoutY();
    }

    private Shape createCircle() {
        Shape circle = new Circle();
        circle = new Circle(20);
        circle.setStroke(CustomColors.DARK_BLUE.getColor());
        circle.setFill(CustomColors.DARK_GREY.getColor());
        circle.relocate(0,0);

        return circle;
    }

    private Shape createRectangle() {
        Shape rect = new Rectangle(70, 70);
        rect.setStroke(CustomColors.LIGHT_TURQUOISE.getColor());
        rect.setFill(CustomColors.DARK_GREY.getColor());
        ((Rectangle) rect).setArcWidth(20);
        ((Rectangle) rect).setArcHeight(20);
        rect.relocate(0,0);

        return rect;
    }


}