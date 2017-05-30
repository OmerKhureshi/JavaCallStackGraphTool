package com.application.threads;

import java.io.*;

public class LogWriterUtil {
    private static File file;
    private static StatePrintWriter printWriter;
    private static boolean isFirstRun = true;

    public static void initialize(File fileName) {
        isFirstRun = false;
        file = fileName;

        try {
            printWriter = new StatePrintWriter(new PrintWriter(new BufferedWriter(new FileWriter(file))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void write(String line) {
        if (isFirstRun || file == null) {
            throw new IllegalStateException("LogWriterUtil should be initialized with file name first." +
                    " Use ObjWrapper.setLogFileName()");
        }

        if (printWriter.isOpen()) {
            printWriter.println(line);
            printWriter.flush();
        } else
            throw new IllegalStateException("File is closed. Hence cannot write to it.");
    }
}

class StatePrintWriter extends PrintWriter {
    StatePrintWriter(PrintWriter writer) {
        super(writer);
    }

    boolean isOpen() {
        return out != null;
    }
}
