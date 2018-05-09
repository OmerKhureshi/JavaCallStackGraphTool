package com.csgt.db.DAO.DAOImplementation;

import com.csgt.db.DAO.DAOInterfaces.TraceDAOInterface;
import com.csgt.db.DatabaseUtil;
import com.csgt.db.TableNames;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the TraceDAOInterface.
 */
public class TraceDAOImpl implements TraceDAOInterface{

    public boolean isTableCreated = false;
    Connection conn = null;

    @Override
    public boolean insert(List<String> vals) {
        Connection conn = DatabaseUtil.getConnection();
        Statement ps = null;
        String sql;

        int processID = Integer.parseInt(vals.get(0));
        int threadID = Integer.parseInt(vals.get(1));
        int methodID = Integer.parseInt(vals.get(2));
        String eventType = vals.get(0);
        String parameters = vals.get(0);

        if (!isTableCreated) {
            createTable();
        }

        try {
            ps = conn.createStatement();

            sql = "INSERT INTO " + TableNames.CALL_TRACE_TABLE + " VALUES(\n"+
                    processID +","+
                    threadID +","+
                    methodID +",'"+
                    eventType +"','"+
                    parameters +
                    "')";

            ps.executeUpdate(sql);
            ps.close();
            ps = null;
            conn.close();
            conn = null;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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

        return true;
    }

    @Override
    public boolean createTable() {
        Statement ps = null;
        Connection cn = null;
        cn = DatabaseUtil.getConnection();
        String sql;
        ResultSet rs;

        if (isTableCreated) return true;

        try {
            ps = cn.createStatement();

            sql = "CREATE TABLE " + TableNames.CALL_TRACE_TABLE + " (\n" +
                    "   \"processID\" INTEGER not null primary key,\n" +
                    "    \"threadID\" INTEGER,\n" +
                    "    \"methodID\" INTEGER,\n" +
                    "    \"message\" VARCHAR(50),\n" +
                    "    \"parameter\" VARCHAR(20)\n" +
                        /*"   FOREIGN KEY(\"methodID\") REFERENCES METHOD(\"methdID\")"+ */
                    ")";

            ps.executeUpdate(sql);
            ps.close();
            ps = null;
            cn.close();
            cn = null;
            isTableCreated = true;
        } catch (SQLException e) {
            String sqlError = e.getSQLState();
            if (sqlError.equals("X0Y32")) {
                System.out.println("TableNames already exists.");
                isTableCreated = true;
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

    @Override
    public ResultSet select(int numOfRows) {
        Connection c = DatabaseUtil.getConnection();
        Statement ps = null;
        ResultSet rs = null;
        ArrayList<String> process = new ArrayList<>();
        String query;

        try{
            ps = c.createStatement();
            ps.setMaxRows(numOfRows);
            query = "SELECT * FROM " + TableNames.CALL_TRACE_TABLE;
            rs = ps.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        finally{
//            if (ps != null){
//                try { ps.close();} catch (SQLException e){;}
//                ps = null;
//            }
//            if (c != null){
//                try {c.close();} catch(SQLException e) {;}
//                c = null;
//            }
//        }

        return rs;
    }
}
