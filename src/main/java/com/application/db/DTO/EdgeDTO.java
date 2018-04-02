package com.application.db.DTO;

public class EdgeDTO extends BaseDTO {

    private String id;
    private String sourceElementId;
    private String targetElementId;
    private float startX;
    private float startY;
    private float endX;
    private float endY;
    private float collapsed;

    public EdgeDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceElementId() {
        return sourceElementId;
    }

    public void setSourceElementId(String sourceElementId) {
        this.sourceElementId = sourceElementId;
    }

    public String getTargetElementId() {
        return targetElementId;
    }

    public void setTargetElementId(String targetElementId) {
        this.targetElementId = targetElementId;
    }

    public float getStartX() {
        return startX;
    }

    public void setStartX(float startX) {
        this.startX = startX;
    }

    public float getStartY() {
        return startY;
    }

    public void setStartY(float startY) {
        this.startY = startY;
    }

    public float getEndX() {
        return endX;
    }

    public void setEndX(float endX) {
        this.endX = endX;
    }

    public float getEndY() {
        return endY;
    }

    public void setEndY(float endY) {
        this.endY = endY;
    }

    public float getCollapsed() {
        return collapsed;
    }

    public void setCollapsed(float collapsed) {
        this.collapsed = collapsed;
    }
}
