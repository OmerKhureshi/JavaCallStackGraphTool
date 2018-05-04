package com.application.db.DAO.DAOImplementation;

import com.application.db.DTO.BookmarkDTO;
import com.application.db.DatabaseUtil;
import com.application.db.TableNames;
import com.application.db.model.Bookmark;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class BookmarksDAOImpl {

    public static boolean isTableCreated() {
        return DatabaseUtil.isTableCreated(TableNames.BOOKMARKS);
    }

    public static void createTable() {
        if (!isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                String sql = "CREATE TABLE " + TableNames.BOOKMARKS + " (" +
                        "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                        "element_id INT, " +
                        "color VARCHAR(10), " +
                        "collapsed INT" +
                        ")";
                ps.execute(sql);
                System.out.println("** Creating table " + TableNames.BOOKMARKS);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void dropTable() {
        if (isTableCreated()) {
            try (Connection c = DatabaseUtil.getConnection(); Statement ps = c.createStatement()) {
                String sql = "DROP TABLE " + TableNames.BOOKMARKS;
                System.out.println(">> Dropping table " + TableNames.BOOKMARKS);
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
        if (!isTableCreated())
            createTable();

        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.createStatement();
            sql = "SELECT * FROM " + TableNames.BOOKMARKS + " WHERE " + where;
            ResultSet resultSet = ps.executeQuery(sql);
            return resultSet;
        } catch (SQLException e) {
            System.out.println("Line that threw error: " + sql);
            e.printStackTrace();
        }
        throw new IllegalStateException("No results for the select query. " + sql);
    }

    public static void updateWhere(String columnName, String columnValue, String where) {
        if (isTableCreated()) {
            try {
                conn = DatabaseUtil.getConnection();
                ps = conn.createStatement();
                sql = "UPDATE " + TableNames.BOOKMARKS +
                        " SET " + columnName + " = " + columnValue +
                        " WHERE " + where;
                ps.executeUpdate(sql);
                return;
            } catch (SQLException e) {
                System.out.println("Line that threw error: " + sql);
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("Table does not exist. Hence cannot fetch any rows from it.");

    }

    public static Map<String, BookmarkDTO> getBookmarkDTOs() {
        if (!isTableCreated())
            createTable();

        if (!ElementDAOImpl.isTableCreated()) {
            ElementDAOImpl.createTable();
        }
        if (!CallTraceDAOImpl.isTableCreated()) {
            CallTraceDAOImpl.createTable();
        }

        Map<String, BookmarkDTO> result = new HashMap<>();
        String query = "SELECT E.ID as EID, CT.THREAD_ID, CT.MESSAGE, B.COLOR, E.BOUND_BOX_X_COORDINATE, E.BOUND_BOX_Y_COORDINATE, E.COLLAPSED " +
                "FROM " + TableNames.BOOKMARKS + " AS B " +
                "JOIN " + TableNames.ELEMENT_TABLE + " AS E ON B.ELEMENT_ID = E.ID " +
                "JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT ON E.ID_ENTER_CALL_TRACE = CT.ID ";

        try (ResultSet rs = DatabaseUtil.select(query)) {
            while (rs.next()) {
                result.put(rs.getString("EID"),
                        new BookmarkDTO(
                                rs.getString("EID"),
                                rs.getString("THREAD_ID"),
                                rs.getString("MESSAGE"),
                                rs.getString("COLOR"),
                                rs.getDouble("BOUND_BOX_X_COORDINATE"),
                                rs.getDouble("BOUND_BOX_Y_COORDINATE"),
                                rs.getInt("COLLAPSED")
                        ));
            }
        } catch (SQLException e) {
            System.out.println("Query that threw exception: " + query);
            e.printStackTrace();
        }
        return result;
    }


    public static Map<String, Bookmark> getBookmarks() {
        if (!isTableCreated())
            createTable();

        Map<String, Bookmark> result = new HashMap<>();
        String query = "SELECT E.ID as EID, CT.THREAD_ID, CT.MESSAGE, B.COLOR, E.BOUND_BOX_X_COORDINATE, E.BOUND_BOX_Y_COORDINATE, E.COLLAPSED " +
                "FROM " + TableNames.BOOKMARKS + " AS B " +
                "JOIN " + TableNames.ELEMENT_TABLE + " AS E ON B.ELEMENT_ID = E.ID " +
                "JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT ON E.ID_ENTER_CALL_TRACE = CT.ID ";

        try (ResultSet rs = DatabaseUtil.select(query)) {
            while (rs.next()) {
                result.put(rs.getString("EID"),
                        new Bookmark(
                                rs.getString("EID"),
                                rs.getString("THREAD_ID"),
                                rs.getString("MESSAGE"),
                                rs.getString("COLOR"),
                                rs.getDouble("BOUND_BOX_X_COORDINATE"),
                                rs.getDouble("BOUND_BOX_Y_COORDINATE"),
                                rs.getInt("COLLAPSED")
                        ));
            }
        } catch (SQLException e) {
            System.out.println("Query that threw exception: " + query);
            e.printStackTrace();
        }
        return result;
    }

    public static void insertBookmark(Bookmark bookmark) {
        if (!isTableCreated())
            createTable();

        String query = "INSERT INTO " + TableNames.BOOKMARKS + " " +
                "(ELEMENT_ID, COLOR, COLLAPSED) " +
                "VALUES (" +
                bookmark.getElementId() + ", '" +
                bookmark.getColor() + "', " +
                "0)";

        DatabaseUtil.executeUpdate(query);

    }

    public static void insertBookmark(BookmarkDTO bookmarkDTO) {
        if (!isTableCreated())
            createTable();

        String query = "INSERT INTO " + TableNames.BOOKMARKS + " " +
                "(ELEMENT_ID, COLOR, COLLAPSED) " +
                "VALUES (" +
                bookmarkDTO.getElementId() + ", '" +
                bookmarkDTO.getColor() + "', " +
                "0)";

        DatabaseUtil.executeUpdate(query);

    }

    public static void deleteBookmark(String cellId) {
        String query = "DELETE FROM " + TableNames.BOOKMARKS + " WHERE ELEMENT_ID = " + cellId;
        DatabaseUtil.executeUpdate(query);
    }

    public static void deleteBookmarks() {
        String query = "DELETE FROM " + TableNames.BOOKMARKS;
        DatabaseUtil.executeUpdate(query);
    }

}
