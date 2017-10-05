package com.application.fxgraph.graph;

import javafx.geometry.BoundingBox;

import java.util.*;

public class DeltaMap {


    /*
     * Scenarios.
     * 1. First load.
     * 2. Partial scroll
     *      downwards
     *      upwards.
     *      left or right
     * 3. Random scroll
     *
     * */

    /*
    * ---------------------------------------------------------------------------------------------
        Components of the solution.

        ---------------------------------------------
        1. Determine if partial scroll or random scroll.
        2. If partial scroll, get the outer most circles.
            Check direction of scroll.
                if down scroll, build tree from bottom most circle downwards.
                if up scroll, build tree from top most circle upwards.
        3. If random scroll, get the yMin.
            Check direction of scroll.
            Compensate for the delta in the right direction.
            Fetch elements from DB and create circles.
            Build tree downwards from yMin.
        4. Save the new viewport dimensions and delta values for four directions


        ---------------------------------------------


        On Scroll,

        0. Determine if the new viewport position is partial scroll or new random position.
        If viewport is beyond the current DM boundaries, this is new random position, else if viewport only partly overlaps with boundaries,
        this is partial scroll.

        boolean checkIfPartialScroll(ViewPort)
            gdup, gddown, gdleft, gdright, vp.ymin, yp.ymax, vp.xmin, vp.xmax

            if not vp.intersects(boundbox formed by global delta values)
                this is new random position

            else
                this is partial scroll

        problem: when you jump to a location in history, which stores the location, when jumping back to it, what to display?
        // sol: instead of storing location, store the element id. When going to history, build entire tree around this id.
        //

        1. If random position, get top most circle from DB (adjusted with delta and adjusted with collapsed values,
        get bottom most un-collapsed circle?).
        Else, in case of partial scroll, depending on scrolling direction, get the outer most circle.

        void buildTree(Viewport)

        On change of screen, (switch thread), save currently loaded circles to temporary map. Then switch to the new threads saved values and coordinates.

        DB considerations: No reads from db if loads from old threads map, else load like random position. Not huge impact.

        *****problem: delta problem: Will there be huge inconsistency due to delta inaccuracy in a random position? No, i don't think so.


        2. Position Circles: Relative to the outer most circle, position the others as per respective deltas.

        2.5. Fill while the view port is empty. when full, stop filling.

        3. Update the global delta values for all four directions.

        Problem: when starting in random position, the outer most circle (adjusted with delta) from DB is not accurate as we are not accounting
        for other delta values in middle that we have not come across. But i think it does not matter. Because partial scrolling from that point onwards,
        shifts everything relatively to the random position.

        void updateGlobalDeltaValues()

    */


    /*
     * deltaMap keys are x coordinates and values are the offsets or deltas.
     * All elements must be shifted up by an offset that is mapped by a key which is
     * the largest key smaller than or equal to the x coordinates of the element.
     */
    private static TreeMap<Double, Double> deltaMap = new TreeMap<>();

    // public static double yMin;
    public static double yMax;
    // public static double upperDelta;
    public static double lowerDelta;

    public static boolean isAnyCircleMinimized = false;
    public static boolean isAnyCircleMaximized = false;



    public void updateGlobalDeltaValues() {

    }

    // The viewport dimensions for the last updated boundaries.
    private static BoundingBox lastStoredViewPort = new BoundingBox(0, 0, 0, 0);

    public static BoundingBox getLastStoredViewPort() {
        return lastStoredViewPort;
    }

    // stores the global delta values for four directions.
    private static double[] globalDeltaValuse = new double[4]; // top, right, bottom, left.

    public static double[] getGlobalDeltaValuse() {
        return globalDeltaValuse;
    }

    // private DeltaMap() {
    //     deltaMap = new TreeMap<>();
    // }
    //
    // public static TreeMap<Double, Double> getDeltaMap() {
    //     if (deltaMap == null) {
    //         deltaMap = new TreeMap<>();
    //     }
    //
    //     return deltaMap;
    // }

    public static Object getDelta(double y) {
        // Get the value
        if (deltaMap.floorEntry(y) != null)
            return deltaMap.floorEntry(y).getValue();

        return null;
    }

    public static Object getLargestKeySmallerThan(double y) {
        return deltaMap.floorKey(y);
    }

    public static void put(double y, double delta) {

        // add new delta to all values whose keys >= y
        deltaMap.tailMap(y, false).replaceAll((yCrd, dlt) -> dlt + delta);

        // If a key already exists, overwrite.
        // Else, create a new entry for the new delta + delta of upper region.
        if (deltaMap.lowerKey(y) != null) {
            double largestKeySmallerThanX = deltaMap.lowerKey(y);
            double valueOfLargestKeySmallerThanX = deltaMap.get(largestKeySmallerThanX);
            double finalVal = valueOfLargestKeySmallerThanX + delta;
            deltaMap.put(y, finalVal);
        } else {
            deltaMap.put(y, delta);
        }

        System.out.println("--------------------key = " + y + " --- delta = " + delta + " -------------------");
        System.out.println(deltaMap);
        System.out.println("--------------------key = " + y + " --- delta = " + delta + " -------------------");
        System.out.println();
    }

    public static void remove(double y) {

        // Remove key == y. Should be present.
        // For all keys > y, value -= delta added when y was created.

        double delta = deltaMap.remove(y);
        double val = delta - deltaMap.floorEntry(y).getValue();

        List<Double> keysToDecrement = new LinkedList<>(deltaMap.tailMap(y, false).keySet());

        keysToDecrement.forEach(key -> deltaMap.put(key, deltaMap.get(key) - val));

    }


    public static double getMaxDelta(double yMax) {
        double maxKey = 0;

        for (Map.Entry<Double, Double> entry : deltaMap.entrySet()) {

            double key = entry.getKey();
            double delta = entry.getValue();

            if (key - delta <= yMax) {
                maxKey = key;
            } else {
                break;
            }

        }

        return maxKey;
    }


    public static void main(String[] args) {
        // TreeMap<Integer, Integer> deltaMap = new TreeMap<>();
        // deltaMap.put(2, 2);
        // deltaMap.put(3, 3);
        // deltaMap.put(4, 4);
        // deltaMap.put(5, 5);
        // deltaMap.put(6, 6);
        // deltaMap.put(1, 1);
        // deltaMap.put(0, 0);
        // deltaMap.put(-11, -11);
        //
        //

        put((double) 0, (double) 0);
        put((double) 2, (double) 2);
        put((double) 1, (double) 1);
        put((double) 3, (double) 3);
        put((double) 4, (double) 4);
        put((double) 5, (double) 5);
        put((double) 6, (double) 6);


        System.out.println(deltaMap);
        System.out.println();
        remove((double) 3);
        System.out.println(deltaMap);


    }
}
