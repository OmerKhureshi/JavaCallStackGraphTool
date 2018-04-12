package com.application.controller;

import com.application.Main;

public class ControllerLoader {
    public static MainController mainController;
    public static CenterLayoutController centerLayoutController;
    public static CanvasController canvasController;
    public static MenuController menuController;

    public static void register(MainController mainController) {
        ControllerLoader.mainController = mainController;
    }

    public static void register(CenterLayoutController centerLayoutController) {
        ControllerLoader.centerLayoutController = centerLayoutController;
    }

    public static void register(CanvasController canvasController) {
        ControllerLoader.canvasController = canvasController;
    }

    public static void register(MenuController menuController) {
        ControllerLoader.menuController = menuController;
    }

}
