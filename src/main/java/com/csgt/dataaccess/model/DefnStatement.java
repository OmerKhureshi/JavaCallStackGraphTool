package com.csgt.dataaccess.model;

/**
 * Model for a log statement in the Method Definition File.
 */
public class DefnStatement {
    int methodId;
    String packageName;
    String methodName;
    String arguments;

    public DefnStatement(int methodId, String packageName, String methodName, String arguments) {
        this.methodId = methodId;
        this.packageName = packageName;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    public int getMethodId() {
        return methodId;
    }

    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
}
