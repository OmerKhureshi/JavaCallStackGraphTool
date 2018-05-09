package com.csgt.db.DAO.DAOInterfaces;

import com.csgt.controller.files.fileHandler.MethodDefinitionLogFile;

import java.sql.ResultSet;

/**
 * Data Access Object Interface for each log statement in the Call Trace Log file
 */
public interface DefnDAOInterface {
    String DEFINITION_TABLE_NAME = MethodDefinitionLogFile.getFileName();

    boolean insert();
    boolean createTable();
    ResultSet select(int numOfRows);

}
