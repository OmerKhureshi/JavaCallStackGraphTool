package com.csgt.controller;

import com.csgt.dataaccess.DAO.BookmarksDAOImpl;
import com.csgt.dataaccess.DAO.HighlightDAOImpl;
import com.csgt.dataaccess.DAO.MethodDefDAOImpl;
import com.csgt.dataaccess.DTO.BookmarkDTO;
import com.csgt.dataaccess.DatabaseUtil;
import com.csgt.dataaccess.TableNames;
import com.csgt.presentation.graph.ColorProp;
import com.csgt.presentation.graph.NodeCell;
import com.csgt.presentation.graph.SizeProp;
import com.csgt.presentation.CustomProgressBar;
import com.csgt.controller.files.FileNames;
import com.csgt.controller.files.LoadedFiles;
import com.csgt.controller.modules.ModuleLocator;
import com.csgt.controller.tasks.ConstructTreeTask;
import com.csgt.controller.tasks.ParseFileTask;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class handles all actions related to the menu.
 */
public class MenuController {
    // Menu bar
    @FXML
    private MenuBar menuBar;

    // File Menu
    @FXML
    private MenuItem chooseMethodDefMenuItem;
    @FXML
    private MenuItem chooseCallTraceMenuItem;
    @FXML
    private MenuItem chooseDBMenuItem;
    private Glyph methodDefnGlyph;
    private Glyph callTraceGlyph;
    private Glyph chooseDBGlyph;

    // Run Menu
    @FXML
    private MenuItem runAnalysisMenuItem;
    @FXML
    private MenuItem resetMenuItem;
    private Glyph runAnalysisGlyph;
    private Glyph resetGlyph;

    // View Menu
    private Glyph saveImageGlyph;
    private Glyph refreshGlyph;
    @FXML
    private Menu viewMenu;
    @FXML
    private MenuItem saveImageMenuItem;
    @FXML
    private MenuItem refreshMenuItem;

    // Highlights Menu
    private Glyph highlightItemsGlyph;
    @FXML
    private MenuItem highlightMenu;
    @FXML
    private MenuItem addHighlightMenuItem;

    // Settings Menu
    @FXML
    private Menu settingMenu;
    @FXML
    private CheckMenuItem liteModeCheckMenuItem;
    public Boolean isLiteModeEnabled = true;

    // Debug menu button
    @FXML
    private Menu debugMenu;
    @FXML
    private MenuItem printViewPortDimsMenuItem;
    @FXML
    private  MenuItem printNodeCountMenuItem;

    // Bookmarks menu button
    private Glyph bookmarksGlyph;
    @FXML
    private Menu bookmarksMenu;

    private Stage mStage;
    private Button applyButton;
    private Button cancelButton;

    private Map<String, CheckBox> firstCBMap;
    private Map<String, CheckBox> secondCBMap;
    private Map<String, Color> colorsMap;
    private boolean anyColorChange = false;

    private boolean firstTimeSetUpHighlightsWindowCall = true;

    CustomProgressBar customProgressBar;

    @FXML
    private void initialize() {
        setUpFileMenu();
        setUpRunMenu();
        setUpBookmarksMenu();
        setUpHighlightsMenu();
        setUpViewMenu();
        setUpSettingMenu();
        setUpDebugMenu();
        initMenuGraphics();
        // autoRun();
        setRemainingMenuGraphics(false);

        ControllerLoader.register(this);
    }

    private void setUpHighlightsMenu() {
        addHighlightMenuItem.setOnAction(event -> showHighlightsWindow());
    }

    private void autoRun() {
        setFiles();
        onRun();
    }

    private void setFiles() {
        File methodDefLogFile = new File("/Users/skhureshi/Documents/Logs/MD1.txt");
        LoadedFiles.setFile(FileNames.METHOD_DEF.getFileName(), methodDefLogFile);

        File callTraceLogFile = new File("/Users/skhureshi/Documents/Logs/CT1.txt");
        LoadedFiles.setFile(FileNames.Call_Trace.getFileName(), callTraceLogFile);

    }

