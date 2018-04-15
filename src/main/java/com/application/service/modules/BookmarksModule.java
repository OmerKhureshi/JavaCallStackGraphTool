package com.application.service.modules;

import com.application.controller.ControllerLoader;
import com.application.db.DAO.DAOImplementation.BookmarksDAOImpl;
import com.application.db.DTO.BookmarkDTO;

import java.util.Map;

public class BookmarksModule {

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
