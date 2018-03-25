package com.application.service.modules;

public class ModuleLocator {
    private static ElementTreeModule elementTreeModule;
    private static GraphLoaderModule graphLoaderModule;

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
}
