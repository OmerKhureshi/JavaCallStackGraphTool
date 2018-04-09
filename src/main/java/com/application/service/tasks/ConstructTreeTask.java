package com.application.service.tasks;

import com.application.controller.ControllerUtil;
import com.application.db.DAO.DAOImplementation.EdgeDAOImpl;
import com.application.db.DAO.DAOImplementation.ElementDAOImpl;
import com.application.db.DAO.DAOImplementation.ElementToChildDAOImpl;
import com.application.db.DTO.EdgeDTO;
import com.application.db.DTO.ElementDTO;
import com.application.db.DTO.ElementToChildDTO;
import com.application.fxgraph.ElementHelpers.EdgeElement;
import com.application.fxgraph.ElementHelpers.Element;
import com.application.logs.parsers.ParseCallTrace;
import com.application.service.files.FileNames;
import com.application.service.files.LoadedFiles;
import com.application.service.modules.ElementTreeModule;
import com.application.service.modules.ModuleLocator;
import javafx.concurrent.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public class ConstructTreeTask extends Task<Void> {

    private File callTraceLogFile;
    ElementTreeModule elementTreeModule;

    Consumer<Void> onSuccess;

    public ConstructTreeTask() {
        this.callTraceLogFile = LoadedFiles.getFile(FileNames.Call_Trace.getFileName());
        elementTreeModule = ModuleLocator.getElementTreeModule();
    }

    @Override
    protected Void call() throws Exception {
        computeAndInsertElements();
        computeAndInsertEdges();

        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
    }

    private void computeAndInsertEdges() {
        System.out.println("ConstructTreeTask.computeAndInsertEdges");
        List<EdgeElement> edgeElementList = new ArrayList<>();
        ModuleLocator.getElementTreeModule().recursivelyInsertEdgeElementsIntoDB(
                ElementTreeModule.greatGrandParent,
                edgeElementList
        );

        List<EdgeDTO> edgeDTOList = ControllerUtil.convertEdgeElementsToEdgeDTO(edgeElementList);
        EdgeDAOImpl.insert(edgeDTOList);
    }

    private void computeAndInsertElements() {
        LinesInserted linesInserted = new LinesInserted(
                0,
                2 * ParseCallTrace.countNumberOfLines(callTraceLogFile)
        );

        updateTitle("Writing to DB.");
        updateMessage("Please wait... total records: " + linesInserted.total + " records processed: " + linesInserted.insertedSoFar);
        updateProgress(linesInserted.insertedSoFar, linesInserted.total);

        elementTreeModule.calculateElementProperties();
        Element root = ElementTreeModule.greatGrandParent;

        if (root == null)
            return;

        Queue<Element> queue = new LinkedList<>();
        queue.add(root);

        List<Element> elementList = new ArrayList<>();
        List<ElementToChildDTO> elementToChildDTOList = new ArrayList<>();

        Element element;
        while ((element = queue.poll()) != null) {
            elementList.add(element);

            ElementToChildDTO elementToChildDTO = new ElementToChildDTO();
            elementToChildDTO.setParentId(String.valueOf(element.getParent() == null ? -1 : element.getParent().getElementId()));
            elementToChildDTO.setChildId(String.valueOf(element.getElementId()));

            elementToChildDTOList.add(elementToChildDTO);

            // ElementDAOImpl.insert(element);
            // ElementToChildDAOImpl.insert(
            //         element.getParent() == null ? -1 : element.getParent().getElementId(),
            //         element.getElementId());

            if (element.getChildren() != null) {
                element.getChildren().forEach(queue::add);
            }

            linesInserted.insertedSoFar++;
            updateMessage("Please wait... total records: " + linesInserted.total + " records processed: " + linesInserted.insertedSoFar);
            updateProgress(linesInserted.insertedSoFar, linesInserted.total);
        }

        List<ElementDTO> elementDTOList = ControllerUtil.convertElementToElementDTO(elementList);
        ElementDAOImpl.insert(elementDTOList);
        ElementToChildDAOImpl.insert(elementToChildDTOList);
    }


        public class LinesInserted {
            long insertedSoFar = 0;
            long total = 0;

            LinesInserted(long insertedSoFar, long totalBytes) {
                this.insertedSoFar = insertedSoFar;
                this.total = totalBytes;
            }
        }
    }
