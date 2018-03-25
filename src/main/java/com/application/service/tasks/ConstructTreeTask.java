package com.application.service.tasks;

import com.application.db.DAO.DAOImplementation.CallTraceDAOImpl;
import com.application.db.DAO.DAOImplementation.ElementDAOImpl;
import com.application.db.DAO.DAOImplementation.ElementToChildDAOImpl;
import com.application.db.DAO.DAOImplementation.MethodDefnDAOImpl;
import com.application.db.DatabaseUtil;
import com.application.fxgraph.ElementHelpers.Element;
import com.application.logs.fileHandler.CallTraceLogFile;
import com.application.logs.fileHandler.MethodDefinitionLogFile;
import com.application.logs.fileIntegrity.CheckFileIntegrity;
import com.application.logs.parsers.ParseCallTrace;
import com.application.service.files.LoadedFiles;
import com.application.service.modules.ElementTreeModule;
import com.application.service.modules.ModuleLocator;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class ConstructTreeTask extends Task<Void> {

    private File methodDefinitionLogFile;
    private File callTraceLogFile;

    Consumer<Void> onSuccess;

    public ConstructTreeTask(Consumer onSuccess) {
        this.methodDefinitionLogFile = LoadedFiles.getFile("methodDefLogFile");
        this.callTraceLogFile = LoadedFiles.getFile("callTraceLogFile");

        this.onSuccess = onSuccess;
    }

    @Override
    protected Void call() throws Exception {
        // Inserting log files into Database.
        LinesInserted linesInserted = new LinesInserted(
                0,
                2 * ParseCallTrace.countNumberOfLines(CallTraceLogFile.getFile())
        );

        updateTitle("Writing to DB.");
        updateMessage("Please wait... total records: " + linesInserted.total + " records processed: " + linesInserted.insertedSoFar);
        updateProgress(linesInserted.insertedSoFar, linesInserted.total);
        ModuleLocator.getElementTreeModule().calculateElementProperties();

        Element root = ElementTreeModule.greatGrandParent;
        if (root == null)
            return null;

        Queue<Element> queue = new LinkedList<>();
        queue.add(root);

        Element element;
        while ((element = queue.poll()) != null) {
            ElementDAOImpl.insert(element);
            ElementToChildDAOImpl.insert(
                    element.getParent() == null ? -1 : element.getParent().getElementId(),
                    element.getElementId());

            if (element.getChildren() != null) {
                element.getChildren().forEach(queue::add);
            }

            linesInserted.insertedSoFar++;
            updateMessage("Please wait... total records: " + linesInserted.total + " records processed: " + linesInserted.insertedSoFar);
            updateProgress(linesInserted.insertedSoFar, linesInserted.total);
        }

        // Insert lines and properties into database.
        ModuleLocator.getElementTreeModule().recursivelyInsertEdgeElementsIntoDB(ElementTreeModule.greatGrandParent);

        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
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
