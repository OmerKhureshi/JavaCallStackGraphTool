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
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
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
    static ListView<String> threadListView;
    ObservableList<String> threadsObsList;
    boolean methodDefnFileSet;
    boolean callTraceFileSet;
    Text selectMethodDefn = new Text("");
    Text selectCallTrace = new Text("");
    FlowPane instructionsNode;
    ConvertDBtoElementTree convertDBtoElementTree;

    // Menu bar
    MenuBar mb;

    Menu file; // File menu button
    MenuItem chooseMethodDefn;
    MenuItem chooseCallTrace;

    Menu run;  // Run menu button
    MenuItem runAnalysis;
    MenuItem reset;

    Menu saveImageMenu;  // Save Image menu button
    MenuItem saveImage;

    // Status bar
    Group statusBar;
    Label statusBarLabel = new Label();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        graph = new Graph();
        root = new BorderPane();
        EventHandlers.saveRef(this);

        // Original.
        // addGraphComponents();
        // Layout layout = new RandomLayout(graph);
        // layout.execute();

        System.out.println("Max memory: " + Runtime.getRuntime().maxMemory() / 1000000);
        System.out.println("Free memory: " + Runtime.getRuntime().freeMemory() / 1000000);
        System.out.println("Total memory: " + Runtime.getRuntime().totalMemory() / 1000000);

        // Create Menu Bar
        setUpMenu();

        // Create Status Bar
        setUpStatusBar();

        scene = new Scene(root, 1000, 300);

