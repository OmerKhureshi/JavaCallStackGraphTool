package com.application.service.modules;

import com.application.controller.ControllerLoader;
import com.application.db.DAO.DAOImplementation.BookmarksDAOImpl;
import com.application.db.DTO.BookmarkDTO;

import java.util.HashMap;
import java.util.Map;

public class BookmarksModule {

    public Map<String, String> getBookmarks() {
        Map<String, String> bookmarkMap = new HashMap<>();

        Map<String, BookmarkDTO> bookmarkDTOMap = BookmarksDAOImpl.getBookmarkDTOs();

        for (HashMap.Entry<String, BookmarkDTO> entry: bookmarkDTOMap.entrySet()) {
            String text = " Id:" + entry.getValue().getElementId() +
                    "  |  Method:" + entry.getValue().getMethodName() +
                    "  |  Thread:" + entry.getValue().getThreadId();
            bookmarkMap.putIfAbsent(entry.getKey(), text);
        }

        return bookmarkMap;
    }

    public Map<String, BookmarkDTO> getBookmarkDTOs() {
        return BookmarksDAOImpl.getBookmarkDTOs();
    }

    public void insertBookmark(BookmarkDTO bookmarkDTO) {
        BookmarksDAOImpl.insertBookmark(bookmarkDTO);
        ControllerLoader.canvasController.addBookmarks();
        ControllerLoader.menuController.updateBookmarksMenu();

    }

    public void deleteBookmark(String elementId) {
        ControllerLoader.canvasController.removeBookmarkFromUI(elementId);
        BookmarksDAOImpl.deleteBookmark(elementId);
        ControllerLoader.menuController.updateBookmarksMenu();
    }

    public void deleteAllBookmarks() {
        ControllerLoader.canvasController.removeAllBookmarksFromUI();
        BookmarksDAOImpl.deleteBookmarks();
    }
}
