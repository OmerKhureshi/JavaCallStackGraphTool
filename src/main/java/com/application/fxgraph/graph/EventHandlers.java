package com.application.fxgraph.graph;

import com.application.fxgraph.ElementHelpers.ConvertDBtoElementTree;
import com.application.Main;
import com.application.db.DAOImplementation.*;
import com.application.db.DatabaseUtil;
import com.application.db.TableNames;
import com.application.fxgraph.cells.CircleCell;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.controlsfx.control.PopOver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.application.fxgraph.graph.Graph.cellLayer;

public class EventHandlers {

    final DragContext dragContext = new DragContext();

    Graph graph;
    static Main main;

    public EventHandlers(Graph graph) {
        this.graph = graph;
    }

    public void setCustomMouseEventHandlers(final Node node) {
        // *****************
        // Show popup to display element details on mouse hover on an element.
        // node.setOnMouseEntered(onMouseHoverToShowInfoEventHandler);
        // node.setOnMousePressed(onMouseHoverToShowInfoEventHandler);
        // *****************


        // *****************
        // For debugging. Prints all mouse events.
        // node.addEventFilter(MouseEvent.ANY, onMouseHoverToShowInfoEventHandler);
        // node.addEventFilter(MouseEvent.ANY, event -> System.out.println(event));
        // *****************


        // *****************
        // Click on an element to collapse the subtree rooted at that element.
        node.setOnMousePressed(onMousePressedToCollapseTree);
        // *****************


        // *****************
        // To dismiss the pop over when cursor leaves the circle. But this makes it impossible to click buttons on pop
        // over because the pop over hides when the cursor is moved to click the button.
        // node.setOnMouseExited(onMouseExitToDismissPopover);
        // *****************


        // *****************
        // Make elements draggable.
        // node.setOnMousePressed(onMousePressedEventHandler);
        // node.setOnMouseDragged(onMouseDraggedEventHandler);
        // node.setOnMouseReleased(onMouseReleasedEventHandler);
        // *****************

    }

    private PopOver popOver;
/*


    Fix failing task


*/

    private EventHandler<MouseEvent> onMouseHoverToShowInfoEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            if (popOver != null) {
                popOver.hide();
            }

            Node node = (Node) event.getSource();
            CircleCell cell = (CircleCell) node;
            String timeStamp;
            int methodId, processId, threadId;
            String parameters, packageName = "", methodName = "", parameterTypes = "", eventType, lockObjectId;
            double xCord, yCord;


            // Do Not Uncomment
            // String sql = "Select * from " + TableNames.ELEMENT_TABLE + " " +
            //         "JOIN " + TableNames.CALL_TRACE_TABLE + " ON " + TableNames.CALL_TRACE_TABLE + ".id = " + TableNames.ELEMENT_TABLE+ ".ID_ENTER_CALL_TRACE " +
            //         "JOIN " + TableNames.METHOD_DEFINITION_TABLE + " ON " + TableNames.METHOD_DEFINITION_TABLE + ".ID = " + TableNames.CALL_TRACE_TABLE + ".METHOD_ID " +
            //         "WHERE " + TableNames.ELEMENT_TABLE + ".ID = " + cell.getCellId();
            // System.out.println("your query: " + sql);

            // Please. Please do not try to combine the next two queries into one. Unless you want to spend another day tyring to prove it to yourself.

            String sql = "Select * from " + TableNames.ELEMENT_TABLE + " " +
                    "JOIN " + TableNames.CALL_TRACE_TABLE + " ON " + TableNames.CALL_TRACE_TABLE + ".id = " + TableNames.ELEMENT_TABLE+ ".ID_ENTER_CALL_TRACE " +
                    "WHERE " + TableNames.ELEMENT_TABLE + ".ID = " + cell.getCellId();

