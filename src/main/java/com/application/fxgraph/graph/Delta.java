/*
package com.application.fxgraph.graph;

import com.application.db.DAO.DAOImplementation.ElementDAOImpl;
import com.application.db.DAO.DAOImplementation.TraceDAOImpl;
import javafx.concurrent.Task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

public class Delta {

    private final static int numberOfLeafsInGrid = 10;
    private final static int gridSize = numberOfLeafsInGrid * BoundBox.unitHeightFactor;

    private static double height;
    private static double screenBottomY;
    private static LinkedHashMap<Integer, Double> deltaValMap = new LinkedHashMap<>();
    private static LinkedHashMap<Integer, Integer> cellIDMap = new LinkedHashMap<>();

    private static boolean calledOnce = false;


    public static void init() {
        ResultSet rootElementRS = ElementDAOImpl.selectWhere("ID_ENTER_CALL_TRACE = -1");
        try {
            if (rootElementRS.next()) {
                height = rootElementRS.getDouble("Bound_Box_Y_Bottom_Left");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int ind = 0;
        double val = 0;
        while (ind < height) {
            deltaValMap.put(ind, val++);
            cellIDMap.put(ind, 0);
            ind += gridSize;
        }
    }

    public static int getNextGridBottomY(double y) {
        return ((int) (screenBottomY / gridSize) + 2) * gridSize;
    }


    public static void clearNextGridBottomY(double y) {
        int nextGridBottomY = ((int) (y / gridSize) + 2 ) * gridSize;
        deltaValMap.put(nextGridBottomY, 0d);

        System.out.println("Delta::clearNextGridBottomY: setting delta = 0 at y = " + y + " and nextGridBottomY = " + nextGridBottomY);
    }


    */
/**
     * This method is invoked when screen is scrolled.
     * It checks if any updates are required in the cells below the next grid and if needed, triggers the updates in a background thread,
     *
     * @param screenBottomY
     *//*

    public static void onScroll(double screenBottomY) {
        Delta.screenBottomY = screenBottomY;

        // get next grid's bottom delta value
        int nextGridBottomY = ((int) (Delta.screenBottomY / gridSize) + 2) * gridSize;
        double delta = deltaValMap.get(nextGridBottomY);

        // trigger updates only if next grid's delta != 0
        // and if the updates were not already triggered for that grid.
        if (delta != 0 && !calledOnce) {
            calledOnce = true;
            Task<Void> updateTask = EventHandlers.updateTreeBelowYWrapper(nextGridBottomY, delta);
            // set delta = 0 at this gridbottom
            updateTask.setOnSucceeded(event -> {
                System.out.println("Delta::onScroll: background thread updateTask completed. Setting delta = 0 at this grid bottom.");
                unSetCalledOnce();
                deltaValMap.put(nextGridBottomY, 0d);
            });

            new Thread(updateTask).start();
        }
    }


    public synchronized static void unSetCalledOnce() {
        calledOnce = false;
    }


    // Keep recurring till the end of next grid is reached
    public static boolean shouldRecurse(double y, int cellId, double delta) {
        int nextGridBottomY = ((int) (screenBottomY / gridSize) + 2) * gridSize;
        if (y < nextGridBottomY) {
            return true;
        } else {
            updateDeltaVal(y, cellId, delta);
            return false;
        }
    }

    public static double getDeltaVal(double y) {
        int thisGridBottomY = ((int) (y / gridSize)) * gridSize;
        return deltaValMap.get(thisGridBottomY);
    }

    public static void updateDeltaVal(double y, int cellId, double newDeltaVal) {
        int nextGridBottomY = ((int) (y / gridSize)) * gridSize;
        cellIDMap.put(nextGridBottomY, cellId);
        deltaValMap.put(nextGridBottomY, newDeltaVal + deltaValMap.get(nextGridBottomY));
    }


    public static void resetDelta() {
        deltaValMap.clear();
        cellIDMap.clear();
        calledOnce = false;
    }

    // happy region -> current grid + upper grid + lower grid.
    // loaded region -> happy + 1 upper grid + 1 lower grid.


   // on scroll.
   //       if view port is not completely contained by happy region, then UIDrawing is required.

}*/
