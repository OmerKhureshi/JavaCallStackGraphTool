package com.application.fxgraph.cells;

import com.application.fxgraph.graph.Cell;
import com.application.fxgraph.graph.CustomColors;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

public class CircleCell extends Cell {

    private Label label;
    private StackPane idBubble;
    private Label methodNameLabel;
    private Shape nodeShape;

    private StackPane minMaxStackPane;
    private StackPane infoStackPane;

    private StackPane rootStackPane;

    private double bookmarkStrokeWidth = 3;
    private double firstPortion = 0.7;
    private double secondPortion = 1 - firstPortion;

    private double rectWidth = 70;
    private double rectHeight = 70;

    private Rectangle bookmarkBar;

    public CircleCell(String id) {
        super(id);

        // Uncomment to see a colored background on the whole circle cell stack pane.
        // setStyle("-fx-background-smallButtonsColor: mediumslateblue");

        nodeShape = createRectangle();

        label = new Label("This is a long string");


        setUpIdLabel(id);
        setUpMethodName();
        setUpButtons();
        setUpDropShadow();
        setFill();
        setUpBookmark();

        // rootStackPane = new StackPane(nodeShape, methodNameLabel, idBubble, minMaxStackPane, infoStackPane);
        // getChildren().addAll(rootStackPane);

        getChildren().addAll(nodeShape, methodNameLabel, idBubble, minMaxStackPane, infoStackPane, bookmarkBar);
        idBubble.toFront();

        // setView(group);
        this.toFront();
    }

    private void setUpBookmark() {
        bookmarkBar = new Rectangle(rectWidth, 4);
        bookmarkBar.relocate(0, 0);
        bookmarkBar.setFill(Color.TRANSPARENT);
        bookmarkBar.setArcWidth(20);
        bookmarkBar.setArcHeight(20);
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
        minMaxButton.setFill(CustomColors.TRANSPARENT.getPaint());

        // minMaxButton.setStroke(CustomColors.DARK_GREY.getPaint());

        Glyph minMaxGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.EXPAND);
        minMaxGlyph.setColor(smallButtonsColor);

        minMaxStackPane = new StackPane(minMaxButton, minMaxGlyph);
        minMaxStackPane.relocate(((Rectangle) nodeShape).getWidth() * 0.5 - 3, ((Rectangle) nodeShape).getHeight() * firstPortion);

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
        infoButton.setFill(CustomColors.TRANSPARENT.getPaint());
        // infoButton.setStroke(CustomColors.DARK_GREY.getPaint());

        Glyph infoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.INFO_CIRCLE);
        infoGlyph.setColor(smallButtonsColor);

        infoStackPane = new StackPane(infoButton, infoGlyph);
        infoStackPane.relocate(((Rectangle) nodeShape).getWidth() * 0 + 3, ((Rectangle) nodeShape).getHeight() * firstPortion );
    }


    public CircleCell (String id, float xCoordinate, float yCoordinate) {
        this(id);
        this.relocate(xCoordinate , yCoordinate);
        this.toFront();
    }

    public CircleCell(String id, float xCoordinate, float yCoordinate, String methodName) {
        this(id, xCoordinate, yCoordinate);
        this.methodNameLabel.setText(methodName);
    }

    public void setLabel(String text) {
        this.label.setText(text);
    }

    private void setUpMethodName() {
        methodNameLabel = new Label("");
        methodNameLabel.setPrefWidth(85);
        methodNameLabel.setWrapText(true);
        methodNameLabel.setTextFill(Color.BLACK);
        // methodNameLabel.setStyle("-fx-background-smallButtonsColor: papayawhip; -fx-background-radius: 7; -fx-border-smallButtonsColor: burlywood; -fx-border-radius: 7; -fx-border-width: 2");
        methodNameLabel.setAlignment(Pos.CENTER);
        methodNameLabel.setTextAlignment(TextAlignment.CENTER);
        methodNameLabel.setFont(Font.font(12));
        methodNameLabel.setMaxWidth(((Rectangle) nodeShape).getWidth() - 5);
        methodNameLabel.setMinWidth(((Rectangle) nodeShape).getWidth() - 5);
        methodNameLabel.setMaxHeight(((Rectangle) nodeShape).getHeight() * firstPortion);
        methodNameLabel.setMinHeight(((Rectangle) nodeShape).getHeight() * firstPortion);

        // align the method name to top of the square.
        methodNameLabel.relocate(2.5, 0);//-this.methodNameLabel.getMinHeight()/2);
        // Center the method name label below the circle.
        // this.methodNameLabel.setMinWidth(this.methodNameLabel.getText().length()*2);
        // this.methodNameLabel.relocate(-this.methodNameLabel.getPrefWidth() * .25, 45);//-this.methodNameLabel.getMinHeight()/2);

    }

    public void setMethodNameLabel(String methodName) {
        this.methodNameLabel.setText(methodName);
    }
