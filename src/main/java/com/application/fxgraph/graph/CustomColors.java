package com.application.fxgraph.graph;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum CustomColors {

    DARK_BLUE(Paint.valueOf("#003366")),
    DARK_GREY(Paint.valueOf("#243447")),
    WHITE(Paint.valueOf("#FFFFFF")),
    ORANGE(Paint.valueOf("#FF851B")),
    RED(Paint.valueOf("#FF4136")),
    LIGHT_PEACH(Paint.valueOf("#ffc3a0")),
    DARK_PEACH(Paint.valueOf("#fb968a")),
    DARK_MAHENDI(Paint.valueOf("#848463")),
    DULL_LIGHT_BLUE(Paint.valueOf("#a0c5c4")),
    DULL_BROWN(Paint.valueOf("#86664b")),
    LIGHT_TURQUOISE(Paint.valueOf("#89ecda")),
    LIGHTEST_BLUE(Paint.valueOf("#cadbfd")),
    TRANSPARENT(Color.TRANSPARENT),;


    public Paint getColor() {
        return this.colorVal;
    }

    private Paint colorVal;

    CustomColors(Paint colorVal) {
        this.colorVal = colorVal;
    }
}
