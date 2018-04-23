package com.application.db.DAO.DAOImplementation;

import com.application.db.DTO.ElementDTO;
import com.application.db.DTO.MethodDefDTO;
import com.application.db.DatabaseUtil;
import com.application.db.TableNames;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.application.db.TableNames.METHOD_DEFINITION_TABLE;

public class MethodDefDAOImpl {

    public static boolean isTableCreated() {
        return DatabaseUtil.isTableCreated(METHOD_DEFINITION_TABLE);
    }

    public static void createTable() {
        if (!isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                sql = "CREATE TABLE " + METHOD_DEFINITION_TABLE + " (" +
                        "id INTEGER NOT NULL PRIMARY KEY , " +
                        "package_name VARCHAR(200), " +
                        "method_name VARCHAR(50), " +
                        "parameter_types VARCHAR(200)" +
                        ")";
                ps.execute(sql);
                System.out.println("** Creating table " + TableNames.METHOD_DEFINITION_TABLE);
            } catch (SQLException e) {
                System.err.println("MethodDefDAOImpl::createTable: SQL Exception on create table");
                e.printStackTrace();
            }
        }
    }

    public static void insert(List<String> vals) {
        if (!isTableCreated())
            createTable();

        int methodID = Integer.parseInt(vals.get(0));
        String packageName = vals.get(1);
        String methodName = vals.get(2);
        String arguments = vals.get(3);
        String sql = null;
        try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
            sql = "INSERT INTO " + METHOD_DEFINITION_TABLE + " VALUES(\n"+
                    methodID + ", '" +
                    packageName + "', '" +
                    methodName + "', '" +
                    arguments + "'" +
                    ")";
            ps.execute(sql);
        } catch (SQLException e) {
            System.out.println(" Exception caused by: " + sql);
            e.printStackTrace();
        }
    }

    public static void dropTable() {
        //        System.out.println("starting dropTable");
        if (isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                String sql= "Drop table " + METHOD_DEFINITION_TABLE;
                System.out.println(">> Dropping table " + TableNames.METHOD_DEFINITION_TABLE);

                ps.execute(sql);
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //        System.out.println("ending dropTable");
    }


    public static List<String> getMethodPackageString() {
        List<String> methodDefDTOs = new ArrayList<>();

        String sql = "SELECT * FROM " + TableNames.METHOD_DEFINITION_TABLE;

        try (ResultSet rs = DatabaseUtil.select(sql)) {
            while (rs.next()) {
                MethodDefDTO methodDefDTO = processMethodDefDTO(rs);
                methodDefDTOs.add(methodDefDTO.getPackageName() + "." + methodDefDTO.getMethodName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }

        return methodDefDTOs;
    }

    public static List<MethodDefDTO> getMethodDefDTOS() {
        List<MethodDefDTO> methodDefDTOs = new ArrayList<>();

        String sql = "SELECT * FROM " + TableNames.METHOD_DEFINITION_TABLE;

        try (ResultSet rs = DatabaseUtil.select(sql)) {
            while (rs.next()) {
                methodDefDTOs.add(processMethodDefDTO(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close();
        }

        return methodDefDTOs;
    }

    private static MethodDefDTO processMethodDefDTO(ResultSet rs) {
        MethodDefDTO methodDefDTO = new MethodDefDTO();

        try {
            methodDefDTO.setId(String.valueOf(rs.getInt("ID")));
            methodDefDTO.setMethodName(rs.getString("method_name"));
            methodDefDTO.setPackageName(rs.getString("package_name"));
            methodDefDTO.setParameterTypes(rs.getString("parameter_types"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return methodDefDTO;
    }


    static Connection conn;
    static Statement ps;
    static String sql;
    public static ResultSet selectWhere(String where) {
        if (isTableCreated()) try {
            conn = DatabaseUtil.getConnection();
            ps = conn.createStatement();
            sql = "SELECT * FROM " + METHOD_DEFINITION_TABLE + " WHERE " + where;
            ResultSet resultSet = ps.executeQuery(sql);
            //                resultSet.next();
            //                System.out.println(resultSet.getInt("id"));
            return resultSet;
        } catch (SQLException e) {
            System.out.println("Line that threw error: " + sql);
            e.printStackTrace();
        }
        throw new IllegalStateException("Table does not exist. Hence cannot fetch any rows from it.");
    }

    public static void insertList(List<List<String>> parsedLineList) {

        try (Statement statement = DatabaseUtil.getConnection().createStatement()) {
            parsedLineList.forEach(parsedLine -> {
                try {
                    statement.addBatch(getInsertSQL(parsedLine));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static String getInsertSQL(List<String> vals) {
        int methodID = Integer.parseInt(vals.get(0));
        String packageName = vals.get(1);
        String methodName = vals.get(2);
        String arguments = vals.get(3);
        return "INSERT INTO " + METHOD_DEFINITION_TABLE + " VALUES(\n"+
                methodID + ", '" +
                packageName + "', '" +
                methodName + "', '" +
                arguments + "'" +
                ")";
    }

}
