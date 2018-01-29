package com.application.fxgraph.ElementHelpers;

import java.util.List;

public class SimplifiedElement {

    private SimplifiedElement parentElement;
    private String elementId;
    private String methodName;
    private List<SimplifiedElement> children;

    public SimplifiedElement(String elementId, String methodName) {
        this.elementId = elementId;
        this.methodName = methodName;
    }

    public SimplifiedElement getParentElement() {
        return parentElement;
    }

    public void setParentElement(SimplifiedElement parentElement) {
        this.parentElement = parentElement;
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
}
