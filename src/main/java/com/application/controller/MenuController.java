package com.application.controller;

import com.application.presentation.CustomProgressBar;
import com.application.service.files.LoadedFiles;
import com.application.service.tasks.ConstructTreeTask;
import com.application.service.tasks.ParseFileTask;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

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

    private MainController mainController;

    private File methodDefinitionLogFile;
    private File callTraceLogFile;
    private File dbFile;

    @FXML
    private void initialize() {
        System.out.println("MenuController.initialize ");
        onStartUp();
    }

    private void onStartUp() {
        setUpMenu();
    }

    private void setUpMenu() {
        setUpFileMenu();
        setUpRunMenu();
    }

    private void setUpFileMenu() {
        chooseMethodDefMenuItem.setOnAction(event -> {
            try {
                File methodDefLogFile = ControllerUtil.fileChooser("Choose method definition log fileMenu.", "Text Files", "*.txt");
                LoadedFiles.setFile("methodDefLogFile", methodDefLogFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        chooseCallTraceMenuItem.setOnAction(event -> {
            try {
                File callTraceLogFile = ControllerUtil.fileChooser("Choose call trace log fileMenu.", "Text Files", "*.txt");
                LoadedFiles.setFile("callTraceLogFile", callTraceLogFile );

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        openDBMenuItem.setOnAction(event -> {
            try {
                File dbFile = ControllerUtil.directoryChooser("Choose an existing database.");
                LoadedFiles.setFile("db", dbFile);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setUpRunMenu() {
        runAnalysisMenuItem.setOnAction(event -> {
            this.mainController.showGraphPane();

            Task<Void> parseTask = new ParseFileTask();
            Task<Void> constructTreeTask = new ConstructTreeTask();

            CustomProgressBar customProgressBar = new CustomProgressBar("", "",
                    Arrays.asList(parseTask, constructTreeTask));

            ExecutorService es = Executors.newSingleThreadExecutor();
            es.submit(parseTask);
            es.submit(constructTreeTask);
            es.shutdown();

            constructTreeTask.setOnSucceeded((e) -> customProgressBar.close());

        });

        resetMenuItem.setOnAction(event -> {
            System.out.println("reset clicked.");
            this.mainController.showInstructionsPane();
        });
    }

    public void setParentController(MainController mainController) {
        this.mainController = mainController;
    }

}
