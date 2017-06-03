package com.application;

import com.application.db.DAOImplementation.CallTraceDAOImpl;
import com.application.db.DAOImplementation.ElementDAOImpl;
import com.application.db.DAOImplementation.MethodDefnDAOImpl;
import com.application.db.DatabaseUtil;
import com.application.fxgraph.ElementHelpers.ConvertDBtoElementTree;
import com.application.fxgraph.ElementHelpers.Element;
import com.application.fxgraph.cells.CircleCell;
import com.application.fxgraph.graph.*;
import com.application.logs.fileHandler.CallTraceLogFile;
import com.application.logs.fileHandler.MethodDefinitionLogFile;
import com.application.logs.fileIntegrity.CheckFileIntegrity;
import com.application.logs.parsers.ParseCallTrace;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URI;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class Main extends Application {
    Graph graph;
    Model model;
    BorderPane root;
    Scene scene;
    public Stage primaryStage;
    Label statusBarLabel = new Label();
    static ListView<String> threadListView;
    ObservableList<String> threadsObsList;

    ConvertDBtoElementTree convertDBtoElementTree;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        graph = new Graph();
        root = new BorderPane();
        EventHandlers.saveRef(this);

        reload();

        // Original.
        // addGraphComponents();
        // Layout layout = new RandomLayout(graph);
        // layout.execute();

        System.out.println("Max memory: " + Runtime.getRuntime().maxMemory() / 1000000);
        System.out.println("Free memory: " + Runtime.getRuntime().freeMemory() / 1000000);
        System.out.println("Total memory: " + Runtime.getRuntime().totalMemory() / 1000000);

        MenuBar mb = new MenuBar();
        Menu file = new Menu("File");
        MenuItem demoOne = new MenuItem("Load Demo 1");
        MenuItem demoTwo = new MenuItem("Load Demo 2");
        MenuItem demoThree = new MenuItem("Load Demo 3");

        file.getItems().addAll(demoOne, demoTwo, demoThree);
        mb.getMenus().add(file);

        demoOne.setOnAction(event -> {
            DatabaseUtil.resetDB();

            CallTraceLogFile.setFileName("logs/L-Instrumentation_call_trace_Demo_1.txt");
            MethodDefinitionLogFile.setFileName("logs/L-Instrumentation_method_definitions_Demo_1.txt");
            reload();
        });

        demoTwo.setOnAction(event -> {
            DatabaseUtil.resetDB();

            CallTraceLogFile.setFileName("logs/L-Instrumentation_call_trace_Demo_2.txt");
            MethodDefinitionLogFile.setFileName("logs/L-Instrumentation_method_definitions_Demo_2.txt");
            reload();
        });

        demoThree.setOnAction(event -> {
            DatabaseUtil.resetDB();

            CallTraceLogFile.setFileName("logs/L-Instrumentation_call_trace_Demo_3.txt");
            MethodDefinitionLogFile.setFileName("logs/L-Instrumentation_method_definitions_Demo_3.txt");
            reload();
        });

        root.setTop(mb);

        Group statusBar = new Group();
        statusBarLabel.setText("Done Loading into Database. Application ready.");
        statusBar.getChildren().add(statusBarLabel);
        root.setBottom(statusBar);
        scene = new Scene(root, 1000, 300);
        URL url = getClass().getClassLoader().getResource("css/application.css");
        String css = url.toExternalForm();
        scene.getStylesheets().add(css);
        // scene.getStylesheets().add(getClass().getResource("css/application.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public void reload() {
        // Layout Center
        graph = new Graph();
        root.setCenter(null);
        root.setCenter(graph.getScrollPane());
        ((ZoomableScrollPane) graph.getScrollPane()).saveRef(this);

        // Layout Left
        // threadListView = new ListView<>();
        threadsObsList = FXCollections.observableArrayList();
        threadListView = new ListView<>();
        threadListView.setItems(threadsObsList);
        root.setLeft(threadListView);

        threadListView.setOnMouseClicked(event -> {
            String selectedItem = threadListView.getSelectionModel().getSelectedItem();
            String threadId = selectedItem.split(" ")[1];
            showThread(threadId);
        });

        addGraphCellComponents();
        String firstThreadID = (String)threadsObsList.get(0).split(" ")[1];
        System.out.println(">>>>> " + firstThreadID);
        showThread(firstThreadID);
        threadListView.getSelectionModel().select(0);
    }

    private void addGraphCellComponents() {
        // Check log file integrity.
        CheckFileIntegrity.checkFile(CallTraceLogFile.getFile());

        DatabaseUtil.resetDB();
        statusBarLabel.setText("Loading log file.");

        // Parse method definition file and insert into database.
        new ParseCallTrace().readFile(MethodDefinitionLogFile.getFile(), MethodDefnDAOImpl::insert);

        convertDBtoElementTree = new ConvertDBtoElementTree();

        new ParseCallTrace().readFile(CallTraceLogFile.getFile(),
                parsedLineList -> {
                    try {
                        int autoIncrementedId = CallTraceDAOImpl.insert(parsedLineList);
                        convertDBtoElementTree.StringToElementList(parsedLineList, autoIncrementedId);
                    } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {  // Todo Create a custom exception class and clean this.
                        e.printStackTrace();
                    }
                });

        convertDBtoElementTree.calculateElementProperties();
        Graph.drawPlaceHolderLines();

        statusBarLabel.setText("Populating database.");

        convertDBtoElementTree.recursivelyInsertElementsIntoDB(ConvertDBtoElementTree.greatGrandParent);
        convertDBtoElementTree.recursivelyInsertEdgeElementsIntoDB(convertDBtoElementTree.greatGrandParent);

        // Get thread list and populate
        threadsObsList.clear();
        ConvertDBtoElementTree.greatGrandParent.getChildren().stream()
                .forEach(element -> {
                    Element child = element.getChildren().get(0);
                    int callTraceId = -1;
                    if (child != null) callTraceId = child.getFkEnterCallTrace();
                    try (ResultSet rs = CallTraceDAOImpl.selectWhere("id = " + callTraceId)) {
                        if (rs.next()) {
                            int threadId = rs.getInt("thread_id");
                            threadsObsList.add("Thread: " + threadId);
                        }
                    } catch (SQLException e) {
                    }
                });

        onScrollingScrollPane();
        statusBarLabel.setText("Ready.");
    }

    private void createCircleCellsRecursively(Element root, Model model) {
        if (root == null) {
            return;
        }
        createCircleCell(root, model);

        if (root.getChildren() != null){
            root.getChildren()
                    .forEach(ele -> createCircleCellsRecursively(ele, model));
        }
    }

    public void createCircleCell(Element root, Model model) {
        CircleCell targetCell = model.addCircleCell(String.valueOf(root.getElementId()), root);
        if (root.getParent() != null) {
            CircleCell sourceCell = root.getParent().getCircleCell();
            model.addEdge(sourceCell, targetCell);
        }
    }

    public Map<Integer, CircleCell> fromDBToUI() {
        Map resMap = new HashMap<Integer, CircleCell>();
        // Do fast
        // monitor scroll hvalue changes and load more circles.
        try {
            ResultSet rs = ElementDAOImpl.selectWhere("parent_id = -1");
            rs.next();
            int grandParentId = rs.getInt("id");
            float grandParentXCoordinate = rs.getFloat("bound_box_x_coordinate");
            float grandParentYCoordinate = rs.getFloat("bound_box_y_coordinate");
            CircleCell grandParentCell = new CircleCell(String.valueOf(grandParentId), grandParentXCoordinate, grandParentYCoordinate);
            model.addCell(grandParentCell);

            rs = ElementDAOImpl.selectWhere("parent_id = " + grandParentId);
            while (rs.next()) {
                int cellId = rs.getInt("id");
                float cellXCoordinate = rs.getFloat("bound_box_x_coordinate");
                float cellYCoordinate = rs.getFloat("bound_box_y_coordinate");
                // For each level 1 element, draw on UI.
                CircleCell targetCell = new CircleCell(String.valueOf(cellId), cellXCoordinate, cellYCoordinate);
                model.addCell(targetCell);
                model.addEdge(grandParentCell, targetCell);
                resMap.put(cellId, targetCell);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resMap;
    }


    public void showThread(String threadId) {
        convertDBtoElementTree.setCurrentThreadId(threadId);
        convertDBtoElementTree.removeFromCellLayer();
        onScrollingScrollPane();
    }
    public void onScrollingScrollPane() {
        if (convertDBtoElementTree!= null && graph != null) {
            convertDBtoElementTree.getCirclesToLoadIntoViewPort(graph);
            graph.myEndUpdate();
        }
    }

    private void addGraphComponents() {

        Model model = graph.getModel();

        graph.beginUpdate();

        //        model.addCell("Cell A", CellType.RECTANGLE);
        //        model.addCell("Cell B", CellType.RECTANGLE);
        //        model.addCell("Cell C", CellType.RECTANGLE);
        //        model.addCell("Cell D", CellType.TRIANGLE);
        //        model.addCell("Cell E", CellType.TRIANGLE);
        //        model.addCell("Cell F", CellType.RECTANGLE);
        //        model.addCell("Cell G", CellType.RECTANGLE);
        model.addCell("Cell A", CellType.RECTANGLE);
        model.addCell("Cell B", CellType.RECTANGLE);
        model.addCell("Cell C", CellType.RECTANGLE);
        model.addCell("Cell D", CellType.RECTANGLE);
        model.addCell("Cell E", CellType.RECTANGLE);
        model.addCell("Cell F", CellType.RECTANGLE);
        model.addCell("Cell G", CellType.RECTANGLE);
        model.addCell("Cell H", CellType.RECTANGLE);
        model.addCell("Cell I", CellType.RECTANGLE);
        model.addCell("Cell J", CellType.RECTANGLE);
        model.addCell("Cell K", CellType.RECTANGLE);

        model.addEdge("Cell A", "Cell B");
        model.addEdge("Cell A", "Cell C");
        //        model.addEdge("Cell B", "Cell C");
        model.addEdge("Cell C", "Cell D");
        model.addEdge("Cell B", "Cell E");
        model.addEdge("Cell D", "Cell F");
        model.addEdge("Cell D", "Cell G");
        model.addEdge("Cell G", "Cell H");
        model.addEdge("Cell G", "Cell I");
        model.addEdge("Cell G", "Cell J");
        model.addEdge("Cell G", "Cell K");

        graph.endUpdate();
    }

    public static void main(String[] args){
        launch(args);
    }


    public static void makeSelection(String threadId) {
        Platform.runLater(() -> threadListView.getSelectionModel().select("Thread: " + threadId));

    }
}