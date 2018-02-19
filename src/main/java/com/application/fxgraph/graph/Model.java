package com.application.fxgraph.graph;

import com.application.db.DAOImplementation.BookmarksDAOImpl;
import com.application.db.model.Bookmark;
import com.application.fxgraph.ElementHelpers.Element;
// import com.application.fxgraph.ElementHelpers.SimplifiedElement;
import com.application.fxgraph.cells.CircleCell;
import com.application.fxgraph.cells.RectangleCell;
import com.application.fxgraph.cells.TriangleCell;
import javafx.scene.Node;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.util.*;
import java.util.List;

public class Model {

    Cell graphParent;

    List<Cell> allCells;
    List<Cell> addedCells;
    List<Cell> removedCells;

    List<Edge> allEdges;
    List<Edge> addedEdges;
    List<Edge> removedEdges;

    Map<String, Cell> cellMap; // <id,cell>

    // Global HashMaps to store UI elements.
    private Map<String, CircleCell> circleCellsOnUI = new HashMap<>();
    List<CircleCell> listCircleCellsOnUI = new ArrayList<>();

    private Map<String, Edge> edgesOnUI = new HashMap<>();
    List<Edge> listEdgesOnUI = new ArrayList<>();

    private Map<Integer, com.application.fxgraph.graph.RectangleCell> highlightsOnUI = new HashMap<>();

    // private Map<String, SimplifiedElement> simplifiedElementMap = new HashMap<>();

    private Map<String, Bookmark> bookmarkMap = new HashMap<>();
    private Map<String, Rectangle> barMarkMap = new HashMap<>();

    public Map<String, Bookmark> getBookmarkMap() {
        return bookmarkMap;
    }

    public Model() {
        graphParent = new Cell("_ROOT_");
        // clear model, create lists
        clear();
    }

    /**
     * new/modified methods start
     */

    public Map<String, Bookmark> updateAndGetBookmarkMap() {
        bookmarkMap = BookmarksDAOImpl.getBookmarks();
        return bookmarkMap;
    }

    public void updateBookmarkMap() {
        bookmarkMap = BookmarksDAOImpl.getBookmarks();
        bookmarkMap.keySet().forEach(id -> {
            Rectangle rect = new Rectangle(bookmarkMap.get(id).getxCoordinate(), bookmarkMap.get(id).getyCoordinate(), 10, 2);
            rect.setFill(Paint.valueOf(bookmarkMap.get(id).getColor()));
            barMarkMap.put(id, rect);
        });
    }

    public Map<String, Rectangle> getBarMarkMap() {
        return barMarkMap;
    }

    public boolean uiUpdateRequired = true;

    public void stackRectangles() {

        List<com.application.fxgraph.graph.RectangleCell> list = new ArrayList<>(highlightsOnUI.values());

        // Sort the list of rectangles according to area.
        list.sort((o1, o2) -> (int) (o1.getBoundsInParent().getWidth() * o1.getBoundsInParent().getHeight() - o2.getBoundsInParent().getWidth() * o2.getBoundsInParent().getHeight()));
        list.forEach(Node::toBack);
        // getEdgesOnUI().forEach((id, edge) -> edge.toFront());
        // getCircleCellsOnUI().forEach((id, circleCell) -> circleCell.toFront());

        // In order of smaller to larger rectangles, send each to back. Results in larger highlights behind or below smaller ones.
        // System.out.println("fronting ======================================================================");
        // System.out.println("width of first. " + list.get(0).getWidth());
        // System.out.println("width of first. " + list.get(0).getRectangle().getWidth());
        // System.out.println("width of first. " + list.get(0).getBoundsInParent().getWidth());
        //
        // System.out.println("fronting end ======================================================================");
    }

    // public void addSimplifiedElementToMap(SimplifiedElement element) {
    //         simplifiedElementMap.put(element.getElementId(), element);
    // }
    //
    // public void removeSimplifiedElementFromMap(String elementId) {
    //     if (simplifiedElementMap.get(elementId) != null) {
    //         simplifiedElementMap.remove(elementId);
    //     }
    // }
    //
    // public Map<String, SimplifiedElement> getSimplifiedElementMap() {
    //     return simplifiedElementMap;
    // }

    public void addCell(CircleCell circleCell) {
        // circleCell.toFront();
        // circleCell.setTranslateZ(10);
        // synchronized (Main.getLock()) {
        if (!circleCellsOnUI.containsKey(circleCell.getCellId())) {
            // System.out.println( "Model::addCell: " + circleCell.getCellId());
            circleCellsOnUI.put(circleCell.getCellId(), circleCell);
            // System.out.println( "Model::addCell: circleCellsOnUI.size() " +circleCellsOnUI.size());
            listCircleCellsOnUI.add(circleCell);

        }
        // }
    }

