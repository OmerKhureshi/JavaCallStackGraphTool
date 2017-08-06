package com.application.fxgraph.graph;

import java.util.*;

public class DeltaMap {

    /*
     * deltaMap keys are x coordinates and values are the offsets or deltas.
     * All elements must be shifted up by an offset that is mapped by a key which is
     * the largest key smaller than or equal to the x coordinates of the element.
     */
    private static TreeMap<Double, Double> deltaMap = new TreeMap<>();

    public static double yMin;
    public static double yMax;
    public static double upperDelta;
    public static double lowerDelta;

    public static boolean isAnyCircleMinimized = false;
    public static boolean isAnyCircleMaximized = false;


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

        for (Map.Entry<Double, Double> entry: deltaMap.entrySet()) {

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

        put((double)0, (double)0);
        put((double)2, (double)2);
        put((double)1, (double)1);
        put((double)3, (double)3);
        put((double)4, (double)4);
        put((double)5, (double)5);
        put((double)6, (double)6);


        System.out.println(deltaMap);
        System.out.println();
        remove((double)3);
        System.out.println(deltaMap);


    }



}
