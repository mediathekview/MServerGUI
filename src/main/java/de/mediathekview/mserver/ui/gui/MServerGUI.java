package de.mediathekview.mserver.ui.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.ListSelectionView;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import de.mediathekview.mlib.messages.MessageTypes;
import de.mediathekview.mserver.crawler.CrawlerManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class MServerGUI extends Application
{

    private static final int DETAILS_TAB_INDEX = 1;
    private static final Logger LOG = LogManager.getLogger(MServerGUI.class);
    private static final String FILE_EXTENSION_SEPERATOR = ".";

    private static final String BUNDLE_KEY_SELECTION_VIEW_TARGET = "selectionView.target";

    private static final String BUNDLE_KEY_SELECTION_VIEW_SOURCE = "selectionView.source";

    private final CrawlerManager crawlerManager;

    @FXML
    private MenuBar menuBar;

    @FXML
    private ListSelectionView<Sender> crawlerSelectionView;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private PieChart statisticChart;

    @FXML
    private BarChart<String, Number> processChart;

    @FXML
    private ListView<String> messageList;

    @FXML
    private Button startButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button startImport;

    @FXML
    private Button startImportUrl;

    @FXML
    private CheckBox debugCheckBox;

    @FXML
    private TabPane mainTabPane;

    private final ResourceBundle bundle;
    private ObservableList<Data> pieChartData;
    private MessageUpdator messageUpdator;
    private MessageTask messageTask;
    private ObservableList<Series<String, Number>> processChartData;

    public MServerGUI()
    {
        crawlerManager = CrawlerManager.getInstance();
        bundle = ResourceBundle.getBundle("MServerGUI", Locale.getDefault());
    }

    @Override
    public void start(final Stage aPrimaryStage) throws Exception
    {
        final Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/MServerGUI.fxml"), bundle);

        final Scene scene = new Scene(root);
        aPrimaryStage.setScene(scene);

        aPrimaryStage.show();
    }

    @FXML
    public void initialize()
    {
        crawlerSelectionView
                .setSourceItems(FXCollections.observableArrayList(crawlerManager.getAviableSenderToCrawl()));
        crawlerSelectionView.setSourceHeader(new Label(bundle.getString(BUNDLE_KEY_SELECTION_VIEW_SOURCE)));
        crawlerSelectionView.setTargetHeader(new Label(bundle.getString(BUNDLE_KEY_SELECTION_VIEW_TARGET)));

        crawlerSelectionView.getTargetItems().addListener((ListChangeListener<Sender>) event -> checkStartButton());

        pieChartData = FXCollections.observableArrayList();
        statisticChart.setData(pieChartData);

        processChartData = FXCollections.observableArrayList();
        processChart.setData(processChartData);

        final ObservableList<String> messages = FXCollections.observableArrayList();
        messageList.setItems(messages);

        messageUpdator = new MessageUpdator(messages, debugCheckBox);
        messageTask = new MessageTask();
        messageTask.valueProperty().addListener(
                (final ObservableValue<? extends MessageWrapper> aObservable, final MessageWrapper aOldValue,
                        final MessageWrapper aNewValue) -> messageUpdator.offerMessage(aNewValue));
        new Thread(messageUpdator).start();
        crawlerManager.addMessageListener(messageTask);
    }

    @FXML
    protected void quit()
    {
        messageTask.stop();
        messageUpdator.stop();
        crawlerManager.stop();
        Platform.exit();
        System.exit(0);
    }

    @FXML
    public void startCrawler()
    {
        mainTabPane.getSelectionModel().select(DETAILS_TAB_INDEX);
        final CrawlerTask crawlerTask =
                new CrawlerTask(bundle, pieChartData, processChartData, crawlerSelectionView.getTargetItems());
        crawlerTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                (final WorkerStateEvent t) -> enableControls());
        progressBar.progressProperty().bind(crawlerTask.progressProperty());
        new Thread(crawlerTask).start();
        disableControls();
    }

    @FXML
    public void openSaveDialog(final Event aEvent)
    {
        boolean hasError = false;
        do
        {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(bundle.getString("titles.dialogs.save"));

            final List<ExtensionFilter> extensionFilters =
                    Arrays.stream(FilmlistFormats.values()).map(this::toExtensionFilter).collect(Collectors.toList());
            fileChooser.getExtensionFilters().addAll(extensionFilters);

            final File selectedFile = fileChooser.showSaveDialog(eventToStage(aEvent));
            if (selectedFile == null)
            {
                hasError = false;
            }
            else
            {
                final Path selectedPath = selectedFile.toPath();
                if (Files.isWritable(selectedPath) || Files.isWritable(selectedPath.getParent()))
                {
                    hasError = false;
                    final FilmlistFormats saveFormat = FilmlistFormats
                            .valueOf(fileChooser.selectedExtensionFilterProperty().get().getDescription());
                    crawlerManager.saveFilmlist(selectedPath, saveFormat);
                }
                else
                {
                    hasError = true;
                    messageUpdator.offerMessage(
                            new MessageWrapper(bundle.getString("error.noSaveRights"), MessageTypes.ERROR));
                }
            }
        }
        while (hasError);
    }

    @FXML
    public void openFileImportDialog(final Event aEvent)
    {
        try
        {
            boolean hasError = false;
            do
            {
                final FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(bundle.getString("titles.dialogs.import"));

                final List<ExtensionFilter> extensionFilters = Arrays.stream(FilmlistFormats.values())
                        .map(this::toExtensionFilter).collect(Collectors.toList());
                fileChooser.getExtensionFilters().addAll(extensionFilters);

                final File selectedFile = fileChooser.showOpenDialog(eventToStage(aEvent));
                if (selectedFile == null)
                {
                    hasError = false;
                }
                else
                {
                    final Path selectedPath = selectedFile.toPath();
                    if (Files.exists(selectedPath) && Files.isReadable(selectedPath))
                    {
                        hasError = false;
                        final FilmlistImportTask importTask = new FilmlistImportTask(eventToStage(aEvent), bundle,
                                FilmlistFormats.valueOf(fileChooser.getSelectedExtensionFilter().getDescription()),
                                selectedPath.toAbsolutePath().toString());
                        importTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                                (final WorkerStateEvent t) -> enableControls());
                        disableControls();
                        new Thread(importTask).start();
                    }
                    else
                    {
                        hasError = true;
                    }
                }
            }
            while (hasError);
        }
        catch (final IOException ioException)
        {
            LOG.fatal("Unexpected error while importing the film list.", ioException);
            throw new IllegalStateException(ioException);
        }
    }

    private void disableControls()
    {
        startButton.setDisable(true);
        saveButton.setDisable(true);
        startImport.setDisable(true);
        startImportUrl.setDisable(true);
        menuBar.setDisable(true);
    }

    private void enableControls()
    {
        checkStartButton();
        saveButton.setDisable(false);
        startImport.setDisable(false);
        startImportUrl.setDisable(false);
        menuBar.setDisable(false);
    }

    private void checkStartButton()
    {
        startButton.setDisable(crawlerSelectionView.getTargetItems().isEmpty());
    }

    @FXML
    public void openUrlImportDialog(final Event aEvent) throws IOException
    {
        final ImportUrlDialog importUrlDialog = new ImportUrlDialog(bundle);
        final Optional<ImportUrlResult> result = importUrlDialog.showAndWait();

        if (result.isPresent())
        {
            try
            {
                final FilmlistImportTask importTask = new FilmlistImportTask(eventToStage(aEvent), bundle,
                        result.get().getFilmlistFormats(), result.get().getUrl());
                importTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                        (final WorkerStateEvent t) -> enableControls());
                new Thread(importTask).start();
            }
            catch (final IOException ioException)
            {
                LOG.fatal("Unexpected error while importing the film list.", ioException);
                throw new IllegalStateException(ioException);
            }
            disableControls();
        }

    }

    public void openPreferences()
    {

    }

    public void openAbout()
    {

    }

    public static final Stage eventToStage(final Event aEvent)
    {
        return Stage.class.cast(Control.class.cast(aEvent.getSource()).getScene().getWindow());
    }

    public ExtensionFilter toExtensionFilter(final FilmlistFormats aFilmlistFormats)
    {
        return new ExtensionFilter(aFilmlistFormats.name(),
                FILE_EXTENSION_SEPERATOR + aFilmlistFormats.getFileExtension());
    }

}
