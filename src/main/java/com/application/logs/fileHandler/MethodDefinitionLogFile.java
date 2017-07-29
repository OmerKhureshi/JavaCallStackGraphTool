package com.application.logs.fileHandler;

import com.application.db.DAOImplementation.FilesDAOImpl;
import com.application.db.DatabaseUtil;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MethodDefinitionLogFile {
    private static String fileName = "";
    public static final String FILE_TYPE = "METHODDEFN";

    MethodDefinitionLogFile() {
        // Get the resources
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

    private static File file = new File(Thread.currentThread().getContextClassLoader().getResource(fileName).getFile());

    public static String getFileName() {
        return fileName;
    }

    public static void setFileName(String newFileName) {
        fileName = newFileName;
        file = new File(Thread.currentThread().getContextClassLoader().getResource(fileName).getFile());
    }
}
