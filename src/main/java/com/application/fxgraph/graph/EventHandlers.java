package com.application.fxgraph.graph;

import com.application.Main;
import com.application.db.DAOImplementation.CallTraceDAOImpl;
import com.application.db.DAOImplementation.ElementDAOImpl;
import com.application.db.DAOImplementation.ElementToChildDAOImpl;
import com.application.db.DAOImplementation.MethodDefnDAOImpl;
import com.application.db.DatabaseUtil;
import com.application.db.TableNames;
import com.application.fxgraph.ElementHelpers.ConvertDBtoElementTree;
import com.application.fxgraph.cells.CircleCell;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.PopOver;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.application.db.TableNames.*;

public class EventHandlers {

    private static ConvertDBtoElementTree convertDBtoElementTree;
    private final DragContext dragContext = new DragContext();
    private List<CircleCell> addLaterCircle = new LinkedList<>();
    private List<Edge> addLaterEdge = new LinkedList<>();
    Graph graph;
    private static Main main;
    private PopOver popOver;


    public EventHandlers(Graph graph) {
        this.graph = graph;
    }

    void setCustomMouseEventHandlers(final Node node) {
        // *****************
        // Show popup to display element details on mouse hover on an element.
        // node.setOnMouseEntered(onMouseHoverToShowInfoEventHandler);
        // node.setOnMousePressed(onMouseHoverToShowInfoEventHandler);
        // *****************


        // *****************
        // Click on an element to collapse the subtree rooted at clicked element.
        node.setOnMousePressed(onMousePressedToCollapseTree);
        // *****************


        // *****************
        // For debugging. Prints all mouse events.
        // node.addEventFilter(MouseEvent.ANY, onMouseHoverToShowInfoEventHandler);
        // node.addEventFilter(MouseEvent.ANY, event -> System.out.println(event));
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
                    } catch (SQLException ignored) {}

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
                             Statement ps = conn.createStatement()) {


                            sql = "SELECT * FROM " + TableNames.CALL_TRACE_TABLE + " AS parent WHERE MESSAGE = 'WAIT-EXIT' " +
                                    "AND LOCKOBJID = '" + lockObjectId + "' " +
                                    "AND TIME_INSTANT >= '" + timeStamp + "' " +
                                    "AND (SELECT count(*) FROM " + TableNames.CALL_TRACE_TABLE + " AS child " +
                                    "WHERE child.message = 'WAIT-ENTER' " +
                                    "AND LOCKOBJID = '" + lockObjectId + "' " +
                                    "AND child.TIME_INSTANT >= '" + timeStamp + "' " +
                                    "AND child.TIME_INSTANT <= parent.time_instant ) = 0";

                            int ctId;

                            try (ResultSet resultSet = ps.executeQuery(sql)) {
                                while (resultSet.next()) {
                                    ctId = resultSet.getInt("id");
                                    ctIdList.add(ctId);
                                }
                            }

                            ctIdList.forEach(id -> {
                                try (ResultSet elementRS = ElementDAOImpl.selectWhere("id_exit_call_trace = " + id)) {
                                    // Can be more than a single row.
                                    while (elementRS.next()) {
                                        int elementId = elementRS.getInt("id");
                                        eleIdList.add(elementId);
                                    }
                                } catch (SQLException ignored) {
                                }
                            });
                        }
                    }

                    List<Button> buttonList = new ArrayList<>();
                    String finalPackageName = packageName;
                    String finalMethodName = methodName;
                    eleIdList.forEach(elementId ->{
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

                    Button collapse = new Button("Collapse");
                    collapse.setOnAction(event1 -> {
                        collapseThisTree(cell);
                    });

                    gridPane.add(collapse, 0, rowIndex);

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

    private void collapseThisTree(CircleCell cell) {


        CellLayer cellLayer = (CellLayer) graph.getCellLayer();
        // CircleCell cell = (CircleCell) event.getSource();
        String cellId =  cell.getCellId();
        int collapsed = 0;
        double BBYTopLeft = 0, BBYBottomLeft = 0;

        System.out.println("Clicked cell: " + cellId);
        System.out.println("Layout Y of " + cellId + " id " + cell.getLayoutY());

        // Get element row from Element table.
        try (ResultSet cellRS = ElementDAOImpl.selectWhere("id = " + cellId)) {
            if (cellRS.next()) {
                collapsed = cellRS.getInt("collapsed");
                BBYTopLeft = cellRS.getDouble("BOUND_BOX_Y_TOP_LEFT");
                BBYBottomLeft = cellRS.getDouble("BOUND_BOX_Y_BOTTOM_LEFT");
            }
        } catch (SQLException ignored) {}

            /*
             * Valid statuses of a circle based on whether it is maximized or minimized/collapsed.
             * --------------------
             * Status - Description
             * --------------------
             *   0    - Classic Visible Circle - Show cell on UI. Starting value for all cells.
             *   1    - Classic Invisible Circle - parent of this cell was minimized. Don't show on UI
             *   2    - Classic Minimized Circle - this cell was minimized. Show on UI. Don't show children on UI.
             *   3    - Minimized circle with Parent Minimized - parent of this cell was minimized. This cell was also minimized. Don't expand this cell's children. Don't show on UI.
             */


            /*
             * Statuses of circles.
             * -----------------------------
             * Status : Visible : Minimized
             * -----------------------------
             *    0   :   YES   :   NO
             *    1   :   NO    :   NO
             *    2   :   YES   :   YES
             *    3   :   NO    :   YES
             */


            /*
             * When Minimizing.
             *  Circles that can be clicked: 0 (2 handled while maximizing)
             *  Children of clicked cell: 0, 2
             */


            /*
             * When Maximizing.
             *  Circles that can be clicked: 2 (0 handled while minimizing)
             *  Children of clicked cell: 1, 3
             */

        if (collapsed == 1) {
            // expand sub tree.
            // This does not happen because there is no circle with collapsed = 1 that is visible on UI.
            System.out.println("onMousePressedToCollapseTree: cell: " + cellId + " ; collapsed: " + collapsed);
            throw new IllegalStateException("This cell should not have been on the UI.");
        } else if (collapsed == 0) {
            // Minimize now.

            cell.setColorWhenMinimized();
            // cell.setLabel("+");
            ElementDAOImpl.updateWhere("collapsed", "2", "id = " + cellId);

            Map<String, CircleCell> mapCircleCellsOnUI = graph.getModel().getCircleCellsOnUI();
            List<CircleCell> listCircleCellsOnUI = graph.getModel().getListCircleCellsOnUI();
            List<String> removeCircleCells = new ArrayList<>();

            Map<String, Edge> mapEdgesOnUI = graph.getModel().getEdgesOnUI();
            List<Edge> listEdgesOnUI = graph.getModel().getListEdgesOnUI();
            List<String> removeEdges = new ArrayList<>();

            double fullCellHeight = BBYBottomLeft - BBYTopLeft;  // BoundBox height of the cell.
            // DeltaMap.yMin = cell.getLayoutY() - fullCellHeight / 2;  // Actual yMin of the BoundBox after any shifts.

            DeltaMap.yMax = cell.getLayoutY() + fullCellHeight;  // Actual yMax of the BoundBox after any shifts. May not be same as BBYBottomLeft.
            // DeltaMap.upperDelta = fullCellHeight / 2 - BoundBox.unitHeightFactor/2;
            // DeltaMap.upperDelta = DeltaMap.upperDelta < BoundBox.unitHeightFactor? 0 : DeltaMap.upperDelta;

            DeltaMap.lowerDelta = fullCellHeight - BoundBox.unitHeightFactor;


            System.out.println("0 -> 2 : ");
            // System.out.println("yMin: " + DeltaMap.yMin + " : upperDelta: " + DeltaMap.upperDelta);
            System.out.println("yMax: " + DeltaMap.yMax + " : upperDelta: " + DeltaMap.lowerDelta);

            DeltaMap.isAnyCircleMinimized = true;

            removeAndUpdate(cellId, removeCircleCells, removeEdges);
            // recursivelyRemove(cellId, removeCircleCells, removeEdges);

            removeCircleCells.forEach(circleCellId -> {
                if (mapCircleCellsOnUI.containsKey(circleCellId)) {
                    CircleCell circleCell = mapCircleCellsOnUI.get(circleCellId);
                    cellLayer.getChildren().remove(circleCell);
                    mapCircleCellsOnUI.remove(circleCellId);
                    listCircleCellsOnUI.remove(circleCell);
                }
            });

            removeEdges.forEach(edgeId -> {
                if (mapEdgesOnUI.containsKey(edgeId)) {
                    Edge edge = mapEdgesOnUI.get(edgeId);
                    cellLayer.getChildren().remove(edge);
                    mapEdgesOnUI.remove(edgeId);
                    listEdgesOnUI.remove(edge);
                }
            });

            if (BBYBottomLeft == 0) {
                System.out.println(" Probably: not clicked on circle but still put new delta in map");
            }



        } else if (collapsed == 2) {
            /*
             * This node was collapsed earlier. Expand now.
             */

            cell.setColorWhenMaximized();
            // cell.setLabel("-");

            DeltaMap.isAnyCircleMaximized = true;


            /*
             * For circles going 2 -> 0, calculate upper and lower Delta.
             * Later while iterating through the tree rooted at this cell,
             * modify delta values for child circles going 3 -> 2
             */
            double fullCellHeight = BBYBottomLeft - BBYTopLeft;  // BoundBox height of the cell.
            // DeltaMap.yMin = cell.getLayoutY() - BoundBox.unitHeightFactor / 2;

            DeltaMap.yMax = cell.getLayoutY() + BoundBox.unitHeightFactor;
            // DeltaMap.yMax = cell.getLayoutY() + fullCellHeight;
            System.out.println("yMax at start : " + DeltaMap.yMax);

            // DeltaMap.upperDelta = fullCellHeight / 2;
            double height = addAndUpdate(cellId, cell.getLayoutY());

            // Handling case for single circles with status 0 when minimized and then maximized
            DeltaMap.lowerDelta = height == 0 ? 0 :  (height - 1) * BoundBox.unitHeightFactor;

            System.out.println("2 -> 0 : ");
            // System.out.println("yMin: " + DeltaMap.yMin + " : upperDelta: " + DeltaMap.upperDelta);
            System.out.println("yMax: " + DeltaMap.yMax + " : lowerDelta: " + DeltaMap.lowerDelta);
            System.out.println();

        } else if (collapsed == 3) {
            System.out.println("onMousePressedToCollapseTree: cell: " + cellId + " ; collapsed: " + collapsed);
            throw new IllegalStateException("This cell should not have been on the UI.");
        }

        // convertDBtoElementTree.loadUIComponentsInsideVisibleViewPort(graph);
        if (DeltaMap.isAnyCircleMinimized) graph.moveCirclesAfterMinimization();
        if (DeltaMap.isAnyCircleMaximized) graph.moveCirclesAfterMaximization();

        addLaterCircle.forEach(circle -> {
            graph.getModel().addCell(circle);
        });

        addLaterCircle.clear();
        addLaterEdge.forEach(edge -> graph.getModel().addEdge(edge));
        addLaterEdge.clear();

        graph.updateCellLayer();
    }



    private EventHandler<MouseEvent> onMousePressedToCollapseTree = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            CircleCell cell = (CircleCell) event.getSource();
            collapseThisTree(cell);
        }
    };



    private double addAndUpdate(String cellId, double yMin) {
        double height = 0;
        addLaterCircle.clear();
        addLaterEdge.clear();
        try {
            Statement statement = DatabaseUtil.getConnection().createStatement();
            height = recursivelyAdd(cellId, statement, yMin);
            // System.out.println("Total children height: " + height);
            statement.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return height;
    }

    private double recursivelyAdd(String cellId, Statement statement, double upperSiblingHeight) {
        double height = 0;

        System.out.println();
        System.out.println("EventHandlers::recursivelyAdd ");
        System.out.println("at cell: " + cellId);
        System.out.println("received upperSiblingHeight:  " + upperSiblingHeight);

        // Get element row from Element table.
        try (ResultSet elementRS = ElementDAOImpl.selectWhere("id = " + cellId)) {

            if (elementRS.next()) {
                int collapsed = elementRS.getInt("collapsed");
                if (collapsed == 0) {
                    throw new IllegalStateException("Collapsed cannot be 0 here.");
                } else if (collapsed == 1) {
                    System.out.println("Collapsed: 1");
                    // ElementDAOImpl.updateWhere("collapsed", "0", "id = " + cellId);
                    statement.addBatch("UPDATE " + ELEMENT_TABLE + " SET COLLAPSED = 0 WHERE id = " + cellId);

                    float xCoordinateTemp = elementRS.getFloat("bound_box_x_coordinate");
                    float yCoordinateTemp = elementRS.getFloat("bound_box_y_coordinate");
                    // CircleCell cell = new CircleCell(cellId, xCoordinateTemp, yCoordinateTemp);

                    CircleCell cell = new CircleCell(cellId, xCoordinateTemp, (float)upperSiblingHeight);
                    cell.setMethodName(getMethodNameFromDB(cellId));

                    addLaterCircle.add(cell);
                    System.out.println("Added cell to addLaterCircle: " + cellId);
                    System.out.println("At: " + upperSiblingHeight);
                    // graph.getModel().addCell(cell);


                    // Add edge
                    try (ResultSet parentRS = ElementToChildDAOImpl.selectWhere("child_id = " + cellId)) {
                        if (parentRS.next()) {
                            String parentId = String.valueOf(parentRS.getInt("parent_id"));
                            CircleCell parentCell = graph.getModel().getCircleCellsOnUI().get(parentId);
                            if (parentCell == null) {
                                for (CircleCell circleCell : addLaterCircle) {
                                    if (circleCell.getCellId().equalsIgnoreCase(parentId)) {
                                        parentCell = circleCell;
                                    }
                                }
                            }
                            Edge edge = new Edge(parentCell, cell);

                            // EdgeDAOImpl.updateWhere("collapsed", "0","fk_target_element_id = " + cellId);
                            statement.addBatch("UPDATE " + EDGE_TABLE + " SET COLLAPSED = 0 WHERE fk_target_element_id = " + cellId);
                            addLaterEdge.add(edge);
                            // graph.getModel().addEdge(edge);
                        }
                    }
                    // graph.myEndUpdate();
                    // graph.updateCellLayer();

                    try (ResultSet childrenRS = ElementToChildDAOImpl.selectWhere("parent_id = " + cellId)) {
                        boolean empty = false;
                        while (childrenRS.next()) {
                            empty = true;
                            String childId = String.valueOf(childrenRS.getInt("child_id"));
                            // upperSiblingHeight += height * BoundBox.unitHeightFactor;
                            System.out.println("passing on for cellId: " + childId + " upperSiblingHeight: " + (upperSiblingHeight + (height * BoundBox.unitHeightFactor)));
                            System.out.println("passing on to next recursion height: " + height);

                            height += recursivelyAdd(childId, statement, upperSiblingHeight + (height * BoundBox.unitHeightFactor));
                        }

                        if (!empty) {
                            height = 1;
                        }
                    }

                } else if (collapsed == 2) {
                    // Here only for the one time during the circle click.

                    System.out.println("Collapsed 2");

                    // update collapsed=0
                    // ElementDAOImpl.updateWhere("collapsed", "0", "id = " + cellId);
                    statement.addBatch("UPDATE " + ELEMENT_TABLE + " SET COLLAPSED = 0 WHERE id = " + cellId);

                    // for all children with collapsed=1, show and update collapsed=0
                    try (ResultSet childrenRS = ElementToChildDAOImpl.selectWhere("parent_id = " + cellId)) {
                        while (childrenRS.next()) {
                            String childId = String.valueOf(childrenRS.getInt("child_id"));
                            // upperSiblingHeight += height * BoundBox.unitHeightFactor;
                            System.out.println("passing on for cellId: " + childId + " upperSiblingHeight: " + (upperSiblingHeight + (height * BoundBox.unitHeightFactor)));
                            System.out.println("passing on to next recursion height: " + height);

                            height += recursivelyAdd(
                                    childId, statement,
                                    upperSiblingHeight + (height * BoundBox.unitHeightFactor)
                            );

                        }
                    }

                } else if (collapsed == 3) {
                    System.out.println("Collapsed 3");

                    // ElementDAOImpl.updateWhere("collapsed", "2", "id = " + cellId);
                    statement.addBatch("UPDATE " + ELEMENT_TABLE + " SET COLLAPSED = 2 WHERE id = " + cellId);

                    float xCoordinateTemp = elementRS.getFloat("bound_box_x_coordinate");
                    float yCoordinateTemp = elementRS.getFloat("bound_box_y_coordinate");
                    // CircleCell cell = new CircleCell(cellId, xCoordinateTemp, yCoordinateTemp);
                    CircleCell cell = new CircleCell(cellId, xCoordinateTemp, (float)upperSiblingHeight);
                    cell.setMethodName(getMethodNameFromDB(cellId));
                    addLaterCircle.add(cell);

                    System.out.println("Added cell to addLaterCircle: " + cellId);
                    // graph.getModel().addCell(cell);

                    try (ResultSet parentRS = ElementToChildDAOImpl.selectWhere("child_id = " + cellId)) {
                        if (parentRS.next()) {
                            String parentId = String.valueOf(parentRS.getInt("parent_id"));
                            CircleCell parentCell = graph.getModel().getCircleCellsOnUI().get(parentId);
                            if (parentCell == null) {
                                for (CircleCell circleCell : addLaterCircle) {
                                    if (circleCell.getCellId().equalsIgnoreCase(parentId)) {
                                        parentCell = circleCell;
                                    }
                                }
                            }
                            Edge edge = new Edge(parentCell, cell);
                            // graph.getModel().addEdge(edge);
                            addLaterEdge.add(edge);

                            // EdgeDAOImpl.updateWhere("collapsed", "0", "fk_target_element_id = " + cellId);
                            statement.addBatch("UPDATE " + EDGE_TABLE + " SET COLLAPSED = 0 WHERE fk_target_element_id = " + cellId);

                        }
                    }

                    // get height of cell.
                    // subtract from deltamap ymax.
                    // update deltas
                    double cellYMin =  elementRS.getInt("BOUND_BOX_Y_TOP_LEFT");
                    double cellYMax =  elementRS.getInt("BOUND_BOX_Y_BOTTOM_LEFT");
                    double fullCellHeight = cellYMax - cellYMin;
                    // DeltaMap.yMax -= fullCellHeight - BoundBox.unitHeightFactor;
                    // DeltaMap.upperDelta -= fullCellHeight/2;
                    // DeltaMap.lowerDelta -= fullCellHeight - BoundBox.unitHeightFactor;

                    System.out.println("3 -> 2 : fullCellHeight: " + fullCellHeight);
                    // System.out.println("yMax: " + DeltaMap.yMax);


                    height = 1;

                    // graph.myEndUpdate();
                    // graph.updateCellLayer();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Exiting recursivelyAdd for : " + cellId);
        return height;
    }

    private void removeAndUpdate(String cellId, List<String> removeCircleCells, List<String> removeEdges ) {
        try {
            Statement statement = DatabaseUtil.getConnection().createStatement();
            recursivelyRemove(cellId, removeCircleCells, removeEdges, statement);
            statement.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void recursivelyRemove(String cellId, List<String> removeCircleCells, List<String> removeEdges, Statement statement) {
        // try (ResultSet childrenRS = ElementToChildDAOImpl.selectWhere("parent_id = " + cellId)) {
        String sql = "SELECT * " +
                "FROM " + ELEMENT_TABLE + " AS E JOIN " + ELEMENT_TO_CHILD_TABLE + " AS EC ON E.ID = EC.CHILD_ID " +
                "WHERE EC.PARENT_ID = " + cellId;

        try (ResultSet childrenRS = DatabaseUtil.select(sql)) {
            try {
                while (childrenRS.next()) {
                    String childId = String.valueOf(childrenRS.getInt("child_id"));
                    int collapsed = childrenRS.getInt("COLLAPSED");
                    removeCircleCells.add(childId);
                    removeEdges.add(childId);

                    statement.addBatch("UPDATE " + ELEMENT_TABLE + " SET COLLAPSED = 1 WHERE id = " + childId + " AND collapsed = 0");
                    // ElementDAOImpl.updateWhere("collapsed", "1",
                    //         "id = " + childId + " AND collapsed = 0");

                    statement.addBatch("UPDATE " + ELEMENT_TABLE + " SET COLLAPSED = 3 WHERE id = " + childId + " AND collapsed = 2");
                    // ElementDAOImpl.updateWhere("collapsed", "3",
                    //         "id = " + childId + " AND collapsed = 2");

                    statement.addBatch("UPDATE " + TableNames.EDGE_TABLE + " SET COLLAPSED = 1 WHERE fk_target_element_id = " + childId);
                    // EdgeDAOImpl.updateWhere("collapsed", "1",
                    //         "fk_target_element_id = " + childId);

                    if (collapsed == 2) {

                        // get height of cell.
                        // subtract from deltamap ymax.
                        // update deltas
                        double cellYMin =  childrenRS.getInt("BOUND_BOX_Y_TOP_LEFT");
                        double cellYMax =  childrenRS.getInt("BOUND_BOX_Y_BOTTOM_LEFT");
                        double fullCellHeight = cellYMax - cellYMin;
                        DeltaMap.yMax -= (fullCellHeight - BoundBox.unitHeightFactor);
                        // double temp = fullCellHeight/2 - BoundBox.unitHeightFactor / 2;
                        // temp = temp < BoundBox.unitHeightFactor? 0 : temp;
                        // DeltaMap.upperDelta -= temp;
                        DeltaMap.lowerDelta -= (fullCellHeight - BoundBox.unitHeightFactor);

                        System.out.println("2 -> 3 : fullCellHeight: " + + fullCellHeight);
                        // System.out.println("yMin: " + DeltaMap.yMin + " : upperDelta: " + DeltaMap.upperDelta);
                        System.out.println("yMax: " + DeltaMap.yMax + " : upperDelta: " + DeltaMap.lowerDelta);
                    }

                    recursivelyRemove(childId, removeCircleCells, removeEdges, statement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException ignored) {}
    }




    private String getMethodNameFromDB(String cellId) {
        String methodName = "";
        try {
            ResultSet rs = DatabaseUtil.select("SELECT METHOD_NAME " +
                    "FROM " + METHOD_DEFINITION_TABLE + " AS MD " +
                    "WHERE ID = (SELECT METHOD_ID " +
                    "            FROM " + CALL_TRACE_TABLE + " AS CT " +
                    "            WHERE CT.ID = (SELECT ID_ENTER_CALL_TRACE " +
                    "                           FROM " + ELEMENT_TABLE + " AS E " +
                    "                           WHERE E.ID = " + cellId + "))");
            if (rs.next()) {
                methodName = rs.getString("METHOD_NAME");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return methodName;
    }

    @SuppressWarnings("unused")
    EventHandler<MouseEvent> onMouseExitToDismissPopover = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (popOver != null)
                popOver.hide();
        }
    };


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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    EventHandler<MouseEvent> onMouseReleasedEventHandler = event -> {};

    class DragContext {
        double x;
        double y;
    }

    public static void saveRef(ConvertDBtoElementTree c) {
        convertDBtoElementTree = c;
    }
    public static void saveRef(Main m) {
        main = m;
    }
}