            try (ResultSet callTraceRS = DatabaseUtil.select(sql)){
                // try (ResultSet callTraceRS = CallTraceDAOImpl.selectWhere("id = (Select id_enter_call_trace FROM " + TableNames.ELEMENT_TABLE +
                //         " WHERE id = " + cell.getCellId() + ")")) {
                if (callTraceRS.next()) {
                    timeStamp = callTraceRS.getString("time_instant");
                    methodId = callTraceRS.getInt("method_id");
                    processId = callTraceRS.getInt("process_id");
                    threadId = callTraceRS.getInt("thread_id");
                    parameters = callTraceRS.getString("parameters");
                    eventType = callTraceRS.getString("message");
                    lockObjectId = callTraceRS.getString("lockobjid");
                    xCord = callTraceRS.getFloat("bound_box_x_coordinate");
                    yCord = callTraceRS.getFloat("bound_box_y_coordinate");


                    try (ResultSet methodDefRS = MethodDefnDAOImpl.selectWhere("id = " + methodId)) {
                        if (methodDefRS.next()) {
                            packageName = methodDefRS.getString("package_name");
                            methodName = methodDefRS.getString("method_name");
                            parameterTypes = methodDefRS.getString("parameter_types");
                        }

                        if (methodId == 0 ) {
                            methodName = eventType;
                            packageName = "N/A";
                            parameterTypes = "N/A";
                            parameters = "N/A";
                        }
                    } catch (SQLException e) {}

                    // Save the clicked element into recent menu.
                    graph.addToRecent(packageName + "." + methodName, new Graph.XYCoordinate(xCord, yCord, threadId));

                    // System.out.println("hValue: " + graph.getScrollPane().getHvalue());
                    // System.out.println("vValue: " + graph.getScrollPane().getVvalue());


                    Label lMethodName = new Label(methodName);
                    Label lPackageName = new Label(packageName);
                    Label lParameterTypes = new Label(parameterTypes);
                    Label lParameters = new Label(parameters);
                    Label lProcessId = new Label(String.valueOf(processId));
                    Label lThreadId = new Label(String.valueOf(threadId));
                    Label lTimeInstant = new Label(timeStamp);

                    GridPane gridPane = new GridPane();
                    gridPane.setPadding(new Insets(10, 10, 10, 10));
                    gridPane.setVgap(10);
                    gridPane.setHgap(20);
                    gridPane.add(new Label("Method Name: "), 0, 0);
                    gridPane.add(lMethodName, 1, 0);

                    gridPane.add(new Label("Package Name: "), 0, 1);
                    gridPane.add(lPackageName, 1, 1);

                    gridPane.add(new Label("Parameter Types: "), 0, 2);
                    gridPane.add(lParameterTypes, 1, 2);

                    gridPane.add(new Label("Parameters: "), 0, 3);
                    gridPane.add(lParameters, 1, 3);

                    gridPane.add(new Label("Process ID: "), 0, 4);
                    gridPane.add(lProcessId, 1, 4);

                    gridPane.add(new Label("Thread ID: "), 0, 5);
                    gridPane.add(lThreadId, 1, 5);

                    gridPane.add(new Label("Time of Invocation: "), 0, 6);
                    gridPane.add(lTimeInstant, 1, 6);


                    /*
                    * wait-enter -> lock released.
                    *       Get all elements with same lock id and notify-enter
                    * wait-exit -> lock reacquired.
                    *
                    * notify-enter / notify-exit -> lock released
                    *
                    * object lock flow:
                    * wait-enter -> notify-enter / notify-exit -> wait-exit
                    * */

                    List<Integer> ctIdList = new ArrayList<>();
                    List<Integer> eleIdList = new ArrayList<>();
                    if (eventType.equalsIgnoreCase("WAIT-ENTER")) {
                        int ctId = -2;  // Will throw exception if value not changed. Which is what we want.
                        sql = "lockobjid = '" + lockObjectId + "'" +
                                " AND (message = 'NOTIFY-ENTER' OR message = 'NOTIFYALL-ENTER')" +
                                " AND time_instant >= " + "'" + timeStamp + "'";

                        try (ResultSet rs = CallTraceDAOImpl.selectWhere(sql)) {
                            if (rs.next()) {
                                ctId = rs.getInt("id");
                                ctIdList.add(ctId);
                            }
                        }

                        try (ResultSet elementRS = ElementDAOImpl.selectWhere("id_enter_call_trace = " + ctId)) {
                            // Expecting to see a single row.
                            if (elementRS.next()) {
                                int elementId = elementRS.getInt("id");
                                eleIdList.add(elementId);
                            }
                        }
                    } else if (eventType.equalsIgnoreCase("NOTIFY-ENTER")) {

                        try (Connection conn = DatabaseUtil.getConnection(); Statement ps = conn.createStatement()) {


                            sql = "SELECT * FROM " + TableNames.CALL_TRACE_TABLE + " AS parent\n" +
                                    "WHERE MESSAGE = 'WAIT-EXIT' \n" +
                                    "AND LOCKOBJID = '" + lockObjectId + "' " +
                                    "AND TIME_INSTANT >= '" + timeStamp + "' \n" +
                                    "AND (SELECT count(*) \n" +
                                    "FROM " + TableNames.CALL_TRACE_TABLE + " AS child \n" +
                                    "WHERE child.message = 'WAIT-ENTER' \n" +
                                    "AND LOCKOBJID = '" + lockObjectId + "' " +
                                    "AND child.TIME_INSTANT >=  '" + timeStamp + "' \n" +
                                    "AND child.TIME_INSTANT <= parent.time_instant\n" +
                                    ")\n" +
                                    "= 0\n";

                            // System.out.println("Sql: " + sql);
                            int ctId = -2;
                            try (ResultSet resultSet = ps.executeQuery(sql)) {
                                if (resultSet.next()) {
                                    ctId = resultSet.getInt("id");
                                    ctIdList.add(ctId);
                                }
                            }

                            try (ResultSet elementRS = ElementDAOImpl.selectWhere("id_exit_call_trace = " + ctId)) {
                                // Expecting to see a single row.
                                if (elementRS.next()) {
                                    int elementId = elementRS.getInt("id");
                                    eleIdList.add(elementId);
                                }
                            }
                        }

                    } else if (eventType.equalsIgnoreCase("NOTIFYALL-ENTER")) {
                        try (Connection conn = DatabaseUtil.getConnection();
                             Statement ps = conn.createStatement();) {


                            sql = "SELECT * FROM " + TableNames.CALL_TRACE_TABLE + " AS parent WHERE MESSAGE = 'WAIT-EXIT' " +
                                    "AND LOCKOBJID = '" + lockObjectId + "' " +
                                    "AND TIME_INSTANT >= '" + timeStamp + "' " +
                                    "AND (SELECT count(*) FROM " + TableNames.CALL_TRACE_TABLE + " AS child " +
                                    "WHERE child.message = 'WAIT-ENTER' " +
                                    "AND LOCKOBJID = '" + lockObjectId + "' " +
                                    "AND child.TIME_INSTANT >= '" + timeStamp + "' " +
                                    "AND child.TIME_INSTANT <= parent.time_instant ) = 0";

                            int ctId = -2;

                            try (ResultSet resultSet = ps.executeQuery(sql)) {
                                while (resultSet.next()) {
                                    ctId = resultSet.getInt("id");
                                    ctIdList.add(ctId);
                                }
                            }

                            ctIdList.stream().forEach(id -> {
                                try (ResultSet elementRS = ElementDAOImpl.selectWhere("id_exit_call_trace = " + id)) {
                                    // Can be more than a single row.
                                    while (elementRS.next()) {
                                        int elementId = elementRS.getInt("id");
                                        eleIdList.add(elementId);
                                    }
                                } catch (SQLException e) {
                                }
                            });
                        }
                    }

                    List<Button> buttonList = new ArrayList<>();
                    String finalPackageName = packageName;
                    String finalMethodName = methodName;
                    eleIdList.stream().forEach(elementId ->{
                        String query = "SELECT E.ID AS EID, bound_box_x_coordinate, bound_box_y_coordinate, THREAD_ID " +
                                "FROM CALL_TRACE AS CT " +
                                "JOIN ELEMENT AS E ON CT.ID = E.ID_ENTER_CALL_TRACE " +
                                "WHERE E.ID = " + elementId;
                        try (ResultSet elementRS = DatabaseUtil.select(query)){
                            // try (ResultSet elementRS = ElementDAOImpl.selectWhere("id = " + elementId)){
                            if (elementRS.next()) {
                                int id = elementRS.getInt("EID");
                                String targetThreadId = String.valueOf(elementRS.getInt("thread_id"));
                                float xCoordinate = elementRS.getFloat("bound_box_x_coordinate");
                                float yCoordinate = elementRS.getFloat("bound_box_y_coordinate");
                                double width = graph.getScrollPane().getContent().getBoundsInLocal().getWidth();
                                double height = graph.getScrollPane().getContent().getBoundsInLocal().getHeight();


                                // go to location.
                                Button button = new Button();
                                button.setOnMouseClicked(event1 -> {
                                    ConvertDBtoElementTree.resetRegions();
                                    main.showThread(targetThreadId);
                                    Main.makeSelection(targetThreadId);
                                    graph.moveScrollPane(xCoordinate, yCoordinate);

                                    System.out.println("Moving scroll pane: " + xCoordinate + " " + yCoordinate);
                                });
                                buttonList.add(button);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });

                    String message = "", actionMsg = "";
                    switch (eventType.toUpperCase()) {
                        case "WAIT-ENTER":
                            message = "Wait method was invoked and therefore, \nthe lock on object ( object id = " + lockObjectId +") \nwas released and reaquired here.";
                            actionMsg = "Go to Notify or NotifyAll \nmethods invocations.";
                            break;

                        case "NOTIFY-ENTER":
                            message = "Notify method was invoked and therefore, \nthe lock on object ( object id = " + lockObjectId +") \nwas released here.";
                            actionMsg = "Go to wait \nmethods invocations.";
                            break;
                        case "NOTIFYALL-ENTER":
                            message = "NotifyAll method was invoked and therefore, \nthe lock on object ( object id = " + lockObjectId +") \nwas released here.";
                            actionMsg = "Go to wait \nmethods invocations.";
                            break;
                    }
                    Label labelMessage = new Label(message);
                    labelMessage.setWrapText(true);

                    Label labelActionMsg = new Label(actionMsg);

                    gridPane.add(labelMessage, 0 , 7);
                    gridPane.add(labelActionMsg, 0 , 8);
                    int rowIndex = 8;
                    for (Button button: buttonList) {
                        button.setText("Goto node");
                        gridPane.add(button, 1, rowIndex++ );
                    }

                    popOver = new PopOver(gridPane);
                    popOver.setAnimated(true);
                    // popOver.detach();
                    popOver.setAutoHide(true);
                    popOver.show(node);
                }
            } catch (SQLException e) {
                System.out.println("Line that threw exception: " + sql);
                e.printStackTrace();
            }


        }
    };

