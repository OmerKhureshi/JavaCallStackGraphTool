package com.application.fxgraph.graph;

import com.application.db.DAOImplementation.ElementDAOImpl;

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



    public static void onScroll(double y) {
        screenBottomY = y;
        int nextKey = ((int) (y / gridSize) + 1) * gridSize;
        double delta = deltaValMap.get(nextKey);
        System.out.println("Delta::onScroll: delta val: " + delta);
        if (delta != 0 && !calledOnce) {
            calledOnce = true;
            // call update db
        }
    }


    public synchronized static void unSetCalledOnce() {
        calledOnce = false;
    }


    // Keep recurring till the end of next grid is reached
    public static boolean shouldReccurse(double y) {
        int nextGridBottomY = ((int) (screenBottomY / gridSize) + 2) * gridSize;
        return y < nextGridBottomY;
    }

    public static double getDeltaVal(double y) {
        int thisGridBottomY = ((int) (y / gridSize)) * gridSize;
        return deltaValMap.get(thisGridBottomY);
    }

    public static void updateDeltaVal(int cellId, double newDeltaVal) {
        int nextGridBottomY = ((int) (y / gridSize) + 2) * gridSize;
        cellIDMap.put(nextGridBottomY, cellId);
        deltaValMap.put(nextGridBottomY, newDeltaVal + deltaValMap.get(nextGridBottomY));
    }


    public static void resetDelta() {
        deltaValMap.clear();
        cellIDMap.clear();
        calledOnce = false;
    }


    // class Store {
    //     int cellId;
    //     double delta;
    //
    //     Store(int cellId, double delta) {
    //         this.cellId = cellId;
    //         this.delta = delta;
    //     }
    // }

}