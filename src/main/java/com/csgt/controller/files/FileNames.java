package com.csgt.controller.files;

public enum FileNames {

    METHOD_DEF("methodDef"),
    Call_Trace("callTrace"),
    DB("dataaccess");

    private String fileName;

    FileNames(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
