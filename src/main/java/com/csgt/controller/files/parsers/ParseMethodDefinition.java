package com.csgt.controller.files.parsers;

import com.csgt.controller.tasks.ParseFileTask;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ParseMethodDefinition{

    public List<List<String>> parseFile(File logFile, ParseFileTask.BytesRead bytesRead, Consumer<Void> cmd) {
        List<List<String>> parsedLinesList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
          br.lines().forEachOrdered(line -> {
              List<String> parsedLine = parse(line);
              parsedLinesList.add(parsedLine);
              bytesRead.readSoFar += line.length();
              cmd.accept(null);
          });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return parsedLinesList;
    }

    public List<String> parse(String line) {
        return Arrays.asList(line.split("\\|"));
    }
}
