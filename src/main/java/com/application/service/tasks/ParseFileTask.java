package com.application.service.tasks;

import com.application.db.DAO.DAOImplementation.CallTraceDAOImpl;
import com.application.db.DAO.DAOImplementation.MethodDefDAOImpl;
import com.application.db.DatabaseUtil;
import com.application.logs.fileIntegrity.CheckFileIntegrity;
import com.application.logs.parsers.ParseCallTrace;
import com.application.logs.parsers.ParseMethodDefinition;
import com.application.presentation.CustomProgressBar;
import com.application.service.files.FileNames;
import com.application.service.files.LoadedFiles;
import com.application.service.modules.ModuleLocator;
import javafx.concurrent.Task;

import java.io.File;
import java.util.List;

public class ParseFileTask extends Task<Void> {

    private File methodDefinitionLogFile;
    private File callTraceLogFile;


    public ParseFileTask() {
        this.methodDefinitionLogFile = LoadedFiles.getFile(FileNames.METHOD_DEF.getFileName());
        this.callTraceLogFile = LoadedFiles.getFile(FileNames.Call_Trace.getFileName());
    }

    @Override
    protected Void call() {
        // Reset Database
        // updateTitle("Resetting the Database.");
        DatabaseUtil.resetDB();

        BytesRead bytesRead = new BytesRead(
                0,
                methodDefinitionLogFile.length() + 2 * callTraceLogFile.length()
        );
        // Method Definition log file integrity check.
        updateTitle("Checking integrity of Call Trace Log file.");
        updateMessage("Please wait... total Bytes: " + bytesRead.total + " bytes processed: " + bytesRead.readSoFar);
        updateProgress(bytesRead.readSoFar, bytesRead.total);

        CheckFileIntegrity.checkFile(LoadedFiles.getFile(FileNames.Call_Trace.getFileName()), bytesRead, (Void) -> {
            updateMessage("Please wait... total Bytes: " + bytesRead.total + " bytes processed: " + bytesRead.readSoFar);
            updateProgress(bytesRead.readSoFar, bytesRead.total);
        });

        // Parse Method Definition Log files.
        updateTitle("Parsing log files.");
        updateMessage("Please wait... total Bytes: " + bytesRead.total + " bytes processed: " + bytesRead.readSoFar);
        // updateProgress(bytesRead.readSoFar, bytesRead.total);

        List<List<String>> parsedMDLineList = new ParseMethodDefinition().parseFile(
                LoadedFiles.getFile(FileNames.METHOD_DEF.getFileName()),
                bytesRead,
                (Void) -> {
                    updateMessage("Please wait... total Bytes: " + bytesRead.total + " bytes processed: " + bytesRead.readSoFar);
                    updateProgress(bytesRead.readSoFar, bytesRead.total);
                });

        MethodDefDAOImpl.insertList(parsedMDLineList);

        // new ParseMethodDefinition().readFile(MethodDefinitionLogFile.getFile(), bytesRead, parsedLineList -> {
        //     MethodDefDAOImpl.insert(parsedLineList);
        //     updateMessage("Please wait... total Bytes: " + bytesRead.total + " bytes processed: " + bytesRead.readSoFar);
        //     updateProgress(bytesRead.readSoFar, bytesRead.total);
        // });

        new ParseCallTrace().readFile(
                LoadedFiles.getFile(FileNames.Call_Trace.getFileName()),
                bytesRead,
                parsedLineList -> {
                    int autoIncrementedId = CallTraceDAOImpl.insert(parsedLineList);
                    ModuleLocator.getElementTreeModule().StringToElementList(parsedLineList, autoIncrementedId);
                    updateMessage("Please wait... total Bytes: " + bytesRead.total + " bytes processed: " + bytesRead.readSoFar);
                    updateProgress(bytesRead.readSoFar, bytesRead.total);
                });

        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
    }

    public class BytesRead {
        public long readSoFar;
        long total;

        BytesRead(long readSoFar, long totalBytes) {
            this.readSoFar = readSoFar;
            this.total = totalBytes;
        }
    }
}
