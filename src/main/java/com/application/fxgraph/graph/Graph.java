package com.application.fxgraph.graph;

import com.application.fxgraph.ElementHelpers.ConvertDBtoElementTree;
import com.application.fxgraph.ElementHelpers.Element;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

public class Graph {

    private Model model;
    private Group canvas;
    private ZoomableScrollPane scrollPane;
    //    private ScrollPane scrollPane;
    EventHandlers eventHandlers;

    /**
     * the pane wrapper is necessary or else the scrollpane would always align
     * the top-most and left-most child to the top and left eg when you drag the
     * top child down, the entire scrollpane would move down
     */
    static CellLayer cellLayer;

    public Graph() {
        this.model = new Model();
        canvas = new Group();
        cellLayer = new CellLayer();
        canvas.getChildren().add(cellLayer);
        eventHandlers = new EventHandlers(this);
        scrollPane = new ZoomableScrollPane(canvas);
        // scrollPane = new ScrollPane(canvas);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
    }

    public static void drawPlaceHolderLines() {
        // Line hPlaceHolderLine = new Line(0, 0, (Element.getMaxLevelCount() + 2) * BoundBox.unitWidthFactor, 0);
        Line hPlaceHolderLine = new Line(0, 0, (Element.getMaxLevelCount() + 2) * BoundBox.unitWidthFactor, 0);
        hPlaceHolderLine.setStrokeWidth(0.001);
        cellLayer.getChildren().add(hPlaceHolderLine);

        Line vPlaceHolderLine = new Line(0, 0, 0, ConvertDBtoElementTree.greatGrandParent.getLeafCount() * BoundBox.unitHeightFactor);
        vPlaceHolderLine.setStrokeWidth(0.001);
        cellLayer.getChildren().add(vPlaceHolderLine);
        // System.out.println("Lines have been drawn: level: " + Element.getMaxLevelCount() * BoundBox.unitWidthFactor + "; leaf: " + Element.getMaxLeafCount() * BoundBox.unitHeightFactor );
    }

    public static void drawPlaceHolderLines(int height, int width) {
        Line hPlaceHolderLine = new Line(0, 0, (width + 2) * BoundBox.unitWidthFactor, 0);
        hPlaceHolderLine.setStrokeWidth(.001);
        cellLayer.getChildren().add(hPlaceHolderLine);

        Line vPlaceHolderLine = new Line(0, 0, 0, height * BoundBox.unitHeightFactor);
        vPlaceHolderLine.setStrokeWidth(.001);
        cellLayer.getChildren().add(vPlaceHolderLine);
    }

    public ScrollPane getScrollPane() {
        return this.scrollPane;
    }

    public Pane getCellLayer() {
        return this.cellLayer;
    }

    public Model getModel() {
        return model;
    }

    public void beginUpdate() {
    }

    public void myEndUpdate() {
        model.listCircleCellsOnUI.stream().forEach(circleCell -> {
            if (circleCell !=null && !getCellLayer().getChildren().contains(circleCell))
                Platform.runLater(() ->  getCellLayer().getChildren().add(circleCell));
        });
        model.listEdgesOnUI.stream().forEach(edge -> {
            if (!getCellLayer().getChildren().contains(edge))
                getCellLayer().getChildren().add(edge);
        });
        // getCellLayer().getChildren().addAll(model.listCircleCellsOnUI);
        // getCellLayer().getChildren().addAll(model.listEdgesOnUI);

        model.listCircleCellsOnUI.stream()
                .forEach(circleCell -> {
                    eventHandlers.makeDraggable(circleCell);
                });

        model.clearListCircleCellsOnUI();
        model.clearListEdgesOnUI();
    }

    public void endUpdate() {
        // add components to graph pane
        getCellLayer().getChildren().addAll(model.getAddedEdges());
        getCellLayer().getChildren().addAll(model.getAddedCells());

        // remove components from graph pane
        getCellLayer().getChildren().removeAll(model.getRemovedCells());
        getCellLayer().getChildren().removeAll(model.getRemovedEdges());

        // enable dragging of cells
        for (Cell cell : model.getAddedCells()) {
            eventHandlers.makeDraggable(cell);
        }

        // every cell must have a parent, if it doesn't, then the graphParent is
        // the parent
        getModel().attachOrphansToGraphParent(model.getAddedCells());

        // remove reference to graphParent
        getModel().disconnectFromGraphParent(model.getRemovedCells());

        // merge added & removed cells with all cells
        getModel().merge();

    }

    public double getScale() {
        //        throw new IllegalStateException(">>>>>>>>>> Invoking getScale()");
        return this.scrollPane.getScaleValue();
    }

    public static BoundingBox getViewPortDims(ScrollPane scrollPane) {
        double scale = ZoomableScrollPane.getScaleValue();

        // http://stackoverflow.com/questions/26240501/javafx-scrollpane-update-viewportbounds-on-scroll
        double hValue = scrollPane.getHvalue();
        double scaledContentWidth = scrollPane.getContent().getLayoutBounds().getWidth();// * scale;
        double scaledViewportWidth = scrollPane.getViewportBounds().getWidth() / scale;

        double vValue = scrollPane.getVvalue();
        double scaledContentHeight = scrollPane.getContent().getLayoutBounds().getHeight();// * scale;
        double scaledViewportHeight = scrollPane.getViewportBounds().getHeight() / scale;

        double minX = hValue * (scaledContentWidth - scaledViewportWidth);
        double minY = vValue * (scaledContentHeight - scaledViewportHeight);

        // System.out.println("Scale: " + scale);
//        System.out.println("vValue: " + vValue + " : hValue: " + hValue);
//        System.out.println("Content height: " + scaledContentHeight + " : width: " + scaledContentWidth);
//        System.out.println("Viewport height: " + scaledViewportHeight + " : width: " + scaledViewportWidth);
//        System.out.println("minY: " + minY + " : minX: " + minX);
//        System.out.println();

        return new BoundingBox(minX, minY, scaledViewportWidth, scaledViewportHeight);
    }
}