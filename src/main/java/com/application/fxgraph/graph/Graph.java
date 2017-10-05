package com.application.fxgraph.graph;

import com.application.fxgraph.ElementHelpers.ConvertDBtoElementTree;
import com.application.fxgraph.ElementHelpers.Element;
import com.application.fxgraph.cells.CircleCell;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.util.*;

public class Graph {

    private Model model;
    private Group canvas;
    private ZoomableScrollPane scrollPane;
    //    private ScrollPane scrollPane;
    private EventHandlers eventHandlers;

    /**
     * the pane wrapper is necessary or else the scrollpane would always align
     * the top-most and left-most child to the top and left eg when you drag the
     * top child down, the entire scrollpane would move down
     */
    private static CellLayer cellLayer;

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


    public void moveCirclesAfterMinimization() {

        DeltaMap.isAnyCircleMinimized = false;

        // For all the circles on UI.
        System.out.println("Graph::moveCirclesAfterMinimization");

        // System.out.println("yMin: " + DeltaMap.yMin + " : upper delta: " + DeltaMap.upperDelta);
        System.out.println("yMax: " + DeltaMap.yMax + " : lower delta: " + DeltaMap.lowerDelta);


        cellLayer.getChildren().forEach(item -> {
            if (item instanceof CircleCell) {
                CircleCell node = (CircleCell) item;

                // if (node.getLayoutY() >= DeltaMap.yMin && node.getLayoutY() < DeltaMap.yMax) {
                //     System.out.println("Moving by half shift: " + node.getCellId() + " : " + DeltaMap.yMin + " : " + DeltaMap.upperDelta);
                //     node.relocate(node.getLayoutX(), node.getLayoutY() - DeltaMap.upperDelta);
                //
                // } else

                if (node.getLayoutY() >= DeltaMap.yMax) {
                    System.out.println("Moving by full shift: " + node.getCellId() + " : " + DeltaMap.yMax + " : "  + DeltaMap.lowerDelta);
                    node.relocate(node.getLayoutX(), node.getLayoutY() - DeltaMap.lowerDelta);

                }

                //
                //     if (DeltaMap.getDelta(node.getLayoutY()) != null) {
                //         double delta = (double) DeltaMap.getDelta(node.getLayoutY());
                //
                //         System.out.println("Relocated Circle:");
                //         System.out.println(((CircleCell) node).getCellId() + " from " + node.getLayoutY());
                //         node.relocate(node.getLayoutX(), node.getLayoutY() - delta);
                //         System.out.println(((CircleCell) node).getCellId() + " to " + node.getLayoutY());
                //         System.out.println();
                //     }
                //
                // } else if(item instanceof Edge) {
                //     Edge node = (Edge) item;
                //
                //     if (DeltaMap.getDelta(node.getLayoutY()) != null) {
                //         double delta = (double) DeltaMap.getDelta(node.getLayoutY());
                //
                //         System.out.println("Relocated Edge:");
                //         System.out.println(((Edge) node).getEdgeId() + " from " + node.getLayoutY());
                //         node.relocate(node.getLayoutX(), node.getLayoutY() - delta);
                //         System.out.println(((Edge) node).getEdgeId() + " to " + node.getLayoutY());
                //         System.out.println();
                //     }
                //
            }
        });

    }

