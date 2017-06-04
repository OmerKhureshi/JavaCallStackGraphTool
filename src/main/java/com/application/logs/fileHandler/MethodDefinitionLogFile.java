package com.application.logs.fileHandler;

import java.io.File;

public class MethodDefinitionLogFile {
    // private static String fileName = "L-Instrumentation_method_definitions.txt";
    // private static String fileName = "logs/L-Instrumentation_method_definitions_Demo_2.txt";
    private static String fileName = "";
    MethodDefinitionLogFile() {
        // Get the resources
        // http://stackoverflow.com/a/21722773/3690248
        file = new File(Thread.currentThread().getContextClassLoader().getResource(fileName).getFile());
    }

    public static File getFile() {
        return file;
    }

    public static void setFile(File newFile) {
        file = newFile;
    }

    private static File file = new File(Thread.currentThread().getContextClassLoader().getResource(fileName).getFile());

    public static String getFileName() {
        return fileName;
    }

    public static void setFileName(String newFileName) {
        fileName = newFileName;
        file = new File(Thread.currentThread().getContextClassLoader().getResource(fileName).getFile());
    }
}