    public void addEdge(Edge edge) {
        // edge.toBack();
        // edge.setTranslateZ(.5);
        addedEdges.add(edge);
        if (edge != null && !edgesOnUI.containsKey(edge.getEdgeId())) {
            // System.out.println();
            // System.out.println();
            // System.out.println(">>>> Adding edge: " + edge.getEdgeId());
            // System.out.println(">>>> List: Before adding: size:" + listEdgesOnUI.size());
            // listEdgesOnUI.stream()
            //         .forEach(s -> System.out.print("    : " +s.getEdgeId()));
            // System.out.println();
            //
            // System.out.println(">>>> MAP: Before adding: size:" + edgesOnUI.size());
            // edgesOnUI.entrySet().stream()
            //         .forEach(s -> System.out.print("    : " + s.getKey()));
            // System.out.println();
            //**********************************************
            edgesOnUI.put(edge.getEdgeId(), edge);
            listEdgesOnUI.add(edge);
            //**********************************************

            // System.out.println(">>>> List: After adding: size:" + listEdgesOnUI.size());
            // listEdgesOnUI.stream()
            //         .forEach(s -> System.out.print("    : " + s.getEdgeId()));
            // System.out.println();
            // System.out.println(">>>> MAP: After adding: size:" + edgesOnUI.size());
            // edgesOnUI.entrySet().stream()
            //         .forEach(s -> System.out.print("    : " + s.getKey()));
            // System.out.println();
        }
    }

    public void addHighlight(Integer id, com.application.fxgraph.graph.RectangleCell rectangle) {
        // uiUpdateRequired = true;
        highlightsOnUI.putIfAbsent(id, rectangle);
    }


    // Clear methods.

    public void clearMaps() {
        // System.out.println("Model::clearMaps");
        circleCellsOnUI.clear();
        edgesOnUI.clear();
        highlightsOnUI.clear();
    }

    public void clearListCircleCellsOnUI() {
        listCircleCellsOnUI.clear();
    }

    public void clearListEdgesOnUI() {
        listEdgesOnUI.clear();
    }


    // Getters

    public List<CircleCell> getListCircleCellsOnUI() {
        return listCircleCellsOnUI;
    }

    public Map<String, CircleCell> getCircleCellsOnUI() {
        return circleCellsOnUI;
    }

    public Map<String, Edge> getEdgesOnUI() {
        return edgesOnUI;
    }

    public List<Edge> getListEdgesOnUI() {
        return listEdgesOnUI;
    }

    public Map<Integer, com.application.fxgraph.graph.RectangleCell> getHighlightsOnUI() {
        return highlightsOnUI;
    }

    /**
     * new methods end
     */


    public void clear() {

        allCells = new ArrayList<>();
        addedCells = new ArrayList<>();
        removedCells = new ArrayList<>();

        allEdges = new ArrayList<>();
        addedEdges = new ArrayList<>();
        removedEdges = new ArrayList<>();

        cellMap = new HashMap<>(); // <id,cell>

    }

    public void clearAddedLists() {
        addedCells.clear();
        addedEdges.clear();
    }

    public List<Cell> getAddedCells() {
        return addedCells;
    }

    public List<Cell> getRemovedCells() {
        return removedCells;
    }

    public List<Cell> getAllCells() {
        return allCells;
    }

    public List<Edge> getAddedEdges() {
        return addedEdges;
    }

    public List<Edge> getRemovedEdges() {
        return removedEdges;
    }

    public List<Edge> getAllEdges() {
        return allEdges;
    }

    public void addCell(String id, CellType type) {

        switch (type) {

            case RECTANGLE:
                RectangleCell rectangleCell = new RectangleCell(id);
                addCell(rectangleCell);
                break;

            case TRIANGLE:
                TriangleCell triangleCell = new TriangleCell(id);
                addCell(triangleCell);
                break;

            case CIRCLE:
                CircleCell circleCell = new CircleCell(id);
                addCell(circleCell);
                break;

            default:
                throw new UnsupportedOperationException("Unsupported type: " + type);
        }
    }
/*
    // used?
    public CircleCell addCircleCell(String id, Element element) {
        CircleCell circleCell = new CircleCell(id, element);
        element.setCircleCell(circleCell);
        addCell(circleCell);
        return circleCell;
    }*/

    public void addCell(Cell cell) {
        addedCells.add(cell);
        cellMap.put(cell.getCellId(), cell);
    }

    public void addEdge(CircleCell sourceCell, CircleCell targetCell) {
        Edge edge = new Edge(sourceCell, targetCell);
        addedEdges.add(edge);
    }

    public void addEdge(String sourceId, String targetId) {

        Cell sourceCell = cellMap.get(sourceId);
        Cell targetCell = cellMap.get(targetId);

        Edge edge = new Edge(sourceCell, targetCell);

        addedEdges.add(edge);

    }

    /**
     * Attach all cells which don't have a parent to graphParent
     *
     * @param cellList
     */
    public void attachOrphansToGraphParent(List<Cell> cellList) {

        for (Cell cell : cellList) {
            if (cell.getCellParents().size() == 0) {
                graphParent.addCellChild(cell);
            }
        }
    }

    /**
     * Remove the graphParent reference if it is set
     *
     * @param cellList
     */
    public void disconnectFromGraphParent(List<Cell> cellList) {

        for (Cell cell : cellList) {
            graphParent.removeCellChild(cell);
        }
    }

    public void merge() {

        // cells
        allCells.addAll(addedCells);
        allCells.removeAll(removedCells);

        addedCells.clear();
        removedCells.clear();

        // edges
        allEdges.addAll(addedEdges);
        allEdges.removeAll(removedEdges);

        addedEdges.clear();
        removedEdges.clear();

    }

}