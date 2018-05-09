package com.csgt.db.DAO.DAOInterfaces;

import java.sql.ResultSet;
import java.util.List;

/**
 * Data Access Object Interface for each log statement in the Call Trace Log file
 */
public interface TraceDAOInterface {
    public static final String TRACE_TABLE_NAME = "CALL_TRACE";

    public boolean insert(List<String> values);
    public boolean createTable();
    public ResultSet select(int numOfRows);

}
