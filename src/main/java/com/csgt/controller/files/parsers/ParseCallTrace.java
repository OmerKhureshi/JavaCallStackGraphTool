package com.csgt.controller.files.parsers;

import com.csgt.controller.tasks.ParseFileTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Parser class for Call Trace Log file.
 */
public class ParseCallTrace  {

    public void readFile(File logFile, ParseFileTask.BytesRead bytesRead, Consumer<List<String>> cmd) {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(logFile))){
            while ((line = br.readLine()) != null) {
                bytesRead.readSoFar += line.length(); // not accurate. But we don't need accuracy here.
                List<String> brokenLineList = parse(line);
                cmd.accept(brokenLineList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            line = null;
        }
    }

    private List<String> parse(String line) {
        return Arrays.asList(line.split("\\|"));
    }

    public static int countNumberOfLines(File file) {
        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(file));
            lnr.skip(Long.MAX_VALUE);
            return lnr.getLineNumber() + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
