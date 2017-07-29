package com.application.logs.fileHandler;

import com.application.db.DAOImplementation.FilesDAOImpl;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CallTraceLogFile {

    private static String fileName = "";
    public static final String FILE_TYPE = "CALL_TRACE";

    private static File file = new File(Thread.currentThread().getContextClassLoader().getResource(fileName).getFile());

    CallTraceLogFile() {
        // http://stackoverflow.com/a/21722773/3690248
        file = new File(Thread.currentThread().getContextClassLoader().getResource(fileName).getFile());
    }

    public static File getFile() {
        return file;
    }

    public static void setFile(File newFile) {
        file = newFile;

        ResultSet rs = FilesDAOImpl.selectWhere("FILE_TYPE = '" + FILE_TYPE + "'");
        try {
            if (!rs.next()) {
                FilesDAOImpl.insert(FILE_TYPE, file.getPath());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getFileName() {
        return fileName;
    }

    public static void setFileName(String newFileName) {
        fileName = newFileName;
        file = new File(Thread.currentThread().getContextClassLoader().getResource(fileName).getFile());
    }
}
