package com.application.controller;

import com.application.db.DTO.BaseDTO;
import com.application.db.DTO.EdgeDTO;
import com.application.db.DTO.ElementDTO;
import com.application.fxgraph.ElementHelpers.EdgeElement;
import com.application.fxgraph.ElementHelpers.Element;
import com.application.fxgraph.cells.CircleCell;
import com.application.fxgraph.graph.Cell;
import com.application.fxgraph.graph.Edge;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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


    public static void setPrimaryStage(Stage primaryStage) {
        ControllerUtil.primaryStage = primaryStage;
    }

    public static List<CircleCell> convertElementDTOTOCell(List<ElementDTO> elementDTOList) {
        List<CircleCell> circleCellList = new ArrayList<>();

        System.out.println();
        System.out.println("ControllerUtil.convertElementDTOTOCell ");
        elementDTOList.forEach(elementDTO -> {
            CircleCell circleCell = new CircleCell(elementDTO.getId(), elementDTO.getBoundBoxXCoordinate(), elementDTO.getBoundBoxYCoordinate(), elementDTO.getMethodName());
            circleCellList.add(circleCell);
            System.out.print(circleCell.getCellId() + ", ");
        });

        return circleCellList;
    }

    public static List<ElementDTO> convertElementToElementDTO(List<Element> elements) {
        return elements.stream()
                .map((element) -> {
                    ElementDTO elementDTO = new ElementDTO();
                    elementDTO.setId(String.valueOf(element.getElementId()));
                    elementDTO.setParentId(element.getParent() == null? -1 : element.getParent().getElementId());
                    elementDTO.setIdEnterCallTrace(element.getFkEnterCallTrace());
                    elementDTO.setIdExitCallTrace(element.getFkExitCallTrace());
                    elementDTO.setBoundBoxXTopLeft(element.getBoundBox().xTopLeft);
                    elementDTO.setBoundBoxYTopLeft(element.getBoundBox().yTopLeft);
                    elementDTO.setBoundBoxXTopRight(element.getBoundBox().xTopRight);
                    elementDTO.setBoundBoxYTopRight(element.getBoundBox().yTopRight);
                    elementDTO.setBoundBoxXBottomRight(element.getBoundBox().xBottomRight);
                    elementDTO.setBoundBoxYBottomRight(element.getBoundBox().yBottomRight);
                    elementDTO.setBoundBoxXBottomLeft(element.getBoundBox().xBottomLeft);
                    elementDTO.setBoundBoxYBottomLeft(element.getBoundBox().yBottomLeft);
                    elementDTO.setBoundBoxXCoordinate(element.getBoundBox().xCoordinate);
                    elementDTO.setBoundBoxYCoordinate(element.getBoundBox().yCoordinate);
                    elementDTO.setIndexInParent(element.getIndexInParent());
                    elementDTO.setLeafCount(element.getLeafCount());
                    elementDTO.setLevelCount(element.getLevelCount());
                    elementDTO.setCollapsed(element.getIsCollapsed());
                    elementDTO.setDelta((float) element.getDelta());
                    elementDTO.setDeltaX((float) element.getDeltaX());

                    return elementDTO;
                })
                .collect(Collectors.toList());
    }

    public static List<EdgeDTO> convertEdgeElementsToEdgeDTO(List<EdgeElement> edgeList) {
        return edgeList.stream()
                .map((edge) -> {
                    EdgeDTO edgeDTO = new EdgeDTO();
                    edgeDTO.setSourceElementId(String.valueOf(edge.getSourceElement().getElementId()));
                    edgeDTO.setTargetElementId(String.valueOf(edge.getTargetElement().getElementId()));
                    edgeDTO.setStartX((float) edge.getStartX());
                    edgeDTO.setStartY((float) edge.getStartY());
                    edgeDTO.setEndX((float) edge.getEndX());
                    edgeDTO.setEndY((float) edge.getEndY());
                    edgeDTO.setCollapsed(edge.getCollpased());

                    return edgeDTO;
                })
                .collect(Collectors.toList());
    }

    public static List<Edge> convertEdgeDTOToEdges(List<EdgeDTO> edgeDTOList) {
        return edgeDTOList.stream()
                .map(edgeDTO -> {
                    return new Edge(edgeDTO.getTargetElementId(),
                            edgeDTO.getStartX(), edgeDTO.getEndX(),
                            edgeDTO.getStartY(), edgeDTO.getEndY());
                })
                .collect(Collectors.toList());
    }

}
