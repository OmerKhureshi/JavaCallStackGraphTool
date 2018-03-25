package com.application.service.tasks;

import com.application.db.DAO.DAOImplementation.CallTraceDAOImpl;
import com.application.db.DAO.DAOImplementation.MethodDefnDAOImpl;
import com.application.db.DatabaseUtil;
import com.application.logs.fileHandler.CallTraceLogFile;
import com.application.logs.fileHandler.MethodDefinitionLogFile;
import com.application.logs.fileIntegrity.CheckFileIntegrity;
import com.application.logs.parsers.ParseCallTrace;
import com.application.service.files.LoadedFiles;
import com.application.service.modules.ModuleLocator;
import javafx.concurrent.Task;

import java.io.File;
import java.sql.SQLException;

public class ParseFileTask extends Task<Void> {

    private File methodDefinitionLogFile;
    private File callTraceLogFile;

    public ParseFileTask() {
        this.methodDefinitionLogFile = LoadedFiles.getFile("methodDefLogFile");
        this.callTraceLogFile = LoadedFiles.getFile("callTraceLogFile");
    }

    @Override
    protected Void call() {
        // Reset Database
        updateTitle("Resetting the Database.");
        DatabaseUtil.resetDB();

        BytesRead bytesRead = new BytesRead(
                0,
                methodDefinitionLogFile.length() + 2 * callTraceLogFile.length()
        );
        updateTitle("Checking call trace fileMenu for errors.");
        updateMessage("Please wait... total Bytes: " + bytesRead.total + " bytes processed: " + bytesRead.readSoFar);
        updateProgress(bytesRead.readSoFar, bytesRead.total);
        CheckFileIntegrity.checkFile(CallTraceLogFile.getFile(), bytesRead);

        // Parse Log files.
        new ParseCallTrace().readFile(MethodDefinitionLogFile.getFile(), bytesRead, MethodDefnDAOImpl::insert);
        updateTitle("Parsing log files.");
        new ParseCallTrace().readFile(CallTraceLogFile.getFile(), bytesRead,
                parsedLineList -> {
                    try {
                        int autoIncrementedId = CallTraceDAOImpl.insert(parsedLineList);
                        ModuleLocator.getElementTreeModule().StringToElementList(parsedLineList, autoIncrementedId);
                        updateMessage("Please wait... total Bytes: " + bytesRead.total + " bytes processed: " + bytesRead.readSoFar);
                        updateProgress(bytesRead.readSoFar, bytesRead.total);
                    } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {  // Todo Create a custom exception class and clean this.
                        e.printStackTrace();
                    }
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