/*
    // Used?
    public void setColor(Color smallButtonsColor) {
        circle.setFill(smallButtonsColor);
    }*/

    public StackPane getMinMaxStackPane() {
        return minMaxStackPane;
    }

    public StackPane getInfoStackPane() {
        return infoStackPane;
    }

    public void bookmarkCell(String color) {
        // nodeShape.setStroke(Paint.valueOf(color));
        // nodeShape.setStrokeWidth(bookmarkStrokeWidth);

        bookmarkBar.setFill(Paint.valueOf(color));
    }

    public void removeBookmark() {
        // nodeShape.setStroke(CustomColors.LIGHT_TURQUOISE.getPaint());
        // nodeShape.setStrokeWidth(1);

        bookmarkBar.setFill(Color.TRANSPARENT);
    }

    @Override
    public String toString() {
        return "CircleCell: id: " + getCellId() + "; x: " + getLayoutX() + "; y: " + getLayoutY();
    }

    private Shape createCircle() {
        Shape circle = new Circle();
        circle = new Circle(20);
        circle.setStroke(CustomColors.DARK_BLUE.getPaint());
        circle.setFill(CustomColors.DARK_GREY.getPaint());
        circle.relocate(0,0);

        return circle;
    }

    private Shape createRectangle() {
        Shape rect = new Rectangle(rectWidth, rectHeight);
        // rect.setStroke(CustomColors.LIGHT_TURQUOISE.getPaint());
        // rect.setFill(CustomColors.DARK_GREY.getPaint());
        ((Rectangle) rect).setArcWidth(20);
        ((Rectangle) rect).setArcHeight(20);
        rect.relocate(0,0);

        return rect;
    }

    private void setFill() {
        // Stop[] stops = new Stop[] { new Stop(0, Color.valueOf("#04b3e3")), new Stop(1, Color.valueOf("#04cabe"))};
        Stop[] stops = new Stop[] { new Stop(0, cell1Color), new Stop(1, cell2Color)};
        // Stop[] stops = new Stop[] { new Stop(0, Color.valueOf("#b2f0d9")), new Stop(1, Color.valueOf("#b2e9f0"))};
        LinearGradient linearGradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        nodeShape.setFill(linearGradient);
    }

    private void setUpIdLabel(String id) {
        double x = 0, y = 0;
        Label idLabel = new Label(id);
        idLabel.setFont(Font.font(10));
        idLabel.setTextFill(Color.DIMGREY);

        double height = 15;
        double width = idLabel.getText().length() * 6 + 6;

        Shape background = new Rectangle();
        ((Rectangle) background).setWidth(width);
        ((Rectangle) background).setHeight(height);
        ((Rectangle) background).setArcHeight(10);
        ((Rectangle) background).setArcWidth(10);
        background.setFill(idBubbleBackgroundColor);

        DropShadow dropShadow = new DropShadow();
        dropShadow.setWidth(width);
        dropShadow.setHeight(width);
        dropShadow.setOffsetX(2);
        dropShadow.setOffsetY(2);
        dropShadow.setRadius(5);
        dropShadow.setColor(idBubbleShadowColor);
        background.setEffect(dropShadow);


        // background.setStroke(CustomColors.DARK_GREY.getPaint());

        idBubble = new StackPane();
        idBubble.getChildren().addAll(background, idLabel);
        idBubble.relocate(-((Rectangle) background).getWidth() * 0.5, -((Rectangle) background).getHeight() * 0.5);
    }

    private void setUpDropShadow() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setWidth(rectWidth);
        dropShadow.setHeight(rectHeight);
        dropShadow.setOffsetX(12);
        dropShadow.setOffsetY(12);
        dropShadow.setRadius(40);
        dropShadow.setColor(cellShadowColor);
        nodeShape.setEffect(dropShadow);
    }
    // Color smallButtonsColor = Color.valueOf("#e5a2d0");
    Color smallButtonsColor = Color.WHITE;
    Color idBubbleBackgroundColor = Color.valueOf("#f6dfef");
    Color cell1Color = Color.valueOf("#b2baf0");
    Color cell2Color = Color.valueOf("#bab2f0");
    Color cellShadowColor = Color.rgb(96, 112, 224, .47);
    Color idBubbleShadowColor = Color.rgb(106,30,83, 0.30);

}