package com.csgt.dataaccess.DAO;

import com.csgt.dataaccess.DTO.ElementToChildDTO;
import com.csgt.dataaccess.DatabaseUtil;
import com.csgt.dataaccess.TableNames;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

import static com.csgt.dataaccess.TableNames.ELEMENT_TO_CHILD_TABLE;

public class ElementToChildDAOImpl {
    public static void createTable() {
        String sql = "";
        if (!isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                sql = "CREATE TABLE " + ELEMENT_TO_CHILD_TABLE + " (" +
                        "parent_id INTEGER, " +
                        "child_id INTEGER" +
                        // "CONSTRAINT foreignKeyOne FOREIGN KEY(parent_id) REFERENCES " + TableNames.ELEMENT_TABLE + "(ID), " +
                        // "CONSTRAINT foreignKeyTwo FOREIGN KEY(child_id) REFERENCES " + TableNames.ELEMENT_TABLE + "(ID)" +
                        ")";
                ps.execute(sql);
                // System.out.println("** Creating table " + TableNames.ELEMENT_TO_CHILD_TABLE);
            } catch (SQLException e) {
                System.out.println("ElementToChildDAOImpl.createTable exception caused by query: " + sql);
                e.printStackTrace();
            }
        }
    }

    public static boolean isTableCreated() {
        return DatabaseUtil.isTableCreated(ELEMENT_TO_CHILD_TABLE);
    }

    public static void insert(int elementId, int childId) {
        if (!isTableCreated())
            createTable();

        try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
            String sql = "INSERT INTO " + ELEMENT_TO_CHILD_TABLE + " VALUES( " +
                    elementId + ", " +
                    childId +
                    ")";
            ps.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void insert(List<ElementToChildDTO> elementToChildDTOs) {
        if (!isTableCreated())
            createTable();

        DatabaseUtil.addAndExecuteBatch(getQueryList(elementToChildDTOs));
    }

    private static List<String> getQueryList(List<ElementToChildDTO> elementToChildDTOS) {
        return elementToChildDTOS.stream().map(elementToChildDTO -> {
            return "INSERT INTO " + ELEMENT_TO_CHILD_TABLE + " VALUES( " +
                    elementToChildDTO.getParentId() + ", " +
                    elementToChildDTO.getChildId() +
                    ")";
        }).collect(Collectors.toList());
    }

    public static void dropTable() {
        if (isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                String sql= "Drop table " + TableNames.ELEMENT_TO_CHILD_TABLE;
                System.out.println(">> Dropping table " + TableNames.ELEMENT_TO_CHILD_TABLE);
                ps.execute(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    static Connection conn;
    static Statement ps;
    static String sql;
    public static ResultSet selectWhere(String where) {
        if (isTableCreated()) {
            try  {
                conn = DatabaseUtil.getConnection();
                ps = conn.createStatement();
                sql = "SELECT * FROM " + TableNames.ELEMENT_TO_CHILD_TABLE + " WHERE " + where;
                ResultSet resultSet = ps.executeQuery(sql);
                return resultSet;
            } catch (SQLException e) {
                System.out.println("Line that threw error: " + sql);
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("Table does not exist. Hence cannot fetch any rows from it.");
    }

    public static void close() {
        try {
            ps.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
