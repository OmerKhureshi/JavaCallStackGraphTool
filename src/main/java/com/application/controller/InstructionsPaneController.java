package com.application.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class InstructionsPaneController {

    @FXML private Label methodDefnInfoLabel;
    @FXML private Label callTraceInfoLabel;
    @FXML private Label runInfoLabel;

    @FXML private void initialize() {
    }

    public void setMethodDefnInfoLabel(String methodDefnInfoLabelString) {
        this.methodDefnInfoLabel.setText(methodDefnInfoLabelString);
    }

    public void setCallTraceInfoLabel(String callTraceInfoLabelString) {
        this.callTraceInfoLabel.setText(callTraceInfoLabelString);
    }

    public void setRunInfoLabel(String runInfoLabelString) {
        this.runInfoLabel.setText(runInfoLabelString);
    }

}
