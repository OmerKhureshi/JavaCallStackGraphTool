package com.csgt.controller;

import com.csgt.Main;
import com.csgt.dataaccess.DAO.*;
import com.csgt.dataaccess.DTO.BookmarkDTO;
import com.csgt.dataaccess.DTO.ElementDTO;
import com.csgt.dataaccess.DatabaseUtil;
import com.csgt.dataaccess.TableNames;
import com.csgt.presentation.graph.NodeCell;
import com.csgt.presentation.graph.BoundBox;
import com.csgt.presentation.graph.HighlightCell;
import com.csgt.controller.modules.ElementTreeModule;
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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;
import org.controlsfx.glyphfont.Glyph;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class handles all events related to the nodes such as info button and collapse button.
 */
public class EventHandlers {

    private static ElementTreeModule elementTreeModule;
    final DragContext dragContext = new DragContext();
    private boolean allowClicks = true;
    public PopOver popOver = new PopOver();

    public void setCustomMouseEventHandlers(final Node node) {
        ((NodeCell)node).getInfoStackPane().setOnMousePressed(infoButtonOnClickEventHandler);
        ((NodeCell)node).getMinMaxStackPane().setOnMousePressed(minMaxButtonOnClickEventHandler);
    }

    private EventHandler<MouseEvent> infoButtonOnClickEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            if (popOver != null) {
                popOver.hide();
            }

            Node node = (Node) event.getSource();
            NodeCell cell = (NodeCell) node.getParent();

            String timeStamp;
            int elementId, methodId, processId, threadId, collapsed;
            String parameters, packageName = "", methodName = "", parameterTypes = "", eventType, lockObjectId;
            double xCord, yCord;


            String sql = "Select E.ID as EID, TIME_INSTANT, METHOD_ID, PROCESS_ID, THREAD_ID, PARAMETERS, COLLAPSED, " +
                    "MESSAGE, LOCKOBJID, BOUND_BOX_X_COORDINATE, BOUND_BOX_Y_COORDINATE from " + TableNames.ELEMENT_TABLE + " AS E " +
                    "JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT ON CT.id = E.ID_ENTER_CALL_TRACE " +
                    "WHERE E.ID = " + cell.getCellId();

