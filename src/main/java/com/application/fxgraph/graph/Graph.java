package com.application.fxgraph.graph;

import com.application.db.DAOImplementation.BookmarksDAOImpl;
import com.application.db.model.Bookmark;
import com.application.fxgraph.ElementHelpers.ConvertDBtoElementTree;
import com.application.fxgraph.ElementHelpers.Element;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.Map;

public class Graph {

    private Model model;
    private Group canvas;
    private ZoomableScrollPane scrollPane;

    // private Pane barPane;

    public EventHandlers getEventHandlers() {
        return eventHandlers;
    }

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

/*
    public void setUpBarPane() {
        newbarPane = new Pane();
        StackPane.setAlignment(barPane, Pos.CENTER_RIGHT);
        StackPane.setMargin(barPane, new Insets(12,15,28,0));
        barPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1); -fx-background-radius: 0;");
        barPane.setMaxWidth(15);
        getModel().updateBookmarkMap();
        addMarksToBarPane();
        // System.out.println(">>>>>>: " + barPane.heightProperty().get());
        // System.out.println(">>>>>>: " + barPane.heightProperty().getValue());
        // System.out.println(">>>>>>: " + barPane.getHeight());
        // System.out.println(">>>>>>: " + barPane.getMaxHeight());
        // System.out.println(">>>>>>: " + barPane.getMinHeight());
        // System.out.println(">>>>>>: " + barPane.getPrefHeight());
        // System.out.println(">>>>>>: " + barPane.getLayoutBounds().getHeight());
        // System.out.println(" --------------- ");
        //
        // barPane.heightProperty().addListener(eve -> {
        //     System.out.println(barPane.getHeight());
        // });
    }
*/

/*
    public void addMarksToBarPane() {
        getModel().getBookmarkMap().values().forEach(bookmark -> {
            System.out.println("Graph.addMarksToBarPane: bookmark: " + bookmark);
            addMarkToBarPane(bookmark);
        });
        System.out.println("2. barPane.getHeight() = " + barPane.getHeight());

    }
*/


   /* public void addMarkToBarPane(Bookmark bookmark) {
        Rectangle rect = new Rectangle(0, 0, 40, 4);
        rect.setLayoutY(getVValue(bookmark.getyCoordinate()) * barPane.getHeight() * 0.9);
        // rect.setLayoutX(getHValue(bookmark.getxCoordinate()) * barPane.getWidth() * 0.9);
        // rect.setFill(Paint.valueOf(bookmark.getColor()));
        rect.setFill(Color.RED);
        model.getBarMarkMap().put(bookmark.getElementId(), rect);
        barPane.getChildren().add(rect);
        // System.out.println("Graph.addMarkToBarPane: added rect: layoutY:" + rect.getLayoutY());
        // System.out.println("Graph.addMarkToBarPane: " + (getVValue(bookmark.getyCoordinate()) * barPane.getHeight() * 0.9));
        //
        // System.out.println("1. barPane.getHeight() = " + barPane.getHeight());
        // System.out.println("bookmark.getyCoordinate() = " + bookmark.getyCoordinate());
        // System.out.println("getVValue(bookmark.getyCoordinate()) = " + getVValue(bookmark.getyCoordinate()));
    }*/

/*
    public void removeMarkFromBarPane(String elementId) {
        Rectangle rect = model.getBarMarkMap().get(elementId);
        model.getBarMarkMap().remove(elementId);
        barPane.getChildren().remove(rect);
    }
*/


    public void clearCellLayer() {
        // System.out.println("Graph::clearCellLayer:");
        cellLayer.getChildren().clear();
        // System.out.println("Graph::clearCellLayer: END");
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

        model.listCircleCellsOnUI.forEach(circleCell -> {
                    eventHandlers.setCustomMouseEventHandlers(circleCell);
                });

        model.clearListCircleCellsOnUI();
        model.clearListEdgesOnUI();
    }

    public void updateCellLayer() {

        // System.out.println("Graph::updateCellLayer: method started");
        // System.out.println("Graph::updateCellLayer: gighlightsOnUI.size() " + model.getHighlightsOnUI().size());
        model.getCircleCellsOnUI().forEach((id, circleCell) -> {
            if (!cellLayer.getChildren().contains(circleCell)) {
                // System.out.println("Graph::updateCellLayer: Adding circleCell to cellLayer: " + circleCell.getCellId());
                cellLayer.getChildren().add(circleCell);
                circleCell.toFront();
                eventHandlers.setCustomMouseEventHandlers(circleCell);
            }
        });

        model.getEdgesOnUI().forEach((id, edge) -> {
            if (!cellLayer.getChildren().contains(edge)) {
                cellLayer.getChildren().add(edge);
                edge.toBack();
            }
        });

        model.getHighlightsOnUI().forEach((id, rectangle) -> {
            if (!cellLayer.getChildren().contains(rectangle)) {
                // System.out.println("Graph::updateCellLayer: Adding highlight to cellLayer: ");
                cellLayer.getChildren().add(rectangle);
                rectangle.toBack();
            }
        });

        model.stackRectangles();
        model.uiUpdateRequired = false;

        // System.out.println("Graph::updateCellLayer: method ended");
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
            eventHandlers.setCustomMouseEventHandlers(cell);
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

    public BoundingBox getViewPortDims() {
        ScrollPane scrollPane1 = getScrollPane();
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
        /*
            1 -> contentWidth - viewPortWidth
            ? -> x cord

            ? = xCord/contentWidth
        */
        return new BoundingBox(minX, minY, scaledViewportWidth, scaledViewportHeight);
    }

    public double getHValue(double xCoordinate) {
        double scaledContentWidth = getScrollPane().getContent().getLayoutBounds().getWidth();// * scale;
        double scaledViewportWidth = getScrollPane().getViewportBounds().getWidth(); // / scale;

        return xCoordinate / (scaledContentWidth - scaledViewportWidth);
    }

    public double getVValue(double yCoordinate) {
        double scaledContentHeight = getScrollPane().getContent().getLayoutBounds().getHeight();// * scale;
        double scaledViewportHeight = getScrollPane().getViewportBounds().getHeight(); // / scale;

        return yCoordinate / (scaledContentHeight - scaledViewportHeight);
    }

    public void moveScrollPane(double hValue, double vValue){
        getScrollPane().setHvalue(hValue);
        getScrollPane().setVvalue(vValue);
    }

    private Map<String, XYCoordinate> recentLocationsMap = new HashMap<>();

    public Map<String, XYCoordinate> getRecentLocationsMap() {
        return recentLocationsMap;
    }

    public void addToRecent(String classMethodName, XYCoordinate location) {
        recentLocationsMap.put(classMethodName, location);
    }

    public void clearRecents() {
        recentLocationsMap.clear();
    }

    public static class XYCoordinate {

        public double x;
        public double y;
        public int threadId;
        XYCoordinate(double x, double y, int threadId) {
            this.x = x;
            this.y = y;
            this.threadId = threadId;
        }
    }

    /*public Pane getBarPane() {
        return barPane;
    }*/

/*
    public void setBarPane(Pane barPane) {
        this.barPane = barPane;
    }
*/
}