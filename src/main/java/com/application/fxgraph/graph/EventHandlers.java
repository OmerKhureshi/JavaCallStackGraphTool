package com.application.fxgraph.graph;

import com.application.db.model.Bookmark;
import com.application.fxgraph.ElementHelpers.ConvertDBtoElementTree;
import com.application.Main;
import com.application.db.DAOImplementation.*;
import com.application.db.DatabaseUtil;
import com.application.db.TableNames;
// import com.application.fxgraph.ElementHelpers.SimplifiedElement;
import com.application.fxgraph.cells.CircleCell;
import javafx.animation.FillTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;
import org.controlsfx.glyphfont.Glyph;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static com.application.fxgraph.graph.Graph.cellLayer;

public class EventHandlers {

    private static ConvertDBtoElementTree convertDBtoElementTree;
    final DragContext dragContext = new DragContext();
    // static Map<String, Double> deltaCache = new HashMap<>();
    private boolean clickable = true;

    Graph graph;
    static Main main;

    public EventHandlers(Graph graph) {
        this.graph = graph;
    }

    public static void resetEventHandlers() {
        // deltaCache = new HashMap<>();
    }

    public void setCustomMouseEventHandlers(final Node node) {
        // *****************
        // Show popup to display element details on mouse hover on an element.
        // node.setOnMouseEntered(infoButtonOnClickEventHandler);
        // node.setOnMousePressed(infoButtonOnClickEventHandler);
        // node.setOnMousePressed(minMaxButtonOnClickEventHandler);
        // *****************

        // node.setOnMouseEntered(onMouseEnterToShowNav);

        ((CircleCell)node).getInfoGroup().setOnMouseEntered(showInfoButtonEventHandeler);
        ((CircleCell)node).getInfoGroup().setOnMouseExited(hideInfoButtonEventHandler);
        ((CircleCell)node).getInfoGroup().setOnMousePressed(infoButtonOnClickEventHandler);

        ((CircleCell)node).getMinMaxGroup().setOnMouseEntered(showMinMaxEventHandler);
        ((CircleCell)node).getMinMaxGroup().setOnMouseExited(hideMinMaxEventHandler);
        ((CircleCell)node).getMinMaxGroup().setOnMousePressed(minMaxButtonOnClickEventHandler);

        // *****************
        // For debugging. Prints all mouse events.
        // node.addEventFilter(MouseEvent.ANY, infoButtonOnClickEventHandler);
        // node.addEventFilter(MouseEvent.ANY, event -> System.out.println(event));
        // *****************


        // *****************
        // Click on an element to collapse the subtree rooted at that element.
        // node.setOnMousePressed(minMaxButtonOnClickEventHandler);
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

    private EventHandler<MouseEvent> infoButtonOnClickEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            if (popOver != null) {
                popOver.hide();
            }

            Node node = (Node) event.getSource();
            // CircleCell cell = (CircleCell) node;
            CircleCell cell = (CircleCell) node.getParent();
            String timeStamp;
            int elementId, methodId, processId, threadId, collapsed;
            String parameters, packageName = "", methodName = "", parameterTypes = "", eventType, lockObjectId;
            double xCord, yCord;


            // Do Not Uncomment
            // String sql = "Select * from " + TableNames.ELEMENT_TABLE + " " +
            //         "JOIN " + TableNames.CALL_TRACE_TABLE + " ON " + TableNames.CALL_TRACE_TABLE + ".id = " + TableNames.ELEMENT_TABLE+ ".ID_ENTER_CALL_TRACE " +
            //         "JOIN " + TableNames.METHOD_DEFINITION_TABLE + " ON " + TableNames.METHOD_DEFINITION_TABLE + ".ID = " + TableNames.CALL_TRACE_TABLE + ".METHOD_ID " +
            //         "WHERE " + TableNames.ELEMENT_TABLE + ".ID = " + cell.getCellId();
            // System.out.println("your query: " + sql);

            // Please. Please do not try to combine the next two queries into one. Unless you want to spend another day tyring to prove it to yourself.

            String sql = "Select E.ID as EID, TIME_INSTANT, METHOD_ID, PROCESS_ID, THREAD_ID, PARAMETERS, COLLAPSED, " +
                    "MESSAGE, LOCKOBJID, BOUND_BOX_X_COORDINATE, BOUND_BOX_Y_COORDINATE from " + TableNames.ELEMENT_TABLE + " AS E " +
                    "JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT ON CT.id = E.ID_ENTER_CALL_TRACE " +
                    "WHERE E.ID = " + cell.getCellId();

            try (ResultSet callTraceRS = DatabaseUtil.select(sql)) {
                // try (ResultSet callTraceRS = CallTraceDAOImpl.selectWhere("id = (Select id_enter_call_trace FROM " + TableNames.ELEMENT_TABLE +
                //         " WHERE id = " + cell.getCellId() + ")")) {
                if (callTraceRS.next()) {
                    elementId = callTraceRS.getInt("EID");
                    timeStamp = callTraceRS.getString("time_instant");
                    methodId = callTraceRS.getInt("method_id");
                    processId = callTraceRS.getInt("process_id");
                    threadId = callTraceRS.getInt("thread_id");
                    parameters = callTraceRS.getString("parameters");
                    eventType = callTraceRS.getString("message");
                    lockObjectId = callTraceRS.getString("lockobjid");
                    xCord = callTraceRS.getFloat("bound_box_x_coordinate");
                    yCord = callTraceRS.getFloat("bound_box_y_coordinate");
                    collapsed = callTraceRS.getInt("COLLAPSED");


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
                                int eId = elementRS.getInt("id");
                                eleIdList.add(eId);
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
                                    int eId = elementRS.getInt("id");
                                    eleIdList.add(eId);
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
                                        int eId = elementRS.getInt("id");
                                        eleIdList.add(eId);
                                    }
                                } catch (SQLException e) {
                                }
                            });
                        }
                    }

                    List<Button> buttonList = new ArrayList<>();
                    String finalPackageName = packageName;
                    String finalMethodName = methodName;
                    eleIdList.stream().forEach(eId -> {
                        String query = "SELECT E.ID AS EID, bound_box_x_coordinate, bound_box_y_coordinate, THREAD_ID " +
                                "FROM CALL_TRACE AS CT " +
                                "JOIN ELEMENT AS E ON CT.ID = E.ID_ENTER_CALL_TRACE " +
                                "WHERE E.ID = " + eId;
                        try (ResultSet elementRS = DatabaseUtil.select(query)) {
                            // try (ResultSet elementRS = ElementDAOImpl.selectWhere("id = " + eId)){
                            if (elementRS.next()) {
                                int id = elementRS.getInt("EID");
                                String targetThreadId = String.valueOf(elementRS.getInt("thread_id"));
                                float xCoordinate = elementRS.getFloat("bound_box_x_coordinate");
                                float yCoordinate = elementRS.getFloat("bound_box_y_coordinate");

                                double width = graph.getScrollPane().getContent().getBoundsInLocal().getWidth();
                                double height = graph.getScrollPane().getContent().getBoundsInLocal().getHeight();

                                // go to location.
                                Button jumpToButton = new Button();
                                jumpToButton.setOnMouseClicked(event1 -> {
                                    System.out.println("EventHandlers.handle: jumpToButton Clicked. for eleId: " + eId);
                                    jumpTo(eId, targetThreadId, collapsed);
                                });
                                buttonList.add(jumpToButton);
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

                    // Collapse and Expand subtree button
                    Button minMaxButton = new Button("min / max");
                    minMaxButton.setOnMouseClicked(event1 -> {
                                minMaxButtonOnClick(cell, threadId);
                            }
                    );

                    gridPane.add(minMaxButton, 1, rowIndex++);

                    // Add Bookmark button
                    // Group bookmarkGroup = new Group();
                    final Color[] bookmarkColor = new Color[1];
                    bookmarkColor[0] = Color.INDIANRED;

                    ColorPicker bookmarkColorPicker = new ColorPicker(Color.INDIANRED);
                    bookmarkColorPicker.setOnAction(e -> {
                        bookmarkColor[0] = bookmarkColorPicker.getValue();
                    });
                    // bookmarkColorPicker.getStyleClass().add("button");
                    // bookmarkColorPicker.setStyle(
                    //         "-fx-color-label-visible: false; " +
                    //                 "-fx-background-radius: 15 15 15 15;");

                    Button addBookmarkButton = new Button("Add Bookmark");
                    Button removeBookmarkButton = new Button("Remove bookmark");

                    String finalMethodNameTemp = methodName;
                    addBookmarkButton.setOnMouseClicked(event1 -> {
                        Bookmark bookmark = new Bookmark(
                                String.valueOf(elementId),
                                String.valueOf(threadId),
                                finalMethodNameTemp,
                                bookmarkColor[0].toString(),
                                xCord,
                                yCord,
                                collapsed);

                        BookmarksDAOImpl.insertBookmark(bookmark);
                        graph.getModel().updateBookmarkMap();
                        convertDBtoElementTree.clearAndUpdateCellLayer();
                        removeBookmarkButton.setDisable(false);
                        addBookmarkButton.setDisable(true);

                        // graph.addMarkToBarPane(bookmark);
                    });

                    gridPane.add(bookmarkColorPicker, 1, rowIndex++);
                    gridPane.add(addBookmarkButton, 1, rowIndex++);

                    removeBookmarkButton.setDisable(!graph.getModel().getBookmarkMap().containsKey(String.valueOf(elementId)));

                    removeBookmarkButton.setOnMouseClicked(eve -> {
                        BookmarksDAOImpl.deleteBookmark(String.valueOf(elementId));
                        graph.getModel().updateBookmarkMap();
                        convertDBtoElementTree.clearAndUpdateCellLayer();
                        addBookmarkButton.setDisable(false);

                        // graph.removeMarkFromBarPane(String.valueOf(elementId));
                    });

                    gridPane.add(removeBookmarkButton, 1, rowIndex++);

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

    // EventHandler<MouseEvent> onMouseReleasedEventHandlereEnterToShowNav = new EventHandler<MouseEvent>() {
    //     @Override
    //     public void handle(MouseEvent event) {
    //         CircleCell cell = ((CircleCell) event.getSource());
    //         SimplifiedElement ele = graph.getModel().getSimplifiedElementMap().get(cell.getCellId());
    //
    //         ArrayList<String> methodNames = new ArrayList<>();
    //         while (ele.getParentElement() != null) {
    //             methodNames.add("Id: " + ele.getElementId() + "   Method: " + ele.getMethodName());
    //             ele = ele.getParentElement();
    //         }
    //
    //         Collections.reverse(methodNames);
    //         String navString = String.join(" > ", methodNames);
    //     }
    // };

    EventHandler<MouseEvent> onMouseExitToDismissPopover = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (popOver != null)
                popOver.hide();
        }
    };

    EventHandler<MouseEvent> showInfoButtonEventHandeler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Group node = (Group) event.getSource();
            Arc arc = (Arc) node.getChildren().get(0);
            Glyph glyph = ((Glyph) node.getChildren().get(1));
            Node glyphNode = node.getChildren().get(1);

            FillTransition ftArc = new FillTransition(Duration.millis(50), arc, Color.TRANSPARENT, Color.web("#DDDDDD"));
            ftArc.setOnFinished(e -> {
                glyph.setColor(Color.BLACK);
            });

            ftArc.play();

        }
    };

    EventHandler<MouseEvent> hideInfoButtonEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Group node = (Group) event.getSource();
            Arc arc = (Arc) node.getChildren().get(0);
            Glyph glyph = ((Glyph) node.getChildren().get(1));

            FillTransition ftArc = new FillTransition(Duration.millis(50), arc, Color.web("#DDDDDD"), Color.TRANSPARENT);
            ftArc.setOnFinished(e -> {
                glyph.setColor(Color.TRANSPARENT);
            });
            ftArc.play();
        }
    };


    EventHandler<MouseEvent> showMinMaxEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Group node = (Group) event.getSource();
            Arc arc = (Arc) node.getChildren().get(0);
            Glyph glyph = ((Glyph) node.getChildren().get(1));

            arc.setFill(Color.web("#DDDDDD"));
            glyph.setColor(Color.BLACK);
        }
    };

    EventHandler<MouseEvent> hideMinMaxEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            Group node = (Group) event.getSource();
            Arc arc = (Arc) node.getChildren().get(0);
            Glyph glyph = ((Glyph) node.getChildren().get(1));

            arc.setFill(Color.TRANSPARENT);
            glyph.setColor(Color.TRANSPARENT);
        }
    };


    private void minMaxButtonOnClick(CircleCell clickedCell, int threadId) {
        {
            if (popOver != null) {
                popOver.hide();
            }

            if (!clickable) {
                System.out.println(">>>>>>>>>>>>>>>>>>> Clickable is false. <<<<<<<<<<<<<<<<<<<<<");
                return;
            }

            clickable = false;

            String clickedCellID = clickedCell.getCellId();

            int collapsed = 0, parentId = 0;
            double clickedCellTopLeftY = 0, clickedCellTopLeftX = 0, clickedCellTopRightX = 0, clickedCellBoundBottomLeftY = 0, newDelta = 0, newDeltaX = 0;

            try (ResultSet cellRS = ElementDAOImpl.selectWhere("id = " + clickedCellID)) {
                if (cellRS.next()) {
                    collapsed = cellRS.getInt("collapsed");
                    clickedCellTopLeftY = cellRS.getDouble("bound_box_y_top_left");
                    clickedCellTopLeftX = cellRS.getDouble("bound_box_x_top_left");
                    clickedCellTopRightX = cellRS.getDouble("bound_box_x_top_right");
                    clickedCellBoundBottomLeftY = cellRS.getDouble("bound_box_y_bottom_left");
                    newDelta = cellRS.getDouble("delta");
                    newDeltaX = cellRS.getDouble("delta_x");
                    parentId = cellRS.getInt("parent_id");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            /*
             * collapsed - actions
             *     0     - Cell visible on UI. Starting value for all cells.
             *     1     - parent of this cell was minimized. Don't show on UI
             *     2     - this cell was minimized. Show on UI. Don't show children on UI.
             *    >= 3   - parent of this cell was minimized. This cell was also minimized. Don't expand this cell's children. Don't show on UI.
             */
            if (collapsed == 1 || collapsed >= 3) {
                // expand sub tree.
                System.out.println("minMaxButtonOnClickEventHandler: cell: " + clickedCellID + " ; collapsed: " + collapsed);
            } else if (collapsed == 0) {
                // MINIMIZE SUBTREE

                // ((Circle) clickedCell.getChildren().get(0)).setFill(Color.BLUE);
                // ((Circle) ( (Group)cell.getView() )
                //             .getChildren().get(0))
                //             .setFill(Color.BLUE);
                // cell.getChildren().get(0).setStyle("-fx-background-color: blue");
                // cell.setStyle("-fx-background-color: blue");
                main.setStatus("Please wait ......");

                System.out.println("====== Minimize cellId: " + clickedCellID + " ------ ");

                // ElementDAOImpl.updateWhere("collapsed", "2", "id = " + clickedCellID);

                Statement statement = DatabaseUtil.createStatement();

                int nextCellId = getNextLowerSiblingOrAncestorNode(Integer.parseInt(clickedCellID), clickedCellTopLeftX, clickedCellTopLeftY, threadId);
                int lastCellId = getLowestCellInThread(threadId);

                // delta = clickedCellBoundBottomLeftY - clickedCellTopLeftY - BoundBox.unitHeightFactor;
                newDelta = clickedCellBoundBottomLeftY - clickedCellTopLeftY - BoundBox.unitHeightFactor;

                // calculate the new value of newDeltaX
                String getDeltaXQuery = "SELECT MAX(BOUND_BOX_X_TOP_RIGHT) AS MAX_X FROM " + TableNames.ELEMENT_TABLE + " " +
                        "WHERE ID >= " + clickedCellID + " AND ID < " + nextCellId + " " +
                        "AND COLLAPSED IN (0, 2)";

                try (ResultSet rs = DatabaseUtil.select(getDeltaXQuery)){
                    if (rs.next()) {
                        newDeltaX = rs.getFloat("MAX_X") - clickedCellTopRightX;
                        System.out.println("EventHandler::minMaxButtonOnClickEventHandler on collapse: newDeltaX: " + newDeltaX);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                double clickedCellBottomY = clickedCellTopLeftY + BoundBox.unitHeightFactor;

                // deltaCache.put(clickedCellID, delta);

                String updateClickedElement = "UPDATE " + TableNames.ELEMENT_TABLE + " " +
                        "SET COLLAPSED = 2, " +
                        "DELTA = " + newDelta + ", " +
                        "DELTA_X = " + newDeltaX + " " +
                        "WHERE ID = " + clickedCellID;
                DatabaseUtil.executeUpdate(updateClickedElement);


                removeChildrenFromUI(Integer.parseInt(clickedCellID), nextCellId);
                moveLowerTreeByDelta(clickedCellID, clickedCellBottomY, newDelta);
                // updateAllParentHighlightsOnUI(clickedCellID, clickedCellTopLeftX, clickedCellTopLeftY, newDelta, newDeltaX);
                updateDBInBackgroundThread(Integer.parseInt(clickedCellID), clickedCellTopLeftY, clickedCellBoundBottomLeftY, clickedCellTopLeftX,
                        clickedCellTopRightX, newDelta, newDeltaX, true,
                        nextCellId, threadId, lastCellId, parentId);

            } else if (collapsed == 2) {
                // MAXIMIZE SUBTREE

                // ((Circle) clickedCell.getChildren().get(0)).setFill(Color.RED);
                // ( (Circle) ( (Group)cell.getView() ).getChildren().get(0) ).setFill(Color.RED);
                main.setStatus("Please wait ......");
                System.out.println("====== Maximize cellId: " + clickedCellID + " ++++++ ");

                // double delta = deltaCache.get(clickedCellID);

                expandTreeAt(clickedCellID, parentId, threadId, newDelta, newDeltaX,
                        clickedCellTopLeftX, clickedCellTopLeftY, clickedCellBoundBottomLeftY, clickedCellTopRightX );
            }
        }
    }

    private void expandTreeAt(String clickedCellID, int parentId, int threadId, double newDelta, double newDeltaX,
                              double clickedCellTopLeftX, double clickedCellTopLeftY, double clickedCellBoundBottomLeftY, double clickedCellTopRightX) {
        int nextCellId = getNextLowerSiblingOrAncestorNode(Integer.parseInt(clickedCellID), clickedCellTopLeftX, clickedCellTopLeftY, threadId);
        int lastCellId = getLowestCellInThread(threadId);

        double clickedCellBottomY = clickedCellTopLeftY + BoundBox.unitHeightFactor;
        double newClickedCellBottomY = clickedCellTopLeftY + BoundBox.unitHeightFactor + newDelta;

        moveLowerTreeByDelta(clickedCellID, clickedCellBottomY, -newDelta);
        updateAllParentHighlightsOnUI(clickedCellID, clickedCellTopLeftX, clickedCellTopLeftY, -newDelta, -newDeltaX);

        ElementDAOImpl.updateWhere("collapsed", "0", "id = " + clickedCellID);
        updateDBInBackgroundThread(Integer.parseInt(clickedCellID), clickedCellTopLeftY, clickedCellBoundBottomLeftY,
                clickedCellTopLeftX, clickedCellTopRightX, -newDelta, -newDeltaX, false,
                nextCellId, threadId, lastCellId, parentId);
    }

    private void expandTreeAt(String cellId, int threadId) {
        String clickedCellID = cellId;
        int collapsed = 0;
        double clickedCellTopLeftY = 0;
        double clickedCellTopLeftX = 0;
        double clickedCellTopRightX = 0;
        double clickedCellBoundBottomLeftY = 0;
        double newDelta = 0;
        double newDeltaX = 0;
        int parentId = 0;

        try (ResultSet cellRS = ElementDAOImpl.selectWhere("id = " + clickedCellID)) {
            if (cellRS.next()) {
                collapsed = cellRS.getInt("collapsed");
                clickedCellTopLeftY = cellRS.getDouble("bound_box_y_top_left");
                clickedCellTopLeftX = cellRS.getDouble("bound_box_x_top_left");
                clickedCellTopRightX = cellRS.getDouble("bound_box_x_top_right");
                clickedCellBoundBottomLeftY = cellRS.getDouble("bound_box_y_bottom_left");
                newDelta = cellRS.getDouble("delta");
                newDeltaX = cellRS.getDouble("delta_x");
                parentId = cellRS.getInt("parent_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        expandTreeAt(clickedCellID, parentId, threadId, newDelta, newDeltaX, clickedCellTopLeftX, clickedCellTopLeftY, clickedCellBoundBottomLeftY, clickedCellTopRightX);
    }


    private EventHandler<MouseEvent> minMaxButtonOnClickEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            CircleCell cell = ((CircleCell) ((Node) event.getSource()).getParent());
            minMaxButtonOnClick(cell, Integer.valueOf(main.getCurrentSelectedThread()));
        }
    };

    private void expandParentTreeChain(int cellId, int threadId) {
        // System.out.println("EventHandlers.expandParentTreeChain: method started");
        Deque<Integer> stack = new LinkedList<>();

        String getAllParentIDsQuery = "SELECT MAX(ID) AS IDS " +
                "FROM " + TableNames.ELEMENT_TABLE + " AS E " +
                "WHERE E.ID < " + cellId + " " +
                "AND E.BOUND_BOX_X_COORDINATE < (SELECT BOUND_BOX_X_COORDINATE " +
                                                    "FROM " + TableNames.ELEMENT_TABLE + " AS E1 " +
                                                    "WHERE E1.ID = " + cellId + ") " +
                "AND EXISTS (SELECT * FROM " + TableNames.CALL_TRACE_TABLE + " AS CT " +
                                "WHERE CT.ID = E.ID_ENTER_CALL_TRACE AND " +
                                "CT.THREAD_ID = " + threadId + ")" +
                "AND E.PARENT_ID > 1 " +
                "AND E.COLLAPSED <> 0 " +
                "GROUP BY E.BOUND_BOX_X_COORDINATE " +
                "ORDER BY IDS ASC ";

        try (ResultSet rs = DatabaseUtil.select(getAllParentIDsQuery)) {
            while (rs.next()) {
                // System.out.println("EventHandlers.expandParentTreeChain: expandTreeAt: " + String.valueOf(rs.getInt("IDS")));
                expandTreeAt(String.valueOf(rs.getInt("IDS")), threadId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // System.out.println("EventHandlers.expandParentTreeChain: method ended");
    }

    // jumpTo(int cellId, String threadId)

    public void jumpTo(int cellId, String threadId, int collapsed) {

        // make changes in DB if needed
        if (collapsed != 0) {
            expandParentTreeChain(cellId, Integer.parseInt(threadId));
        }

        // update UI
        ConvertDBtoElementTree.resetRegions();
        main.showThread(threadId);
        Main.makeSelection(threadId);

        try (ResultSet rs = ElementDAOImpl.selectWhere("ID = " + cellId)){
            if (rs.next()) {
                double xCord = rs.getDouble("BOUND_BOX_X_COORDINATE");
                double yCord = rs.getDouble("BOUND_BOX_Y_COORDINATE");
                graph.moveScrollPane(graph.getHValue(xCord), graph.getVValue(yCord));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private int getLowestCellInThread(int threadId) {
        String maxEleIdQuery = "SELECT MAX(E.ID) AS MAXID " +
                "FROM " + TableNames.ELEMENT_TABLE + " AS E JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT " +
                "ON E.ID_ENTER_CALL_TRACE = CT.ID " +
                "WHERE CT.THREAD_ID = " + threadId;


        try (ResultSet eleIdRS = DatabaseUtil.select(maxEleIdQuery)){
            if (eleIdRS.next()) {
                return eleIdRS.getInt("MAXID");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Integer.MAX_VALUE;
    }

    private int getNextLowerSiblingOrAncestorNode(int clickedCellId, double x, double y, int threadId) {
        System.out.println("EventHandlers.getNextLowerSiblingOrAncestorNode: started");
        int lastCellId = getLowestCellInThread(threadId) + 1;

        String getNextQuery = "SELECT " +
                "CASE " +
                "WHEN MIN(E.ID) IS NULL THEN " + lastCellId + " " +
                "ELSE MIN(E.ID) " +
                "END " +
                "AS MINID " +
                "FROM " + TableNames.ELEMENT_TABLE + " AS E JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT " +
                "ON E.ID_ENTER_CALL_TRACE = CT.ID " +
                "WHERE E.BOUND_BOX_Y_TOP_LEFT > " + y + " " +
                "AND E.BOUND_BOX_X_TOP_LEFT <= " + x + " " +
                "AND E.ID > " + clickedCellId + " " +
                "AND CT.THREAD_ID = " + threadId;

        try (ResultSet rs = DatabaseUtil.select(getNextQuery)) {
            if (rs.next()) {
                // System.out.println("EventHandler::getNextLowerSiblingOrAncestorNode: we have result: " + rs.getInt("MINID"));
                return rs.getInt("MINID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // System.out.println("EventHandler::getNextLowerSiblingOrAncestorNode: we dont hav res" + Integer.MAX_VALUE);
        System.out.println("EventHandlers.getNextLowerSiblingOrAncestorNode: ended");
        return Integer.MAX_VALUE;
    }

    private void setClickable() {
        clickable = true;
        main.setStatus("Done");
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


    private void updateDBInBackgroundThread(int clickedCellId, double topY, double bottomY, double leftX, double rightX, double delta, double deltaX, boolean isCollapsed, int nextCellId, int threadId, int lastCellId, int parentId) {

        Statement statement = DatabaseUtil.createStatement();
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // System.out.println("==================== Starting thread updateDBInBackgroundThread. ====================");
                updateCollapseValForSubTreeBulk(topY, bottomY, rightX, statement, isCollapsed, clickedCellId, nextCellId, threadId);

                // No upate required for single line children
                if (delta == 0) {
                    System.out.println("Optimized for single line children collapses.");
                    updateParentHighlightsInDB(clickedCellId, isCollapsed, statement, delta, nextCellId, leftX, topY, threadId);

                    statement.executeBatch();
                    Platform.runLater(() -> convertDBtoElementTree.clearAndUpdateCellLayer());
                    return null;
                }

                updateParentChainRecursive(clickedCellId, delta, statement);

                if (nextCellId != Integer.MAX_VALUE) {
                    // only if next lower sibling ancestor node is present.
                    updateTreeBelowYBulk(topY + BoundBox.unitHeightFactor, delta, statement, nextCellId, lastCellId, threadId);
                    statement.executeBatch();
                }

                updateParentHighlightsInDB(clickedCellId, isCollapsed, statement, delta, nextCellId, leftX, topY, threadId);
                updateChildrenHighlightsInDB(clickedCellId, isCollapsed, statement, delta, nextCellId, threadId);
                statement.executeBatch();

                Platform.runLater(() -> convertDBtoElementTree.clearAndUpdateCellLayer());
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

    private void updateCollapseValForSubTreeBulk(double topY, double bottomY, double leftX, Statement statement, boolean isMinimized, int startCellId, int endCellId, int threadId) {

        // Collapsed value -> description
        // 0               -> visible     AND  uncollapsed
        // 2               -> visible     AND  collapsed
        // >2              -> not visible AND  collapsed
        // <0              -> not visible AND  collapsed

        // System.out.println("EventHandler::updateCollapseValForSubTreeBulk: method started");
        String updateCellQuery;
        String updateEdgeQuery;
        String updateEdgeQuery2;
        String updateHighlightsQuery;

        if (isMinimized) {
            // Update the collapse value in the subtree rooted at the clicked cell.
            updateCellQuery = "UPDATE " + TableNames.ELEMENT_TABLE + " AS E " +
                    "SET E.COLLAPSED = " +
                    "CASE " +
                    "WHEN E.COLLAPSED <= 0 THEN E.COLLAPSED - 1 " +
                    "WHEN E.COLLAPSED >= 2 THEN E.COLLAPSED + 1 " +
                    "ELSE E.COLLAPSED " +
                    "END " +
                    "WHERE " +
                    // "(bound_box_y_coordinate >= " + topY + " " +
                    // "AND bound_box_y_coordinate < " + bottomY + " " +
                    // "AND bound_box_x_coordinate >= " + leftX + ") " +
                    // "AND " +
                    "E.ID > " + startCellId + " " +
                    "AND E.ID < " + endCellId + " " +
                    "AND EXISTS (SELECT * FROM " + TableNames.CALL_TRACE_TABLE + " AS CT " +
                                    "WHERE CT.ID = E.ID_ENTER_CALL_TRACE AND " +
                                    "CT.THREAD_ID = " + threadId + ")";

            // and exists (select * from call_trace where id=id_enter_call_trace and thread_id=something.

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
                        "WHERE EDGE.FK_TARGET_ELEMENT_ID > " + startCellId + " " +
                        "AND EDGE.FK_TARGET_ELEMENT_ID < " + endCellId + " " +
                        "AND ELE.COLLAPSED NOT IN (0, 2) " +
                        // "AND ELE.COLLAPSED <= 2" +
                        ")";

            // System.out.println("updateCollapseValForSubTreeBulk for minimize: edge query: " + updateEdgeQuery2);


            updateHighlightsQuery = "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " " +
                    "SET COLLAPSED = 1 " +
                    "WHERE ELEMENT_ID > " + startCellId + " " +
                    "AND ELEMENT_ID < " + endCellId;

            // System.out.println("EventHandler::updateCollapseValForSubTreeBulk: updateHighlightsQuery for collapse: " + updateHighlightsQuery);


        } else {
            updateCellQuery = "UPDATE " + TableNames.ELEMENT_TABLE + " AS E " +
                    "SET E.COLLAPSED = " +
                    "CASE " +
                    "WHEN E.COLLAPSED < 0 THEN E.COLLAPSED + 1 " +
                    "WHEN E.COLLAPSED > 2 THEN E.COLLAPSED - 1 " +
                    "ELSE E.COLLAPSED " +
                    "END " +
                    "WHERE " +
                    // "bound_box_y_coordinate >= " + topY + " " +
                    // "AND bound_box_y_coordinate < " + bottomY + " " +
                    // "AND bound_box_x_coordinate >= " + leftX + " " +
                    // "AND " +
                    "E.ID > " + startCellId + " " +
                    "AND E.ID < " + endCellId + " " +
                    "AND EXISTS (SELECT * FROM " + TableNames.CALL_TRACE_TABLE + " AS CT " +
                                    "WHERE CT.ID = E.ID_ENTER_CALL_TRACE AND " +
                                    "CT.THREAD_ID = " + threadId + ")";

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

    private void updateTreeBelowYBulk(double y, double delta, Statement statement, int nextCellId, int lastCellId, int threadId) {
        // System.out.println("EventHandler::updateTreeBelowYBulk: method started");
        String updateCellsQuery = "UPDATE " + TableNames.ELEMENT_TABLE + " " +
                "SET bound_box_y_top_left = bound_box_y_top_left - " + delta + ", " +
                "bound_box_y_top_right = bound_box_y_top_right - " + delta + ", " +
                "bound_box_y_bottom_left = bound_box_y_bottom_left - " + delta + ", " +
                "bound_box_y_bottom_right = bound_box_y_bottom_right - " + delta + ", " +
                "bound_box_y_coordinate = bound_box_y_coordinate - " + delta + " " +
                "WHERE bound_box_y_coordinate >= " + y + " " +
                "AND ID >= " + nextCellId + " " +
                "AND ID <= " + lastCellId;
        // System.out.println("updateTreeBelowYBulk: updateCellsQuery: " + updateCellsQuery);

        String updateEdgeStartPoingQuery = "UPDATE " + TableNames.EDGE_TABLE + " " +
                "SET START_Y =  START_Y - " + delta + " " +
                "WHERE START_Y >= " + y + " " +
                "AND FK_SOURCE_ELEMENT_ID >= " + nextCellId + " " +
                "AND FK_SOURCE_ELEMENT_ID <= " + lastCellId;
        // System.out.println("updateTreeBelowYBulk: updateEdgeStartPoingQuery: " + updateEdgeStartPoingQuery);

        String updateEdgeEndPointQuery = "UPDATE " + TableNames.EDGE_TABLE + " " +
                "SET END_Y =  END_Y - " + delta + " " +
                "WHERE END_Y >= " + y + " " +
                "AND FK_TARGET_ELEMENT_ID >= " + nextCellId + " " +
                "AND FK_TARGET_ELEMENT_ID <= " + lastCellId;
        // System.out.println("updateTreeBelowYBulk: updateEdgeEndPointQuery: " + updateEdgeEndPointQuery);

        String updateHighlightsQuery = "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " " +
                "SET START_Y = START_Y - " + delta + " " +
                "WHERE ELEMENT_ID >= " + nextCellId + " " +
                "AND THREAD_ID = " + threadId;

        // System.out.println("EventHandler::updateTreeBelowYBulk: updateHighlightsQuery: " + updateHighlightsQuery);

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

    private void updateAllParentHighlightsOnUI(String clickedCellId, double x, double y, double delta, double deltaX) {
        double finalX = x + BoundBox.unitWidthFactor * 0.5;
        double finalY = y + BoundBox.unitHeightFactor * 0.5;
        graph.getModel().getHighlightsOnUI().forEach((id, rectangleCell) -> {
            // if this is the clicked cell, make highlight unit dimensions.
            if (rectangleCell.getElementId() == Integer.valueOf(clickedCellId)) {
                rectangleCell.setHeight(rectangleCell.getPrefHeight() - delta);
                rectangleCell.setWidth(rectangleCell.getPrefWidth() - deltaX);
            }

            // if this rectangle contains y, then shrink it by delta
            else if (rectangleCell.getBoundsInParent().contains(finalX, finalY)) {
                rectangleCell.setHeight(rectangleCell.getPrefHeight() - delta);

            }
        });
    }

    private void updateChildrenHighlightsInDB(int cellId, boolean isCollapsed, Statement statement, double delta, int nextCellId, int threadId) {
        // System.out.println("EventHandlers.updateChildrenHighlightsInDB: method started");

        if (cellId <= 1) {
            System.out.println("EventHandlers.updateChildrenHighlightsInDB return 1");
            return;
        }

        double startX = 0, startY = 0, width = 0, height = 0;

        double startXOffset = 30;
        double widthOffset = 30;
        double startYOffset = -10;
        double heightOffset = -20;

         if (!isCollapsed) {
            // get all highlights that are contained within the expanded subtree.
            String getHighlightsToResize = "SELECT * " +
                    "FROM HIGHLIGHT_ELEMENT " +
                    "WHERE  ELEMENT_ID > " + cellId + " " +
                    "AND ELEMENT_ID < " + nextCellId + " " +
                    "AND COLLAPSED IN (0, 2) " +
                    "AND HIGHLIGHT_TYPE = 'FULL' " +
                    "AND THREAD_ID = " + threadId;

            // System.out.println(" getHighlightsToResize: " + getHighlightsToResize);

            try (ResultSet getHighlightsRS = DatabaseUtil.select(getHighlightsToResize)) {
                while (getHighlightsRS.next()) {

                    try (ResultSet elementRS = HighlightDAOImpl.selectWhere("ELEMENT_ID = " + getHighlightsRS.getInt("ELEMENT_ID"))) {
                        if (elementRS.next()) {
                            startX = elementRS.getDouble("START_X");
                            startY = elementRS.getDouble("START_Y");
                            width = elementRS.getDouble("WIDTH");
                            height = elementRS.getDouble("HEIGHT");
                            threadId = elementRS.getInt("THREAD_ID");
                        }
                    }

                    // For all the highlights obtained above, adjust their widht so that the highlights cover only the visible cells.
                    String updatChildrenHighlightsQuery = "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " AS H " +
                            "SET H.WIDTH = " +
                            "((SELECT MAX(E1.BOUND_BOX_X_TOP_RIGHT) FROM " + TableNames.ELEMENT_TABLE + " AS E1 " +
                            "JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT ON E1.ID_ENTER_CALL_TRACE = CT.ID " +
                            "WHERE E1.BOUND_BOX_Y_COORDINATE >= " + startY + " " +
                            "AND E1.BOUND_BOX_Y_COORDINATE <= " + ( startY + height) + " " +
                            "AND E1.BOUND_BOX_X_COORDINATE >= " + startX + " " +
                            "AND CT.THREAD_ID = " + threadId + " " +
                            "AND E1.COLLAPSED IN (0, 2)" +
                            ") - H.START_X + " + widthOffset + ") " +
                            "WHERE H.ID = " + getHighlightsRS.getInt("ID");

                    statement.addBatch(updatChildrenHighlightsQuery);

                    System.out.println("EventHandlers.updateChildrenHighlightsInDB For cellId: " + cellId + " updatChildrenHighlightsQuery: " + updatChildrenHighlightsQuery);


                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // System.out.println("EventHandlers.updateChildrenHighlightsInDB: method ended");
    }


    private void updateParentHighlightsInDB(int cellId, boolean isCollapsed, Statement statement, double delta, int nextCellId, double x, double y, int threadId){
        // System.out.println("EventHandlers.updateParentHighlightsInDB: method started");

        if (cellId <= 1) {
            System.out.println("EventHandlers.updateParentHighlightsInDB return 1");
            return;
        }

        // int threadId = 0;
        // double startX = 0, startY = 0, width = 0, height = 0;
        //
        // try (ResultSet rs = HighlightDAOImpl.selectWhere("ELEMENT_ID = " + cellId)) {
        //     if (rs.next()) {
        //         startX = rs.getDouble("START_X");
        //         startY = rs.getDouble("START_Y");
        //         width = rs.getDouble("WIDTH");
        //         height = rs.getDouble("HEIGHT");
        //         threadId = rs.getInt("THREAD_ID");
        //     } else {
        //         System.out.println("EventHandlers.updateParentHighlightsInDB returning");
        //         return;
        //     }
        // } catch (SQLException e) {
        //     e.printStackTrace();
        // }

        // String getHighlightIdsToResize = "SELECT ID " +
        //         "FROM HIGHLIGHT_ELEMENT " +
        //         "WHERE START_X <= " + startX + " " +
        //         "AND START_X + WIDTH >=  " + (startX + width) + " " +
        //         "AND START_Y <= " + startY + " " +
        //         "AND START_Y + HEIGHT >= " + (startY + height) + " " +
        //         "AND ELEMENT_ID < " + cellId + " " +
        //         "AND COLLAPSED = 0 " +
        //         "AND THREAD_ID = " + threadId;


        double startXOffset = 30;
        double widthOffset = 30;
        double startYOffset = -10;
        double heightOffset = -20;

        // get all highlights that contain the clicked cell and belong to the same thread and have element id < clicked cell id.
        // i.e., get all the parent highlights.
        String getHighlightIdsToResize = "SELECT ID " +
                "FROM HIGHLIGHT_ELEMENT " +
                "WHERE START_X <= " + (x+startXOffset) + " " +
                "AND START_X + WIDTH >=  " + (x+startXOffset) + " " +
                "AND START_Y <= " + (y+startYOffset) + " " +
                "AND START_Y + HEIGHT >= " + (y+startYOffset) + " " +
                "AND ELEMENT_ID <= " + cellId + " " +
                "AND COLLAPSED = 0 " +
                "AND HIGHLIGHT_TYPE = 'FULL' " +
                "AND THREAD_ID = " + threadId;

        // System.out.println("EventHandlers.updateParentHighlightsInDB: getHighlightIdsToResize: " + getHighlightIdsToResize);

        try (ResultSet rs = DatabaseUtil.select(getHighlightIdsToResize)) {
            while (rs.next()) {
                // For all the highlights obtained above, adjust their width and height so that the highlights cover only the visible cells.
                String updateHighlightHeight = "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " AS H " +
                        "SET H.HEIGHT = HEIGHT - " + delta + " " +
                        "WHERE H.ID = " + rs.getInt("ID");

                String updateHighlighWidth = "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " AS H " +
                        "SET " +
                        // "H.HEIGHT = HEIGHT - " + delta + ", " +
                        "H.WIDTH = " +
                            "((SELECT MAX(E1.BOUND_BOX_X_TOP_RIGHT) FROM " + TableNames.ELEMENT_TABLE + " AS E1 " +
                            "JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT ON E1.ID_ENTER_CALL_TRACE = CT.ID " +
                            "WHERE E1.BOUND_BOX_Y_COORDINATE >= H.START_Y " +
                            "AND E1.BOUND_BOX_Y_COORDINATE <= (H.START_Y + H.HEIGHT) " +
                            "AND E1.BOUND_BOX_X_COORDINATE >= H.START_X " +
                            // "AND E1.BOUND_BOX_X_COORDINATE <= (H.START_X + H.WIDTH) " +
                            "AND CT.THREAD_ID = " + threadId + " " +
                            // "AND E1.COLLAPSED IN (0, 2)" +
                            "AND (E1.COLLAPSED = 0 OR E1.COLLAPSED = 2)" +
                            ") - H.START_X + " + widthOffset + ") " +

                        // "((SELECT MAX(H1.START_X + H1.WIDTH)" +
                        // "FROM HIGHLIGHT_ELEMENT AS H1 " +
                        // "WHERE H1.START_Y >= (SELECT H2.START_Y " +
                        // "FROM HIGHLIGHT_ELEMENT AS H2 " +
                        // "WHERE H2.ID = H.ID) " +
                        // "AND (H1.START_Y + H1.HEIGHT) <= (SELECT H3.START_Y + H3.HEIGHT " +
                        // "FROM HIGHLIGHT_ELEMENT AS H3 " +
                        // "WHERE H3.ID = H.ID) " +
                        // "AND H1.THREAD_ID = (SELECT H4.THREAD_ID " +
                        // "FROM HIGHLIGHT_ELEMENT AS H4 " +
                        // "WHERE H4.ID = H.ID) " +
                        // "AND H1.ID != H.ID " +
                        // "AND H1.COLLAPSED = 0) " +
                        // "- H.START_X) " +

                        "WHERE H.ID = " + rs.getInt("ID");

                statement.addBatch(updateHighlightHeight);
                statement.addBatch(updateHighlighWidth);

                System.out.println("EventHandlers.updateParentHighlightsInDB For cellId: " + cellId + " updateHighlightHeight " + updateHighlightHeight);
                System.out.println("EventHandlers.updateParentHighlightsInDB For cellId: " + cellId + " updateHighlighWidth: " + updateHighlighWidth);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // System.out.println("EventHandlers.updateParentHighlightsInDB method ends");
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