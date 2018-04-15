package com.application.controller;

import com.application.Main;
import com.application.db.DAO.DAOImplementation.*;
import com.application.db.DTO.BookmarkDTO;
import com.application.db.DTO.ElementDTO;
import com.application.db.DatabaseUtil;
import com.application.db.TableNames;
import com.application.fxgraph.cells.CircleCell;
import com.application.fxgraph.graph.*;
import com.application.service.modules.ElementTreeModule;
import com.application.service.modules.ModuleLocator;
import com.sun.scenario.effect.InvertMask;
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
import java.util.*;


@SuppressWarnings("ALL")
public class EventHandlers {

    private static ElementTreeModule elementTreeModule;
    final DragContext dragContext = new DragContext();
    private boolean clickable = true;

    // Graph graph;
    // static Main main;

    // public EventHandlers(Graph graph) {
    //     this.graph = graph;
    // }

    public static void resetEventHandlers() {
        // deltaCache = new HashMap<>();
    }

    public void setCustomMouseEventHandlers(final Node node) {
        ((CircleCell)node).getInfoStackPane().setOnMousePressed(infoButtonOnClickEventHandler);
        ((CircleCell)node).getMinMaxStackPane().setOnMousePressed(minMaxButtonOnClickEventHandler);
    }

    private PopOver popOver;

    private EventHandler<MouseEvent> infoButtonOnClickEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            if (popOver != null) {
                popOver.hide();
            }

            Node node = (Node) event.getSource();
            CircleCell cell = (CircleCell) node.getParent();

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
                     * */

                    List<Integer> ctIdList = new ArrayList<>();
                    List<Integer> eleIdList = new ArrayList<>();
                    if (eventType.equalsIgnoreCase("WAIT-ENTER")) {
                        int ctId = -2;  // Will throw exception if value not changed. Which is what we want.
                        sql = "lockobjid = '" + lockObjectId + "'" +
                                " AND (message = 'NOTIFY-ENTER' OR message = 'NOTIFYALL-ENTER')" +
                                " AND time_instant >= " + "'" + timeStamp + "'";

                        CallTraceDAOImpl.getThreadIdsWhere(sql).stream().forEach(id -> {
                            ctIdList.add(id);
                        });

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
                            // try (ResultSet elementRS = ElementDAOImpl.getWhere("id = " + eId)){
                            if (elementRS.next()) {
                                int id = elementRS.getInt("EID");
                                String targetThreadId = String.valueOf(elementRS.getInt("thread_id"));
                                float xCoordinate = elementRS.getFloat("bound_box_x_coordinate");
                                float yCoordinate = elementRS.getFloat("bound_box_y_coordinate");

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
                   /* Button minMaxButton = new Button("min / max");
                    minMaxButton.setOnMouseClicked(event1 -> {
                                minMaxButtonOnClick(cell, threadId);
                            }
                    );

                    gridPane.add(minMaxButton, 1, rowIndex++);*/

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

                        ModuleLocator.getBookmarksModule().insertBookmark(bookmarkDTO);

                        removeBookmarkButton.setDisable(false);
                        addBookmarkButton.setDisable(true);
                    });

                    boolean contains = ModuleLocator.getBookmarksModule().getBookmarkDTOs().containsKey(String.valueOf(elementId));
                    addBookmarkButton.setDisable(contains);
                    removeBookmarkButton.setDisable(!contains);

                    removeBookmarkButton.setOnMouseClicked(eve -> {
                        ModuleLocator.getBookmarksModule().deleteBookmark(String.valueOf(elementId));
                        addBookmarkButton.setDisable(false);
                    });

                    HBox hBox = new HBox();
                    hBox.getChildren().addAll(bookmarkColorPicker, addBookmarkButton, removeBookmarkButton);

                    gridPane.add(hBox, 1, rowIndex++);
                    // gridPane.add(bookmarkColorPicker, 1, rowIndex++);
                    // gridPane.add(addBookmarkButton, 1, rowIndex++);
                    // gridPane.add(removeBookmarkButton, 1, rowIndex++);

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


