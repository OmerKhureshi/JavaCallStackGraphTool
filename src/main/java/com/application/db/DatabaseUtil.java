package com.application.db;

import com.application.db.DAO.DAOImplementation.*;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.application.db.DAO.DAOImplementation.BookmarksDAOImpl.createTable;

public class DatabaseUtil {

    private static File dataSourceDir;
    private static final String METHOD_DEFINITION_TABLE = "Method_Defn";
    private static final String CALL_TRACE_TABLE = "Call_Trace";

    private static boolean methodDefnTableCreated = false;
    private static boolean callTraceTableCreated = false;

    private static String prefix = "Databases" + File.separator + "DB_";

      //Todo  Use apache connection pool to get database connection instance link: http://stackoverflow.com/a/6507820/3690248

    public static boolean isTableCreated(String tableName) {
        try(Connection c = DatabaseUtil.getConnection()) {
            return c.getMetaData().getTables(null, null, tableName, null).next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void closeResultSet(ResultSet resultSet) {
        try {
            resultSet.close();
        } catch (SQLException e) {
            System.out.println("DatabaseUtil::close: error code: " + e.getErrorCode());
        }
    }

    private static Connection createDatabaseConnection() {
        // System.out.println("DatabaseUtil.createDatabaseConnection: method started.");
        String driver = "org.apache.derby.jdbc.EmbeddedDriver";
        Connection conn = null;
        try {
            Class.forName(driver).newInstance();
            String url;
            if (dataSourceDir == null) {
                // this is a fresh start, create a new DB.
                java.util.Date date = new java.util.Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmsss");
                String sDate = dateFormat.format(date);

                // File file = new File(prefix + sDate);
                // if (file.exists()) {
                //
                // }

                url = "jdbc:derby:" + prefix + sDate + ";create=true";
                // url = "jdbc:derby:DB;create=true";
                dataSourceDir = new File(prefix + sDate);
                // System.out.println("DatabaseUtil.createDatabaseConnection dataSourceDir == null: new url: " + url);
            } else {
                url = "jdbc:derby:" + dataSourceDir.getPath() + ";create=true";
            }
            conn = DriverManager.getConnection(url);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        // System.out.println("DatabaseUtil.createDatabaseConnection: method ended");
        return conn;
    }

    public static void shutdownDatabase() {

        String driver = "org.apache.derby.jdbc.EmbeddedDriver";
        Connection c = null;
        try {
            Class.forName(driver).newInstance();
            String url = "jdbc:derby:;shutdown=true";
            c = DriverManager.getConnection(url);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            String sqlError = e.getSQLState();
            if (sqlError.equals("XJ015")) {
                System.out.println("Derby database shutdown successful.");
            }
        }
    }

    public static void setDBDir(File dir) {
        dataSourceDir = dir;
    }

    public static Connection getConnection() {
        return createDatabaseConnection();
    }

    public static boolean createCallTrace()
            throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        Statement ps = null;
        Connection cn = getConnection();
        String sql;
        ResultSet rs;

        if (callTraceTableCreated) return true;

        try {
            ps = cn.createStatement();

            sql = "CREATE TABLE " + CALL_TRACE_TABLE + " (\n" +
                    "   \"processID\" INTEGER not null,\n" +
                    "    \"threadID\" INTEGER,\n" +
                    "    \"methodID\" INTEGER,\n" +
                    "    \"message\" VARCHAR(20),\n" +
                    "    \"parameter\" VARCHAR(200)\n" +
                        /*"   FOREIGN KEY(\"methodID\") REFERENCES METHOD(\"methdID\")"+ */
                    ")";

            System.out.println("Created call trace table now. ");
            ps.executeUpdate(sql);
            ps.close();
            ps = null;
            cn.close();
            cn = null;
            callTraceTableCreated = true;
        } catch (SQLException e) {
            String sqlError = e.getSQLState();
            if (sqlError.equals("X0Y32")) {
                System.out.println(TableNames.CALL_TRACE_TABLE + " table already exists.");
                callTraceTableCreated = true;
                return true;
            } else {
                e.printStackTrace();
                System.out.println("Other error.");
                return false;
            }
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    ;
                }
                ps = null;
            }
            if (cn != null) {
                try {
                    cn.close();
                } catch (SQLException e) {
                    ;
                }
                cn = null;
            }
        }

        return  true;
    }

