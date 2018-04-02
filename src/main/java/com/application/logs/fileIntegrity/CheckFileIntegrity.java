package com.application.logs.fileIntegrity;

import com.application.Main;
import com.application.logs.parsers.Command;
import com.application.service.tasks.ParseFileTask;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CheckFileIntegrity {
    public static void checkFile (File file, Main.BytesRead bytesRead) {
        try {
            throw new Exception("Dont invoked this methdod.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkFile(File file, ParseFileTask.BytesRead bytesRead, Consumer<Void> cmd) {
        String line = null;
        Deque<Integer> stack;
        int linesRead = 0;
        long workDone = 0;

        stack = new LinkedList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while((line = br.readLine()) != null) {

                bytesRead.readSoFar += line.length();
                cmd.accept(null);

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
                throw up;  // Yuck! Not having any of that either :(
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            int finalLinesRead = linesRead;
            String finalLine = line;

            Platform.runLater(() -> {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Problem with the call trace log file.");
                alert.setHeaderText("An error occurred while reading the " + file.getName() + " file.");
                alert.setContentText("This usually happens either due to the log files not conforming to the syntax rules " +
                        "or due to a mismatch in the count of ENTER and EXIT statements. Please load a different set of log files.");
                ButtonType resetButtonType = new ButtonType("Reset");
                alert.getButtonTypes().setAll(resetButtonType);

                alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

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
            System.out.println("Log file integrity check completed. If no exceptions were thrown, then log file format is valid.");
        }

    }

    static Main main;
    public static void saveRef(Main m) {
        main = m;
    }
}
