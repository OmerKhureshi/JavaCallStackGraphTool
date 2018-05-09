package com.csgt.presentation.graph;

import com.csgt.Main;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Scale;

public class ZoomableScrollPane extends ScrollPane {
    private Group zoomGroup;
    private Scale scaleTransform;
    private Node content;
    private static double scaleValue = 1.0;
    private double delta = 0.1;
    private Main main;

    private static DoubleProperty hValProperty;
    private static ChangeListener<Number> hValListener;

    private static DoubleProperty vValProperty;
    private static ChangeListener<Number> vValListener;

    public ZoomableScrollPane(Node content) {
        this.content = content;
        Group contentGroup = new Group();
        zoomGroup = new Group();
        contentGroup.getChildren().add(zoomGroup);
        zoomGroup.getChildren().add(content);
        setContent(contentGroup);
        scaleTransform = new Scale(scaleValue, scaleValue, 0, 0);
        zoomGroup.getTransforms().add(scaleTransform);
        zoomGroup.setOnScroll(new ZoomHandler());

        setHbarPolicy(ScrollBarPolicy.ALWAYS);
        setVbarPolicy(ScrollBarPolicy.ALWAYS);
/*
        hValProperty = hvalueProperty();
        hValListener = (observable, oldValue, newValue) -> main.updateUi();

        vValProperty = vvalueProperty();
        vValListener = (observable, oldValue, newValue) -> main.updateUi();
        //
        // hValProperty.addListener(hValListener);
        // vValProperty.addListener(vValListener);
        //

        turnOnListeners();

        viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
            if (main != null) {
                main.updateUi();
            }
        });
*/

        setStyle("-fx-background-color: transparent;");

    }

    public static void turnOffListeners() {
        hValProperty.removeListener(hValListener);
        vValProperty.removeListener(vValListener);
    }

    public static void turnOnListeners() {
        hValProperty.addListener(hValListener);
        vValProperty.addListener(vValListener);
    }

    public void saveRef(Main m) {
        main = m;
    }

    public static double getScaleValue() {
        return scaleValue;
    }

    public void zoomToActual() {
        zoomTo(1.0);
    }

    public void zoomTo(double scaleValue) {
        this.scaleValue = scaleValue;
        scaleTransform.setX(scaleValue);
        scaleTransform.setY(scaleValue);
    }

    public void zoomActual() {

        scaleValue = 1;
        zoomTo(scaleValue);

    }

    public void zoomOut() {
        scaleValue -= delta;

        if (Double.compare(scaleValue, 0.1) < 0) {
            scaleValue = 0.1;
        }

        zoomTo(scaleValue);
    }

    public void zoomIn() {

        scaleValue += delta;

        if (Double.compare(scaleValue, 10) > 0) {
            scaleValue = 10;
        }

        zoomTo(scaleValue);

    }

    /**
     *
     * @param minimizeOnly
     *            If the content fits already into the viewport, then we don't
     *            zoom if this parameter is true.
     */
    public void zoomToFit(boolean minimizeOnly) {

        double scaleX = getViewportBounds().getWidth() / getContent().getBoundsInLocal().getWidth();
        double scaleY = getViewportBounds().getHeight() / getContent().getBoundsInLocal().getHeight();

        // consider current scale (in content calculation)
        scaleX *= scaleValue;
        scaleY *= scaleValue;

        // distorted zoom: we don't want it => we search the minimum scale
        // factor and apply it
        double scale = Math.min(scaleX, scaleY);

        // check precondition
        if (minimizeOnly) {

            // check if zoom factor would be an enlargement and if so, just set
            // it to 1
            if (Double.compare(scale, 1) > 0) {
                scale = 1;
            }
        }

        // apply zoom
        zoomTo(scale);

    }

    private class ZoomHandler implements EventHandler<ScrollEvent> {

        /*
         *  Add change listener to hValue and vValue.
         */
        @Override
        public void handle(ScrollEvent scrollEvent) {
            if (scrollEvent.isControlDown()) {
                if (scrollEvent.getDeltaY() < 0) {
                    scaleValue -= delta;
                    if (scaleValue < .05) {
                        scaleValue = .05;
                    }
                    scaleValue = Math.max(scaleValue, 0.05);
                } else {
                    scaleValue += delta;
                }

                zoomTo(scaleValue);
                scrollEvent.consume();
            }
            // System.out.println("Zoom value: " + scaleValue);
        }
    }
}