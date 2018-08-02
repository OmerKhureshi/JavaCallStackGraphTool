package com.csgt.dataaccess.DTO;

public class ElementToChildDTO extends BaseDTO {
    private String parentId;
    private String childId;

    public ElementToChildDTO() {
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }
}
