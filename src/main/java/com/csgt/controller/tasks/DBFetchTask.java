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

    public static void initiateTask(boolean isRemovalRequired) {
        System.out.println("DBFetchTask.initiateTask isRemovealReq: " + isRemovalRequired);
        DBFetchTask dbFetchTask = new DBFetchTask();
        dbFetchTask.isRemovalRequired = isRemovalRequired;
        dbFetchTask.run();

        dbFetchTask.setOnFailed(event -> dbFetchTask.getException().printStackTrace());
        System.out.println("DBFetchTask.initiateTask ended");
    }

    @Override
    protected Void call(){
        System.out.println(Thread.currentThread().getId() + ": DBFetchTask.call start");
        fetchFromDB();
        System.out.println(Thread.currentThread().getId() + ": DBFetchTask.call ended");
        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        System.out.println(Thread.currentThread().getId() + ": DBFetchTask.succeeded start");
        if (nodeCells != null && edges!= null && highlightRectList != null) {
            ControllerLoader.canvasController.processTaskResults(nodeCells, edges, highlightRectList, bookmarkDTOMap, isRemovalRequired);
        }
        System.out.println(Thread.currentThread().getId() + ": DBFetchTask.succeeded end");
    }

    @Override
    protected void failed() {
        super.failed();
        System.out.println("DBFetchTask.failed");
    }

    private void fetchFromDB() {
        nodeCells = ControllerLoader.canvasController.getNodeCellsFromDB();
        edges = ControllerLoader.canvasController.getEdgesFromDB();
        highlightRectList = ControllerLoader.canvasController.getHighlightsFromDB();
        bookmarkDTOMap = ControllerLoader.canvasController.getBookmarksFromDB();
    }
}
