package com.application.fxgraph.graph;

public class BoundBox {
    public float xTopLeft;
    public float yTopLeft;

    public float xTopRight;
    public float yTopRight;

    public float xBottomRight;
    public float yBottomRight;

    public float xBottomLeft;
    public float yBottomLeft;

    public static final int unitWidthFactor = 120;  // The width of the rectangular space that contains a single element on UI
    public static final int unitHeightFactor = 70;  // The height of the rectangular space that contains a single element on UI

    public float xCoordinate;
    public float yCoordinate;

//    @Override
//    public String toString() {
//        return "BoundBox{" +
//                "xTopLeft=" + xTopLeft +
//                ", yTopLeft=" + yTopLeft +
//                ", xTopRight=" + xTopRight +
//                ", yTopRight=" + yTopRight +
//                ", xBottomRight=" + xBottomRight +
//                ", yBottomRight=" + yBottomRight +
//                ", xBottomLeft=" + xBottomLeft +
//                ", yBottomLeft=" + yBottomLeft +
//                ", unitWidthFactor=" + unitWidthFactor +
//                ", unitHeightFactor=" + unitHeightFactor +
//                ", xCoordinate=" + xCoordinate +
//                ", yCoordinate=" + yCoordinate +
//                '}';
//    }
}
