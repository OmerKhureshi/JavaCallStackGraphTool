package com.application.controller;

import com.application.fxgraph.graph.ColorProp;
import com.application.service.files.FileNames;
import com.application.service.files.LoadedFiles;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

public class InstructionsPaneController {

    @FXML
    private FlowPane flowPane;
    @FXML
    private Label headerLabel;

    @FXML
    private Label fileHeaderLabel;
    @FXML
    private Label methodDefInfoLabel;
    @FXML
    private Label callTraceInfoLabel;
    @FXML
    private Label fileRunInfoLabel;

    @FXML
    private Label orLabel;

    @FXML
    private Label dbHeaderLabel;
    @FXML
    private Label dbInfoLabel;
    @FXML
    private Label dbRunInfoLabel;

    private Glyph fileHeaderGlyph;
    private Glyph methodDefInfoGlyph;
    private Glyph callTraceInfoGlyph;
    private Glyph fileRunInfoGlyph;

    private Glyph dbHeaderGlyph;
    private Glyph dbInfoGlyph;
    private Glyph dbRunInfoGlyph;


    @FXML private void initialize() {
        ControllerLoader.register(this);

        initGraphics();
    }

    private void initGraphics() {
        double leftInset = 20;

        flowPane.setPadding(new Insets(40));
        orLabel.setPadding(new Insets(10, 0, 10, 40));

        double headerFontSize = headerLabel.getFont().getSize();
        headerLabel.setStyle("-fx-font-size: " + (headerFontSize + 10));
        headerLabel.setTextFill(ColorProp.GREY);

        fileHeaderGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_CIRCLE_RIGHT);
        fileHeaderGlyph.setColor(ColorProp.GREY);
        fileHeaderLabel.setGraphic(fileHeaderGlyph);
        fileHeaderLabel.setTextFill(ColorProp.GREY);
        double fileHeaderFontSize = fileHeaderLabel.getFont().getSize();
        fileHeaderLabel.setStyle("-fx-font-size: " + (fileHeaderFontSize + 5));

        methodDefInfoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT);
        methodDefInfoGlyph.setColor(ColorProp.GREY);
        methodDefInfoLabel.setGraphic(methodDefInfoGlyph);
        methodDefInfoLabel.setPadding(new Insets(0, 0, 0, leftInset));

        callTraceInfoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT);
        callTraceInfoGlyph.setColor(ColorProp.GREY);
        callTraceInfoLabel.setGraphic(callTraceInfoGlyph);
        callTraceInfoLabel.setPadding(new Insets(0, 0, 0, leftInset));

        fileRunInfoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT);
        fileRunInfoGlyph.setColor(ColorProp.GREY);
        fileRunInfoLabel.setGraphic(fileRunInfoGlyph);
        fileRunInfoLabel.setPadding(new Insets(0, 0, 0, leftInset));


        dbHeaderGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_CIRCLE_RIGHT);
        dbHeaderGlyph.setColor(ColorProp.GREY);
        dbHeaderLabel.setGraphic(dbHeaderGlyph);
        dbHeaderLabel.setTextFill(ColorProp.GREY);
        double dbHeaderFontSize = dbHeaderLabel.getFont().getSize();
        dbHeaderLabel.setStyle("-fx-font-size: " + (dbHeaderFontSize + 5));


        dbInfoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT);
        dbInfoGlyph.setColor(ColorProp.GREY);
        dbInfoLabel.setGraphic(dbInfoGlyph);
        dbInfoLabel.setPadding(new Insets(0, 0, 0, leftInset));


        dbRunInfoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT);
        dbRunInfoGlyph.setColor(ColorProp.GREY);
        dbRunInfoLabel.setGraphic(dbRunInfoGlyph);
        dbRunInfoLabel.setPadding(new Insets(0, 0, 0, leftInset));

    }

    public void setMethodDefGraphics(boolean enabled) {
        if (enabled) {
            // methodDefInfoLabel.setWrapText(true);
            // methodDefInfoLabel.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
            methodDefInfoLabel.setText("Method definitions log file loaded successfully. File Name: "
                    + LoadedFiles.getFile(FileNames.METHOD_DEF.getFileName()).getName());

            methodDefInfoGlyph.setIcon(FontAwesome.Glyph.CHECK);
            methodDefInfoGlyph.setColor(ColorProp.GREEN);
            methodDefInfoLabel.setGraphic(methodDefInfoGlyph);

        } else {
            methodDefInfoGlyph.setIcon(FontAwesome.Glyph.ARROW_RIGHT);
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
            callTraceInfoGlyph.setIcon(FontAwesome.Glyph.ARROW_RIGHT);
            callTraceInfoGlyph.setColor(ColorProp.BLACK);
        }
    }

    public void setFileRunInfoGraphics(boolean enabled) {
        if (enabled) {
            fileRunInfoGlyph.setColor(ColorProp.GREEN);
        } else {
            fileRunInfoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT);
            fileRunInfoGlyph.setColor(ColorProp.GREY);
        }
    }

    public void setDBInfoGraphics(boolean enabled) {
        if (enabled) {
            dbInfoGlyph.setIcon(FontAwesome.Glyph.CHECK);
            dbInfoGlyph.setColor(ColorProp.GREEN);

            dbInfoLabel.setText("Database loaded successfully. DB Name: "
                    + LoadedFiles.getFile(FileNames.DB.getFileName()).getName());

        } else {
            dbInfoGlyph.setIcon(FontAwesome.Glyph.ARROW_RIGHT);
            dbInfoGlyph.setColor(ColorProp.BLACK);
        }
    }

    public void setDBRunInfoGraphics(boolean enabled) {
        if (enabled) {
            dbRunInfoGlyph.setColor(ColorProp.GREEN);
        } else {
            dbRunInfoGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT);
            dbRunInfoGlyph.setColor(ColorProp.GREY);
        }
    }

}
