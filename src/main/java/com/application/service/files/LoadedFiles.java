package com.application.service.files;

import java.io.File;

public class LoadedFiles {
    private static File methodDefLogFile;
    private static File callTraceLogFile;
    private static File dbFile;

    private static boolean isloadedFromDB = false;

    public static void setFile(String name, File file) {
        if (name.equalsIgnoreCase(FileNames.METHOD_DEF.getFileName())) {
            methodDefLogFile = file;
        } else if (name.equalsIgnoreCase(FileNames.Call_Trace.getFileName())) {
            callTraceLogFile = file;
        } else if (name.equalsIgnoreCase(FileNames.DB.getFileName())) {
            dbFile = file;
        }
    }

    public static File getFile(String name) {
        if (name.equalsIgnoreCase(FileNames.METHOD_DEF.getFileName())) {
            return methodDefLogFile;
        } else if (name.equalsIgnoreCase(FileNames.Call_Trace.getFileName())) {
            return callTraceLogFile;
        } else if (name.equalsIgnoreCase(FileNames.DB.getFileName())) {
            return dbFile;
        }

        return null;
    }

    public static boolean isLoadedFromDB() {
        return isloadedFromDB;
    }

    public static void setLoadFromDB(boolean isloadedFromDB) {
        LoadedFiles.isloadedFromDB = isloadedFromDB;
    }
}
