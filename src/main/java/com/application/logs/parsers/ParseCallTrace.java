package com.application.logs.parsers;

import com.application.Main;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ParseCallTrace implements FileParser {
    private BufferedReader br;
    String line;

    @Override
    public void readFile(File logFile, Main.BytesRead bytesRead, Consumer<List<String>> cmd) {
        try {
            br = new BufferedReader(new FileReader(logFile));
            // ToDo Use streams to perform buffered read and insert.
            while ((line = br.readLine()) != null) {
                bytesRead.readSoFar += line.length(); // not accurate. But we don't need accuracy here.
                List<String> brokenLineList = parse(line);
                cmd.accept(brokenLineList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            br = null;
            line = null;
        }
    }

    public List<String> parse(String line) {
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
