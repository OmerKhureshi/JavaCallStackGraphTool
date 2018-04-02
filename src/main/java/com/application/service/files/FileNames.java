package com.application.service.files;

public enum FileNames {

    METHOD_DEF("methodDef"),
    Call_Trace("callTrace"),
    DB("db");

    private String fileName;

    FileNames(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