//        URL url = getClass().getClassLoader().getResource("css/application.css");
//        String css = url.toExternalForm();
//        scene.getStylesheets().add(css);
//        scene.getStylesheets().add(getClass().getResource("css/application.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void setUpMenu() {
        mb = new MenuBar();

        file = new Menu("File");
        chooseMethodDefn = new MenuItem("Select Method Definition log file");
        chooseCallTrace = new MenuItem("Select Call Trace log file");
        file.getItems().addAll(chooseMethodDefn, chooseCallTrace);

        run = new Menu("Run");
        reset = new MenuItem("Reset");
        runAnalysis = new MenuItem("Run");
        runAnalysis.setDisable(true);
        run.getItems().addAll(runAnalysis, reset);

        saveImageMenu = new Menu("Save Image");
        saveImage = new MenuItem("Save Image");
        saveImageMenu.getItems().add(saveImage);

        mb.getMenus().addAll(file, run, saveImageMenu);

        populateInstructions();
        setMenuActions();

        root.setTop(mb);
    }

    public void setMenuActions() {
        chooseMethodDefn.setOnAction(event -> {
//            DatabaseUtil.resetDB();
            File methodDefnFile = chooseLogFile("MethodDefinition");
            if (methodDefnFile != null) {
                MethodDefinitionLogFile.setFile(methodDefnFile);
                selectMethodDefn.setText("");
                changeBool("methodDefnFileSet", true);
            }
        });

        chooseCallTrace.setOnAction(event -> {
//            DatabaseUtil.resetDB();
            File callTraceFile = chooseLogFile("CallTrace");
            if (callTraceFile != null) {
                CallTraceLogFile.setFile(callTraceFile);
                selectCallTrace.setText("");
                changeBool("callTraceFileSet", true);
            }
        });

        runAnalysis.setOnAction(event -> {
            reload();
        });

        reset.setOnAction(event -> {
            reset();
        });

        // Capture and save the currently loaded UI tree.
        saveImage.setOnAction(event -> {
            saveUIImage();
        });
    }

    public void setUpStatusBar() {
        statusBar = new Group();
        statusBarLabel.setText("Application ready.");
        statusBar.getChildren().add(statusBarLabel);
        root.setBottom(statusBar);
    }

    public void reset() {
        root.setCenter(null);
        root.setLeft(null);
        runAnalysis.setDisable(true);
        changeBool("methodDefnFileSet", false);
        changeBool("callTraceFileSet", false);
        populateInstructions();

    }

    public void reload() {
        // statusBarLabel.setText("Loading. Please wait.");

        if (!methodDefnFileSet || !callTraceFileSet) {
            System.out.println("Returning without effect");
            return;
        }

        // Layout Center
        graph = null;
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

        System.out.printf("Checkpoint 1: Before calling addGraphCellComponents()");
        addGraphCellComponents();
        System.out.printf("Checkpoint 1: After returning from addGraphCellComponents()");
        String firstThreadID = threadsObsList.get(0).split(" ")[1];
        showThread(firstThreadID);
        threadListView.getSelectionModel().select(0);
    }

    public void setUpThreadsView() {

    }

    public void saveUIImage() {
        System.out.println("In saveUIImage.");
        ScrollPane scrollPane = graph.getScrollPane();
        WritableImage image = scrollPane.snapshot(new SnapshotParameters(), null);

        File file = new File("chart.png");

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            System.out.println("saveUIImage exception");
        }
    }

    private void addGraphCellComponents() {
        // Check log file integrity.
//        System.out.printf("Checkpoint 1: Before calling checkFile()");
        CheckFileIntegrity.checkFile(CallTraceLogFile.getFile());
//        System.out.printf("Checkpoint 1: After returning from checkFile()");

        DatabaseUtil.resetDB();

        long startTime = System.currentTimeMillis();
        System.out.println("Checkpoint 1: Before calling readFile() on method definition");
        // Parse method definition file and insert into database.
        new ParseCallTrace().readFile(MethodDefinitionLogFile.getFile(), MethodDefnDAOImpl::insert);
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Checkpoint 1: After returning readFile() on method definition. Took: " + elapsed);

        convertDBtoElementTree = new ConvertDBtoElementTree();

        startTime = System.currentTimeMillis();
        System.out.println("Checkpoint 1: Before calling readFile() on call trace");
        new ParseCallTrace().readFile(CallTraceLogFile.getFile(),
                parsedLineList -> {
                    try {
                        int autoIncrementedId = CallTraceDAOImpl.insert(parsedLineList);
                        convertDBtoElementTree.StringToElementList(parsedLineList, autoIncrementedId);
                    } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {  // Todo Create a custom exception class and clean this.
                        e.printStackTrace();
                    }
                });
        elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Checkpoint 1: After returning from readFile() on call trace. Took: " + elapsed);

        startTime = System.currentTimeMillis();
        convertDBtoElementTree.calculateElementProperties();
        elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Checkpoint 1: After returning from calculateElementProperties. Took: " + elapsed);

        Graph.drawPlaceHolderLines();

        startTime = System.currentTimeMillis();
        convertDBtoElementTree.recursivelyInsertElementsIntoDB(ConvertDBtoElementTree.greatGrandParent);
        elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Checkpoint 1: After returning from recursivelyInsertElementsIntoDB. Took: " + elapsed);

        startTime = System.currentTimeMillis();
        convertDBtoElementTree.recursivelyInsertEdgeElementsIntoDB(convertDBtoElementTree.greatGrandParent);
        elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Checkpoint 1: After returning from recursivelyInsertEdgeElementsIntoDB. Took: " + elapsed);

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

        System.out.println("Checkpoint 1: Before calling onScrollingScrollPane");
        onScrollingScrollPane();
        System.out.println("Checkpoint 1: After returning from onScrollingScrollPane");
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
//        statusBarLabel.setText("Ready.");
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

    public File chooseLogFile(String logType) {
        FileChooser fileChooser = new FileChooser();

        if (logType.equalsIgnoreCase("CallTrace")) {
            fileChooser.setTitle("Choose call trace log file.");
        } else {
            fileChooser.setTitle("Choose method definition log file.");
        }

        File logFile = fileChooser.showOpenDialog(primaryStage);
        if (logFile != null) {
            return logFile;
        }

        return null;
    }

    public void changeBool(String type, boolean val) {
        if (type.equalsIgnoreCase("methodDefnFileSet"))
            methodDefnFileSet = val;
        else if (type.equalsIgnoreCase("callTraceFileSet"))
            callTraceFileSet = val;

        if (methodDefnFileSet && callTraceFileSet) {
            runAnalysis.setDisable(false);
            selectMethodDefn.setText("Click Run.");
        }
    }

    public void populateInstructions() {
        instructionsNode = new FlowPane();
        root.setCenter(instructionsNode);
        selectMethodDefn.setText("Select Method Definition log file.");
        selectCallTrace.setText("Select Call Trace log file.");
        instructionsNode.getChildren().addAll(selectMethodDefn, selectCallTrace);
        instructionsNode.setAlignment(Pos.CENTER);
        instructionsNode.setOrientation(Orientation.VERTICAL);
        instructionsNode.setPadding(new Insets(5,5,5,5));
        instructionsNode.setVgap(10);
    }
}