    public void moveCirclesAfterMaximization() {
        // For all the circles on UI.

        DeltaMap.isAnyCircleMaximized = false;

        System.out.println("Graph::moveCirclesAfterMaximization");
        // System.out.println("yMin: " + DeltaMap.yMin + " : upper delta: " + DeltaMap.upperDelta);
        System.out.println("yMax: " + DeltaMap.yMax + " : lower delta" + DeltaMap.lowerDelta);

        cellLayer.getChildren().forEach(item -> {
            if (item instanceof CircleCell) {
                CircleCell node = (CircleCell) item;

                // if (node.getLayoutY() >= DeltaMap.yMin && node.getLayoutY() < DeltaMap.yMax) {
                //     System.out.println("Moving by half shift: " + node.getCellId() + " : " + DeltaMap.yMin + " : " + DeltaMap.upperDelta);
                //     node.relocate(node.getLayoutX(), node.getLayoutY() + DeltaMap.upperDelta);
                //
                // } else

                if (node.getLayoutY() >= DeltaMap.yMax) {
                    System.out.println("Moving by full shift: " + node.getCellId() + " : " + DeltaMap.yMax + " : "  + DeltaMap.lowerDelta);
                    node.relocate(node.getLayoutX(), node.getLayoutY() + DeltaMap.lowerDelta);

                }
            }
        });
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


    /**
     * This method adds UI elements such as circles, lines and highlights from CircleCellsOnUI,
     * EdgesOnUI and HighlightsOnUI maps respectively if they were not previously added.
     *
     * @return void
     */
    public void updateCellLayer() {

        // Iterate circleCellsOnUI and add circles that are not previously added to the cellLayer.
        model.getCircleCellsOnUI().forEach((id, circleCell) -> {
            if (!cellLayer.getChildren().contains(circleCell)) {
                cellLayer.getChildren().add(circleCell);
                circleCell.toFront();
                eventHandlers.setCustomMouseEventHandlers(circleCell);
            }
        });

        // Iterate edgesOnUI and add edges that are not previously added to the cellLayer.
        model.getEdgesOnUI().forEach((id, edge) -> {
            if (!cellLayer.getChildren().contains(edge)) {
                cellLayer.getChildren().add(edge);
                edge.toBack();
            }
        });

        // Iterate highlightsOnUI and add highlights that are not previously added to the cellLayer.
        model.getHighlightsOnUI().forEach((id, rectangle) -> {
            if (!cellLayer.getChildren().contains(rectangle)) {
                cellLayer.getChildren().add(rectangle);
                rectangle.toBack();
            }
        });

        // Update the global flag.
        model.uiUpdateRequired = false;
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
        // System.out.println("vValue: " + vValue + " : hValue: " + hValue);
        // System.out.println("Content height: " + scaledContentHeight + " : width: " + scaledContentWidth);
        // System.out.println("Viewport height: " + scaledViewportHeight + " : width: " + scaledViewportWidth);
        // System.out.println("minY: " + minY + " : minX: " + minX);
        // System.out.println();

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

    /**
     * This method return true if the view port was partially scrolled, that is if the circles loaded in
     * previous cycle are visible on the screen after scroll. Otherwise it returns false.
     *
     * @return boolean
     */
    public boolean checkIfPartialScroll() {
        if (DeltaMap.getLastStoredViewPort().intersects(getViewPortDims())) {
            return true;
        }
        return false;
    }

    /**
     * This method return the top most circle if view port is scrolled to a new random position. Else in case of a partial scroll,
     * this method returns the top most loaded circle.
     *
     * @return an array of size 2 containing top and bottom preloaded circles on UI.
     */
    public CircleCell[]  getTopAndBottomCircles() {
        System.out.println("Graph::getTopMostCircle");
        CircleCell topMost = null, bottomMost = null;

        if (checkIfPartialScroll()) {
            // If the scroll was a partial scroll, get the top most and bottom most circles that are already loaded on UI.
            double maxY = Double.MIN_VALUE,
                    minY = Double.MAX_VALUE;


            // Iterate through the circles cells on UI and get the top and bottom circles.
            for (Node node: cellLayer.getChildren()) {
                if (node instanceof CircleCell) {
                    if (node.getLayoutY() > maxY) {
                        maxY = node.getLayoutY();
                        bottomMost = (CircleCell) node;
                    }

                    if (node.getLayoutY() < minY) {
                        minY = node.getLayoutY();
                        topMost = (CircleCell)node;
                    }
                }
            }

            System.out.println("topmost circle: " + topMost.getCellId() + " : " + topMost.getLayoutY());
            System.out.println("bottom most circle: " + bottomMost.getCellId() + " : " + bottomMost.getLayoutY());

        } else return null;

        return new CircleCell[]{topMost, bottomMost};
    }

    /**
     * This method returns the yMin coordinate of the region to load the circles
     */
    public double getTopCoordinate() {

        return getViewPortDims().getMinY()
                - getViewPortDims().getHeight() * 2
                - DeltaMap.getGlobalDeltaValuse()[0];

    }


}