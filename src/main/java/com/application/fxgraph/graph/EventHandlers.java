package com.application.fxgraph.graph;

import com.application.fxgraph.ElementHelpers.ConvertDBtoElementTree;
import com.application.Main;
import com.application.db.DAOImplementation.*;
import com.application.db.DatabaseUtil;
import com.application.db.TableNames;
import com.application.fxgraph.ElementHelpers.Element;
import com.application.fxgraph.cells.CircleCell;
import com.sun.xml.internal.bind.v2.model.core.ID;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.PopOver;
import org.omg.CORBA.INTERNAL;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.application.fxgraph.graph.Graph.cellLayer;

public class EventHandlers {

    private static ConvertDBtoElementTree convertDBtoElementTree;
    final DragContext dragContext = new DragContext();
    static Map<String, Double> deltaCache = new HashMap<>();
    private boolean clickable = true;
    private boolean subtreeExpanded = true;
    private boolean posUpdated = true;


    Graph graph;
    static Main main;

    public EventHandlers(Graph graph) {
        this.graph = graph;
    }

    public static void resetEventHandlers() {
        deltaCache = new HashMap<>();
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
                    "JOIN " + TableNames.CALL_TRACE_TABLE + " ON " + TableNames.CALL_TRACE_TABLE + ".id = " + TableNames.ELEMENT_TABLE + ".ID_ENTER_CALL_TRACE " +
                    "WHERE " + TableNames.ELEMENT_TABLE + ".ID = " + cell.getCellId();

            try (ResultSet callTraceRS = DatabaseUtil.select(sql)) {
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

                        if (methodId == 0) {
                            methodName = eventType;
                            packageName = "N/A";
                            parameterTypes = "N/A";
                            parameters = "N/A";
                        }
                    } catch (SQLException e) {
                    }

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
                    eleIdList.stream().forEach(elementId -> {
                        String query = "SELECT E.ID AS EID, bound_box_x_coordinate, bound_box_y_coordinate, THREAD_ID " +
                                "FROM CALL_TRACE AS CT " +
                                "JOIN ELEMENT AS E ON CT.ID = E.ID_ENTER_CALL_TRACE " +
                                "WHERE E.ID = " + elementId;
                        try (ResultSet elementRS = DatabaseUtil.select(query)) {
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
                            message = "Wait method was invoked and therefore, \nthe lock on object ( object id = " + lockObjectId + ") \nwas released and reaquired here.";
                            actionMsg = "Go to Notify or NotifyAll \nmethods invocations.";
                            break;

                        case "NOTIFY-ENTER":
                            message = "Notify method was invoked and therefore, \nthe lock on object ( object id = " + lockObjectId + ") \nwas released here.";
                            actionMsg = "Go to wait \nmethods invocations.";
                            break;
                        case "NOTIFYALL-ENTER":
                            message = "NotifyAll method was invoked and therefore, \nthe lock on object ( object id = " + lockObjectId + ") \nwas released here.";
                            actionMsg = "Go to wait \nmethods invocations.";
                            break;
                    }
                    Label labelMessage = new Label(message);
                    labelMessage.setWrapText(true);

                    Label labelActionMsg = new Label(actionMsg);

                    gridPane.add(labelMessage, 0, 7);
                    gridPane.add(labelActionMsg, 0, 8);
                    int rowIndex = 8;
                    for (Button button : buttonList) {
                        button.setText("Goto node");
                        gridPane.add(button, 1, rowIndex++);
                    }

                    // For debugging.

                    Button minMaxButton = new Button("min / max");
                    minMaxButton.setOnMouseClicked(event1 -> {
                                invokeOnMousePressedEventHandler(cell);
                            }
                    );

                    gridPane.add(minMaxButton, 1, rowIndex++);

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


    @SuppressWarnings("Duplicates")
    private void invokeOnMousePressedEventHandler(CircleCell cell) {

    }


    private EventHandler<MouseEvent> onMousePressedToCollapseTree = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {

            if (!clickable) {
                System.out.println(">>>>>>>>>>>>>>>>>>> Clickable is false. <<<<<<<<<<<<<<<<<<<<<");
                return;
            }

            // posUpdated = false;
            // System.out.println("on click: set posUpdated: " + posUpdated);
            // subtreeExpanded = false;
            // System.out.println("on click: set subtreeExpanded: " + subtreeExpanded);
            clickable = false;
            // System.out.println("on click: set clickable: " + clickable);

            CellLayer cellLayer = (CellLayer) graph.getCellLayer();
            CircleCell clickedCell = (CircleCell) event.getSource();
            String clickedCellID = clickedCell.getCellId();
            int collapsed = 0;
            double clickedCellTopLeftY = 0;
            double clickedCellTopLeftX = 0;
            double clickedCellTopRightX = 0;
            double clickedCellBoundBottomLeftY = 0;
            double newDelta = 0;

            try (ResultSet cellRS = ElementDAOImpl.selectWhere("id = " + clickedCellID)) {
                if (cellRS.next()) {
                    collapsed = cellRS.getInt("collapsed");
                    clickedCellTopLeftY = cellRS.getDouble("bound_box_y_top_left");
                    clickedCellTopLeftX = cellRS.getDouble("bound_box_x_top_left");
                    clickedCellTopRightX = cellRS.getDouble("bound_box_x_top_right");
                    clickedCellBoundBottomLeftY = cellRS.getDouble("bound_box_y_bottom_left");
                    newDelta = cellRS.getDouble("delta");
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
            if (collapsed == 1 || collapsed >= 3) {
                // expand sub tree.
                System.out.println("onMousePressedToCollapseTree: cell: " + clickedCellID + " ; collapsed: " + collapsed);
            } else if (collapsed == 0) {
                // MINIMIZE SUBTREE

                // System.out.println(">>>> clicked on a collapsed = 0  cell.");
                ((Circle) clickedCell.getChildren().get(0)).setFill(Color.BLUE);

                // ((Circle) ( (Group)cell.getView() )
                //             .getChildren().get(0))
                //             .setFill(Color.BLUE);
                // cell.getChildren().get(0).setStyle("-fx-background-color: blue");
                // cell.setStyle("-fx-background-color: blue");
                clickedCell.setLabel("+");
                main.setStatus("Please wait ......");

                subtreeExpanded = true;
                System.out.println("====== Minimize cellId: " + clickedCellID + " ------ ");

                // ElementDAOImpl.updateWhere("collapsed", "2", "id = " + clickedCellID);


                Statement statement = DatabaseUtil.createStatement();

                // delta = clickedCellBoundBottomLeftY - clickedCellTopLeftY - BoundBox.unitHeightFactor;
                newDelta = clickedCellBoundBottomLeftY - clickedCellTopLeftY - BoundBox.unitHeightFactor;
                double clickedCellBottomY = clickedCellTopLeftY + BoundBox.unitHeightFactor;

                // deltaCache.put(clickedCellID, delta);

                String updateClickedElement = "UPDATE " + TableNames.ELEMENT_TABLE + " " +
                        "SET COLLAPSED = 2, DELTA = " + newDelta + " " +
                        "WHERE ID = " + clickedCellID;
                DatabaseUtil.executeUpdate(updateClickedElement);

                int nextCellId = getNextLowerSiblingOrAncestorNode(Integer.parseInt(clickedCellID), clickedCellTopLeftX, clickedCellTopLeftY);


                removeChildrenFromUI(Integer.parseInt(clickedCellID), nextCellId);
                moveLowerTreeByDelta(clickedCellID, clickedCellBottomY, newDelta);
                updateDBInBackgroundThread(Integer.parseInt(clickedCellID), clickedCellTopLeftY, clickedCellBoundBottomLeftY, clickedCellTopLeftX, clickedCellTopRightX, newDelta, 1, nextCellId);

            } else if (collapsed == 2) {
                // MAXIMIZE SUBTREE

                ((Circle) clickedCell.getChildren().get(0)).setFill(Color.RED);
                // ( (Circle) ( (Group)cell.getView() ).getChildren().get(0) ).setFill(Color.RED);
                clickedCell.setLabel("-");
                main.setStatus("Please wait ......");
                System.out.println("====== Maximize cellId: " + clickedCellID + " ++++++ ");

                // double delta = deltaCache.get(clickedCellID);

                int nextCellId = getNextLowerSiblingOrAncestorNode(Integer.parseInt(clickedCellID), clickedCellTopLeftX, clickedCellTopLeftY);


                double clickedCellBottomY = clickedCellTopLeftY + BoundBox.unitHeightFactor;
                double newClickedCellBottomY = clickedCellTopLeftY + BoundBox.unitHeightFactor + newDelta;

                moveLowerTreeByDelta(clickedCellID, clickedCellBottomY, -newDelta);

                ElementDAOImpl.updateWhere("collapsed", "0", "id = " + clickedCellID);
                updateDBInBackgroundThread(Integer.parseInt(clickedCellID), clickedCellTopLeftY, clickedCellBoundBottomLeftY, clickedCellTopLeftX, clickedCellTopRightX, -newDelta, 0, nextCellId);

            }
        }
    };

    private int getNextLowerSiblingOrAncestorNode(int clickedCellId, double x, double y) {
        String getNextQuery = "SELECT " +
                "CASE " +
                        "WHEN MIN(ID) IS NULL THEN " + Integer.MAX_VALUE + " " +
                        "ELSE MIN(ID) " +
                "END " +
                "AS MINID " +
                "FROM " + TableNames.ELEMENT_TABLE + " " +
                "WHERE BOUND_BOX_Y_TOP_LEFT > " + y + " " +
                "AND BOUND_BOX_X_TOP_LEFT <= " + x + " " +
                "AND ID > " + clickedCellId;

        // System.out.println("EventHandler::getNextLowerSiblingOrAncestorNode: query: " + getNextQuery);


        try (ResultSet rs = DatabaseUtil.select(getNextQuery)) {
            if (rs.next()) {
                // System.out.println("EventHandler::getNextLowerSiblingOrAncestorNode: we have result: " + rs.getInt("MINID"));
                return rs.getInt("MINID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // System.out.println("EventHandler::getNextLowerSiblingOrAncestorNode: we dont hav res" + Integer.MAX_VALUE);
        return Integer.MAX_VALUE;
    }

    private void setClickable() {
        // if (posUpdated && subtreeExpanded) {
        clickable = true;
        main.setStatus("Done");
        // }
    }


    public void removeChildrenFromUI(int cellId, int endCellId) {
        // System.out.println("EventHandler::removeChildrenFromUI: method stated. start cellid: " + cellId + " end cellid: " + endCellId);
        Map<String, CircleCell> mapCircleCellsOnUI = graph.getModel().getCircleCellsOnUI();
        List<String> removeCircleCells = new ArrayList<>();

        Map<String, Edge> mapEdgesOnUI = graph.getModel().getEdgesOnUI();
        List<String> removeEdges = new ArrayList<>();

        Map<Integer, RectangleCell> highlightsOnUi = graph.getModel().getHighlightsOnUI();
        List<Integer> removeHighlights = new ArrayList<>();

        mapCircleCellsOnUI.forEach((id, circleCell) -> {
            int intId = Integer.parseInt(id);
            if (intId > cellId && intId < endCellId) {
                removeCircleCells.add(id);
            }
        });

        mapEdgesOnUI.forEach((id, edge) -> {
            int intId = Integer.parseInt(id);
            if (intId > cellId && intId < endCellId) {
                removeEdges.add(id);
            }
        });

        highlightsOnUi.forEach((id, rectangle) -> {
            // System.out.println("EventHandler::removeChildrenFromUI: foreach in highlightsOnUi: id: " + id);
            int elementId = rectangle.getElementId();
            if (elementId> cellId && elementId < endCellId) {
                // System.out.println("EventHandler::removeChildrenFromUI: adding to removeHighlights, elementId: " + elementId);
                removeHighlights.add(id);
            }
        });

        removeCircleCells.forEach((id) -> {
            if (mapCircleCellsOnUI.containsKey(id)) {
                CircleCell cell = mapCircleCellsOnUI.get(id);
                cellLayer.getChildren().remove(cell);
                mapCircleCellsOnUI.remove(id);
            }
        });

        removeEdges.forEach((id) -> {
            Edge edge = mapEdgesOnUI.get(id);
            if (edge != null) {
                mapEdgesOnUI.remove(id);
                cellLayer.getChildren().remove(edge);
            }
        });

        removeHighlights.forEach((id) -> {
            if (highlightsOnUi.containsKey(id)) {
                RectangleCell rectangleCell = highlightsOnUi.get(id);
                int elementId = highlightsOnUi.get(id).getElementId();
                // System.out.println("EventHandler::removeChildrenFromUI: removing from highlightsOnUi and cellLayer: " + id + " ElementId: " + elementId);
                highlightsOnUi.remove(id);
                cellLayer.getChildren().remove(rectangleCell);
            }
        });

        // System.out.println("EventHandler::removeChildrenFromUI: method ended.");

        // System.out.println("EventHandler::removeChildrenFromUI: clicked on cell id: " + cellId);
        // try (ResultSet rs = ElementDAOImpl.selectWhere("id = " + cellId)) {
        //     if (rs.next()) {
        //         float clickedCellTopRightX = rs.getFloat("bound_box_x_top_right");
        //         float clickedCellTopY = rs.getFloat("bound_box_y_top_left");
        //         float leafCount = rs.getInt("leaf_count");
        //         float clickedCellHeight = leafCount * BoundBox.unitHeightFactor;
        //         float clickedCellBottomY = clickedCellTopY + BoundBox.unitHeightFactor;
        //         float clickedCellBoundBottomY = rs.getFloat("bound_box_y_bottom_left");
        //
        //         // System.out.println("EventHandler::removeChildrenFromUI: clickedCellTopY Top: " + clickedCellTopY + " ; clickedCellTopY Bottom: " + (clickedCellTopY + clickedCellHeight));
        //
        //         // Remove all children cells and edges that end at these cells from UI
        //         mapCircleCellsOnUI.forEach((id, circleCell) -> {
        //             double thisCellTopLeftX = circleCell.getLayoutX();
        //             double thisCellTopY = circleCell.getLayoutY();
        //
        //             if (thisCellTopY >= clickedCellBottomY && thisCellTopY < clickedCellBoundBottomY && thisCellTopLeftX > clickedCellTopRightX) {
        //                 // if (thisCellTopY >= clickedCellTopY ) {
        //                 // System.out.println("adding to remove list: cellId: " + id + " cell: " + circleCell);
        //                 removeCircleCells.add(id);
        //                 removeEdges.add(id);
        //             } else if (thisCellTopY == clickedCellTopY && thisCellTopLeftX >= clickedCellTopRightX) {
        //                 // System.out.println("adding to remove list: cellId: " + id + " cell: " + circleCell);
        //                 removeCircleCells.add(id);
        //                 removeEdges.add(id);
        //             }
        //         });
        //
        //         // Get edges which don't have an target cicle rendered on UI.
        //         mapEdgesOnUI.forEach((id, edge) -> {
        //             double thisLineEndY = edge.line.getEndY();
        //             double thisLineStartY = edge.line.getStartY();
        //             double thisLineStartX = edge.line.getStartX();
        //             if (thisLineEndY >= clickedCellTopY && thisLineEndY <= clickedCellBoundBottomY && thisLineStartY >= clickedCellTopY && thisLineStartX >= (clickedCellTopRightX-BoundBox.unitWidthFactor)) {
        //                 System.out.println("adding to remove list: edge ID-: " + id);
        //                 removeEdges.add(id);
        //             }
        //         });
        //
        //         removeCircleCells.forEach((id) -> {
        //             if (mapCircleCellsOnUI.containsKey(id)) {
        //                 CircleCell cell = mapCircleCellsOnUI.get(id);
        //                 cellLayer.getChildren().remove(cell);
        //                 mapCircleCellsOnUI.remove(id);
        //             }
        //         });
        //
        //         removeEdges.forEach((id) -> {
        //             Edge edge = mapEdgesOnUI.get(id);
        //             if (edge != null) {
        //                 // System.out.println("removing edge: edgeId: " + id + "; edge target id: " + edge.getEdgeId());
        //                 mapEdgesOnUI.remove(id);
        //                 cellLayer.getChildren().remove(edge);
        //             }
        //         });
        //
        //
        //         // System.out.println("Contents of mapEdgesOnUI after removing");
        //         // mapEdgesOnUI.forEach((s, edge) -> {
        //         //     System.out.println("edgeID: "  + s);
        //         // });
        //         //
        //         // System.out.println("Contents of mapCircleCellsOnUI after removing");
        //         // mapCircleCellsOnUI.forEach((s, edge) -> {
        //         //     System.out.println("CellID: "  + s);
        //         // });
        //     }
        // } catch (SQLException e) {
        //     e.printStackTrace();
        // }
    }


    private void moveLowerTreeByDelta(String clickedCellID, double clickedCellBottomY, double delta) {
        // System.out.println("EventHandler::moveLowerTreeByDelta: starting method");
        // System.out.println("EventHandler::moveLowerTreeByDelta: input: clickedCellId: " + clickedCellID + " , clickedCellBottomY: " + clickedCellBottomY + " , delta: " + delta);

        Map<String, CircleCell> mapCircleCellsOnUI = graph.getModel().getCircleCellsOnUI();
        Map<String, Edge> mapEdgesOnUI = graph.getModel().getEdgesOnUI();
        Map<Integer, RectangleCell> mapHighlightsOnUI = graph.getModel().getHighlightsOnUI();


        // For each circle cell on UI that is below the clicked cell, move up by delta
        mapCircleCellsOnUI.forEach((thisCellID, thisCircleCell) -> {
            double thisCellTopY = thisCircleCell.getLayoutY();

            if (thisCellTopY >= clickedCellBottomY) {
                thisCircleCell.relocate(thisCircleCell.getLayoutX(), thisCellTopY - delta);
                // System.out.println(" moved clickedCellID: " + thisCellID + " to y: " + (thisCellTopY - delta));
            }

        });


        // For each edge on UI whose endY or startY is below the clicked cell, relocate that edge appropriately
        mapEdgesOnUI.forEach((id, edge) -> {
            double thisEdgeEndY = edge.line.getEndY();
            double thisEdgeStartY = edge.line.getStartY();

            if (thisEdgeEndY >= clickedCellBottomY) {
                edge.line.setEndY(thisEdgeEndY - delta);
                // System.out.println(" moved edgeId: " + id + " to endY: " + edge.line.getEndY());

            }

            if (thisEdgeStartY >= clickedCellBottomY) {
                edge.line.setStartY(thisEdgeStartY - delta);
                // System.out.println(" moved edgeId: " + id + " to endY: " + edge.line.getStartY());
            }
        });


        // For each highlight rectangle whose startY is below the clicked cell, relocate that rectangle appropriately
        mapHighlightsOnUI.forEach((id, rectangleCell) -> {
            double y = rectangleCell.getLayoutY();
            // double rectLayoutY = rectangleCell.getChildren().get(0).getLayoutY();
            // double rectLayoutY2 = rectangleCell.getRectangle().getLayoutY();
            // double rectY2 = rectangleCell.getRectangle().getY();
            // int elementId = rectangleCell.getElementId();
            // System.out.println("EventHandler::moveLowerTreeByDelta: looking at rect id=" + id + " elementId=" + elementId + " y=" + y + " rectLayoutY=" + rectLayoutY +" rectLayoutY2="+rectLayoutY2+" rectY2="+rectY2);
            if (y >= clickedCellBottomY - BoundBox.unitHeightFactor/2) {
                // System.out.println("moving it up");
                rectangleCell.relocate(rectangleCell.getLayoutX(), y - delta);
            }
        });

        // System.out.println("EventHandler::moveLowerTreeByDelta: ending method");
    }


    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private void updateDBInBackgroundThread(int clickedCellId, double topY, double bottomY, double leftX, double rightX, double delta, int min, int nextCellId) {

        Statement statement = DatabaseUtil.createStatement();
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // System.out.println("==================== Starting thread updateDBInBackgroundThread. ====================");
                // int nextCellId = getNextLowerSiblingOrAncestorNode(clickedCellId, leftX, topY);

                if (min == 1) {
                    // on minimization
                    // int endCellId = nextCellId == 0 ? Integer.MAX_VALUE : nextCellId;
                    updateCollapseValForSubTreeBulk(topY, bottomY, rightX, statement, true, clickedCellId, nextCellId);
                } else {
                    // on maximation
                    // int endCellId = nextCellId == 0 ? Integer.MAX_VALUE : nextCellId;
                    updateCollapseValForSubTreeBulk(topY, bottomY, rightX, statement, false, clickedCellId, nextCellId);
                    // expandSubtreeAndUpdateColValsRecursive(String.valueOf(clickedCellId));
                }

                // No upate required for single line children
                if (delta == 0) {
                    // System.out.println("Optimized for singel line children collapses.");
                    statement.executeBatch();
                    Platform.runLater(() -> convertDBtoElementTree.forceUiRendering(graph));
                    return null;
                }

                updateParentChainRecursive(clickedCellId, delta, statement);
                // updateAllParentHighlights(topY, delta, statement);

                statement.executeBatch();

                if (nextCellId != Integer.MAX_VALUE) {
                    // only if next lower sibling ancestor node is present.
                    updateTreeBelowYBulk(topY + BoundBox.unitHeightFactor, delta, statement, nextCellId);
                    statement.executeBatch();
                }

                Platform.runLater(() -> convertDBtoElementTree.forceUiRendering(graph));
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                setClickable();
                // System.out.println("==================== updateDBInBackgroundThread Successful. ====================");
            }

            @Override
            protected void failed() {
                super.failed();
                try {
                    throw new Exception(">>>>>>>>>>>>>>>>>>>>>>> updateDBInBackgroundThread failed. <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        task.setOnFailed(event -> task.getException().printStackTrace());

        new Thread(task).start();
    }

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private void  updateCollapseValForSubTreeBulk(double topY, double bottomY, double leftX, Statement statement, boolean isMinimized, int startCellId, int endCellId) {
        // System.out.println("EventHandler::updateCollapseValForSubTreeBulk: method started");
        String updateCellQuery;
        String updateEdgeQuery;
        String updateEdgeQuery2;
        String updateHighlightsQuery;

        if (isMinimized) {
            // Update the collapse value in the subtree rooted at the clicked cell.
            updateCellQuery = "UPDATE " + TableNames.ELEMENT_TABLE + " " +
                    "SET COLLAPSED = " +
                    "CASE " +
                    "WHEN COLLAPSED <= 0 THEN COLLAPSED - 1 " +
                    "WHEN COLLAPSED >= 2 THEN COLLAPSED + 1 " +
                    "ELSE COLLAPSED " +
                    "END " +
                    "WHERE " +
                    // "(bound_box_y_coordinate >= " + topY + " " +
                    // "AND bound_box_y_coordinate < " + bottomY + " " +
                    // "AND bound_box_x_coordinate >= " + leftX + ") " +
                    // "AND " +
                    "ID > " + startCellId + " " +
                    "AND ID < " + endCellId;

            // System.out.println("updateCollapseValForSubTreeBulk for minimize:cell query: " + updateCellQuery);

            // Update the collapse value in the subtree rooted at the clicked cell.
            updateEdgeQuery = "UPDATE " + TableNames.EDGE_TABLE + " " +
                    "SET COLLAPSED = " +
                    "CASE " +
                    "WHEN COLLAPSED = " + CollapseType.EDGE_VISIBLE + " THEN " + CollapseType.EDGE_NOTVISIBLE + " " +
                    // "WHEN COLLAPSED = " + CollapseType.EDGE_NOTVISIBLE + " THEN " + CollapseType.EDGE_PARENT_NOTVISIBLE + " " +
                    "ELSE COLLAPSED " +
                    "END " +
                    "WHERE " +
                    // "END_Y >= " + topY + " " +
                    // "AND END_Y <= " + bottomY + " " +
                    // "AND END_X >= " + leftX + " " +
                    // "AND " +
                    "FK_TARGET_ELEMENT_ID > " + startCellId + " " +
                    "AND FK_TARGET_ELEMENT_ID < " + endCellId;


            updateEdgeQuery2 = "UPDATE " + TableNames.EDGE_TABLE + " " +
                    "SET COLLAPSED = " +
                        "CASE " +
                            "WHEN COLLAPSED = 0 THEN 1 " +
                            "ELSE COLLAPSED " +
                        "END " +
                    "WHERE FK_TARGET_ELEMENT_ID IN " +
                    "(SELECT ELE.ID FROM " + TableNames.ELEMENT_TABLE + " AS ELE JOIN " + TableNames.EDGE_TABLE + " as EDGE " +
                        "ON ELE.ID = EDGE.FK_TARGET_ELEMENT_ID " +
                        "WHERE EDGE.FK_TARGET_ELEMENT_ID > " +  startCellId + " " +
                        "AND EDGE.FK_TARGET_ELEMENT_ID < " + endCellId + " " +
                        // "AND ELE.COLLAPSED >= 0 " +
                        // "AND ELE.COLLAPSED <= 2" +
                    ")";

            // System.out.println("updateCollapseValForSubTreeBulk for minimize: edge query: " + updateEdgeQuery2);


            updateHighlightsQuery = "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " " +
                    "SET COLLAPSED = 1 " +
                    "WHERE ELEMENT_ID > " + startCellId + " " +
                    "AND ELEMENT_ID < " + endCellId;

            // System.out.println("EventHandler::updateCollapseValForSubTreeBulk: updateHighlightsQuery for collapse: " + updateHighlightsQuery);


        } else {
            updateCellQuery = "UPDATE " + TableNames.ELEMENT_TABLE + " " +
                    "SET COLLAPSED = " +
                    "CASE " +
                    "WHEN COLLAPSED < 0 THEN COLLAPSED + 1 " +
                    "WHEN COLLAPSED > 2 THEN COLLAPSED - 1 " +
                    "ELSE COLLAPSED " +
                    "END " +
                    "WHERE " +
                    // "bound_box_y_coordinate >= " + topY + " " +
                    // "AND bound_box_y_coordinate < " + bottomY + " " +
                    // "AND bound_box_x_coordinate >= " + leftX + " " +
                    // "AND " +
                    "ID > " + startCellId + " " +
                    "AND ID < " + endCellId + " ";

            // System.out.println("updateCollapseValForSubTreeBulk for mazimize: cell query: " + updateCellQuery);

            // Update the collapse value in the subtree rooted at the clicked cell.
            updateEdgeQuery = "UPDATE " + TableNames.EDGE_TABLE + " " +
                    "SET COLLAPSED = " +
                    "CASE " +
                    "WHEN COLLAPSED = " + CollapseType.EDGE_NOTVISIBLE + " THEN " + CollapseType.EDGE_VISIBLE + " " +
                    // "WHEN COLLAPSED = " + CollapseType.EDGE_PARENT_NOTVISIBLE + " THEN " + CollapseType.EDGE_NOTVISIBLE + " " +
                    "ELSE COLLAPSED " +
                    "END " +
                    "WHERE " +
                    // "END_Y >= " + topY + " " +
                    // "AND END_Y <= " + bottomY + " " +
                    // "AND END_X >= " + leftX + " " +
                    // "AND " +
                    "FK_TARGET_ELEMENT_ID > " + startCellId + " " +
                    "AND FK_TARGET_ELEMENT_ID < " + endCellId + " ";


            updateEdgeQuery2 = "UPDATE " + TableNames.EDGE_TABLE + " " +
                    "SET COLLAPSED = " +
                        "CASE " +
                            "WHEN COLLAPSED = 1 THEN 0 " +
                            "ELSE COLLAPSED " +
                        "END " +
                    "WHERE FK_TARGET_ELEMENT_ID IN " +
                    "(SELECT ELE.ID FROM " + TableNames.ELEMENT_TABLE + " AS ELE JOIN " + TableNames.EDGE_TABLE + " as EDGE " +
                        "ON ELE.ID = EDGE.FK_TARGET_ELEMENT_ID " +
                        "WHERE EDGE.FK_TARGET_ELEMENT_ID > " + startCellId + " " +
                        "AND EDGE.FK_TARGET_ELEMENT_ID < " + endCellId + " " +
                        "AND (ELE.COLLAPSED = 0 OR ELE.COLLAPSED = 2)" +
                    ")";
            // System.out.println("updateCollapseValForSubTreeBulk for mazimize: edge query: " + updateEdgeQuery2);


            updateHighlightsQuery = "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " " +
                    "SET COLLAPSED = 0 " +
                    "WHERE ELEMENT_ID IN " +
                    "(SELECT ID FROM " + TableNames.ELEMENT_TABLE + " " +
                    "WHERE ID > " + startCellId + " " +
                    "AND ID < " + endCellId + " " +
                    "AND (COLLAPSED = 0 OR COLLAPSED = 2)" +
                    ")";

            // System.out.println("EventHandler::updateCollapseValForSubTreeBulk: updateHighlightsQuery for expand: " + updateHighlightsQuery);

        }

        try {
            statement.addBatch(updateCellQuery);
            statement.addBatch(updateEdgeQuery2);
            statement.addBatch(updateHighlightsQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // System.out.println("EventHandler::updateCollapseValForSubTreeBulk: method ended");
    }

    private void updateCollapseValForEdgesSubTreeBulk(Statement statement, boolean isMinimized, int startCellId, int endCellId) throws SQLException {
        String updateEdgeQuery2;

        if (isMinimized) {
            updateEdgeQuery2 = "UPDATE " + TableNames.EDGE_TABLE + " " +
                    "SET COLLAPSED = " +
                    "CASE " +
                    "WHEN COLLAPSED = 0 THEN 1 " +
                    "ELSE COLLAPSED " +
                    "END " +
                    "WHERE FK_TARGET_ELEMENT_ID IN " +
                    "(SELECT ELE.ID FROM " + TableNames.ELEMENT_TABLE + " AS ELE JOIN " + TableNames.EDGE_TABLE + " as EDGE " +
                        "ON ELE.ID = EDGE.FK_TARGET_ELEMENT_ID " +
                        "WHERE EDGE.FK_TARGET_ELEMENT_ID > " + startCellId + " " +
                        "AND EDGE.FK_TARGET_ELEMENT_ID < " + endCellId + " " +
                        "AND (ELE.COLLAPSED <> 0 OR ELE.COLLAPSED <> 2)" +
                    ")";
        } else {

            updateEdgeQuery2 = "UPDATE " + TableNames.EDGE_TABLE + " " +
                    "SET COLLAPSED = " +
                    "CASE " +
                    "WHEN COLLAPSED = 1 THEN 0 " +
                    "ELSE COLLAPSED " +
                    "END " +
                    "WHERE FK_TARGET_ELEMENT_ID IN " +
                    "(SELECT ELE.ID FROM " + TableNames.ELEMENT_TABLE + " AS ELE JOIN " + TableNames.EDGE_TABLE + " as EDGE " +
                    "ON ELE.ID = EDGE.FK_TARGET_ELEMENT_ID " +
                    "WHERE EDGE.FK_TARGET_ELEMENT_ID > " + startCellId + " " +
                    "AND EDGE.FK_TARGET_ELEMENT_ID < " + endCellId + " " +
                    "AND ELE.COLLAPSED = 0 " +
                    "AND ELE.COLLAPSED = 2" +
                    ")";
        }
        statement.addBatch(updateEdgeQuery2);

    }

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private void updateTreeBelowYBulk(double y, double delta, Statement statement, int nextCellId) {
        // System.out.println("EventHandler::updateTreeBelowYBulk: method started");
        String updateCellsQuery = "UPDATE " + TableNames.ELEMENT_TABLE + " " +
                "SET bound_box_y_top_left = bound_box_y_top_left - " + delta + ", " +
                "bound_box_y_top_right = bound_box_y_top_right - " + delta + ", " +
                "bound_box_y_bottom_left = bound_box_y_bottom_left - " + delta + ", " +
                "bound_box_y_bottom_right = bound_box_y_bottom_right - " + delta + ", " +
                "bound_box_y_coordinate = bound_box_y_coordinate - " + delta + " " +
                "WHERE bound_box_y_coordinate >= " + y + " " +
                "AND ID >= " + nextCellId;
        // System.out.println("updateTreeBelowYBulk: updateCellsQuery: " + updateCellsQuery);

        String updateEdgeStartPoingQuery = "UPDATE " + TableNames.EDGE_TABLE + " " +
                "SET START_Y =  START_Y - " + delta + " " +
                "WHERE START_Y >= " + y + " " +
                "AND FK_SOURCE_ELEMENT_ID >= " + nextCellId;
        // System.out.println("updateTreeBelowYBulk: updateEdgeStartPoingQuery: " + updateEdgeStartPoingQuery);

        String updateEdgeEndPointQuery = "UPDATE " + TableNames.EDGE_TABLE + " " +
                "SET END_Y =  END_Y - " + delta + " " +
                "WHERE END_Y >= " + y + " " +
                "AND FK_TARGET_ELEMENT_ID >= " + nextCellId;
        // System.out.println("updateTreeBelowYBulk: updateEdgeEndPointQuery: " + updateEdgeEndPointQuery);

        String updateHighlightsQuery = "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " " +
                "SET START_Y = START_Y - " + delta + " " +
                "WHERE ELEMENT_ID > " + nextCellId;

        System.out.println("EventHandler::updateTreeBelowYBulk: updateHighlightsQuery: " + updateHighlightsQuery);

        try {
            statement.addBatch(updateCellsQuery);
            statement.addBatch(updateEdgeStartPoingQuery);
            statement.addBatch(updateEdgeEndPointQuery);
            statement.addBatch(updateHighlightsQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // System.out.println("EventHandler::updateTreeBelowYBulk: method ended.");
    }

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    /**
     * This method gets cell's BOUND_BOX_Y_BOTTOM_LEFT and calculates it's new value.
     * Then updates cell's BOUND_BOX_Y_BOTTOM_LEFT and BOUND_BOX_Y_BOTTOM_RIGHT values.
     * Recurse to the parent and updates it's BOUND_BOX_Y_BOTTOM_LEFT and BOUND_BOX_Y_BOTTOM_RIGHT values.
     *
     * @param cellId    The id of cell where recursive update starts.
     * @param delta     The value to be subtracted from or added to the columns.
     * @param statement All updated queries are added to this statement as batch.
     */
    private static void updateParentChainRecursive(int cellId, double delta, Statement statement) {
        // System.out.println("EventHandler::updateParentChainRecursive: method started");
        // BASE CONDITION. STOP IF ROOT IS REACHED
        if (cellId == 1) {
            return;
        }

        int parentCellId = 0;

        try (ResultSet currentCellRS = ElementDAOImpl.selectWhere("ID = " + cellId)) {
            if (currentCellRS.next()) {
                parentCellId = currentCellRS.getInt("parent_id");

                // Update this cells bottom y values
                String updateCurrentCell = "UPDATE " + TableNames.ELEMENT_TABLE + " " +
                        "SET bound_box_y_bottom_left = bound_box_y_bottom_left - " + delta + ", " +
                        "bound_box_y_bottom_right = bound_box_y_bottom_right - " + delta + " " +
                        "WHERE ID = " + cellId;

                statement.addBatch(updateCurrentCell);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updateParentChainRecursive(parentCellId, delta, statement);
        // System.out.println("EventHandler::updateParentChainRecursive: method ended");
    }

    private void updateAllParentHighlights(double y, double delta, Statement statement) {
        try {
            String updateParentHighlights = "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " " +
                    "SET HEIGHT = HEIGHT - " + delta + " " +
                    "WHERE HIGHTLIGHT_TYPE = 'FULL' " +
                    "AND START_Y <= " + y + " " +
                    "AND START_Y + HEIGHT <" + y;

            statement.addBatch(updateParentHighlights);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // double startY = 0;
        // try (ResultSet hrs = HighlightDAOImpl.selectWhere("ELEMENT_ID = " + cellId + " AND WHERE HIGHTLIGHT_TYPE = 'SINGLE'")) {
        //     if (hrs.next()) {
        //         startY = hrs.getDouble("START_Y");
        //     }
        //
        //     String updateParentHighlights = "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " " +
        //             "SET HEIGHT = HEIGHT - " + delta + " " +
        //             "WHERE HIGHTLIGHT_TYPE = 'SINGLE' " +
        //             "AND START_Y <= " + y + " " +
        //             "AND START_Y + HEIGHT <" + y;
        //     statement.addBatch(updateParentHighlights);
        //
        // } catch (SQLException e) {
        //     e.printStackTrace();
        // }
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
                double edgeNewEndY = endY;

                // For edges, update the pos values.
                edgeUpdateQuery = "UPDATE " + TableNames.EDGE_TABLE + " " +
                        "SET START_X = " + edgeNewStartX + ", " +
                        "START_Y = " + edgeNewStartY + ", " +
                        "END_X = " + edgeNewEndX + ", " +
                        "END_Y = " + edgeNewEndY +
                        "WHERE FK_TARGET_ELEMENT_ID = " + targetId;


                // System.out.println("EventHandler::getEdgePosUpdateQuery: Update query for edge: " + edgeUpdateQuery);

            }
        } catch (SQLException e) {
            System.out.println("SQL that threw exception: " + getSourceElementRow);
            e.printStackTrace();
        }

        return edgeUpdateQuery;
    }


    private static void addEdgePosUpdateQueryToStatement(int targetId, double y, Statement statement) throws SQLException {


        // Update the startX and startY values of all edges that start at current cell.
        String updateEdgeStartPosQuery = "UPDATE " + TableNames.EDGE_TABLE + " " +
                "SET START_Y = " + y + " " +
                "WHERE FK_SOURCE_ELEMENT_ID = " + targetId;

        // System.out.println("EventHandler::getEdgePosUpdateQuery: Update query for edge: change of start pos: " + updateEdgeStartPosQuery);

        // Update the endX and endY values of all edges that end at current cell.
        String updateEdgeEndPosQuery = "UPDATE " + TableNames.EDGE_TABLE + " " +
                "SET END_Y = " + y + " " +
                "WHERE FK_TARGET_ELEMENT_ID = " + targetId;

        // System.out.println("EventHandler::getEdgePosUpdateQuery: Update query for edge: change of end pos: " + updateEdgeEndPosQuery);

        statement.addBatch(updateEdgeStartPosQuery);
        statement.addBatch(updateEdgeEndPosQuery);

    }


    public void expandSubtreeAndUpdateColValsOfSubtreeRootedAt(String cellId) {
        Task<Void> expandSubtree = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                expandSubtreeAndUpdateColValsRecursive(cellId);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                subtreeExpanded = true;
                System.out.println("set subtreeExpanded: " + subtreeExpanded);
                Platform.runLater(() -> setClickable());
                // System.out.println("EventHandler::updatePosValForLowerTree:  Updated the entire tree successfully");
            }

            @Override
            protected void failed() {
                super.failed();
                System.out.println("EventHandler::updatePosValForLowerTree:  Failed to update tree.");
                try {
                    throw new Exception("updatePosValForLowerTree failed");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // catch the exception in the thread.
        expandSubtree.setOnFailed(event -> {
            expandSubtree.getException().printStackTrace();
        });
        new Thread(expandSubtree).start();


    }

    public void expandSubtreeAndUpdateColValsRecursive(String cellId) {
        // Get element row for this cell
        try (ResultSet elementRS = ElementDAOImpl.selectWhere("id = " + cellId)) {
            if (elementRS.next()) {
                int collapsed = elementRS.getInt("collapsed");
                if (collapsed == 0) {
                    throw new IllegalStateException("Collapsed cannot be 0 here at CellID: " + cellId);
                } else if (collapsed == 1) {
                    System.out.println(" ==> collapse 1 -> 0 at cellId: " + cellId);
                    ElementDAOImpl.updateWhere("collapsed", "0", "id = " + cellId);

                    // Create a new circle cell and add to UI
                    float xCoordinateTemp = elementRS.getFloat("bound_box_x_coordinate");
                    float yCoordinateTemp = elementRS.getFloat("bound_box_y_coordinate");
                    CircleCell cell = new CircleCell(cellId, xCoordinateTemp, yCoordinateTemp);
                    Platform.runLater(() -> graph.getModel().addCell(cell));


                    // Create a new edge and add to UI. Update edge's collapsed=0
                    try (ResultSet parentRS = ElementToChildDAOImpl.selectWhere("child_id = " + cellId)) {
                        if (parentRS.next()) {
                            String parentId = String.valueOf(parentRS.getInt("parent_id"));
                            CircleCell parentCell = graph.getModel().getCircleCellsOnUI().get(parentId);

                            Edge edge = new Edge(parentCell, cell);

                            EdgeDAOImpl.updateWhere("collapsed", "0",
                                    "fk_target_element_id = " + cellId);

                            Platform.runLater(() -> graph.getModel().addEdge(edge));
                        }
                    }

                    // graph.myEndUpdate();
                    Platform.runLater(() -> graph.updateCellLayer());


                    // Recurse to this cells children
                    try (ResultSet childrenRS = ElementToChildDAOImpl.selectWhere("parent_id = " + cellId)) {
                        while (childrenRS.next()) {
                            String childId = String.valueOf(childrenRS.getInt("child_id"));
                            expandSubtreeAndUpdateColValsRecursive(childId);
                        }
                    }

                } else if (collapsed == 2) {
                    System.out.println(" ==> collapse 2 -> 0 at cellId: " + cellId);

                    // update collapsed=0
                    ElementDAOImpl.updateWhere("collapsed", "0", "id = " + cellId);
                    // for all children with collapsed=1, show and update collapsed=0
                    try (ResultSet childrenRS = ElementToChildDAOImpl.selectWhere("parent_id = " + cellId)) {
                        while (childrenRS.next()) {
                            String childId = String.valueOf(childrenRS.getInt("child_id"));
                            expandSubtreeAndUpdateColValsRecursive(childId);
                        }
                    }

                } else if (collapsed == 3) {
                    System.out.println(" ==> collapse 3 -> 2 at cellId: " + cellId);

                    // update collapsed=2
                    ElementDAOImpl.updateWhere("collapsed", "2", "id = " + cellId);

                    // Create new circle cell and add to UI
                    float xCoordinateTemp = elementRS.getFloat("bound_box_x_coordinate");
                    float yCoordinateTemp = elementRS.getFloat("bound_box_y_coordinate");
                    CircleCell cell = new CircleCell(cellId, xCoordinateTemp, yCoordinateTemp);
                    Platform.runLater(() -> graph.getModel().addCell(cell));

                    // Create a new edge and add to UI. Update edge's collapsed=0
                    try (ResultSet parentRS = ElementToChildDAOImpl.selectWhere("child_id = " + cellId)) {
                        if (parentRS.next()) {
                            String parentId = String.valueOf(parentRS.getInt("parent_id"));
                            CircleCell parentCell = graph.getModel().getCircleCellsOnUI().get(parentId);

                            Edge edge = new Edge(parentCell, cell);
                            EdgeDAOImpl.updateWhere("collapsed", "0",
                                    "fk_target_element_id = " + cellId);

                            Platform.runLater(() -> graph.getModel().addEdge(edge));

                        }
                    }
                    // graph.myEndUpdate();
                    Platform.runLater(() -> graph.updateCellLayer());

                    // Do not recurse to children. Stop at this cell.
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public void updateCollapseValForSubTreeRootedAtRecursive(String cellId, List<String> removeCircleCells, List<String> removeEdges) {
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
        } catch (SQLException e) {
        }
    }

    @SuppressWarnings("unused")
    EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Node node = (Node) event.getSource();
            double scale = graph.getScale();
            dragContext.x = node.getBoundsInParent().getMinX() * scale - event.getScreenX();
            dragContext.y = node.getBoundsInParent().getMinY() * scale - event.getScreenY();
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

    EventHandler<MouseEvent> onMouseReleasedEventHandler = event -> {
    };

    class DragContext {
        double x;
        double y;
    }

    public static void saveRef(Main m) {
        main = m;
    }

    public static void saveRef(ConvertDBtoElementTree c) {
        convertDBtoElementTree = c;
    }

}