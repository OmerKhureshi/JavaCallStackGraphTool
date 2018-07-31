package com.csgt.dataaccess.DAO;

import com.csgt.dataaccess.DatabaseUtil;
import com.csgt.dataaccess.TableNames;
import org.apache.commons.lang.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.csgt.dataaccess.TableNames.CALL_TRACE_TABLE;

public class CallTraceDAOImpl {
    private static int currentSelectedThread;

    public static boolean isTableCreated() {
        return DatabaseUtil.isTableCreated(TableNames.CALL_TRACE_TABLE);
    }

    public static void createTable() {
        if (!isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                sql = "CREATE TABLE " + CALL_TRACE_TABLE + " (" +
                        "id INTEGER PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                        "process_id INTEGER not null, " +
                        "thread_id INTEGER, " +
                        "method_id INTEGER, " +
                        "event_type VARCHAR(20), " +
                        "parameters VARCHAR(1600), " +
                        "lockObjId VARCHAR(50), " +
                        // "time_instant VARCHAR(24)" +
                        "time_instant TIMESTAMP" +
                        // "FOREIGN KEY(method_id) REFERENCES " + TableNames.METHOD_DEFINITION_TABLE + "(ID)" +
                        ")";
                ps.execute(sql);
                // System.out.println("** Creating table " + TableNames.CALL_TRACE_TABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static int insert(List<String> val) {

        int autoIncrementedId = -1;
        String time_instant = val.get(0);
        Timestamp timestamp = new Timestamp(Long.valueOf(time_instant));
        int processID = Integer.parseInt(val.get(1));
        int threadID = Integer.parseInt(val.get(2));
        String eventType = val.get(3);
        int methodID = 0;
        String parameters = "";
        String lockObjectId = "";

        if (eventType.equalsIgnoreCase("ENTER")) {
            methodID = Integer.parseInt(val.get(4));
            String params = val.get(5);
            if (params.length() > 1600)
                parameters = StringUtils.abbreviate(parameters, 1550);
//            parameters = val.get(5);
        } else if (eventType.equalsIgnoreCase("WAIT-ENTER") || eventType.equalsIgnoreCase("WAIT-EXIT") ||
                eventType.equalsIgnoreCase("NOTIFY-ENTER") || eventType.equalsIgnoreCase("NOTIFY-EXIT") ||
                eventType.equalsIgnoreCase("NOTIFYALL-ENTER") || eventType.equalsIgnoreCase("NOTIFYALL-EXIT")) {
            lockObjectId = val.get(4);
        }

        if (!isTableCreated())
            createTable();
        String sql = null;
        try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
            sql = "INSERT INTO " + CALL_TRACE_TABLE +
                    "(" +
                    "process_id, " +
                    "thread_id, " +
                    "method_id, " +
                    "event_type, " +
                    "parameters, " +
                    "lockObjId, " +
                    "time_instant" +
                    ")" +
                    " VALUES("+
                    processID  + ", " +
                    threadID   + ", " +
                    methodID   + ", " +
                    "'" + eventType  + "', " +
                    "'" + parameters + "', " +
                    "'" + lockObjectId + "', " +
                    // "'" + time_instant  + "'" +
                    "{ts '" + timestamp + "'}" +
                    ")";

            //            System.out.println("Inserting into call trace the statement: " + sql);
            ps.execute(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                autoIncrementedId = rs.getInt(1);
            }

            // autoIncrementedId = rs.getInt("ID");
            //            System.out.println(TableNames.ELEMENT_TABLE + ": Inserted: " + sql);
        } catch (SQLException e) {
            System.out.println(" Exception caused by: " + sql);
            e.printStackTrace();
        }
        //        System.out.println("ending insert");
        return autoIncrementedId;
    }


    public static void dropTable() {
        //        System.out.println("starting dropTable");
        if (isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                String sql= "Drop table " + TableNames.CALL_TRACE_TABLE;
                ps.executeUpdate(sql);
                ps.close();
                System.out.println(">> Dropping table " + TableNames.CALL_TRACE_TABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //        System.out.println("ending dropTable");
    }

    private static Connection conn;

    static Statement ps;
    static String sql;
    public static ResultSet getWhere(String where) {
        if (isTableCreated()) try {
            conn = DatabaseUtil.getConnection();
            ps = conn.createStatement();
            sql = "SELECT * FROM " + CALL_TRACE_TABLE + " WHERE " + where;
            ResultSet resultSet = ps.executeQuery(sql);

            return resultSet;
        } catch (SQLException e) {
            System.out.println("Line that threw error: " + sql);
            e.printStackTrace();
        }
        throw new IllegalStateException("Table does not exist. Hence cannot fetch any rows from it.");
    }

    public static List<Integer> getThreadIdsWhere(String where) {
        List<Integer> threadList = new ArrayList<>();
        String query = "SELECT DISTINCT(THREAD_ID) FROM " + TableNames.CALL_TRACE_TABLE + " where " + where;
        try (ResultSet rs = DatabaseUtil.select(query)) {
            while (rs != null && rs.next()) {
                threadList.add(rs.getInt("thread_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }

        return threadList;
    }

    public static List<Integer> getDistinctThreadIds() {
        List<Integer> threadList = new ArrayList<>();
        String query = "SELECT DISTINCT(THREAD_ID) FROM " + TableNames.CALL_TRACE_TABLE + " order by THREAD_ID";

        try (ResultSet rs = DatabaseUtil.select(query)) {
            while (rs != null && rs.next()) {
                threadList.add(rs.getInt("thread_id"));
            }
        } catch (SQLException e) {
            System.out.println("CallTraceDAOImpl.getDistinctThreadIds: ");
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }

        // try (Connection conn = DatabaseUtil.getConnection()) {
        //     ResultSet rs = DatabaseUtil.executeQuery(conn, query);
        //     while (rs.next()) {
        //         threadList.add(rs.getInt("thread_id"));
        //     }
        // } catch (SQLException e) {
        //     System.out.println("CallTraceDAOImpl.getDistinctThreadIds: ");
        //     e.printStackTrace();
        // }

        return threadList;
    }

    public static int getThreadIdByMethodNameAndPackageName(String methodName, String packageName) {

        String query = "SELECT thread_id " +
                "FROM " + TableNames.CALL_TRACE_TABLE + " " +
                "JOIN " + TableNames.METHOD_DEFINITION_TABLE + " ON " + TableNames.CALL_TRACE_TABLE + ".method_id " +
                "= " +
                TableNames.METHOD_DEFINITION_TABLE + ".id " +
                "AND " + TableNames.METHOD_DEFINITION_TABLE + ".METHOD_NAME = '" + methodName + "' " +
                "AND " + TableNames.METHOD_DEFINITION_TABLE + ".PACKAGE_NAME = '" + packageName + "'";

        try (ResultSet rs = DatabaseUtil.select(query);) {
            if (rs.next()) {
                return rs.getInt("thread_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }

        return 0;
    }

    public static int getCurrentSelectedThread() {
        return currentSelectedThread;
    }

    public static void setCurrentSelectedThread(int currentSelectedThread) {
        CallTraceDAOImpl.currentSelectedThread = currentSelectedThread;
    }
}
