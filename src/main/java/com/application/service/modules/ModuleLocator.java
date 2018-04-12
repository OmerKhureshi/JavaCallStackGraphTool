package com.application.service.modules;

public class ModuleLocator {
    private static ElementTreeModule elementTreeModule;
    private static GraphLoaderModule graphLoaderModule;
    private static BookmarksModule bookmarksModule;

    public static ElementTreeModule getElementTreeModule() {
        if (elementTreeModule == null) {
            elementTreeModule = new ElementTreeModule();
        }

        return elementTreeModule;
    }

    public static GraphLoaderModule getGraphLoaderModule() {
        if (graphLoaderModule == null) {
            graphLoaderModule = new GraphLoaderModule();
        }

        return graphLoaderModule;
    }

    public static BookmarksModule getBookmarksModule() {
        if (bookmarksModule == null) {
            bookmarksModule = new BookmarksModule();
        }

        return bookmarksModule;
    }
}
