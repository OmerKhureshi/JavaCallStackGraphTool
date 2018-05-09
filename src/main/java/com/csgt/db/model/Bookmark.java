package com.csgt.db.model;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class Bookmark {
    private String elementId;
    private String threadId;
    private String methodName;
    private String color;
    private double xCoordinate;
    private double yCoordinate;
    private int collapsed;

    public static int barWidth = 20, barHeight = 2;

    private Rectangle barMark = new Rectangle(20, 1);

    public Bookmark( String elementId, String threadId, String methodName, String color, double xCoordinate, double yCoordinate, int collapsed) {
        this.elementId = elementId;
        this.threadId = threadId;
        this.methodName = methodName;
        this.color = color;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.collapsed = collapsed;

        barMark.setFill(Paint.valueOf(color));
    }

    @Override
    public String toString() {
        return "Bookmark{" +
                "elementId='" + elementId + '\'' +
                ", threadId='" + threadId + '\'' +
                ", methodName='" + methodName + '\'' +
                ", color='" + color + '\'' +
                ", xCoordinate=" + xCoordinate +
                ", yCoordinate=" + yCoordinate +
                '}';
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(double xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public double getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(double yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public int getCollapsed() {
        return collapsed;
    }

    public void setCollapsed(int collapsed) {
        this.collapsed = collapsed;
    }
}
