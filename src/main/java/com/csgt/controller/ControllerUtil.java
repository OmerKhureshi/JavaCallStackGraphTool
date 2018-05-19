package com.csgt.controller;

import com.csgt.dataaccess.DTO.EdgeDTO;
import com.csgt.dataaccess.DTO.ElementDTO;
import com.csgt.dataaccess.DTO.HighlightDTO;
import com.csgt.dataaccess.model.EdgeElement;
import com.csgt.dataaccess.model.Element;
import com.csgt.presentation.graph.NodeCell;
import com.csgt.presentation.graph.Edge;
import com.csgt.presentation.graph.HighlightCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This call handles the utility functions required by other classes in controller package.
 */
public class ControllerUtil {

    private static Stage primaryStage;

    /**
     *
     * @param elements
     * @return
     */
    public static List<ElementDTO> convertElementToElementDTO(List<Element> elements) {
        return elements.stream()
                .map((element) -> {
                    ElementDTO elementDTO = new ElementDTO();
                    elementDTO.setId(String.valueOf(element.getElementId()));
                    elementDTO.setParentId(element.getParent() == null ? -1 : element.getParent().getElementId());
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
                    elementDTO.setDeltaY((float) element.getDelta());
                    elementDTO.setDeltaX((float) element.getDeltaX());

                    return elementDTO;
                })
                .collect(Collectors.toList());
    }

    public static List<NodeCell> convertElementDTOTOCell(List<ElementDTO> elementDTOList) {
        List<NodeCell> nodeCellList = new ArrayList<>();

        elementDTOList.forEach(elementDTO -> {
            NodeCell nodeCell = new NodeCell(elementDTO.getId(), elementDTO.getBoundBoxXCoordinate(), elementDTO.getBoundBoxYCoordinate(), elementDTO.getMethodName(), elementDTO.getCollapsed());
            nodeCellList.add(nodeCell);
        });

        return nodeCellList;
    }

    public static List<HighlightCell> convertHighlightDTOsToHighlights(List<HighlightDTO> highlightDTOs) {
        List<HighlightCell> highlightCells = new ArrayList<>();

        highlightDTOs.forEach(highlightDTO -> {
            HighlightCell rect = new HighlightCell(highlightDTO.getId(),
                    highlightDTO.getElementId(),
                    highlightDTO.getStartX(), highlightDTO.getStartY(),
                    highlightDTO.getWidth(), highlightDTO.getHeight());

            rect.setColor(highlightDTO.getColor());
            rect.setArcHeight(20);
            rect.setArcWidth(20);
            highlightCells.add(rect);
        });

        return highlightCells;
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
                    edgeDTO.setCollapsed(edge.getCollapsed());

                    return edgeDTO;
                })
                .collect(Collectors.toList());
    }


    public static List<Edge> convertEdgeDTOToEdges(List<EdgeDTO> edgeDTOList) {
        return edgeDTOList.stream()
                .map(edgeDTO -> new Edge(edgeDTO.getTargetElementId(),
                        edgeDTO.getStartX(), edgeDTO.getEndX(),
                        edgeDTO.getStartY(), edgeDTO.getEndY()))
                .collect(Collectors.toList());
    }


    static File fileChooser(String title, String desc, String extensions) throws Exception {
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


    static File directoryChooser(String title) throws Exception {
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
}