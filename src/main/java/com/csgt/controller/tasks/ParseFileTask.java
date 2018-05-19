package com.csgt.controller.tasks;

import com.csgt.dataaccess.DAO.CallTraceDAOImpl;
import com.csgt.dataaccess.DAO.MethodDefDAOImpl;
import com.csgt.dataaccess.DatabaseUtil;
import com.csgt.controller.files.FileUtil;
import com.csgt.controller.files.parsers.ParseCallTrace;
import com.csgt.controller.files.parsers.ParseMethodDefinition;
import com.csgt.controller.files.FileNames;
import com.csgt.controller.files.LoadedFiles;
import com.csgt.controller.modules.ModuleLocator;
import javafx.concurrent.Task;

import java.io.File;
import java.util.List;

/**
 * This task handles parsing both the log files.
 */
public class ParseFileTask extends Task<Void> {

    private File methodDefinitionLogFile;
    private File callTraceLogFile;


    public  ParseFileTask() {
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

        FileUtil.checkFile(LoadedFiles.getFile(FileNames.Call_Trace.getFileName()), bytesRead, (Void) -> {
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
