package com.csgt.controller.files;

import com.csgt.db.DatabaseUtil;

import java.io.File;

public class LoadedFiles {
    private static File methodDefLogFile;
    private static File callTraceLogFile;
    private static File dbFile;

    public static void setFile(String name, File file) {
        if (name.equalsIgnoreCase(FileNames.METHOD_DEF.getFileName())) {
            methodDefLogFile = file;
        } else if (name.equalsIgnoreCase(FileNames.Call_Trace.getFileName())) {
            callTraceLogFile = file;
        } else if (name.equalsIgnoreCase(FileNames.DB.getFileName())) {
            dbFile = file;
            DatabaseUtil.setDBDir(file);
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
        return dbFile != null;
    }

    public static void resetFile() {
        methodDefLogFile = null;
        callTraceLogFile = null;
        dbFile = null;
        DatabaseUtil.setDBDir(null);
    }
}
