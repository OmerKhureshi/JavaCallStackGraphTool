package com.csgt.controller;

public class ControllerLoader {
    public static MainController mainController;
    public static CenterLayoutController centerLayoutController;
    public static CanvasController canvasController;
    public static MenuController menuController;
    public static EventHandlers eventHandlers;
    public static StatusBarController statusBarController;
    public static InstructionsPaneController instructionsPaneController;

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

    public static void register(StatusBarController statusBarController) {
        ControllerLoader.statusBarController = statusBarController;
    }

    public static void register(InstructionsPaneController instructionsPaneController) {
        ControllerLoader.instructionsPaneController = instructionsPaneController;
    }

    public static EventHandlers getEventHandlers() {
        if (eventHandlers == null) {
            eventHandlers = new EventHandlers();
        }
        return eventHandlers;
    }

}
