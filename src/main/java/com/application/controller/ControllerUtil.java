package com.application.controller;

import com.application.db.DTO.BaseDTO;
import com.application.db.DTO.ElementDTO;
import com.application.fxgraph.cells.CircleCell;
import com.application.fxgraph.graph.Cell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ControllerUtil {

    private static Stage primaryStage;

    public static File fileChooser(String title, String desc, String extensions) throws Exception {
        if (primaryStage == null) {
            throw new Exception("Primary Stage not set");
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, extensions));
        fileChooser.setTitle(title);
        File logFile = fileChooser.showOpenDialog(primaryStage);
        if (logFile != null) {
            return logFile;
        }

        return null;
    }

    public static File directoryChooser(String title) throws Exception {
        if (primaryStage == null) {
            throw new Exception("Primary Stage not set");
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        return directoryChooser.showDialog(primaryStage);
    }


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public static List<CircleCell> convertElementDTOTOCell(List<ElementDTO> elementDTOList) {
        List<CircleCell> circleCellList = new ArrayList<>();

        elementDTOList.forEach(elementDTO -> {
            CircleCell circleCell = new CircleCell(elementDTO.getId(), elementDTO.getBoundBoxXCoordinate(), elementDTO.getBoundBoxYCoordinate(), elementDTO.getMethodName());
            circleCellList.add(circleCell);
        });

        return circleCellList;
    }

}