            try (ResultSet callTraceRS = DatabaseUtil.select(sql)) {
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


                    try (ResultSet methodDefRS = MethodDefDAOImpl.selectWhere("id = " + methodId)) {
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
                        e.printStackTrace();
                    }

                    // Save the clicked element into recent menu.
                    // graph.addToRecent(packageName + "." + methodName, new XYCoordinate(xCord, yCord, threadId));


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
                     **/

                    List<Integer> ctIdList = new ArrayList<>();
                    List<Integer> eleIdList = new ArrayList<>();
                    if (eventType.equalsIgnoreCase("WAIT-ENTER")) {
                        int ctId = -2;  // Will throw exception if value not changed. Which is what we want.
                        sql = "lockobjid = '" + lockObjectId + "'" +
                                " AND (message = 'NOTIFY-ENTER' OR message = 'NOTIFYALL-ENTER')" +
                                " AND time_instant >= " + "'" + timeStamp + "'";

                        // // get thread ids of nodes that may acquire the lock that was just released.
                        // CallTraceDAOImpl.getThreadIdsWhere(sql).stream().forEach(id -> {
                        //     ctIdList.add(id);
                        // });

                        try (ResultSet rs = CallTraceDAOImpl.getWhere(sql)) {
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
                        String query = "SELECT E.ID AS EID, bound_box_x_coordinate, bound_box_y_coordinate, THREAD_ID, collapsed " +
                                "FROM CALL_TRACE AS CT " +
                                "JOIN ELEMENT AS E ON CT.ID = E.ID_ENTER_CALL_TRACE " +
                                "WHERE E.ID = " + eId;
                        try (ResultSet elementRS = DatabaseUtil.select(query)) {
                            // try (ResultSet elementRS = ElementDAOImpl.getWhere("id = " + eId)){
                            if (elementRS.next()) {
                                int id = elementRS.getInt("EID");
                                String targetThreadId = String.valueOf(elementRS.getInt("thread_id"));
                                float xCoordinate = elementRS.getFloat("bound_box_x_coordinate");
                                float yCoordinate = elementRS.getFloat("bound_box_y_coordinate");
                                int targetElementCollapsed = elementRS.getInt("collapsed");

                                // go to location.
                                Button jumpToButton = new Button();
                                jumpToButton.setOnMouseClicked(event1 -> {
                                    System.out.println("EventHandlers.handle: jumpToButton Clicked. for eleId: " + eId);
                                    jumpTo(String.valueOf(eId), targetThreadId, targetElementCollapsed);
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

                    // Add Bookmark button
                    // Group bookmarkGroup = new Group();
                    final Color[] bookmarkColor = new Color[1];
                    bookmarkColor[0] = Color.INDIANRED;

                    ColorPicker bookmarkColorPicker = new ColorPicker(Color.INDIANRED);
                    bookmarkColorPicker.getStyleClass().add("button");
                    bookmarkColorPicker.setStyle(
                            "-fx-color-label-visible: false; " +
                                    "-fx-background-radius: 15 15 15 15;");
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
                        BookmarkDTO bookmarkDTO = new BookmarkDTO(
                                String.valueOf(elementId),
                                String.valueOf(threadId),
                                finalMethodNameTemp,
                                bookmarkColor[0].toString(),
                                xCord,
                                yCord,
                                collapsed);

                        ControllerLoader.menuController.insertBookmark(bookmarkDTO);

                        removeBookmarkButton.setDisable(false);
                        addBookmarkButton.setDisable(true);
                    });

                    boolean contains = ControllerLoader.menuController.getBookmarkDTOs().containsKey(String.valueOf(elementId));
                    addBookmarkButton.setDisable(contains);
                    removeBookmarkButton.setDisable(!contains);

                    removeBookmarkButton.setOnMouseClicked(eve -> {
                        ControllerLoader.menuController.deleteBookmark(String.valueOf(elementId));
                        addBookmarkButton.setDisable(false);
                    });

                    HBox hBox = new HBox();
                    hBox.setSpacing(5);
                    hBox.getChildren().addAll(bookmarkColorPicker, addBookmarkButton, removeBookmarkButton);

                    gridPane.add(hBox, 1, rowIndex++);
                    // gridPane.add(bookmarkColorPicker, 1, rowIndex++);
                    // gridPane.add(addBookmarkButton, 1, rowIndex++);
                    // gridPane.add(removeBookmarkButton, 1, rowIndex++);

                    popOver = new PopOver(gridPane);
                    popOver.setAnimated(true);
                    // popOver.detach();
                    // popOver.setAutoHide(true);
                    popOver.setConsumeAutoHidingEvents(false);
                    popOver.show(node);

                }
            } catch (SQLException e) {
                System.out.println("Line that threw exception: " + sql);
                e.printStackTrace();
            }
        }
    };


// backup if i mess up the above method.
    // private EventHandler<MouseEvent> infoButtonOnClickEventHandler = new EventHandler<MouseEvent>() {
    //
    //     @Override
    //     public void handle(MouseEvent event) {
    //         if (popOver != null) {
    //             popOver.hide();
    //         }
    //
    //         Node node = (Node) event.getSource();
    //         NodeCell cell = (NodeCell) node.getParent();
    //
    //         String timeStamp;
    //         int elementId, methodId, processId, threadId, collapsed;
    //         String parameters, packageName = "", methodName = "", parameterTypes = "", eventType, lockObjectId;
    //         double xCord, yCord;
    //
    //
    //         String sql = "Select E.ID as EID, TIME_INSTANT, METHOD_ID, PROCESS_ID, THREAD_ID, PARAMETERS, COLLAPSED, " +
    //                 "MESSAGE, LOCKOBJID, BOUND_BOX_X_COORDINATE, BOUND_BOX_Y_COORDINATE from " + TableNames.ELEMENT_TABLE + " AS E " +
    //                 "JOIN " + TableNames.CALL_TRACE_TABLE + " AS CT ON CT.id = E.ID_ENTER_CALL_TRACE " +
    //                 "WHERE E.ID = " + cell.getCellId();
    //
    //         System.out.println("EventHandlers.handle first sql: " + sql);
    //
    //         try (ResultSet callTraceRS = DatabaseUtil.select(sql)) {
    //             if (callTraceRS.next()) {
    //                 elementId = callTraceRS.getInt("EID");
    //                 timeStamp = callTraceRS.getString("time_instant");
    //                 methodId = callTraceRS.getInt("method_id");
    //                 processId = callTraceRS.getInt("process_id");
    //                 threadId = callTraceRS.getInt("thread_id");
    //                 parameters = callTraceRS.getString("parameters");
    //                 eventType = callTraceRS.getString("message");
    //                 lockObjectId = callTraceRS.getString("lockobjid");
    //                 xCord = callTraceRS.getFloat("bound_box_x_coordinate");
    //                 yCord = callTraceRS.getFloat("bound_box_y_coordinate");
    //                 collapsed = callTraceRS.getInt("COLLAPSED");
    //
    //
    //                 try (ResultSet methodDefRS = MethodDefDAOImpl.selectWhere("id = " + methodId)) {
    //                     if (methodDefRS.next()) {
    //                         packageName = methodDefRS.getString("package_name");
    //                         methodName = methodDefRS.getString("method_name");
    //                         parameterTypes = methodDefRS.getString("parameter_types");
    //                     }
    //
    //                     if (methodId == 0) {
    //                         methodName = eventType;
    //                         packageName = "N/A";
    //                         parameterTypes = "N/A";
    //                         parameters = "N/A";
    //                     }
    //                 } catch (SQLException e) {
    //                     e.printStackTrace();
    //                 }
    //
    //                 // Save the clicked element into recent menu.
    //                 // graph.addToRecent(packageName + "." + methodName, new XYCoordinate(xCord, yCord, threadId));
    //
    //
    //                 Label lMethodName = new Label(methodName);
    //                 Label lPackageName = new Label(packageName);
    //                 Label lParameterTypes = new Label(parameterTypes);
    //                 Label lParameters = new Label(parameters);
    //                 Label lProcessId = new Label(String.valueOf(processId));
    //                 Label lThreadId = new Label(String.valueOf(threadId));
    //                 Label lTimeInstant = new Label(timeStamp);
    //
    //                 GridPane gridPane = new GridPane();
    //                 gridPane.setPadding(new Insets(10, 10, 10, 10));
    //                 gridPane.setVgap(10);
    //                 gridPane.setHgap(20);
    //                 gridPane.add(new Label("Method Name: "), 0, 0);
    //                 gridPane.add(lMethodName, 1, 0);
    //
    //                 gridPane.add(new Label("Package Name: "), 0, 1);
    //                 gridPane.add(lPackageName, 1, 1);
    //
    //                 gridPane.add(new Label("Parameter Types: "), 0, 2);
    //                 gridPane.add(lParameterTypes, 1, 2);
    //
    //                 gridPane.add(new Label("Parameters: "), 0, 3);
    //                 gridPane.add(lParameters, 1, 3);
    //
    //                 gridPane.add(new Label("Process ID: "), 0, 4);
    //                 gridPane.add(lProcessId, 1, 4);
    //
    //                 gridPane.add(new Label("Thread ID: "), 0, 5);
    //                 gridPane.add(lThreadId, 1, 5);
    //
    //                 gridPane.add(new Label("Time of Invocation: "), 0, 6);
    //                 gridPane.add(lTimeInstant, 1, 6);
    //
    //
    //                 /*
    //                  * wait-enter -> lock released.
    //                  *       Get all elements with same lock id and notify-enter
    //                  * wait-exit -> lock reacquired.
    //                  *
    //                  * notify-enter / notify-exit -> lock released
    //                  *
    //                  * object lock flow:
    //                  * wait-enter -> notify-enter / notify-exit -> wait-exit
    //                  **/
    //
    //                 List<Integer> ctIdList = new ArrayList<>();
    //                 List<Integer> eleIdList = new ArrayList<>();
    //                 if (eventType.equalsIgnoreCase("WAIT-ENTER")) {
    //                     int ctId = -2;  // Will throw exception if value not changed. Which is what we want.
    //                     sql = "lockobjid = '" + lockObjectId + "'" +
    //                             " AND (message = 'NOTIFY-ENTER' OR message = 'NOTIFYALL-ENTER')" +
    //                             " AND time_instant >= " + "'" + timeStamp + "'";
    //
    //                     CallTraceDAOImpl.getThreadIdsWhere(sql).stream().forEach(id -> {
    //                         ctIdList.add(id);
    //                     });
    //
    //                     try (ResultSet elementRS = ElementDAOImpl.selectWhere("id_enter_call_trace = " + ctId)) {
    //                         // Expecting to see a single row.
    //                         if (elementRS.next()) {
    //                             int eId = elementRS.getInt("id");
    //                             eleIdList.add(eId);
    //                         }
    //                     }
    //                 } else if (eventType.equalsIgnoreCase("NOTIFY-ENTER")) {
    //
    //                     try (Connection conn = DatabaseUtil.getConnection(); Statement ps = conn.createStatement()) {
    //                         sql = "SELECT * FROM " + TableNames.CALL_TRACE_TABLE + " AS parent\n" +
    //                                 "WHERE MESSAGE = 'WAIT-EXIT' \n" +
    //                                 "AND LOCKOBJID = '" + lockObjectId + "' " +
    //                                 "AND TIME_INSTANT >= '" + timeStamp + "' \n" +
    //                                 "AND (SELECT count(*) \n" +
    //                                 "FROM " + TableNames.CALL_TRACE_TABLE + " AS child \n" +
    //                                 "WHERE child.message = 'WAIT-ENTER' \n" +
    //                                 "AND LOCKOBJID = '" + lockObjectId + "' " +
    //                                 "AND child.TIME_INSTANT >=  '" + timeStamp + "' \n" +
    //                                 "AND child.TIME_INSTANT <= parent.time_instant\n" +
    //                                 ")\n" +
    //                                 "= 0\n";
    //
    //                         // System.out.println("Sql: " + sql);
    //                         int ctId = -2;
    //                         try (ResultSet resultSet = ps.executeQuery(sql)) {
    //                             if (resultSet.next()) {
    //                                 ctId = resultSet.getInt("id");
    //                                 ctIdList.add(ctId);
    //                             }
    //                         }
    //
    //                         try (ResultSet elementRS = ElementDAOImpl.selectWhere("id_exit_call_trace = " + ctId)) {
    //                             // Expecting to see a single row.
    //                             if (elementRS.next()) {
    //                                 int eId = elementRS.getInt("id");
    //                                 eleIdList.add(eId);
    //                             }
    //                         }
    //                     }
    //
    //                 } else if (eventType.equalsIgnoreCase("NOTIFYALL-ENTER")) {
    //                     try (Connection conn = DatabaseUtil.getConnection();
    //                          Statement ps = conn.createStatement()) {
    //
    //
    //                         sql = "SELECT * FROM " + TableNames.CALL_TRACE_TABLE + " AS parent WHERE MESSAGE = 'WAIT-EXIT' " +
    //                                 "AND LOCKOBJID = '" + lockObjectId + "' " +
    //                                 "AND TIME_INSTANT >= '" + timeStamp + "' " +
    //                                 "AND (SELECT count(*) FROM " + TableNames.CALL_TRACE_TABLE + " AS child " +
    //                                 "WHERE child.message = 'WAIT-ENTER' " +
    //                                 "AND LOCKOBJID = '" + lockObjectId + "' " +
    //                                 "AND child.TIME_INSTANT >= '" + timeStamp + "' " +
    //                                 "AND child.TIME_INSTANT <= parent.time_instant ) = 0";
    //
    //                         int ctId = -2;
    //
    //                         try (ResultSet resultSet = ps.executeQuery(sql)) {
    //                             while (resultSet.next()) {
    //                                 ctId = resultSet.getInt("id");
    //                                 ctIdList.add(ctId);
    //                             }
    //                         }
    //
    //                         ctIdList.stream().forEach(id -> {
    //                             try (ResultSet elementRS = ElementDAOImpl.selectWhere("id_exit_call_trace = " + id)) {
    //                                 // Can be more than a single row.
    //                                 while (elementRS.next()) {
    //                                     int eId = elementRS.getInt("id");
    //                                     eleIdList.add(eId);
    //                                 }
    //                             } catch (SQLException e) {
    //                             }
    //                         });
    //                     }
    //                 }
    //
    //                 List<Button> buttonList = new ArrayList<>();
    //                 String finalPackageName = packageName;
    //                 String finalMethodName = methodName;
    //                 eleIdList.stream().forEach(eId -> {
    //                     String query = "SELECT E.ID AS EID, bound_box_x_coordinate, bound_box_y_coordinate, THREAD_ID " +
    //                             "FROM CALL_TRACE AS CT " +
    //                             "JOIN ELEMENT AS E ON CT.ID = E.ID_ENTER_CALL_TRACE " +
    //                             "WHERE E.ID = " + eId;
    //                     try (ResultSet elementRS = DatabaseUtil.select(query)) {
    //                         // try (ResultSet elementRS = ElementDAOImpl.getWhere("id = " + eId)){
    //                         if (elementRS.next()) {
    //                             int id = elementRS.getInt("EID");
    //                             String targetThreadId = String.valueOf(elementRS.getInt("thread_id"));
    //                             float xCoordinate = elementRS.getFloat("bound_box_x_coordinate");
    //                             float yCoordinate = elementRS.getFloat("bound_box_y_coordinate");
    //
    //                             // go to location.
    //                             Button jumpToButton = new Button();
    //                             jumpToButton.setOnMouseClicked(event1 -> {
    //                                 System.out.println("EventHandlers.handle: jumpToButton Clicked. for eleId: " + eId);
    //                                 jumpTo(String.valueOf(eId), targetThreadId, collapsed);
    //                             });
    //                             buttonList.add(jumpToButton);
    //                         }
    //                     } catch (SQLException e) {
    //                         e.printStackTrace();
    //                     }
    //                 });
    //
    //                 String message = "", actionMsg = "";
    //                 switch (eventType.toUpperCase()) {
    //                     case "WAIT-ENTER":
    //                         message = "Wait method was invoked and therefore, \nthe lock on object ( object id = " + lockObjectId + ") \nwas released and reaquired here.";
    //                         actionMsg = "Go to Notify or NotifyAll \nmethods invocations.";
    //                         break;
    //
    //                     case "NOTIFY-ENTER":
    //                         message = "Notify method was invoked and therefore, \nthe lock on object ( object id = " + lockObjectId + ") \nwas released here.";
    //                         actionMsg = "Go to wait \nmethods invocations.";
    //                         break;
    //                     case "NOTIFYALL-ENTER":
    //                         message = "NotifyAll method was invoked and therefore, \nthe lock on object ( object id = " + lockObjectId + ") \nwas released here.";
    //                         actionMsg = "Go to wait \nmethods invocations.";
    //                         break;
    //                 }
    //                 Label labelMessage = new Label(message);
    //                 labelMessage.setWrapText(true);
    //
    //                 Label labelActionMsg = new Label(actionMsg);
    //
    //                 gridPane.add(labelMessage, 0, 7);
    //                 gridPane.add(labelActionMsg, 0, 8);
    //                 int rowIndex = 8;
    //                 for (Button button : buttonList) {
    //                     button.setText("Goto node");
    //                     gridPane.add(button, 1, rowIndex++);
    //                 }
    //
    //                 // Add Bookmark button
    //                 // Group bookmarkGroup = new Group();
    //                 final Color[] bookmarkColor = new Color[1];
    //                 bookmarkColor[0] = Color.INDIANRED;
    //
    //                 ColorPicker bookmarkColorPicker = new ColorPicker(Color.INDIANRED);
    //                 bookmarkColorPicker.getStyleClass().add("button");
    //                 bookmarkColorPicker.setStyle(
    //                         "-fx-color-label-visible: false; " +
    //                                 "-fx-background-radius: 15 15 15 15;");
    //                 bookmarkColorPicker.setOnAction(e -> {
    //                     bookmarkColor[0] = bookmarkColorPicker.getValue();
    //                 });
    //                 // bookmarkColorPicker.getStyleClass().add("button");
    //                 // bookmarkColorPicker.setStyle(
    //                 //         "-fx-color-label-visible: false; " +
    //                 //                 "-fx-background-radius: 15 15 15 15;");
    //
    //                 Button addBookmarkButton = new Button("Add Bookmark");
    //                 Button removeBookmarkButton = new Button("Remove bookmark");
    //
    //                 String finalMethodNameTemp = methodName;
    //                 addBookmarkButton.setOnMouseClicked(event1 -> {
    //                     BookmarkDTO bookmarkDTO = new BookmarkDTO(
    //                             String.valueOf(elementId),
    //                             String.valueOf(threadId),
    //                             finalMethodNameTemp,
    //                             bookmarkColor[0].toString(),
    //                             xCord,
    //                             yCord,
    //                             collapsed);
    //
    //                     ControllerLoader.menuController.insertBookmark(bookmarkDTO);
    //
    //                     removeBookmarkButton.setDisable(false);
    //                     addBookmarkButton.setDisable(true);
    //                 });
    //
    //                 boolean contains = ControllerLoader.menuController.getBookmarkDTOs().containsKey(String.valueOf(elementId));
    //                 addBookmarkButton.setDisable(contains);
    //                 removeBookmarkButton.setDisable(!contains);
    //
    //                 removeBookmarkButton.setOnMouseClicked(eve -> {
    //                     ControllerLoader.menuController.deleteBookmark(String.valueOf(elementId));
    //                     addBookmarkButton.setDisable(false);
    //                 });
    //
    //                 HBox hBox = new HBox();
    //                 hBox.getChildren().addAll(bookmarkColorPicker, addBookmarkButton, removeBookmarkButton);
    //
    //                 gridPane.add(hBox, 1, rowIndex++);
    //                 // gridPane.add(bookmarkColorPicker, 1, rowIndex++);
    //                 // gridPane.add(addBookmarkButton, 1, rowIndex++);
    //                 // gridPane.add(removeBookmarkButton, 1, rowIndex++);
    //
    //                 popOver = new PopOver(gridPane);
    //                 popOver.setAnimated(true);
    //                 // popOver.detach();
    //                 // popOver.setAutoHide(true);
    //                 popOver.setConsumeAutoHidingEvents(false);
    //                 popOver.show(node);
    //
    //             }
    //         } catch (SQLException e) {
    //             System.out.println("Line that threw exception: " + sql);
    //             e.printStackTrace();
    //         }
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

    private EventHandler<MouseEvent> minMaxButtonOnClickEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            System.out.println("EventHandlers.handle. minMaxButtonOnClickEventHandler   ");
            NodeCell cell = ((NodeCell) ((Node) event.getSource()).getParent());
            minMaxButtonOnClick(cell, ControllerLoader.centerLayoutController.getCurrentThreadId());
        }
    };

    private void minMaxButtonOnClick(NodeCell clickedCell, String threadId) {
        {
            if (popOver != null) {
                popOver.hide();
            }

            if (!allowClicks) {
                System.out.println(">>>>>>>>>>>>>>>>>>> Clickable is false. <<<<<<<<<<<<<<<<<<<<<");
                onClickWhenDisabled();
                return;
            }

            disableClicks();

            String clickedCellID = clickedCell.getCellId();
            ElementDTO clickedElementDTO = ElementDAOImpl.getElementDTO(clickedCellID);
            int collapsed = clickedElementDTO.getCollapsed();

            // Collapsed value -> description
            // 0               -> visible     AND  uncollapsed
            // 2               -> visible     AND  collapsed
            // >2              -> not visible AND  collapsed
            // <0              -> not visible AND  collapsed

            if (collapsed == 0) {
                System.out.println();
                System.out.println("EventHandlers.minMaxButtonOnClick: clicked on : " + clickedCellID);
                System.out.println("EventHandlers.minMaxButtonOnClick: collapsed == 0: collapsing now.");
                // visible and uncollapsed --> visible and collapsed
                // this cell only: 0 ->   2
                // all other cells: >2 -> ++1
                // all other cells: <0 -> --1

                // ((Circle) clickedCell.getChildren().get(0)).setFill(Color.BLUE);
                // ((Circle) ( (Group)cell.getView() )
                //             .getChildren().get(0))
                //             .setFill(Color.BLUE);
                // cell.getChildren().get(0).setStyle("-fx-background-color: blue");
                // cell.setStyle("-fx-background-color: blue");
                //
                int nextCellId = ElementDAOImpl.getNextLowerSiblingOrAncestorNode(clickedElementDTO, threadId);
                int lastCellId = ElementDAOImpl.getLowestCellInThread(threadId);
                // delta is only meaningful for a node when minimizing the tree at that node.
                // And is also used later when expanding at that node.
                float newDeltaY = clickedElementDTO.getBoundBoxYBottomLeft() - clickedElementDTO.getBoundBoxYTopLeft() - BoundBox.unitHeightFactor;
                float newDeltaX = ElementDAOImpl.calculateNewDeltaX(clickedElementDTO, String.valueOf(nextCellId));
                double clickedCellBottomY = clickedElementDTO.getBoundBoxYTopLeft() + BoundBox.unitHeightFactor;

                /*
                   update collapse and delta values in dataaccess.
                */
                clickedElementDTO.setDeltaY(newDeltaY);
                clickedElementDTO.setDeltaX(newDeltaX);
                clickedElementDTO.setCollapsed(2);
                ElementDAOImpl.updateCollapseAndDelta(clickedElementDTO);

                // update UI.
                ControllerLoader.canvasController.removeUIComponentsBetween(clickedElementDTO, nextCellId);
                ControllerLoader.canvasController.moveLowerTreeByDelta(clickedElementDTO);

                updateDBInBackgroundThread(clickedElementDTO, true, nextCellId, Integer.valueOf(threadId), lastCellId);

                clickedCell.setCollapsed(2);
            } else if (collapsed == 2) {
                System.out.println();
                System.out.println("EventHandlers.minMaxButtonOnClick: clicked on : " + clickedCellID);
                System.out.println("EventHandlers.minMaxButtonOnClick: collapsed == 2: expanding now.");
                // visible and collapsed --> visible and uncollapsed
                // this cell only :  2 ->   0
                // all other cells: >2 -> --1
                // all other cells: <0 -> ++1

                expandTreeAt(clickedElementDTO, threadId);
                clickedCell.setCollapsed(0);
            }
        }
    }


    private void expandTreeAt(ElementDTO clickedEleDTO, String threadId) {
        float deltaY = clickedEleDTO.getDeltaY();
        float deltaX = clickedEleDTO.getDeltaX();

        int nextCellId = ElementDAOImpl.getNextLowerSiblingOrAncestorNode(clickedEleDTO, threadId);
        int lastCellId = ElementDAOImpl.getLowestCellInThread(threadId);

        double clickedCellBottomY = clickedEleDTO.getBoundBoxYTopLeft() + BoundBox.unitHeightFactor;
        double newClickedCellBottomY = clickedEleDTO.getBoundBoxYTopLeft() + BoundBox.unitHeightFactor + deltaY;

        clickedEleDTO.setDeltaY(-deltaY);
        clickedEleDTO.setDeltaX(-deltaX);

        ControllerLoader.canvasController.moveLowerTreeByDelta(clickedEleDTO);

        updateAllParentHighlightsOnUI(clickedEleDTO);

        clickedEleDTO.setCollapsed(0);
        ElementDAOImpl.updateCollapse(clickedEleDTO);

        updateDBInBackgroundThread(clickedEleDTO, false, nextCellId, Integer.valueOf(threadId), lastCellId);
    }


    private void expandParentTreeChain(ElementDTO elementDTO , String threadId) {
        List<ElementDTO> parentElementDTOs = ElementDAOImpl.getAllParentElementDTOs(elementDTO, threadId);

        parentElementDTOs.stream()
                .map(eleDTO -> {
                    eleDTO.setDeltaY(0);
                    eleDTO.setDeltaX(0);
                    return eleDTO;
                })
                .forEach(eleDTO -> {
                    expandTreeAt(eleDTO, threadId);
                });
    }

    public void jumpTo(String cellId, String threadId, int collapsed) {
        // make changes in DB if needed
        if (collapsed != 0) {
            ElementDTO elementDTO = ElementDAOImpl.getElementDTO(cellId);
            expandParentTreeChain(elementDTO, threadId);
        }

        ControllerLoader.centerLayoutController.switchCurrentThread(threadId);

        try (ResultSet rs = ElementDAOImpl.selectWhere("ID = " + cellId)){
            if (rs.next()) {
                double xCord = rs.getDouble("BOUND_BOX_X_COORDINATE");
                double yCord = rs.getDouble("BOUND_BOX_Y_COORDINATE");
                ControllerLoader.canvasController.moveScrollPane(xCord, yCord);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Platform.runLater(() -> {
        //     if (ControllerLoader.canvasController.nodeCellsOnUI.containsKey(cellId)) {
        //         System.out.println("EventHandlers.jumpTo:::: blinking cell....");
        //         ControllerLoader.canvasController.nodeCellsOnUI.get(cellId).blink();
        //     }
        // });
    }


    private void enableClicks() {
        allowClicks = true;
        ControllerLoader.statusBarController.setStatusText("Ready ");
    }

    private void disableClicks() {
        allowClicks = false;
        ControllerLoader.statusBarController.setStatusText("Processing ...");
    }

    private void onClickWhenDisabled() {
        if (!allowClicks) {
            ControllerLoader.statusBarController.setTimedStatusText(
                    "Please wait while the graphs loads ...",
                    "Processing ...",
                    3 * 1000);
        }
    }

    private void updateDBInBackgroundThread(ElementDTO clickedEleDTO, boolean isCollapsed, int nextCellId, int threadId, int lastCellId) {

        int clickedCellId = Integer.valueOf(clickedEleDTO.getId());
        double topY = clickedEleDTO.getBoundBoxYTopLeft();
        double bottomY = clickedEleDTO.getBoundBoxYBottomLeft();
        double leftX = clickedEleDTO.getBoundBoxXTopLeft();
        double rightX = clickedEleDTO.getBoundBoxXTopRight();
        double deltaY = clickedEleDTO.getDeltaY();
        double newDeltaX = clickedEleDTO.getDeltaX();
        int parentId = Integer.valueOf(clickedEleDTO.getParentId());


        List<String> queryList = new ArrayList<>();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                // get queries to update collapse values for cells, edges and highlights.
                queryList.addAll(getSubTreeUpdateQueries(clickedEleDTO, isCollapsed, nextCellId, threadId));

                // No upate required for single line children
                if (deltaY == 0) {
                    System.out.println("Optimized for single line children collapses.");
                    addParentHighlightResizeQueries(clickedEleDTO, queryList, threadId);
                    DatabaseUtil.executeQueryList(queryList);

                    return null;
                }

                addParentChainUpdateQueryRecursive(clickedEleDTO, deltaY, queryList);

                // if there is a lower sibling node, update the tree below.
                if (nextCellId != Integer.MAX_VALUE) {
                    getLowerTreeeUpdateQueries(topY + BoundBox.unitHeightFactor, deltaY, queryList, nextCellId, lastCellId, threadId);
                }

                addParentHighlightResizeQueries(clickedEleDTO, queryList, threadId);
                addChildrenHighlightResizeQueries(clickedEleDTO, isCollapsed, queryList, nextCellId, threadId);

                System.out.println("EventHandlers.call: printing queries");
                queryList.forEach(query -> System.out.println(query));
                System.out.println();

                DatabaseUtil.executeQueryList(queryList);

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();

                enableClicks();
                ControllerLoader.canvasController.clearAndUpdate();

                System.out.println("==================== updateDBInBackgroundThread Successful. ====================");
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

    private List<String> getSubTreeUpdateQueries(ElementDTO elementDTO, boolean isMinimized, int endCellId, int threadId) {

        // Collapsed value -> description
        // 0               -> visible     AND  uncollapsed
        // 2               -> visible     AND  collapsed
        // >2              -> not visible AND  collapsed
        // <0              -> not visible AND  collapsed

        List<String> queryList = new ArrayList<>();

        int startCellId = Integer.valueOf(elementDTO.getId());
        double topY = elementDTO.getBoundBoxYTopLeft();
        double bottomY = elementDTO.getBoundBoxYBottomLeft();
        double leftX = elementDTO.getBoundBoxXTopLeft();

        String updateCellQuery;
        String updateEdgeQuery;
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
                    "E.ID > " + startCellId + " " +
                    "AND E.ID < " + endCellId + " " +
                    "AND EXISTS (SELECT * FROM " + TableNames.CALL_TRACE_TABLE + " AS CT " +
                    "WHERE CT.ID = E.ID_ENTER_CALL_TRACE AND " +
                    "CT.THREAD_ID = " + threadId + ")";

            // Update the collapse value in the subtree rooted at the clicked cell.
            updateEdgeQuery = "UPDATE " + TableNames.EDGE_TABLE + " " +
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
                    ")";

            updateHighlightsQuery = "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " " +
                    "SET COLLAPSED = 1 " +
                    "WHERE ELEMENT_ID > " + startCellId + " " +
                    "AND ELEMENT_ID < " + endCellId;
        } else {
            updateCellQuery = "UPDATE " + TableNames.ELEMENT_TABLE + " AS E " +
                    "SET E.COLLAPSED = " +
                    "CASE " +
                    "WHEN E.COLLAPSED < 0 THEN E.COLLAPSED + 1 " +
                    "WHEN E.COLLAPSED > 2 THEN E.COLLAPSED - 1 " +
                    "ELSE E.COLLAPSED " +
                    "END " +
                    "WHERE " +
                    "E.ID > " + startCellId + " " +
                    "AND E.ID < " + endCellId + " " +
                    "AND EXISTS (SELECT * FROM " + TableNames.CALL_TRACE_TABLE + " AS CT " +
                    "WHERE CT.ID = E.ID_ENTER_CALL_TRACE AND " +
                    "CT.THREAD_ID = " + threadId + ")";

            // Update the collapse value in the subtree rooted at the clicked cell.
            updateEdgeQuery = "UPDATE " + TableNames.EDGE_TABLE + " " +
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

            updateHighlightsQuery = "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " " +
                    "SET COLLAPSED = 0 " +
                    "WHERE ELEMENT_ID IN " +
                    "(SELECT ID FROM " + TableNames.ELEMENT_TABLE + " " +
                    "WHERE ID > " + startCellId + " " +
                    "AND ID < " + endCellId + " " +
                    "AND (COLLAPSED = 0 OR COLLAPSED = 2)" +
                    ")";
        }

        queryList.add(updateCellQuery);
        queryList.add(updateEdgeQuery);
        queryList.add(updateHighlightsQuery);

        return queryList;
    }

    private void getLowerTreeeUpdateQueries(double y, double delta, List<String> queryList, int nextCellId, int lastCellId, int threadId) {
        queryList.add(ElementDAOImpl.getUpdateElementQueryAfterCollapse(y, delta, nextCellId, lastCellId));
        queryList.add(EdgeDAOImpl.getUpdateEdgeStartPointQuery(y, delta, nextCellId, lastCellId));
        queryList.add(EdgeDAOImpl.getUpdateEdgeEndPointQuery(y, delta, nextCellId, lastCellId));
        queryList.add(HighlightDAOImpl.getUpdateHighlightQuery(y, delta, nextCellId, lastCellId, threadId));
    }

    /**
     * This method gets cell's BOUND_BOX_Y_BOTTOM_LEFT and calculates it's new value.
     * Then updates cell's BOUND_BOX_Y_BOTTOM_LEFT and BOUND_BOX_Y_BOTTOM_RIGHT values.
     * Recurse to the parent and updates it's BOUND_BOX_Y_BOTTOM_LEFT and BOUND_BOX_Y_BOTTOM_RIGHT values.
     *
     * @param elementDTO    The id of cell where recursive updateIfNeeded starts.
     * @param deltaForParentChain     The value to be subtracted from or added to the columns.
     * @param deltaForParentChain All updated queries are added to this statement as batch.
     */
    private static void addParentChainUpdateQueryRecursive(ElementDTO elementDTO, double deltaForParentChain, List<String> queryList) {

        if (elementDTO == null) {
            return;
        }

        int cellId = Integer.valueOf(elementDTO.getId());
        double deltaY = deltaForParentChain;

        // Stop reccursion if root is reached.
        if (cellId == 1) {
            return;
        }

        // Update this cells bottom y values
        String updateCurrentCell = "UPDATE " + TableNames.ELEMENT_TABLE + " " +
                "SET bound_box_y_bottom_left = bound_box_y_bottom_left - " + deltaY + ", " +
                "bound_box_y_bottom_right = bound_box_y_bottom_right - " + deltaY + " " +
                "WHERE ID = " + cellId;

        queryList.add(updateCurrentCell);

        int parentCellId = elementDTO.getParentId();
        ElementDTO parentEleDTO = ElementDAOImpl.getElementDTO(String.valueOf(parentCellId));

        addParentChainUpdateQueryRecursive(parentEleDTO, deltaY, queryList);
    }

    private void updateAllParentHighlightsOnUI(ElementDTO clickedEleDTO) {
        Map<Integer, HighlightCell> mapHighlightsOnUI = ControllerLoader.canvasController.getHighlightsOnUI();

        String clickedCellId = clickedEleDTO.getId();
        float deltaY = clickedEleDTO.getDeltaY();
        float deltaX = clickedEleDTO.getDeltaX();
        double x = clickedEleDTO.getBoundBoxXTopLeft();
        double y = clickedEleDTO.getBoundBoxYTopLeft();


        double finalX = x + BoundBox.unitWidthFactor * 0.5;
        double finalY = y + BoundBox.unitHeightFactor * 0.5;
        mapHighlightsOnUI.forEach((id, highlightCell) -> {
            // if this is the clicked cell, make highlight unit dimensions.
            if (highlightCell.getElementId() == Integer.valueOf(clickedCellId)) {
                highlightCell.setHeight(highlightCell.getPrefHeight() - deltaY);
                highlightCell.setWidth(highlightCell.getPrefWidth() - deltaX);
            }

            // if this rectangle contains y, then shrink it by deltaY
            else if (highlightCell.getBoundsInParent().contains(finalX, finalY)) {
                highlightCell.setHeight(highlightCell.getPrefHeight() - deltaY);

            }
        });
    }

    private void addChildrenHighlightResizeQueries(ElementDTO clickedEleDTO, boolean isCollapsed, List<String> queryList, int nextCellId, int threadId) {
        int cellId = Integer.valueOf(clickedEleDTO.getId());

        if (cellId <= 1) {
            return;
        }

        queryList.addAll(HighlightDAOImpl.getChildrenHighlightResizeQueries(clickedEleDTO, isCollapsed, nextCellId, threadId));
    }

    private void addParentHighlightResizeQueries(ElementDTO clickedEleDTO, List<String> queryList, int threadId){
        int cellId = Integer.valueOf(clickedEleDTO.getId());

        if (cellId <= 1)
            return;

        queryList.addAll(HighlightDAOImpl.getParentHighlightResizeQueries(clickedEleDTO, threadId));
    }


    @SuppressWarnings("unused")
    // EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {
            //     @Override
            //     public void handle(MouseEvent event) {
            //         Node node = (Node) event.getSource();
            //         double scale = graph.getScale();
            //         dragContext.x = node.getBoundsInParent().getMinX() * scale - event.getScreenX();
            //         dragContext.y = node.getBoundsInParent().getMinY() * scale - event.getScreenY();
            //     }
            // };

            // EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
            //     @Override
            //     public void handle(MouseEvent event) {
            //         Node node = (Node) event.getSource();
            //         double offsetX = event.getScreenX() + dragContext.x;
            //         double offsetY = event.getScreenY() + dragContext.y;
            //         // adjust the offset in case we are zoomed
            //         double scale = graph.getScale();
            //         offsetX /= scale;
            //         offsetY /= scale;
            //         node.relocate(offsetX, offsetY);
            //     }
            // };

            EventHandler<MouseEvent> onMouseReleasedEventHandler = event -> {
    };

    class DragContext {
        double x;
        double y;
    }

    public static void saveRef(Main m) {
        // main = m;
    }

    public static void saveRef(ElementTreeModule c) {
        elementTreeModule = c;
    }

    public class XYCoordinate {

        public double x;
        public double y;
        public int threadId;
        XYCoordinate(double x, double y, int threadId) {
            this.x = x;
            this.y = y;
            this.threadId = threadId;
        }
    }
}