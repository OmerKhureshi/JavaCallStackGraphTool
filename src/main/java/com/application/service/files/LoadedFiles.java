package com.application.service.files;

import java.io.File;

public class LoadedFiles {
    private static File methodDefLogFile;
    private static File callTraceLogFile;
    private static File dbFile;

    public static void setFile(String name, File file) {
        switch (name) {
            case "methodDef":
                methodDefLogFile = file;
                break;

            case "callTrace":
                callTraceLogFile = file;
                break;

            case "db":
                dbFile = file;
                break;
        }
    }

    public static File getFile(String name) {
        File file = null;
        switch (name) {
            case "methodDef":
                file = methodDefLogFile;
                break;

            case "callTrace":
                file = callTraceLogFile;
                break;

            case "db":
                file = dbFile;
                break;
        }

        return file;
    }

}
