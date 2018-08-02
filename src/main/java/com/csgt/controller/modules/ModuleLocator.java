package com.csgt.controller.modules;

public class ModuleLocator {
    private static ElementTreeModule elementTreeModule;

    public static ElementTreeModule getElementTreeModule() {
        if (elementTreeModule == null) {
            elementTreeModule = new ElementTreeModule();
        }

        return elementTreeModule;
    }


    public static void resetElementTreeModule() {
        elementTreeModule = null;
    }
}
