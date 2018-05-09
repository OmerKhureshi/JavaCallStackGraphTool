package com.csgt.dataaccess.DTO;

public class ElementDTO extends BaseDTO {

    private String id;
    private int parentId;
    private int idEnterCallTrace;
    private int idExitCallTrace;
    private float boundBoxXTopLeft;
    private float boundBoxYTopLeft;
    private float boundBoxXTopRight;
    private float boundBoxYTopRight;
    private float boundBoxXBottomRight;
    private float boundBoxYBottomRight;
    private float boundBoxXBottomLeft;
    private float boundBoxYBottomLeft;
    private float boundBoxXCoordinate;
    private float boundBoxYCoordinate;
    private float indexInParent;
    private float leafCount;
    private float levelCount;
    private int collapsed;
    private float delta;
    private float deltaX;

    private String thredId;

    private int methodId;

    private String methodName;

    public ElementDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getIdEnterCallTrace() {
        return idEnterCallTrace;
    }

    public void setIdEnterCallTrace(int idEnterCallTrace) {
        this.idEnterCallTrace = idEnterCallTrace;
    }

    public int getIdExitCallTrace() {
        return idExitCallTrace;
    }

    public void setIdExitCallTrace(int idExitCallTrace) {
        this.idExitCallTrace = idExitCallTrace;
    }

    public float getBoundBoxXTopLeft() {
        return boundBoxXTopLeft;
    }

    public void setBoundBoxXTopLeft(float boundBoxXTopLeft) {
        this.boundBoxXTopLeft = boundBoxXTopLeft;
    }

    public float getBoundBoxYTopLeft() {
        return boundBoxYTopLeft;
    }

    public void setBoundBoxYTopLeft(float boundBoxYTopLeft) {
        this.boundBoxYTopLeft = boundBoxYTopLeft;
    }

    public float getBoundBoxXTopRight() {
        return boundBoxXTopRight;
    }

    public void setBoundBoxXTopRight(float boundBoxXTopRight) {
        this.boundBoxXTopRight = boundBoxXTopRight;
    }

    public float getBoundBoxYTopRight() {
        return boundBoxYTopRight;
    }

    public void setBoundBoxYTopRight(float boundBoxYTopRight) {
        this.boundBoxYTopRight = boundBoxYTopRight;
    }

    public float getBoundBoxXBottomRight() {
        return boundBoxXBottomRight;
    }

    public void setBoundBoxXBottomRight(float boundBoxXBottomRight) {
        this.boundBoxXBottomRight = boundBoxXBottomRight;
    }

    public float getBoundBoxYBottomRight() {
        return boundBoxYBottomRight;
    }

    public void setBoundBoxYBottomRight(float boundBoxYBottomRight) {
        this.boundBoxYBottomRight = boundBoxYBottomRight;
    }

    public float getBoundBoxXBottomLeft() {
        return boundBoxXBottomLeft;
    }

    public void setBoundBoxXBottomLeft(float boundBoxXBottomLeft) {
        this.boundBoxXBottomLeft = boundBoxXBottomLeft;
    }

    public float getBoundBoxYBottomLeft() {
        return boundBoxYBottomLeft;
    }

    public void setBoundBoxYBottomLeft(float boundBoxYBottomLeft) {
        this.boundBoxYBottomLeft = boundBoxYBottomLeft;
    }

    public float getBoundBoxXCoordinate() {
        return boundBoxXCoordinate;
    }

    public void setBoundBoxXCoordinate(float boundBoxXCoordinate) {
        this.boundBoxXCoordinate = boundBoxXCoordinate;
    }

    public float getBoundBoxYCoordinate() {
        return boundBoxYCoordinate;
    }

    public void setBoundBoxYCoordinate(float boundBoxYCoordinate) {
        this.boundBoxYCoordinate = boundBoxYCoordinate;
    }

    public float getIndexInParent() {
        return indexInParent;
    }

    public void setIndexInParent(float indexInParent) {
        this.indexInParent = indexInParent;
    }

    public float getLeafCount() {
        return leafCount;
    }

    public void setLeafCount(float leafCount) {
        this.leafCount = leafCount;
    }

    public float getLevelCount() {
        return levelCount;
    }

    public void setLevelCount(float levelCount) {
        this.levelCount = levelCount;
    }

    public int getCollapsed() {
        return collapsed;
    }

    public void setCollapsed(int collapsed) {
        this.collapsed = collapsed;
    }

    public float getDeltaY() {
        return delta;
    }

    public void setDeltaY(float delta) {
        this.delta = delta;
    }

    public float getDeltaX() {
        return deltaX;
    }

    public void setDeltaX(float deltaX) {
        this.deltaX = deltaX;
    }

    public int getMethodId() {
        return methodId;
    }

    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getThredId() {
        return thredId;
    }

    public void setThredId(String thredId) {
        this.thredId = thredId;
    }
}
