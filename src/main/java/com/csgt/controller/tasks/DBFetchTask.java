package com.csgt.controller.tasks;

import com.csgt.controller.ControllerLoader;
import com.csgt.dataaccess.DTO.BookmarkDTO;
import com.csgt.presentation.graph.Edge;
import com.csgt.presentation.graph.HighlightCell;
import com.csgt.presentation.graph.NodeCell;
import javafx.concurrent.Task;

import java.util.List;
import java.util.Map;

/**
 * This task is handles fetching records from the database when user scroll past the preloaded area on the canvas.
 */
public class DBFetchTask extends Task<Void> {

    private List<NodeCell> nodeCells;
    private List<Edge> edges;
    private List<HighlightCell> highlightRectList;
    private Map<String, BookmarkDTO> bookmarkDTOMap;
    private boolean isRemovalRequired = true;
    private static boolean isRunning = false;

    public static void initiateTask(boolean isRemovalRequired) {
        if (isRunning) {
            return;
        }

        isRunning = true;

        DBFetchTask dbFetchTask = new DBFetchTask();
        dbFetchTask.isRemovalRequired = isRemovalRequired;
        dbFetchTask.run();

        dbFetchTask.setOnFailed(event -> dbFetchTask.getException().printStackTrace());
    }

    @Override
    protected Void call(){
        fetchFromDB();
        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();

        if (nodeCells != null && edges!= null && highlightRectList != null) {
            ControllerLoader.canvasController.processTaskResults(nodeCells, edges, highlightRectList, bookmarkDTOMap, isRemovalRequired);
        }
        isRunning = false;
    }

    @Override
    protected void failed() {
        super.failed();
        isRunning = false;
        System.out.println("DBFetchTask.failed");
    }

    private void fetchFromDB() {
        nodeCells = ControllerLoader.canvasController.getNodeCellsFromDB();
        edges = ControllerLoader.canvasController.getEdgesFromDB();
        highlightRectList = ControllerLoader.canvasController.getHighlightsFromDB();
        bookmarkDTOMap = ControllerLoader.canvasController.getBookmarksFromDB();
    }
}