    public static boolean createMethodDefn()
            throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException{

        Statement ps = null;
        Connection cn = getConnection();
        String sql;
        ResultSet rs;

        if (methodDefnTableCreated) return true;

        try {
            ps = cn.createStatement();

            sql = "CREATE TABLE " + METHOD_DEFINITION_TABLE + " (\n" +
                    "   \"methodID\" INTEGER not null primary key,\n" +
                    "    \"packageName\" VARCHAR(200),\n" +
                    "    \"methodName\" VARCHAR(50),\n" +
                    "    \"parameters\" VARCHAR(200)\n" +
                        /*"   FOREIGN KEY(\"methodID\") REFERENCES METHOD(\"methdID\")"+ */
                    ")";

            ps.executeUpdate(sql);
            ps.close();
            ps = null;
            cn.close();
            cn = null;
            System.out.println("Method defn table created.");
            methodDefnTableCreated = true;
        } catch (SQLException e) {
            String sqlError = e.getSQLState();
            if (sqlError.equals("X0Y32")) {
                methodDefnTableCreated = true;
                System.out.println(TableNames.METHOD_DEFINITION_TABLE + " table already exists.");
                return true;
            } else {
                e.printStackTrace();
                return false;
            }
        }
        finally{
            methodDefnTableCreated = true;
            if (ps != null){
                try { ps.close();} catch (SQLException e){;}
                ps = null;
            }
            if (cn != null){
                try {cn.close();} catch(SQLException e) {;}
                cn = null;
            }
        }
        return true;
    }

