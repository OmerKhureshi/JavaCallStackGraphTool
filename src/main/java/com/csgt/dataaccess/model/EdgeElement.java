package com.csgt.dataaccess.model;

import com.csgt.dataaccess.model.Element;

public class EdgeElement {
    private double startX, startY, endX, endY;
    private Element sourceElement, targetElement;

    // collapsed = 0 -> associated target element is not collapsed. Show this edge.
    // collapsed = 1 -> associated target element is collapsed. Hide this edge.
    private int collapsed = 0;

    public EdgeElement(Element sourceElement, Element targetElement) {
        this.sourceElement = sourceElement;
        this.targetElement = targetElement;
    }

    public void calculateEndPoints() {

        startX = ( sourceElement.getBoundBox().xCoordinate); //+ sourceElement.getBoundBox().unitWidthFactor  * .5 );
        startY = ( sourceElement.getBoundBox().yCoordinate); //+ sourceElement.getBoundBox().unitHeightFactor * .5 );
        endX =   ( targetElement.getBoundBox().xCoordinate); //+ targetElement.getBoundBox().unitWidthFactor  * .5 );
        endY =   ( targetElement.getBoundBox().yCoordinate); //+ targetElement.getBoundBox().unitHeightFactor * .5 );

    }

    public double getStartX() {
        return startX;
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public double getStartY() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    public double getEndX() {
        return endX;
    }

    public void setEndX(double endX) {
        this.endX = endX;
    }

    public double getEndY() {
        return endY;
    }

    public void setEndY(double endY) {
        this.endY = endY;
    }

    public Element getSourceElement() {
        return sourceElement;
    }

    public void setSourceElement(Element sourceElement) {
        this.sourceElement = sourceElement;
    }

    public Element getTargetElement() {
        return targetElement;
    }

    public void setTargetElement(Element targetElement) {
        this.targetElement = targetElement;
    }

    public int getCollapsed() {
        return collapsed;
    }

    public void setCollapsed(int collapsed) {
        this.collapsed = collapsed;
    }

}


