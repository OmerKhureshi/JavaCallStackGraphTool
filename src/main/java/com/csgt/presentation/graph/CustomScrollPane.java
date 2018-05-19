package com.csgt.presentation.graph;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.transform.Scale;

/**
 * This class represents an enclosing scroll pane that contains all the nodes on UI.
 */
public class CustomScrollPane extends ScrollPane {
    private static double scaleValue = 1.0;

    public CustomScrollPane(Node content) {
        Group contentGroup = new Group();
        Group containerGroup = new Group();
        contentGroup.getChildren().add(containerGroup);
        containerGroup.getChildren().add(content);
        setContent(contentGroup);
        Scale scaleTransform = new Scale(scaleValue, scaleValue, 0, 0);
        containerGroup.getTransforms().add(scaleTransform);

        setHbarPolicy(ScrollBarPolicy.ALWAYS);
        setVbarPolicy(ScrollBarPolicy.ALWAYS);

        setStyle("-fx-background-color: transparent;");
    }

    public static double getScaleValue() {
        return scaleValue;
    }
}