    public static void insertCTStmt(List<String> vals)
            throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException{

        Connection conn = getConnection();
        Statement ps = null;
        String sql;
//       394 | 0 | 1 | Enter|[2131427413]|2017-03-01 21:34:55.529

        int processID = Integer.parseInt(vals.get(0));
        int threadID = Integer.parseInt(vals.get(1));
        int methodID = Integer.parseInt(vals.get(2));
        String eventType = vals.get(3);
        String parameters = vals.get(4);
//        String timeStamp = vals.get(5);

        if (!callTraceTableCreated) {
            // createCallTrace();
        }

        try {
            ps = conn.createStatement();

//            System.out.println("value of callTraceTableCreated: " + callTraceTableCreated);
            sql = "INSERT INTO " + CALL_TRACE_TABLE + " VALUES(\n"+
                    processID +","+
                    threadID +","+
                    methodID +",'"+
                    eventType +"','"+
                    parameters +
                    "')";

//            System.out.println("Inserting into call trace the statement: " + sql);
            ps.executeUpdate(sql);
            ps.close();
            ps = null;
            conn.close();
            conn = null;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally{
            if (ps != null){
                try { ps.close();} catch (SQLException e){;}
                ps = null;
            }
            if (conn != null){
                try {conn.close();} catch(SQLException e) {;}
                conn = null;
            }
        }
    }

    public static void insertMDStmt(List<String> vals)
            throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException{

        Connection conn = getConnection();
        Statement ps = null;
        String sql = null;

        int methodID = Integer.parseInt(vals.get(0));
        String packageName = vals.get(1);
        String methodName = vals.get(2);
        String arguments = vals.get(3);

        if (!methodDefnTableCreated) {
            System.out.println(TableNames.METHOD_DEFINITION_TABLE + " table is not created.");
            // createMethodDefn();
        }

        try {
            ps = conn.createStatement();

            sql = "INSERT INTO " + METHOD_DEFINITION_TABLE + " VALUES(\n"+
                    methodID + ", '" +
                    packageName + "', '" +
                    methodName + "', '" +
                    arguments + "'" +
                    ")";

            ps.executeUpdate(sql);
            ps.close();
            ps = null;
            conn.close();
            conn = null;

//            System.out.println("Inserted into method defn. ");
        } catch (SQLException e) {
            System.out.println(">>>error causing sql: "+sql);
            e.printStackTrace();
        }
        finally{
            if (ps != null){
                try { ps.close();} catch (SQLException e){;}
                ps = null;
            }
            if (conn != null){
                try {conn.close();} catch(SQLException e) {;}
                conn = null;
            }
        }

    }


    private static Connection conn;
    private static Statement ps;
    public static ResultSet select(String query ) {
        try {
            conn = getConnection();
            ps = conn.createStatement();
            return ps.executeQuery(query);
        } catch (SQLException e) {
            System.out.println("Exception caused by: " + query);
            e.printStackTrace();
        }
        throw new IllegalStateException("Table does not exist. Hence cannot fetch any rows from it.");
    }

    public static void close() {
        try {
            if (conn != null) {
                conn.close();
            }

            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int executeSelectForInt(String query) {
        int res = 0;
        try(ResultSet rs = DatabaseUtil.select(query)) {
            if (rs.next()) {
                res = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }
        // System.out.println("DatabaseUtil.executeSelectForInt: executing query: " + query);
        return res;
    }

    public static void executeUpdate(String query ) {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
            return;
        } catch (SQLException e) {
            System.out.println("Exception caused by: " + query);
            e.printStackTrace();
        }
        throw new IllegalStateException("Table does not exist. Hence cannot fetch any rows from it.");
    }

    public static void dropCallTrace() {
        if (true) {
            try {
                Connection conn = DatabaseUtil.getConnection();
                Statement statement = conn.createStatement();
                String sql= "Drop table " + TableNames.CALL_TRACE_TABLE;
                System.out.println("Dropping call trace table.");
                statement.execute(sql);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else System.out.println(TableNames.CALL_TRACE_TABLE + " table does not exist. in dropCallTrace");

    }

    public static void dropMethodDefn() {
        if (true) {
            try {
                Connection conn = DatabaseUtil.getConnection();
                Statement statement = conn.createStatement();
                String sql= "Drop table " + TableNames.METHOD_DEFINITION_TABLE;
                statement.execute(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else System.out.println(TableNames.METHOD_DEFINITION_TABLE + " table does not exist.");

    }

    public static void resetDB() {
        System.out.println("DatabaseUtil.resetDB");
        shutdownDatabase();
        CallTraceDAOImpl.dropTable();
        MethodDefnDAOImpl.dropTable();
        ElementDAOImpl.dropTable();
        ElementToChildDAOImpl.dropTable();
        EdgeDAOImpl.dropTable();
        HighlightDAOImpl.dropTable();
        FilesDAOImpl.dropTable();
        BookmarksDAOImpl.dropTable();

        CallTraceDAOImpl.createTable();
        MethodDefnDAOImpl.createTable();
        // FilesDAOImpl.createTable();
    }


    public static Statement createStatement() {
        Statement statement = null;
        try {
            statement = DatabaseUtil.getConnection().createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return statement;
    }

    public static void saveDBPath() {
        if (dataSourceDir == null) {
            createDatabaseConnection();  // to set the current dataSourceDir
        }

        FilesDAOImpl.insert("DB", dataSourceDir.getPath());
    }

    public static void setDbPath() {
        ResultSet rs = FilesDAOImpl.selectWhere("FILE_TYPE = 'DB'");
        try {
            if (rs.next()) {
                String sPath = rs.getString("FILE_PATH");
                dataSourceDir = new File(sPath);
                System.out.println("DatabaseUtil.setDbPath: new path: " + dataSourceDir);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static ResultSet executeQuery(Connection conn, String query) throws SQLException {
        Statement statement = conn.createStatement();
        return statement.executeQuery(query);
    }

    public static void addAndExecuteBatch(List<String> queryList) {
        try(Statement statement = DatabaseUtil.createStatement()) {
            queryList.forEach(query -> {
                try {
                    statement.addBatch(query);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void executeQueryList(List<String> queryList) {
        try (Statement statement = getConnection().createStatement()) {
            for (String query : queryList) {
                statement.addBatch(query);
            }

            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}


