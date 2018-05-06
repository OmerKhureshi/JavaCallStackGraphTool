package com.application.logs.fileIntegrity;

import com.application.Main;
import com.application.controller.ControllerLoader;
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
        int enterCounter = 0;
        int exitCounter = 0;

        int linesRead = 0;
        long workDone = 0;

        stack = new LinkedList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {

                bytesRead.readSoFar += line.length();
                cmd.accept(null);

                String msg = line.split("\\|")[3];
                switch (msg.toUpperCase()) {
                    case "WAIT-ENTER":
                    case "NOTIFY-ENTER":
                    case "NOTIFYALL-ENTER":
                    case "ENTER":
                        stack.push(1);
                        enterCounter++;
                        break;

                    case "WAIT-EXIT":
                    case "NOTIFY-EXIT":
                    case "NOTIFYALL-EXIT":
                    case "EXIT":
                        stack.pop();
                        exitCounter++;
                        break;

                    default:
                        System.out.println("error occurred in " + msg.toUpperCase());
                        handleEmptyFile(file);
                }
                ++linesRead;
            }

            if (enterCounter != exitCounter) {
                handleFileNonConformance(file, linesRead, line);

            }

            if (enterCounter == 0 || exitCounter == 0) {
                handleEmptyFile(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleEmptyFile(File file) throws Exception {
        String title = "Problem with the call trace log file.";
        String header = "An error occurred while reading the " + file.getName() + " file.";
        String message = "This usually happens when the log file is empty or doesn't conform to the syntax rules. " +
                "Please reset (View menu -> Reset) and load a different set of log files.";

        ControllerLoader.mainController.showErrorPopup(title, header, message);

        Platform.runLater(() -> {
            ControllerLoader.menuController.closeProgressBar();
            ControllerLoader.instructionsPaneController.setErrorGraphics(true);
        });

        throw new Exception("This usually happens when the log file is empty or doesn't conform to the syntax rules. " +
                "Please reset (View menu -> Reset) and load a different set of log files. ");
    }

    private static void handleFileNonConformance(File file, int linesRead, String line) throws Exception {
        String title = "Problem with the call trace log file.";
        String header = "An error occurred while reading the " + file.getName() + " file.";
        String message = "This usually happens either due to the log files not conforming to the syntax rules " +
                "or due to a mismatch in the count of ENTER and EXIT statements. Please load a different set of log files.";

        ControllerLoader.mainController.showErrorPopup(title, header, message);

        System.out.println("Error occurred in line due to mismatch in count of enters and exits. " +
                "Error at line: " + linesRead + "; Line is: " + line);

        Platform.runLater(() -> {
            ControllerLoader.menuController.closeProgressBar();
            ControllerLoader.instructionsPaneController.setErrorGraphics(true);
        });

        throw new Exception("This usually happens when the log file is empty or doesn't conform to the syntax rules. " +
                "Please reset (View menu -> Reset) and load a different set of log files. ");
    }

    static Main main;
    public static void saveRef(Main m) {
        main = m;
    }
}