    EventHandler<MouseEvent> onMouseExitToDismissPopover = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (popOver != null)
                popOver.hide();
        }
    };





    private EventHandler<MouseEvent> onMousePressedToCollapseTree = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            CellLayer cellLayer = (CellLayer) graph.getCellLayer();
            CircleCell cell = (CircleCell) event.getSource();
            String cellId =  cell.getCellId();
            int collapsed = 0;
            double yTopLeft = 0;
            double yBottomLeft = 0;

            try (ResultSet cellRS = ElementDAOImpl.selectWhere("id = " + cellId)) {
                if (cellRS.next()) {
                    collapsed = cellRS.getInt("collapsed");
                    yTopLeft = cellRS.getDouble("bound_box_y_top_left");
                    yBottomLeft = cellRS.getDouble("bound_box_y_bottom_left");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            /*
             * collapsed - actions
             *     0     - Cell visible on UI. Starting value for all cells.
             *     1     - parent of this cell was minimized. Don't show on UI
             *     2     - this cell was minimized. Show on UI. Don't show children on UI.
             *     3     - parent of this cell was minimized. This cell was also minimized. Don't expand this cell's children. Don't show on UI.
             */
            if (collapsed == 1) {
                // expand sub tree.
                // System.out.println("onMousePressedToCollapseTree: cell: " + cellId + " ; collapsed: " + collapsed);
            } else if (collapsed == 0) {
                // Minimize now.
                // System.out.println(">>>> clicked on a collapsed = 0  cell.");
                ((Circle)cell.getChildren().get(0)).setFill(Color.BLUE);

                // ((Circle) ( (Group)cell.getView() )
                //             .getChildren().get(0))
                //             .setFill(Color.BLUE);
                // cell.getChildren().get(0).setStyle("-fx-background-color: blue");
                // cell.setStyle("-fx-background-color: blue");
                cell.setLabel("+");
                System.out.println("EventHandler::onMousePressedToCollapseTree: updated collapse value at cellid: " + cellId);
                ElementDAOImpl.updateWhere("collapsed", "2", "id = " + cellId);

                removeChildrenFromUI(cellId);



                Statement statement = null;
                try {
                    statement = DatabaseUtil.getConnection().createStatement();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                double delta = yBottomLeft - yTopLeft - BoundBox.unitHeightFactor;


                Task updatePosVal = updatePosValForLowerTree(Integer.parseInt(cellId), delta, statement);
                updateCollapseValForSubTreeRootedAt(cellId, updatePosVal);

         /*
                //Original functionality
                Map<String, CircleCell> mapCircleCellsOnUI = graph.getModel().getCircleCellsOnUI();
                List<CircleCell> listCircleCellsOnUI = graph.getModel().getListCircleCellsOnUI();
                List<String> removeCircleCells = new ArrayList<>();

                Map<String, Edge> mapEdgesOnUI = graph.getModel().getEdgesOnUI();
                List<Edge> listEdgesOnUI = graph.getModel().getListEdgesOnUI();
                List<String> removeEdges = new ArrayList<>();

                updateCollapseValForSubTreeRootedAtRecursive(cellId, removeCircleCells, removeEdges);
                removeCircleCells.forEach(circleCellId -> {
                    if (mapCircleCellsOnUI.containsKey(circleCellId)) {
                        CircleCell circleCell = mapCircleCellsOnUI.get(circleCellId);
                        cellLayer.getChildren().remove(circleCell);
                        mapCircleCellsOnUI.remove(circleCellId);
                        listCircleCellsOnUI.remove(circleCell);
                    }
                });
                // listEdgesOnUI.forEach(edge -> System.out.print(" : " + edge));
                // System.out.println();

                removeEdges.forEach(edgeId -> {
                    if (mapEdgesOnUI.containsKey(edgeId)) {
                        Edge edge = mapEdgesOnUI.get(edgeId);
                        cellLayer.getChildren().remove(edge);
                        mapEdgesOnUI.remove(edgeId);
                        listEdgesOnUI.remove(edge);
                    }
                });*/
                // listEdgesOnUI.forEach(edge -> System.out.print(" : " + edge));
                // System.out.println();

            } else if (collapsed == 2) {
                if (cell != null)
                    ((Circle)cell.getChildren().get(0)).setFill(Color.RED);
                // ( (Circle) ( (Groupq)cell.getView() ).getChildren().get(0) ).setFill(Color.RED);
                cell.setLabel("-");
                recursivelyAdd(cellId);
            } else if (collapsed == 3) {
                System.out.println("onMousePressedToCollapseTree: cell: " + cellId + " ; collapsed: " + collapsed);
                throw new IllegalStateException("This cell should not have been on the UI.");
            }
        }
    };


    public void removeChildrenFromUI (String cellId) {
        Map<String, CircleCell> mapCircleCellsOnUI = graph.getModel().getCircleCellsOnUI();
        List<CircleCell> listCircleCellsOnUI = graph.getModel().getListCircleCellsOnUI();
        List<String> removeCircleCells = new ArrayList<>();

        Map<String, Edge> mapEdgesOnUI = graph.getModel().getEdgesOnUI();
        List<Edge> listEdgesOnUI = graph.getModel().getListEdgesOnUI();
        List<String> removeEdges = new ArrayList<>();


        System.out.println("removeChildrenFromUI: click on cellid : " + cellId);
        try (ResultSet rs = ElementDAOImpl.selectWhere("id = " + cellId)) {
            if (rs.next()) {
                float y = rs.getFloat("bound_box_y_top_left");
                float leafCount = rs.getInt("leaf_count");
                float height = leafCount * BoundBox.unitHeightFactor;
                float width = BoundBox.unitWidthFactor;

                float x = rs.getFloat("bound_box_x_top_right");

                System.out.println("removeChildrenFromUI: y top: " + y + " ; y bottom: " + height);

                // add a x condition as well.
                mapCircleCellsOnUI.forEach((id, circleCell) -> {
                    double topY = circleCell.getLayoutY();
                    double rightX = circleCell.getLayoutX() + width;

                    if (topY >= y && topY < (y + height) && rightX >= x) {
                        System.out.println("adding to remove list: cellId" + id);
                        removeCircleCells.add(id);
                        removeEdges.add(id);
                    }
                });

                removeCircleCells.forEach((id) -> {
                    if (mapCircleCellsOnUI.containsKey(id)) {
                        System.out.println("removeChildrenFromUI: removing cellId: " + id);
                        CircleCell cell = mapCircleCellsOnUI.get(id);
                        cellLayer.getChildren().remove(cell);
                        mapCircleCellsOnUI.remove(id);
                    }
                });

                removeEdges.forEach((id) -> {
                    Edge edge = mapEdgesOnUI.get(id);
                    if (edge != null) {
                        System.out.println("removing edge: edgeId: " + id + "; edge targetid: " + edge.getEdgeId());
                        mapEdgesOnUI.remove(id);
                        cellLayer.getChildren().remove(edge);
                    }
                });

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    private void updateCollapseValForSubTreeRootedAt(String cellId, Task updatePosVals){

        //      updated DB async.
        //             Update tree rooted at the click point <======  THIS METHOD
        //              Update tree the entire tree

        Task<Void> updateDBTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                updateCollapseValForSubTreeRootedAtRecursive(cellId);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                new Thread(updatePosVals).run();

                System.out.println("EventHandler::updateCollapseValForSubTreeRootedAt: Updated db in background thread");
            }

            @Override
            protected void failed() {
                super.failed();

                System.out.println("EventHandler::updateCollapseValForSubTreeRootedAt: Updating db in background failed");
            }
        };

        updateDBTask.setOnSucceeded(event -> {
            System.out.println("EventHandler::updateCollapseValForSubTreeRootedAt:event handler: updatedDBTask succeeded. ");
        });

        updateDBTask.setOnFailed(event -> {
            System.out.println("EventHandler::updateCollapseValForSubTreeRootedAt:event handler: updatedDBTask failed. ");
        });

        new Thread(updateDBTask).run();

        // return updateDBTask;


    }

    private void updateCollapseValForSubTreeRootedAtRecursive(String cellId) {
        try (ResultSet childrenRS = ElementToChildDAOImpl.selectWhere("parent_id = " + cellId)) {
            try {
                while (childrenRS.next()) {
                    String childId = String.valueOf(childrenRS.getInt("child_id"));

                    System.out.println("EventHandler::updateCollapseValForSubTreeRootedAtRecursive: removing child: " + childId);

                    ElementDAOImpl.updateWhere("collapsed", "1",
                            "id = " + childId + " AND collapsed = 0");
                    ElementDAOImpl.updateWhere("collapsed", "3",
                            "id = " + childId + " AND collapsed = 2");

                    EdgeDAOImpl.updateWhere("collapsed", "1",
                            "fk_target_element_id = " + childId);

                    updateCollapseValForSubTreeRootedAtRecursive(childId);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {}
    }





    private Task<Void> updatePosValForLowerTree(int cellId, double delta, Statement statement) {
        Task<Void> updateLowerTree = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updatePosValForLowerTreeRecursive(cellId, delta, statement);
                System.out.println("EventHandler::updatePosValForLowerTree:  executed batch");
                statement.executeBatch();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                System.out.println("EventHandler::updatePosValForLowerTree:  Updated the entire tree successfully");
            }

            @Override
            protected void failed() {
                super.failed();
                System.out.println("EventHandler::updatePosValForLowerTree:  Updation of the full tree was unsuccessful ");

            }
        };

        updateLowerTree.setOnSucceeded(event -> {
            System.out.println("EventHandler::updatePosValForLowerTree:event handler: updateLowerTree succeeded. ");
        });

        updateLowerTree.setOnFailed(event -> {
            System.out.println("EventHandler::updatePosValForLowerTree:event handler: updateLowerTree failed. ");
            if (updateLowerTree.getException() instanceof Exception) {
                updateLowerTree.getException().printStackTrace();

            }
        });

        // new Thread(updateLowerTree).run();
        return updateLowerTree;
    }



    private void updatePosValForLowerTreeRecursive(int cellId, double delta, Statement statement) {

        //      updated DB async.
        //             Update tree rooted at the click point - DONE
        //             Update tree the entire tree - <======  THIS METHOD

        // BASE CONDITION. STOP IF ROOT IS REACHED.
        if (cellId == 1) {
            return;
        }

        System.out.println("EventHandler::updatePosValForLowerTree:  At level for cell ID: " + cellId);


        int parentId = 0;

        // get parent_id of the current cell
        try (ResultSet getParentIdRS = ElementDAOImpl.selectWhere("ID = " + cellId)) {
            if (getParentIdRS.next()) {
                parentId = getParentIdRS.getInt("parent_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // get all lower siblings of the cell id.
        String sql = "SELECT * FROM " + TableNames.ELEMENT_TABLE + " AS E1 " +
                "where E1.PARENT_ID = " + parentId + " " +
                "AND E1.ID > " + cellId;

        String updateQuery = null;
        try (ResultSet rs = DatabaseUtil.select(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                System.out.println("EventHandler::updatePosValForLowerTree:  updating positions of cellId: " + id);

                // For circles, calculate the new pos values.
                double newYTopLeft = rs.getDouble("bound_box_y_top_left") - delta;

                double newYTopRight = rs.getDouble("bound_box_y_top_right") - delta;

                double newYBottomLeft = rs.getDouble("bound_box_y_bottom_left") - delta;

                double newYBottomRight = rs.getDouble("bound_box_y_bottom_right") - delta;

                double newX = rs.getDouble("bound_box_x_coordinate");
                double newY = rs.getDouble("bound_box_y_coordinate") - delta;


                // For circles, update the pos values.
                updateQuery = "UPDATE " + TableNames.ELEMENT_TABLE + " " +
                        "SET bound_box_y_top_left = " + newYTopLeft + ", " +

                        "bound_box_y_top_right = " + newYTopRight + ", " +

                        "bound_box_y_bottom_left = " + newYBottomLeft + ", " +

                        "bound_box_y_bottom_right = " + newYBottomRight + ", " +

                        "bound_box_y_coordinate = " + newY + " " +

                        "WHERE ID = " + id;

                System.out.println("Update query for cell: " + updateQuery);

                statement.addBatch(updateQuery);


                // For edges, update the pos values.
                String edgePosUpdateQuery = getEdgePosUpdateQuery(id, newX, newY);
                statement.addBatch(edgePosUpdateQuery);


                // Update db pos for all children recursively at current cell
                updatePosValForLowerTreeChildrenRecursive(id, delta, statement);
            }
        } catch (SQLException e) {
            System.out.println("SQL that threw exception: " + updateQuery);
            e.printStackTrace();
        }

        // Now go to parent and updated all lower sibling position values.
        updatePosValForLowerTreeRecursive(parentId, delta, statement);

    }


    private void updatePosValForLowerTreeChildrenRecursive(int cellId, double delta, Statement statement) {
        String query = null;

        // get children cells
        try (ResultSet childrenRS = ElementToChildDAOImpl.selectWhere("parent_id = " + cellId)) {
            while (childrenRS.next()) {
                int childId = childrenRS.getInt("child_id");

                // Get element details for current child cell
                ResultSet rs = ElementDAOImpl.selectWhere("id = " + childId);

                if (rs.next()) {

                    System.out.println("EventHandler::updatePosValForLowerTreeChildrenRecursive: updating db pos for child: " + childId);


                    double newYTopLeft = rs.getDouble("bound_box_y_top_left") - delta;

                    double newYTopRight = rs.getDouble("bound_box_y_top_right") - delta;

                    double newYBottomLeft = rs.getDouble("bound_box_y_bottom_left") - delta;

                    double newYBottomRight = rs.getDouble("bound_box_y_bottom_right") - delta;

                    double newX = rs.getDouble("bound_box_x_coordinate");
                    double newY = rs.getDouble("bound_box_y_coordinate") - delta;


                    // Update the delta in current row.
                    String updateQuery = "UPDATE " + TableNames.ELEMENT_TABLE + " " +
                            "SET bound_box_y_top_left = " + newYTopLeft + ", " +

                            "bound_box_y_top_right = " + newYTopRight + ", " +

                            "bound_box_y_bottom_left = " + newYBottomLeft + ", " +

                            "bound_box_y_bottom_right = " + newYBottomRight + ", " +

                            "bound_box_y_coordinate = " + newY + " " +

                            "WHERE ID = " + childId;

                    System.out.println("Update query to add to batch: " + updateQuery);

                    statement.addBatch(updateQuery);

                    // For edges, update the pos values.
                    String edgePosUpdateQuery = getEdgePosUpdateQuery(childId, newX, newY);
                    statement.addBatch(edgePosUpdateQuery);

                    updatePosValForLowerTreeChildrenRecursive(childId, delta, statement);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL that threw exception: " + query);
            e.printStackTrace();
        }
    }



    // This method returns the edge update query.
    // It accepts target x and y coordinate and get the source x and y coordinates.
    private String getEdgePosUpdateQuery(int targetId, double endX, double endY) {
        // Get the element row for source cell
        String getSourceElementRow = "SELECT * FROM " + TableNames.ELEMENT_TABLE + " " +
                "WHERE ID = (" +
                "SELECT FK_SOURCE_ELEMENT_ID FROM " + TableNames.EDGE_TABLE + " " +
                "WHERE FK_TARGET_ELEMENT_ID = " + targetId + ")";

        String edgeUpdateQuery = null;

        try (ResultSet sourceCellRS = DatabaseUtil.select(getSourceElementRow)) {
            if (sourceCellRS.next()) {
                // Update edge only if it exists. Edge only exists if there is a source cell for the current cell (target cell)
                // get x and y coordinates of source and target cell. The current cell is target cell.
                double edgeNewStartX = sourceCellRS.getDouble("bound_box_x_coordinate");
                double edgeNewStartY = sourceCellRS.getDouble("bound_box_y_coordinate");
                double edgeNewEndX = endX;
                double edgeNewEndY  = endY;

                // For edges, update the pos values.
                edgeUpdateQuery = "UPDATE " + TableNames.EDGE_TABLE + " " +
                        "SET START_X = " + edgeNewStartX + ", " +
                        "START_Y = " + edgeNewStartY + ", " +
                        "END_X = " + edgeNewEndX + ", " +
                        "END_Y = " + edgeNewEndY + " " +
                        "WHERE FK_TARGET_ELEMENT_ID = " + targetId;

                System.out.println("EventHandler::getEdgePosUpdateQuery: Update query for edge: " + edgeUpdateQuery);

            }
        } catch (SQLException e) {
            System.out.println("SQL that threw exception: " + getSourceElementRow);
            e.printStackTrace();
        }

        return edgeUpdateQuery;
    }




    public void recursivelyAdd(String cellId) {

        try (ResultSet elementRS = ElementDAOImpl.selectWhere("id = " + cellId)) {
            if (elementRS.next()) {
                int collapsed = elementRS.getInt("collapsed");
                if (collapsed == 0) {
                    throw new IllegalStateException("Collapsed cannot be 0 here.");
                } else if (collapsed == 1) {
                    ElementDAOImpl.updateWhere("collapsed", "0", "id = " + cellId);
                    float xCoordinateTemp = elementRS.getFloat("bound_box_x_coordinate");
                    float yCoordinateTemp = elementRS.getFloat("bound_box_y_coordinate");
                    CircleCell cell = new CircleCell(cellId, xCoordinateTemp, yCoordinateTemp);
                    graph.getModel().addCell(cell);

                    try (ResultSet parentRS = ElementToChildDAOImpl.selectWhere("child_id = " + cellId)) {
                        if (parentRS.next()) {
                            String parentId = String.valueOf(parentRS.getInt("parent_id"));
                            CircleCell parentCell = graph.getModel().getCircleCellsOnUI().get(parentId);

                            Edge edge = new Edge(parentCell, cell);

                            EdgeDAOImpl.updateWhere("collapsed", "0",
                                    "fk_target_element_id = " + cellId);

                            graph.getModel().addEdge(edge);

                        }
                    }
                    // graph.myEndUpdate();
                    graph.updateCellLayer();

                    try (ResultSet childrenRS = ElementToChildDAOImpl.selectWhere("parent_id = " + cellId)) {
                        while (childrenRS.next()) {
                            String childId = String.valueOf(childrenRS.getInt("child_id"));
                            recursivelyAdd(childId);
                        }
                    }

                } else if (collapsed == 2) {
                    // update collapsed=0
                    ElementDAOImpl.updateWhere("collapsed", "0", "id = " + cellId);
                    // for all children with collapsed=1, show and update collapsed=0
                    try (ResultSet childrenRS = ElementToChildDAOImpl.selectWhere("parent_id = " + cellId)) {
                        while (childrenRS.next()) {
                            String childId = String.valueOf(childrenRS.getInt("child_id"));
                            recursivelyAdd(childId);
                        }
                    }

                } else if (collapsed == 3) {
                    ElementDAOImpl.updateWhere("collapsed", "2", "id = " + cellId);
                    float xCoordinateTemp = elementRS.getFloat("bound_box_x_coordinate");
                    float yCoordinateTemp = elementRS.getFloat("bound_box_y_coordinate");
                    CircleCell cell = new CircleCell(cellId, xCoordinateTemp, yCoordinateTemp);
                    graph.getModel().addCell(cell);

                    try (ResultSet parentRS = ElementToChildDAOImpl.selectWhere("child_id = " + cellId)) {
                        if (parentRS.next()) {
                            String parentId = String.valueOf(parentRS.getInt("parent_id"));
                            CircleCell parentCell = graph.getModel().getCircleCellsOnUI().get(parentId);

                            Edge edge = new Edge(parentCell, cell);
                            EdgeDAOImpl.updateWhere("collapsed", "0",
                                    "fk_target_element_id = " + cellId);
                            graph.getModel().addEdge(edge);
                        }
                    }
                    // graph.myEndUpdate();
                    graph.updateCellLayer();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCollapseValForSubTreeRootedAtRecursive(String cellId, List<String> removeCircleCells, List<String> removeEdges ) {
        try (ResultSet childrenRS = ElementToChildDAOImpl.selectWhere("parent_id = " + cellId)) {
            try {
                while (childrenRS.next()) {
                    String childId = String.valueOf(childrenRS.getInt("child_id"));
                    removeCircleCells.add(childId);
                    removeEdges.add(childId);

                    ElementDAOImpl.updateWhere("collapsed", "1",
                            "id = " + childId + " AND collapsed = 0");
                    ElementDAOImpl.updateWhere("collapsed", "3",
                            "id = " + childId + " AND collapsed = 2");

                    EdgeDAOImpl.updateWhere("collapsed", "1",
                            "fk_target_element_id = " + childId);

                    updateCollapseValForSubTreeRootedAtRecursive(childId, removeCircleCells, removeEdges);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {}
    }



    @SuppressWarnings("unused")
    EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Node node = (Node) event.getSource();
            double scale = graph.getScale();
            dragContext.x = node.getBoundsInParent().getMinX() * scale - event.getScreenX();
            dragContext.y = node.getBoundsInParent().getMinY()  * scale - event.getScreenY();
        }
    };

    EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Node node = (Node) event.getSource();
            double offsetX = event.getScreenX() + dragContext.x;
            double offsetY = event.getScreenY() + dragContext.y;
            // adjust the offset in case we are zoomed
            double scale = graph.getScale();
            offsetX /= scale;
            offsetY /= scale;
            node.relocate(offsetX, offsetY);
        }
    };

    EventHandler<MouseEvent> onMouseReleasedEventHandler = event -> {};

    class DragContext {
        double x;
        double y;
    }

    public static void saveRef(Main m) {
        main = m;
    }
}