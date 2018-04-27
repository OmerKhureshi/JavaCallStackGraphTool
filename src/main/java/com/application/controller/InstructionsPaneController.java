package com.application.controller;

import com.application.fxgraph.graph.ColorProp;
import com.application.service.files.FileNames;
import com.application.service.files.LoadedFiles;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

public class InstructionsPaneController {

    @FXML
    private Label methodDefInfoLabel;
    @FXML
    private Label callTraceInfoLabel;
    @FXML
    private Label runInfoLabel;

    @FXML
    private Label dbInfoLabel;

    private Glyph methodDefInfoGlyph;
    private Glyph callTraceInfoGlyph;
    private Glyph dbInfoGlyph;
    private Glyph runInfoGlyph;


    @FXML private void initialize() {
        ControllerLoader.register(this);
        initGraphics();
    }

    private void initGraphics() {
        methodDefInfoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT);
        methodDefInfoGlyph.setColor(ColorProp.GREY);
        methodDefInfoLabel.setGraphic(methodDefInfoGlyph);

        callTraceInfoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT);
        callTraceInfoGlyph.setColor(ColorProp.GREY);
        callTraceInfoLabel.setGraphic(callTraceInfoGlyph);

        dbInfoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT);
        dbInfoGlyph.setColor(ColorProp.GREY);
        dbInfoLabel.setGraphic(dbInfoGlyph);

        runInfoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT);
        runInfoGlyph.setColor(ColorProp.GREY);
        runInfoLabel.setGraphic(runInfoGlyph);
    }

    public void setMethodDefInfoLabel(String methodDefInfoLabel) {
        this.methodDefInfoLabel.setText(methodDefInfoLabel);
    }

    public void setCallTraceInfoLabel(String callTraceInfoLabelString) {
        this.callTraceInfoLabel.setText(callTraceInfoLabelString);
    }

    public void setRunInfoLabel(String runInfoLabelString) {
        this.runInfoLabel.setText(runInfoLabelString);
    }


    public void setMethodDefGraphics(boolean enabled) {
        if (enabled) {
            methodDefInfoGlyph.setIcon(FontAwesome.Glyph.CHECK);
            methodDefInfoGlyph.setColor(ColorProp.GREEN);

            methodDefInfoLabel.setText("Method definitions log file loaded successfully. File Name: "
                    + LoadedFiles.getFile(FileNames.METHOD_DEF.getFileName()).getName());
        } else {
            methodDefInfoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT);
            methodDefInfoGlyph.setColor(ColorProp.GREY);
        }
    }

    public void setCallTraceGraphics(boolean enabled) {
        if (enabled) {
            callTraceInfoGlyph.setIcon(FontAwesome.Glyph.CHECK);
            callTraceInfoGlyph.setColor(ColorProp.GREEN);

            callTraceInfoLabel.setText("Call trace log file loaded successfully. File Name: "
                    + LoadedFiles.getFile(FileNames.Call_Trace.getFileName()).getName());
        } else {
            callTraceInfoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT);
            callTraceInfoGlyph.setColor(ColorProp.GREY);
        }
    }

}
