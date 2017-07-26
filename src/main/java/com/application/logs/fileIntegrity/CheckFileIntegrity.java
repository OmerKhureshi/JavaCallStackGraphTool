package com.application.logs.fileIntegrity;

import com.application.Main;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Optional;

public class CheckFileIntegrity {


    public static void checkFile (File file, Main.BytesRead bytesRead) {
        String line = null;
        Deque<Integer> stack;
        int linesRead = 0;

        stack = new LinkedList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while((line = br.readLine()) != null) {
                bytesRead.readSoFar += line.length();
                String msg = line.split("\\|")[3];
                    switch (msg.toUpperCase()) {
                        case "WAIT-ENTER":
                        case "NOTIFY-ENTER":
                        case "NOTIFYALL-ENTER":
                        case "ENTER":
                            stack.push(1);
                            break;

                        case "WAIT-EXIT":
                        case "NOTIFY-EXIT":
                        case "NOTIFYALL-EXIT":
                        case "EXIT":
                            stack.pop();
                            break;

                        default:
                            IllegalStateException up = new IllegalStateException("Error occurred in line: " + line);
                            throw up;  // Yuck! Not having any of that :(
                    }
                    ++linesRead;
            }
            if (!(stack.isEmpty())) {
                IllegalStateException up = new IllegalStateException("Stack should have been empty, it is not. Error at line " + linesRead + 1);
                throw up;  // Yuck! Not having any of that :(
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
        } catch (NoSuchElementException e) {
            int finalLinesRead = linesRead;
            String finalLine = line;


            Platform.runLater(() -> {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Problem with the call trace log file.");
                alert.setHeaderText("An error occurred while reading the " + file.getName() + " file.");
                alert.setContentText("This usually happens due to mismatch in count of ENTER and EXIT statements");
                ButtonType resetButtonType = new ButtonType("Reset");
                alert.getButtonTypes().setAll(resetButtonType);

                Optional<ButtonType> res = alert.showAndWait();


                if (res.get() == resetButtonType) {
                    main.resetFromOutside();
                }

                e.printStackTrace();
                throw new NoSuchElementException("Error occurred in line due to mismatch in count of enters and exits. " +
                        "Error at line: " + finalLinesRead + "; Line is: " + finalLine);
            });

            // e.printStackTrace();
            // throw new NoSuchElementException("Error occurred in line due to mismatch in count of enters and exits. " +
            //         "Error at line: " + linesRead + "; Line is: " + line);
        } finally {
            System.out.println("File integrity check completed. If no exceptions were thrown, then file is good.");
        }

    }

    static Main main;
    public static void saveRef(Main m) {
        main = m;
    }
}