    // error comment
    private void minMaxButtonOnClick(CircleCell clickedCell, String threadId) {
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

            // double clickedCellTopLeftY = 0, clickedCellTopLeftX = 0, clickedCellTopRightX = 0, clickedCellBoundBottomLeftY = 0, newDelta = 0, newDeltaX = 0;

            ElementDTO clickedElementDTO = ElementDAOImpl.getElementDTO(clickedCellID);

            int collapsed = clickedElementDTO.getCollapsed();
            float newDelta = 0, newDeltaX = 0;


            /*
             * collapsed - actions
             *     0     - Cell visible on UI. Starting value for all cells.
             *     1     - parent of this cell was minimized. Don't show on UI
             *     2     - this cell was minimized. Show on UI. Don't show children on UI.
             *    >= 3   - parent of this cell was minimized. This cell was also minimized. Don't expand this cell's children. Don't show on UI.
             */
            if (collapsed == 0) {
                // MINIMIZE SUBTREE

                // ((Circle) clickedCell.getChildren().get(0)).setFill(Color.BLUE);
                // ((Circle) ( (Group)cell.getView() )
                //             .getChildren().get(0))
                //             .setFill(Color.BLUE);
                // cell.getChildren().get(0).setStyle("-fx-background-color: blue");
                // cell.setStyle("-fx-background-color: blue");

                // will do later....
                // main.setStatus("Please wait ......");

                System.out.println("====== Minimize cellId: " + clickedCellID + " ------ ");

                Statement statement = DatabaseUtil.createStatement();

                int nextCellId = ElementDAOImpl.getNextLowerSiblingOrAncestorNode(clickedElementDTO, threadId);
                int lastCellId = ElementDAOImpl.getLowestCellInThread(threadId);

                newDelta = clickedElementDTO.getBoundBoxYBottomLeft() - clickedElementDTO.getBoundBoxYTopLeft() - BoundBox.unitHeightFactor;

                newDeltaX = ElementDAOImpl.getDeltaX(clickedElementDTO, String.valueOf(nextCellId));

                double clickedCellBottomY = clickedElementDTO.getBoundBoxYTopLeft() + BoundBox.unitHeightFactor;


                clickedElementDTO.setDelta(newDelta);
                clickedElementDTO.setDeltaX(newDeltaX);
                clickedElementDTO.setCollapsed(2);
                ElementDAOImpl.updateElementCollapseValues(clickedElementDTO);

                ControllerLoader.canvasController.removeUIComponentsBetween(clickedElementDTO, nextCellId);
                ControllerLoader.canvasController.moveLowerTreeByDelta(clickedElementDTO);

                updateDBInBackgroundThread(clickedElementDTO, true, nextCellId, Integer.valueOf(threadId), lastCellId);

            } else if (collapsed == 2) {
                // MAXIMIZE SUBTREE

                // ((Circle) clickedCell.getChildren().get(0)).setFill(Color.RED);
                // ( (Circle) ( (Group)cell.getView() ).getChildren().get(0) ).setFill(Color.RED);
                // main.setStatus("Please wait ......");
                System.out.println("====== Maximize cellId: " + clickedCellID + " ++++++ ");

                // double delta = deltaCache.get(clickedCellID);

                expandTreeAt(clickedElementDTO, clickedCellID, parentId, threadId, newDelta, newDeltaX,
                        clickedCellTopLeftX, clickedCellTopLeftY, clickedCellBoundBottomLeftY, clickedCellTopRightX );
            }
        }
    }

    private void expandTreeAt(ElementDTO clickedEleDTO, String clickedCellID, int parentId, String threadId, double newDelta, double newDeltaX,
                              double clickedCellTopLeftX, double clickedCellTopLeftY, double clickedCellBoundBottomLeftY, double clickedCellTopRightX) {
        int nextCellId = ElementDAOImpl.getNextLowerSiblingOrAncestorNode(clickedEleDTO, threadId);
        int lastCellId = ElementDAOImpl.getLowestCellInThread(threadId);

        double clickedCellBottomY = clickedEleDTO.getBoundBoxYTopLeft() + BoundBox.unitHeightFactor;
        double newClickedCellBottomY = clickedEleDTO.getBoundBoxYTopLeft() + BoundBox.unitHeightFactor + newDelta;


        ControllerLoader.canvasController.moveLowerTreeByDelta(clickedElementDTO);
        moveLowerTreeByDelta(clickedCellID, clickedCellBottomY, -newDelta);
        //later ....
        // updateAllParentHighlightsOnUI(clickedCellID, clickedCellTopLeftX, clickedCellTopLeftY, -newDelta, -newDeltaX);

        ElementDAOImpl.updateWhere("collapsed", "0", "id = " + clickedCellID);
        updateDBInBackgroundThread(Integer.parseInt(clickedCellID), clickedCellTopLeftY, clickedCellBoundBottomLeftY,
                clickedCellTopLeftX, clickedCellTopRightX, -newDelta, -newDeltaX, false,
                nextCellId, threadId, lastCellId, parentId);
    }

    // error comment
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
            minMaxButtonOnClick(cell, Integer.valueOf(ControllerLoader.centerLayoutController.getCurrentThreadId()));
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


    // error comment
    public void jumpTo(int cellId, String threadId, int collapsed) {

        // make changes in DB if needed
        if (collapsed != 0) {
            expandParentTreeChain(cellId, Integer.parseInt(threadId));
            try {
                throw new Exception("Cannot jump ");
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    }



    private void setClickable() {
        clickable = true;
        // main.setStatus("Done");
    }




    // error comment


    private void updateDBInBackgroundThread(ElementDTO clickedEleDTO, boolean isCollapsed, int nextCellId, int threadId, int lastCellId) {

        int clickedCellId = Integer.valueOf(clickedEleDTO.getId());
        double topY = clickedEleDTO.getBoundBoxYTopLeft();
        double bottomY = clickedEleDTO.getBoundBoxYBottomLeft();
        double leftX = clickedEleDTO.getBoundBoxXTopLeft();
        double rightX = clickedEleDTO.getBoundBoxXTopRight();
        double delta = clickedEleDTO.getDelta();
        double newDeltaX = clickedEleDTO.getDeltaX();
        int parentId = Integer.valueOf(clickedEleDTO.getParentId());


        Statement statement = DatabaseUtil.createStatement();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // System.out.println("==================== Starting thread updateDBInBackgroundThread. ====================");
                //
                // get queries to update collapse values for cells, edges and highlights.
                updateCollapseValForSubTreeBulk(clickedEleDTO, statement, isCollapsed, nextCellId, threadId);

                // No upate required for single line children
                if (delta == 0) {
                    System.out.println("Optimized for single line children collapses.");
                    //later ...
                    // updateParentHighlightsInDB(clickedCellId, isCollapsed, statement, delta, nextCellId, leftX, topY, threadId);

                    statement.executeBatch();
                    Platform.runLater(() -> elementTreeModule.clearAndUpdateCellLayer());
                    return null;
                }

                updateParentChainRecursive(clickedEleDTO, statement);

                if (nextCellId != Integer.MAX_VALUE) {
                    // only if next lower sibling ancestor node is present.
                    updateTreeBelowYBulk(topY + BoundBox.unitHeightFactor, delta, statement, nextCellId, lastCellId, threadId);
                    statement.executeBatch();
                }

                // later....
                // updateParentHighlightsInDB(clickedCellId, isCollapsed, statement, delta, nextCellId, leftX, topY, threadId);
                // updateChildrenHighlightsInDB(clickedCellId, isCollapsed, statement, delta, nextCellId, threadId);
                statement.executeBatch();

                // Platform.runLater(() -> elementTreeModule.clearAndUpdateCellLayer());
                Platform.runLater(() -> ControllerLoader.canvasController.updateIfNeeded());
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

    private void updateCollapseValForSubTreeBulk(ElementDTO elementDTO, Statement statement, boolean isMinimized, int endCellId, int threadId) {

        // Collapsed value -> description
        // 0               -> visible     AND  uncollapsed
        // 2               -> visible     AND  collapsed
        // >2              -> not visible AND  collapsed
        // <0              -> not visible AND  collapsed

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

        try {
            statement.addBatch(updateCellQuery);
            statement.addBatch(updateEdgeQuery);
            // later...
            // statement.addBatch(updateHighlightsQuery);
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
            // later....
            // statement.addBatch(updateHighlightsQuery);
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
     * @param cellId    The id of cell where recursive updateIfNeeded starts.
     * @param delta     The value to be subtracted from or added to the columns.
     * @param statement All updated queries are added to this statement as batch.
     */
    private static void updateParentChainRecursive(ElementDTO elementDTO, Statement statement) {
        // System.out.println("EventHandler::updateParentChainRecursive: method started");
        // BASE CONDITION. STOP IF ROOT IS REACHED

        int cellId = Integer.valueOf(elementDTO.getId());
        double delta = elementDTO.getDelta();

        if (cellId == 1) {
            return;
        }

        // Update this cells bottom y values
        String updateCurrentCell = "UPDATE " + TableNames.ELEMENT_TABLE + " " +
                "SET bound_box_y_bottom_left = bound_box_y_bottom_left - " + delta + ", " +
                "bound_box_y_bottom_right = bound_box_y_bottom_right - " + delta + " " +
                "WHERE ID = " + cellId;

        try {
            statement.addBatch(updateCurrentCell);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // check if parent id > 1?????
        int parentCellId = elementDTO.getParentId();
        ElementDTO parentEleDTO = ElementDAOImpl.getElementDTO(String.valueOf(parentCellId));

        updateParentChainRecursive(parentEleDTO, statement);
    }

    private void updateAllParentHighlightsOnUI(String clickedCellId, double x, double y, double delta, double deltaX) {
        Map<Integer, RectangleCell> mapHighlightsOnUI = ControllerLoader.canvasController.getHighlightsOnUI();

        double finalX = x + BoundBox.unitWidthFactor * 0.5;
        double finalY = y + BoundBox.unitHeightFactor * 0.5;
        mapHighlightsOnUI.forEach((id, rectangleCell) -> {
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
        // try (ResultSet rs = HighlightDAOImpl.getWhere("ELEMENT_ID = " + cellId)) {
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