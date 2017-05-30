package com.application.logs.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ParseMethodDefinition implements FileParser {
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
    //             ConvertDBtoElementTree convertDBtoElementTree = new ConvertDBtoElementTree();
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

    @Override
    public void readFile(File logFile, Consumer<List<String>> cmd) {
        try {
            br = new BufferedReader(new FileReader(logFile));
            // ToDo Look into streams to perform buffered read and insert.
            while((line = br.readLine()) != null) {
                List<String> brokenLineList = parse(line);
                cmd.accept(brokenLineList);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<String> parse(String line) {
//        System.out.println(Arrays.asList(line.split("\\|")));
        return Arrays.asList(line.split("\\|"));
    }
}
