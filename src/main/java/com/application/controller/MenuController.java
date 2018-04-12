package com.application.controller;

import com.application.presentation.CustomProgressBar;
import com.application.service.files.FileNames;
import com.application.service.files.LoadedFiles;
import com.application.service.tasks.ConstructTreeTask;
import com.application.service.tasks.ParseFileTask;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.util.Duration;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MenuController {

    @FXML private MenuItem chooseMethodDefMenuItem;
    @FXML private MenuItem chooseCallTraceMenuItem;
    @FXML private MenuItem openDBMenuItem;

    @FXML private MenuItem runAnalysisMenuItem;
    @FXML private MenuItem resetMenuItem;

    @FXML
    private MenuItem printViewPortDimsMenuItem;

    private MainController mainController;

    private File methodDefinitionLogFile;
    private File callTraceLogFile;
    private File dbFile;

    @FXML
    private void initialize() {
        // onStartUp();
        autoRun();
        setUpDebugMenu();
    }

    private void onStartUp() {
        setUpMenu();
    }

    private void setUpMenu() {
        setUpFileMenu();
        setUpRunMenu();
        setUpDebugMenu();
    }

    private void autoRun() {
        setFiles();
    }

    private void setFiles() {
        File methodDefLogFile = new File("/Users/skhureshi/Documents/Logs/MD1.txt");
        LoadedFiles.setFile(FileNames.METHOD_DEF.getFileName(), methodDefLogFile);

        File callTraceLogFile = new File("/Users/skhureshi/Documents/Logs/CT1.txt");
        LoadedFiles.setFile(FileNames.Call_Trace.getFileName(), callTraceLogFile );

        onRun();
    }

    private void setUpFileMenu() {
        chooseMethodDefMenuItem.setOnAction(event -> {
            try {
                File methodDefLogFile = ControllerUtil.fileChooser("Choose method definition log fileMenu.", "Text Files", "*.txt");
                LoadedFiles.setFile(FileNames.METHOD_DEF.getFileName(), methodDefLogFile);
                System.out.println("MenuController.setUpFileMenu file: " + methodDefLogFile.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        chooseCallTraceMenuItem.setOnAction(event -> {
            try {
                File callTraceLogFile = ControllerUtil.fileChooser("Choose call trace log fileMenu.", "Text Files", "*.txt");
                LoadedFiles.setFile(FileNames.Call_Trace.getFileName(), callTraceLogFile );
                System.out.println("MenuController.setUpFileMenu file: " + callTraceLogFile );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        openDBMenuItem.setOnAction(event -> {
            try {
                File dbFile = ControllerUtil.directoryChooser("Choose an existing database.");
                LoadedFiles.setFile("db", dbFile);
                LoadedFiles.setFreshLoad(false);
                System.out.println("MenuController.setUpFileMenu db: " + dbFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setUpRunMenu() {
        runAnalysisMenuItem.setOnAction(event -> {
            onRun();
        });

        resetMenuItem.setOnAction(event -> {
            System.out.println("reset clicked.");
            this.mainController.showInstructionsPane();
        });
    }

    private void setUpDebugMenu() {
        printViewPortDimsMenuItem.setOnAction(event -> {
            System.out.println("ScrollPane viewport dimensions");
            System.out.println(ControllerLoader.canvasController.scrollPane.getViewportBounds());
            System.out.println(ControllerLoader.canvasController.getViewPortDims());
        });
    }

    private void onRun() {
        // No need to parse log file and compute graph if loading from DB.
        if (!LoadedFiles.IsFreshLoad()) {
            return;
        }

        Task<Void> parseTask = new ParseFileTask();
        Task<Void> constructTreeTask = new ConstructTreeTask();

        CustomProgressBar customProgressBar = new CustomProgressBar("", "",
                Arrays.asList(parseTask, constructTreeTask));

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(parseTask);
        es.submit(constructTreeTask);
        es.shutdown();

        customProgressBar.bind(parseTask);

        parseTask.setOnSucceeded((e) -> {
            customProgressBar.bind(constructTreeTask);
        });

        constructTreeTask.setOnSucceeded((e) -> {
            customProgressBar.close();
            this.mainController.loadGraphPane();

            // force update UI
            // System.out.println("MenuController.onRun calling update ");
            // System.out.println("MenuController.onRun " + ControllerLoader.canvasController.scrollPane.getViewportBounds());
            // ControllerLoader.canvasController.updateIfNeeded(false);

            System.out.println("MenuController.setUpRunMenu constructTreeTask completed successfully.");
        });

    }

    void setParentController(MainController mainController) {
        this.mainController = mainController;
    }
}
