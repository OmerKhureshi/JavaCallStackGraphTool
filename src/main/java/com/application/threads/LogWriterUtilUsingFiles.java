package com.application.threads;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

public class LogWriterUtilUsingFiles {
    static Path file;

    private static void initialize() {
        file = Paths.get("ObjectWrapperCallTrace.txt");
    }

    public synchronized static void write(String line) {
        if (file == null) {
            initialize();
        }

        try {
            Files.write(file, Collections.singletonList(line), Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
