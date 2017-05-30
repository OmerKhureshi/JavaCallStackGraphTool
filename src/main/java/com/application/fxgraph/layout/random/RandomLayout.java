package com.application.fxgraph.layout.random;

import com.application.fxgraph.graph.Cell;
import com.application.fxgraph.graph.Graph;
import com.application.fxgraph.layout.base.Layout;

import java.util.List;
import java.util.Random;

public class RandomLayout extends Layout {

    Graph graph;

    Random rnd = new Random();

    public RandomLayout(Graph graph) {

        this.graph = graph;

    }

    public void execute() {

        List<Cell> cells = graph.getModel().getAllCells();

        for (Cell cell : cells) {

            double x = rnd.nextDouble() * 500;
            double y = rnd.nextDouble() * 500;

            cell.relocate(x, y);

        }

    }

}