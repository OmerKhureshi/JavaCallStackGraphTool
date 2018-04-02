package com.application.logs.parsers;

import com.application.Main;
import com.application.service.tasks.ParseFileTask;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ParseMethodDefinition{
    private BufferedReader br;
    String line;
    List<String> vals;

    /*
    I need a call back method in readFile method definition, that invokes this call back every time a new line is read from the line.
     */
    // public boolean readFile(File logFile) {
    //     try {
    //         br = new BufferedReader(new FileReader(logFile));
    //         // ToDo Look into streams to perform buffered read and insert.
    //         while((line = br.readLine()) != null) {
    //             List<String> brokenLineList = parse(line);
    //             DatabaseUtil.insertMDStmt(brokenLineList);
    //             ElementTreeModule convertDBtoElementTree = new ElementTreeModule();
    //             convertDBtoElementTree.StringToElementList(brokenLineList);
    //             convertDBtoElementTree.calculateElementProperties();
    //         }
    //     } catch (FileNotFoundException e) {
    //         e.printStackTrace();
    //     } catch (Exception e){
    //         e.printStackTrace();
    //     }
    //     return false;
    // }

    // @Override

    public void readFile(File logFile, Main.BytesRead bytesRead, Consumer<List<String>> cmd) {

    }

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

    public void readFile(File logFile, ParseFileTask.BytesRead bytesRead, Consumer<List<String>> cmd) {
        try {
            br = new BufferedReader(new FileReader(logFile));
            // while((line = br.readLine()) != null) {
            //     bytesRead.readSoFar += line.length();
            //     List<String> brokenLineList = parse(line);
            //     cmd.accept(brokenLineList);
            // }

            br.lines().forEachOrdered(line -> {
                bytesRead.readSoFar += line.length();
                List<String> parsedLine = parse(line);
                cmd.accept(parsedLine);
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<String> parse(String line) {
//        System.out.println(Arrays.asList(line.split("\\|")));
        return Arrays.asList(line.split("\\|"));
    }
}