    private void setUpFileMenu() {
        chooseMethodDefMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN));
        chooseMethodDefMenuItem.setOnAction(event -> {
            try {
                File methodDefLogFile = ControllerUtil.fileChooser("Choose method definition log fileMenu.", "Text Files", "*.txt");
                if (methodDefLogFile == null) {
                    return;
                }

                LoadedFiles.setFile(FileNames.METHOD_DEF.getFileName(), methodDefLogFile);
                setFileRelatedGraphics();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        chooseCallTraceMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN));
        chooseCallTraceMenuItem.setOnAction(event -> {
            try {
                File callTraceLogFile = ControllerUtil.fileChooser("Choose call trace log fileMenu.", "Text Files", "*.txt");
                if (callTraceLogFile == null) {
                    return;
                }

                LoadedFiles.setFile(FileNames.Call_Trace.getFileName(), callTraceLogFile);
                setFileRelatedGraphics();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        chooseDBMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN));
        chooseDBMenuItem.setOnAction(event -> {
            try {
                File dbFile = ControllerUtil.directoryChooser("Choose an existing database.");
                if (dbFile == null) {
                    return;
                }

                LoadedFiles.setFile(FileNames.DB.getFileName(), dbFile);

                setDBRelatedGraphics(true);

                setRunAnalysisMenuItemGraphics(true);
                setResetMenuItemGraphics(true);

                setChooseMethodDefMenuItemGraphics(false, false);
                setChooseCallTraceMenuItemGraphics(false, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setFileRelatedGraphics() {
        boolean methodDefFileSet = LoadedFiles.getFile(FileNames.METHOD_DEF.getFileName()) != null;
        boolean callTraceFileSet = LoadedFiles.getFile(FileNames.Call_Trace.getFileName()) != null;

        if (methodDefFileSet && callTraceFileSet) {
            // do not remove any statements in this loop.
            setChooseMethodDefMenuItemGraphics(true, true);
            setChooseCallTraceMenuItemGraphics(true, true);

            setRunAnalysisMenuItemGraphics(true);
            setResetMenuItemGraphics(true);
            ControllerLoader.instructionsPaneController.setMethodDefGraphics(true);
            ControllerLoader.instructionsPaneController.setCallTraceGraphics(true);
            ControllerLoader.instructionsPaneController.setFileRunInfoGraphics(true);
        } else if (!methodDefFileSet && !callTraceFileSet) {
            // on reset.
            setChooseMethodDefMenuItemGraphics(true, false);
            setChooseCallTraceMenuItemGraphics(true, false);
        } else if (methodDefFileSet) {
            setChooseMethodDefMenuItemGraphics(true, true);
            ControllerLoader.instructionsPaneController.setMethodDefGraphics(true);
        } else if (callTraceFileSet) {
            setChooseCallTraceMenuItemGraphics(true, true);
            ControllerLoader.instructionsPaneController.setCallTraceGraphics(true);
        }
    }

    private void setUpRunMenu() {
        runAnalysisMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN));
        runAnalysisMenuItem.setOnAction(event -> {
            onRun();
        });

        resetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN));
        resetMenuItem.setOnAction(event -> {
            onReset();
        });
    }

    private void setUpViewMenu() {
        saveImageMenuItem.setOnAction(event -> saveUIImage());

        refreshMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN, KeyCombination.CONTROL_DOWN));
        refreshMenuItem.setOnAction(event -> ControllerLoader.canvasController.clearAndUpdate());
    }

    private void setUpSettingMenu() {
        liteModeCheckMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            isLiteModeEnabled = newValue;
            ControllerLoader.statusBarController.setTimedStatusText("Refresh to see changes", "System Ready", 5*1000);
        });
    }

    private void setUpDebugMenu() {
        printViewPortDimsMenuItem.setOnAction(event -> {
            System.out.println("ScrollPane viewport dimensions");
            System.out.println(ControllerLoader.canvasController.scrollPane.getViewportBounds());
            System.out.println(ControllerLoader.canvasController.getViewPortDims());
        });

        printNodeCountMenuItem.setOnAction(event -> {
            System.out.println("Count of nodes: " + ControllerLoader.canvasController.nodeCellsOnUI.size());
            Map<String, NodeCell> map = new TreeMap<>(ControllerLoader.canvasController.nodeCellsOnUI);
            System.out.println();
            map.forEach((s, nodeCell) -> {
                System.out.print(s + ", ");
            });
            System.out.println();
        });
    }

    private void onRun() {
        setDBRelatedGraphics(false);
        setChooseMethodDefMenuItemGraphics(false, !LoadedFiles.isLoadedFromDB());
        setChooseCallTraceMenuItemGraphics(false, !LoadedFiles.isLoadedFromDB());

        setRunAnalysisMenuItemGraphics(false);
        setResetMenuItemGraphics(true);

        setRemainingMenuGraphics(true);

        // No need to parse log file and compute graph if loading from DB.
        if (LoadedFiles.isLoadedFromDB()) {
            ControllerLoader.mainController.loadGraphPane();
            postGraphLoadProcess();
        } else {
            customProgressBar = new CustomProgressBar("", "");

            Task<Void> parseTask = new ParseFileTask();
            Task<Void> constructTreeTask = new ConstructTreeTask();


            ExecutorService es = Executors.newSingleThreadExecutor();
            es.submit(parseTask);
            es.submit(constructTreeTask);
            es.shutdown();

            customProgressBar.bind(parseTask);

            parseTask.setOnSucceeded((e) -> {
                customProgressBar.bind(constructTreeTask);
            });

            constructTreeTask.setOnSucceeded((e) -> {
                customProgressBar.close();
                ControllerLoader.mainController.loadGraphPane();
                postGraphLoadProcess();
            });
        }

    }

    public void closeProgressBar() {
        if (customProgressBar != null) {
            customProgressBar.close();
        }
    }

    public void onReset() {
        ModuleLocator.resetElementTreeModule();
        ControllerLoader.mainController.showInstructionsPane();
        // ControllerLoader.canvasController.onReset();
        // DatabaseUtil.resetDB();

        LoadedFiles.resetFile();

        setDBRelatedGraphics(true);
        setFileRelatedGraphics();

        setResetMenuItemGraphics(false);
        setRunAnalysisMenuItemGraphics(false);

        setRemainingMenuGraphics(false);

        ControllerLoader.instructionsPaneController.setCallTraceGraphics(false);
        ControllerLoader.instructionsPaneController.setMethodDefGraphics(false);
        ControllerLoader.instructionsPaneController.setFileRunInfoGraphics(false);
        ControllerLoader.instructionsPaneController.setErrorGraphics(false);

        ControllerLoader.mainController.alertShown = false;
    }

    private void setUpBookmarksMenu() {
        MenuItem noBookmarksMenuItem = new MenuItem("No bookmarks");
        noBookmarksMenuItem.setDisable(true);
        bookmarksMenu.getItems().add(noBookmarksMenuItem);
    }

    private void updateBookmarksMenu() {
        bookmarksMenu.getItems().clear();

        Map<String, BookmarkDTO> bookmarkDTOs = getBookmarkDTOs();
        MenuItem noBookmarksMenuItem = new MenuItem("No bookmarks");

        if (bookmarkDTOs.size() == 0) {
            noBookmarksMenuItem.setDisable(true);
            bookmarksMenu.getItems().add(noBookmarksMenuItem);

            return;
        }

        bookmarkDTOs.forEach((id, bookmarkDTO) -> {
            Rectangle icon = new Rectangle(10, 30);
            icon.setFill(Paint.valueOf(bookmarkDTO.getColor()));
            icon.setStrokeWidth(1);
            icon.setStroke(ColorProp.GREY);
            icon.setArcWidth(3);
            icon.setArcHeight(3);

            MenuItem bookmarkMenuItem = new MenuItem(
                    " Id:" + bookmarkDTO.getElementId() +
                            "  |  Method:" + bookmarkDTO.getMethodName() +
                            "  |  Thread:" + bookmarkDTO.getThreadId(), icon);

            bookmarkMenuItem.setOnAction(event -> ControllerLoader.canvasController.jumpTo(
                    bookmarkDTO.getElementId(),
                    bookmarkDTO.getThreadId(),
                    bookmarkDTO.getCollapsed()));

            bookmarksMenu.getItems().add(bookmarkMenuItem);
        });

        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();

        bookmarksMenu.getItems().add(separatorMenuItem);

        // clear bookmarks button and logic
        Glyph clearBookmarksGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.TRASH);
        clearBookmarksGlyph.setColor(ColorProp.ENABLED);
        clearBookmarksGlyph.setDisable(bookmarkDTOs.size() == 0);

        MenuItem clearBookmarksMenuItem = new MenuItem("Delete all", clearBookmarksGlyph);

        clearBookmarksMenuItem.setOnAction(event -> {
            ControllerLoader.menuController.deleteAllBookmarks();
            bookmarksMenu.getItems().clear();
            bookmarksMenu.getItems().add(noBookmarksMenuItem);
            noBookmarksMenuItem.setDisable(true);
        });

        bookmarksMenu.getItems().add(clearBookmarksMenuItem);
    }

    private void firstTimeSetUpHighlightsWindow() {
        if (!firstTimeSetUpHighlightsWindowCall)
            return;

        firstTimeSetUpHighlightsWindowCall = false;

        firstCBMap = new HashMap<>();
        secondCBMap = new HashMap<>();
        colorsMap = new HashMap<>();
        anyColorChange = false;

        GridPane gridPane = new GridPane();
        gridPane.setPadding(SizeProp.INSETS);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setMinHeight(GridPane.USE_PREF_SIZE);
        gridPane.setAlignment(Pos.TOP_CENTER);

        Label headingCol1 = new Label("Package and method name");
        headingCol1.setWrapText(true);
        headingCol1.setFont(Font.font("Verdana", FontWeight.BOLD, headingCol1.getFont().getSize() * 1.1));
        GridPane.setConstraints(headingCol1, 0, 0);
        GridPane.setHalignment(headingCol1, HPos.CENTER);

        Label headingCol2 = new Label("Highlight node only");
        headingCol2.setWrapText(true);
        headingCol2.setFont(Font.font("Verdana", FontWeight.BOLD, headingCol2.getFont().getSize() * 1.1));
        GridPane.setConstraints(headingCol2, 1, 0);
        GridPane.setHalignment(headingCol2, HPos.CENTER);

        Label headingCol3 = new Label("Highlight node subtree");
        headingCol3.setWrapText(true);
        headingCol3.setFont(Font.font("Verdana", FontWeight.BOLD, headingCol3.getFont().getSize() * 1.1));
        GridPane.setConstraints(headingCol3, 2, 0);
        GridPane.setHalignment(headingCol3, HPos.CENTER);


        Label headingCol4 = new Label("Choose color");
        headingCol4.setWrapText(true);
        headingCol4.setFont(Font.font("Verdana", FontWeight.BOLD, headingCol4.getFont().getSize() * 1.1));
        GridPane.setConstraints(headingCol4, 3, 0);
        GridPane.setHalignment(headingCol4, HPos.CENTER);


        gridPane.getChildren().addAll(
                headingCol1, headingCol2, headingCol3, headingCol4
        );

        applyButton = new Button("Apply");
        applyButton.setAlignment(Pos.CENTER_RIGHT);

        cancelButton = new Button("Cancel");
        cancelButton.setAlignment(Pos.CENTER_RIGHT);

        Pane hSpacer = new Pane();
        hSpacer.setMinSize(10, 1);
        HBox.setHgrow(hSpacer, Priority.ALWAYS);


        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(gridPane);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        Pane vSpacer = new Pane();
        vSpacer.setMinSize(10, 1);
        VBox.setVgrow(vSpacer, Priority.ALWAYS);

        HBox hBox = new HBox(hSpacer, cancelButton, applyButton);
        hBox.setPadding(SizeProp.INSETS);
        hBox.setSpacing(20);
        hBox.setAlignment(Pos.BOTTOM_CENTER);

        VBox vBox = new VBox(SizeProp.SPACING);
        vBox.setPrefHeight(VBox.USE_PREF_SIZE);
        vBox.setPadding(SizeProp.INSETS);

        vBox.getChildren().addAll(scrollPane, vSpacer, hBox);

        // For debugging purposes. Shows backgrounds in colors.
        // hBox.setStyle("-fx-background-color: #ffb85f");
        // vBox.setStyle("-fx-background-color: yellow");
        // gridPane.setStyle("-fx-background-color: #4dfff3");
        // scrollPane.setStyle("-fx-background-color: #ffb2b3");


        // mRootGroup.getChildren().add();
        Scene mScene = new Scene(vBox, 1000, 500);
        mStage = new Stage();
        mStage.setTitle("Choose highlighting options");
        mStage.setScene(mScene);

        LinkedList<String> methodNamesList = new LinkedList<>();

        // Fetch all methods from method definition fileMenu and display on UI.
        Task<Void> onStageLoad = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Fetch all methods from method definition fileMenu.
                methodNamesList.addAll(MethodDefDAOImpl.getMethodPackageString());
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                final AtomicInteger rowInd = new AtomicInteger(1);

                // Display the methods on UI
                methodNamesList.forEach(fullName -> {

                    // Label
                    Label name = new Label(fullName);
                    name.setWrapText(true);
                    // name.setMaxWidth(250);
                    GridPane.setConstraints(name, 0, rowInd.get());
                    GridPane.setHalignment(name, HPos.CENTER);
                    GridPane.setHgrow(name, Priority.ALWAYS);


                    // First checkbox
                    CheckBox firstCB = new CheckBox();
                    GridPane.setConstraints(firstCB, 1, rowInd.get());
                    GridPane.setHalignment(firstCB, HPos.CENTER);
                    GridPane.setValignment(firstCB, VPos.CENTER);
                    GridPane.setHgrow(firstCB, Priority.ALWAYS);
                    firstCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            firstCBMap.put(fullName, firstCB);
                        } else {
                            firstCBMap.remove(fullName);
                        }
                    });


                    // Second checkbox
                    CheckBox secondCB = new CheckBox();
                    // secondCB.setAlignment(Pos.CENTER);
                    GridPane.setConstraints(secondCB, 2, rowInd.get());
                    GridPane.setHalignment(secondCB, HPos.CENTER);
                    GridPane.setValignment(secondCB, VPos.CENTER);
                    GridPane.setHgrow(secondCB, Priority.ALWAYS);
                    secondCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) secondCBMap.put(fullName, secondCB);
                        else secondCBMap.remove(fullName);
                    });


                    // Color picker
                    ColorPicker colorPicker = new ColorPicker(Color.AQUAMARINE);
                    colorPicker.setOnAction(event -> {
                        anyColorChange = true;
                        colorsMap.put(fullName, colorPicker.getValue());
                    });
                    colorPicker.getStyleClass().add("button");
                    colorPicker.setStyle(
                            "-fx-color-label-visible: false; " +
                                    "-fx-background-radius: 15 15 15 15;");
                    GridPane.setConstraints(colorPicker, 3, rowInd.get());
                    GridPane.setHalignment(colorPicker, HPos.CENTER);
                    GridPane.setValignment(colorPicker, VPos.CENTER);
                    GridPane.setHgrow(colorPicker, Priority.ALWAYS);

                    // For debugging.
                    // Pane colorPane = new Pane();
                    // colorPane.setStyle("-fx-background-color: #99ff85");
                    // GridPane.setConstraints(colorPane, 2, rowInd.get());
                    // Pane colorPane2 = new Pane();
                    // colorPane2.setStyle("-fx-background-color: #e383ff");
                    // GridPane.setConstraints(colorPane2, 1, rowInd.get());
                    // gridPane.getChildren().addAll(name, colorPane2, firstCB, colorPane, secondCB, colorPicker);

                    rowInd.incrementAndGet();

                    // Put every thing together
                    gridPane.getChildren().addAll(name, firstCB, secondCB, colorPicker);

                });
            }
        };

        new Thread(onStageLoad).start();

    }

    private void showHighlightsWindow() {

        if (firstTimeSetUpHighlightsWindowCall)
            firstTimeSetUpHighlightsWindow();

        mStage.show();

        /*
            On Apply button click behaviour.
            For each of the selected methods, insert the bound box properties into Highlights table if not already present.
        */
        Task<Void> taskOnApply = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (!HighlightDAOImpl.isTableCreated()) {
                    HighlightDAOImpl.createTable();
                }

                // For each of the selected methods, insert the bound box properties into Highlights table if not already present.
                Statement statement = DatabaseUtil.getConnection().createStatement();
                firstCBMap.forEach((fullName, checkBox) -> addInsertQueryToStatement(fullName, statement, "SINGLE"));

                secondCBMap.forEach((fullName, checkBox) -> addInsertQueryToStatement(fullName, statement, "FULL"));

                // Delete records from HIGHLIGHT_ELEMENT if that method is not checked in the stage.
                StringJoiner firstSJ = new StringJoiner("','", "'", "'");
                firstCBMap.forEach((fullName, checkBox) -> firstSJ.add(fullName));
                addDeleteQueryToStatement(firstSJ.toString(), statement, "SINGLE");

                StringJoiner secondSJ = new StringJoiner("','", "'", "'");
                secondCBMap.forEach((fullName, checkBox) -> secondSJ.add(fullName));
                addDeleteQueryToStatement(secondSJ.toString(), statement, "FULL");

                updateColors(statement);

                statement.executeBatch();

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                System.out.println("MenuController.showHighlightsWindow.succeeded: ");
                ControllerLoader.canvasController.clearAndUpdate();

                // Stack highlights so that the larger ones are behind smaller ones.
                ControllerLoader.canvasController.stackRectangles();
            }
        };

        applyButton.setOnAction(event -> {
            new Thread(taskOnApply).start();
            mStage.close();
        });

        cancelButton.setOnAction(event -> mStage.close());
    }

    private void addInsertQueryToStatement(String fullName, Statement statement, String highlightType) {
        double startXOffset = 50;
        double widthOffset = -5;
        double startYOffset = -12;
        double heightOffset = -15;

        String[] arr = fullName.split("\\.");
        String methodName = arr[arr.length - 1];
        String packageName = fullName.substring(0, fullName.length() - methodName.length() - 1);

        HighlightDAOImpl.insert(startXOffset, startYOffset, widthOffset, heightOffset, methodName, packageName, highlightType, colorsMap, fullName, statement);
    }

    private void addDeleteQueryToStatement(String fullNames, Statement statement, String highlightType) {
        String sql = "DELETE FROM " + TableNames.HIGHLIGHT_ELEMENT + " " +
                "WHERE HIGHLIGHT_TYPE = '" + highlightType + "' AND METHOD_ID NOT IN " +
                "(SELECT ID FROM " + TableNames.METHOD_DEFINITION_TABLE + " " +
                "WHERE (" + TableNames.METHOD_DEFINITION_TABLE + ".PACKAGE_NAME || '.' || " + TableNames.METHOD_DEFINITION_TABLE + ".METHOD_NAME) " +
                "IN (" + fullNames + "))";

        try {
            statement.addBatch(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateColors(Statement statement) {
        if (!anyColorChange)
            return;

        anyColorChange = false;

        colorsMap.forEach((fullName, color) -> {
            String sql = "UPDATE " + TableNames.HIGHLIGHT_ELEMENT + " SET COLOR = '" + color + "' " +
                    "WHERE METHOD_ID = (SELECT ID FROM " + TableNames.METHOD_DEFINITION_TABLE + " " +
                    "WHERE PACKAGE_NAME || '.' || METHOD_NAME = '" + fullName + "')";
            try {
                statement.addBatch(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public Map<String, BookmarkDTO> getBookmarkDTOs() {
        return BookmarksDAOImpl.getBookmarkDTOs();
    }

    public void insertBookmark(BookmarkDTO bookmarkDTO) {
        BookmarksDAOImpl.insertBookmark(bookmarkDTO);
        ControllerLoader.canvasController.addBookmarks();
        ControllerLoader.menuController.updateBookmarksMenu();

    }

    public void deleteBookmark(String elementId) {
        ControllerLoader.canvasController.removeBookmarkFromUI(elementId);
        BookmarksDAOImpl.deleteBookmark(elementId);
        ControllerLoader.menuController.updateBookmarksMenu();
    }

    public void deleteAllBookmarks() {
        ControllerLoader.canvasController.removeAllBookmarksFromUI();
        BookmarksDAOImpl.deleteBookmarks();
    }

    private void saveUIImage() {
        SnapshotParameters sp = new SnapshotParameters();
        sp.setTransform(Transform.scale(5, 5));

        // WritableImage image = ControllerLoader.canvasController.canvas.snapshot(sp, null);
        WritableImage image = ControllerLoader.canvasController.scrollPane.snapshot(sp, null);

        // Create screenshots folder if id doesn't exist.
        File dir = new File("Screenshots");
        if (!dir.exists()) {
            dir.mkdir();
        }

        String imgPath = "Screenshots" + File.separator + "screenshot-" + dir.list().length + ".png";
        File file = new File(imgPath);
        System.out.println("MenuController.saveUIImage image path: " + file.getPath());
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            System.out.println("saveUIImage exception");
        }
    }


    private void initMenuGraphics() {
        String font = "FontAwesome";
        List<Glyph> glyphsStyling = new ArrayList<>();
        List<MenuItem> menuItemsStyling = new ArrayList<>();

        menuBar = new MenuBar();
        menuBar.setStyle(SizeProp.PADDING_MENU);

        // *****************
        // File Menu
        // *****************
        methodDefnGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.PLUS);
        methodDefnGlyph.setColor(ColorProp.ENABLED);
        chooseMethodDefMenuItem.setGraphic(methodDefnGlyph);

        callTraceGlyph = new Glyph(font, FontAwesome.Glyph.PLUS);
        callTraceGlyph.setColor(Color.DIMGRAY);
        chooseCallTraceMenuItem.setGraphic(callTraceGlyph);

        chooseDBGlyph = new Glyph(font, FontAwesome.Glyph.FOLDER_OPEN);
        chooseDBGlyph.setColor(Color.DIMGRAY);
        chooseDBMenuItem.setGraphic(chooseDBGlyph);

        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();

        menuItemsStyling.add(chooseMethodDefMenuItem);
        menuItemsStyling.add(chooseCallTraceMenuItem);
        menuItemsStyling.add(chooseDBMenuItem);

        // *****************
        // Run Menu
        // *****************
        resetGlyph = new Glyph(font, FontAwesome.Glyph.RETWEET);
        resetGlyph.setColor(ColorProp.ENABLED);
        resetMenuItem.setGraphic(resetGlyph);
        // resetMenuItem.setStyle(SizeProp.PADDING_SUBMENU);

        runAnalysisGlyph = new Glyph(font, FontAwesome.Glyph.PLAY);
        runAnalysisGlyph.setColor(ColorProp.DISABLED);
        runAnalysisMenuItem.setGraphic(runAnalysisGlyph);
        // runAnalysisMenuItem.setStyle(SizeProp.PADDING_SUBMENU);
        runAnalysisMenuItem.setDisable(true);

        menuItemsStyling.add(runAnalysisMenuItem);
        menuItemsStyling.add(resetMenuItem);

        // *****************
        // View Menu
        // *****************
        viewMenu.setDisable(true);

        saveImageGlyph = new Glyph(font, FontAwesome.Glyph.PICTURE_ALT);
        saveImageGlyph.setColor(ColorProp.ENABLED);
        saveImageMenuItem.setGraphic(saveImageGlyph);

        menuItemsStyling.add(saveImageMenuItem);

        refreshGlyph = new Glyph(font, FontAwesome.Glyph.REFRESH);
        refreshGlyph.setColor(ColorProp.ENABLED);
        refreshMenuItem.setGraphic(refreshGlyph);

        menuItemsStyling.add(refreshMenuItem);
        glyphsStyling.add(refreshGlyph);

        // *****************
        // Highlights Menu
        // *****************
        highlightItemsGlyph = new Glyph(font, FontAwesome.Glyph.FLAG);
        highlightItemsGlyph.setColor(ColorProp.ENABLED);
        addHighlightMenuItem = new MenuItem("Highlight method invocations", highlightItemsGlyph);

        highlightMenu.setDisable(true);
        menuItemsStyling.add(addHighlightMenuItem);

        // *****************
        // Bookmarks Menu
        // *****************
        bookmarksMenu.setDisable(true);
        bookmarksGlyph = new Glyph(font, FontAwesome.Glyph.BOOKMARK);
        bookmarksGlyph.setColor(ColorProp.ENABLED);

        // bookmarksMenu.setDisable(true);
        menuItemsStyling.add(bookmarksMenu);
        glyphsStyling.add(bookmarksGlyph);

        // *****************
        // Debug Menu
        // *****************
        // debugMenu.setDisable(true);
        // printCellsMenuItem = new MenuItem("Print circles on canvas to console");
        // printEdgesMenuItem = new MenuItem("Print edges on canvas to console");
        // printBarMarksItem = new MenuItem("Print bookmark marks to console");
        // printHighlightsMenuItem = new MenuItem("Print highlights on canvas to console");

        // debugMenu.getItems().addAll(printCellsMenuItem, printEdgesMenuItem, printHighlightsMenuItem, printBarMarksItem);

        // *****************
        // Main Menu
        // *****************
        glyphsStyling.addAll(Arrays.asList(methodDefnGlyph, callTraceGlyph, chooseDBGlyph, resetGlyph, runAnalysisGlyph,
                saveImageGlyph,
                // recentsGlyph, clearHistoryGlyph,
                highlightItemsGlyph));

        menuItemsStyling.forEach(menuItem -> menuItem.setStyle(SizeProp.PADDING_SUBMENU));
        glyphsStyling.forEach(glyph -> glyph.setStyle(SizeProp.PADDING_ICONS));
    }

    private void setChooseMethodDefMenuItemGraphics(boolean enabled, boolean isSet) {
        if (enabled && isSet) {
            chooseMethodDefMenuItem.setDisable(false);
            methodDefnGlyph.setIcon(FontAwesome.Glyph.CHECK);
            methodDefnGlyph.setColor(ColorProp.GREEN);
        } else if (enabled && !isSet) {
            chooseMethodDefMenuItem.setDisable(false);
            methodDefnGlyph.setIcon(FontAwesome.Glyph.PLUS);
            methodDefnGlyph.setColor(ColorProp.GREY);
        } else if (!enabled && isSet) {
            // after choosing files and clicking run analysis
            chooseMethodDefMenuItem.setDisable(true);
            methodDefnGlyph.setIcon(FontAwesome.Glyph.CHECK);
            methodDefnGlyph.setColor(ColorProp.GREEN);
        } else if (!enabled && !isSet) {
            // after choosing database and clicking run analysis
            chooseMethodDefMenuItem.setDisable(true);
            methodDefnGlyph.setIcon(FontAwesome.Glyph.PLUS);
            methodDefnGlyph.setColor(ColorProp.GREY);
        }
    }

    private void setChooseCallTraceMenuItemGraphics(boolean enabled, boolean isSet) {
        if (enabled && isSet) {
            chooseCallTraceMenuItem.setDisable(false);
            callTraceGlyph.setIcon(FontAwesome.Glyph.CHECK);
            callTraceGlyph.setColor(ColorProp.GREEN);
        } else if (enabled && !isSet) {
            chooseCallTraceMenuItem.setDisable(false);
            callTraceGlyph.setIcon(FontAwesome.Glyph.PLUS);
            callTraceGlyph.setColor(ColorProp.GREY);
        } else if (!enabled && isSet) {
            // after choosing files and clicking run analysis
            chooseCallTraceMenuItem.setDisable(true);
            callTraceGlyph.setIcon(FontAwesome.Glyph.CHECK);
            callTraceGlyph.setColor(ColorProp.GREEN);
        } else if (!enabled && !isSet) {
            // after choosing dataaccess and clicking run analysis
            chooseCallTraceMenuItem.setDisable(true);
            callTraceGlyph.setIcon(FontAwesome.Glyph.PLUS);
            callTraceGlyph.setColor(ColorProp.GREY);
        }
    }

    private void setDBRelatedGraphics(boolean enabled) {
        chooseMethodDefMenuItem.setDisable(enabled);
        chooseCallTraceMenuItem.setDisable(enabled);
        chooseDBMenuItem.setDisable(!enabled);

        if (LoadedFiles.isLoadedFromDB()) {
            chooseDBGlyph.setColor(ColorProp.GREEN);
            ControllerLoader.instructionsPaneController.setDBInfoGraphics(true);
            ControllerLoader.instructionsPaneController.setDBRunInfoGraphics(true);
        } else {
            chooseDBGlyph.setColor(ColorProp.GREY);
        }
    }

    private void setRunAnalysisMenuItemGraphics(boolean enabled) {
        if (enabled) {
            runAnalysisMenuItem.setDisable(false);
            runAnalysisGlyph.setColor(ColorProp.GREEN);
        } else {
            runAnalysisMenuItem.setDisable(true);
            runAnalysisGlyph.setColor(ColorProp.GREY);
        }
    }

    private void setResetMenuItemGraphics(boolean enabled) {
        if (enabled) {
            resetMenuItem.setDisable(false);
            resetGlyph.setColor(ColorProp.GREY);
        } else {
            resetMenuItem.setDisable(true);
            resetGlyph.setColor(ColorProp.GREY);
        }
    }

    private void setRemainingMenuGraphics(boolean enabled) {
        highlightMenu.setDisable(!enabled);
        bookmarksMenu.setDisable(!enabled);
        viewMenu.setDisable(!enabled);
        debugMenu.setDisable(!enabled);
        settingMenu.setDisable(!enabled);
    }

    public void postGraphLoadProcess() {
        updateBookmarksMenu();
    }
}
