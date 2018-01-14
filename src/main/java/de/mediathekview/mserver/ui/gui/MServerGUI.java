package de.mediathekview.mserver.ui.gui;

import static de.mediathekview.mserver.ui.gui.Consts.BUNDLE_KEY_ERROR_FXML_IO;
import static de.mediathekview.mserver.ui.gui.Consts.BUNDLE_KEY_ERROR_NO_SAVE_RIGHTS;
import static de.mediathekview.mserver.ui.gui.Consts.BUNDLE_KEY_ERROR_SAVING_FILMLIST;
import static de.mediathekview.mserver.ui.gui.Consts.BUNDLE_KEY_IMPORT_ERROR;
import static de.mediathekview.mserver.ui.gui.Consts.BUNDLE_KEY_SELECTION_VIEW_SOURCE;
import static de.mediathekview.mserver.ui.gui.Consts.BUNDLE_KEY_SELECTION_VIEW_TARGET;
import static de.mediathekview.mserver.ui.gui.Consts.BUNDLE_KEY_TITLES_DIALOGS_IMPORT;
import static de.mediathekview.mserver.ui.gui.Consts.BUNDLE_KEY_TITLES_DIALOGS_SAVE;
import static de.mediathekview.mserver.ui.gui.Consts.BUNDLE_KEY_TITLE_WINDOW;
import static de.mediathekview.mserver.ui.gui.Consts.BUNDLE_NAME;
import static de.mediathekview.mserver.ui.gui.Consts.FXML_M_SERVER_GUI;
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
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.CrawlerManager;
import de.mediathekview.mserver.ui.gui.dialogs.AboutDialog;
import de.mediathekview.mserver.ui.gui.dialogs.ImportUrlDialog;
import de.mediathekview.mserver.ui.gui.tasks.CrawlerTask;
import de.mediathekview.mserver.ui.gui.tasks.FilmlistImportTask;
import de.mediathekview.mserver.ui.gui.tasks.FilmlistSaveTask;
import de.mediathekview.mserver.ui.gui.tasks.MessageTask;
import de.mediathekview.mserver.ui.gui.tasks.MessageUpdator;
import de.mediathekview.mserver.ui.gui.wrappers.ImportUrlResult;
import de.mediathekview.mserver.ui.gui.wrappers.MessageWrapper;
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

/**
 * The base class for the MServer GUI and the main controller.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br>
 *         <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 *
 */
public class MServerGUI extends Application {
  private static final int DETAILS_TAB_INDEX = 1;
  static {
    MServerConfigManager.getInstance();
  }
  private static final Logger LOG = LogManager.getLogger(MServerGUI.class);
  private static final String FILE_EXTENSION_SEPERATOR = "*.";

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

  public MServerGUI() {
    crawlerManager = CrawlerManager.getInstance();
    bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
  }

  public static final Stage eventToStage(final Event aEvent) {
    return Stage.class.cast(Control.class.cast(aEvent.getSource()).getScene().getWindow());
  }

  public static void main(final String[] args) {
    Application.launch(args);
  }

  @FXML
  public void initialize() {
    initializeCrawleView();
    initializeCharts();
    initializeMessaging();
  }

  /**
   * Creates and shows the about dialog.
   */
  public void openAbout() {
    AboutDialog aboutDialog;
    try {
      aboutDialog = new AboutDialog(bundle);
      aboutDialog.setResizable(false);
      aboutDialog.show();
    } catch (final IOException ioException) {
      LOG.fatal("Something went wrong while opening the about dialog.", ioException);
      showError(BUNDLE_KEY_ERROR_FXML_IO, MessageTypes.FATAL_ERROR);
    }
  }

  /**
   * Creates and shows the film list as file import dialog.
   *
   * @param aEvent The JFX event.
   */
  @FXML
  public void openFileImportDialog(final Event aEvent) {
    try {
      boolean hasError = false;
      do {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString(BUNDLE_KEY_TITLES_DIALOGS_IMPORT));

        final List<ExtensionFilter> extensionFilters = Arrays.stream(FilmlistFormats.values())
            .map(this::toExtensionFilter).collect(Collectors.toList());
        fileChooser.getExtensionFilters().addAll(extensionFilters);

        final File selectedFile = fileChooser.showOpenDialog(eventToStage(aEvent));
        if (selectedFile == null) {
          hasError = false;
        } else {
          final Path selectedPath = selectedFile.toPath();
          if (Files.exists(selectedPath) && Files.isReadable(selectedPath)) {
            hasError = false;
            startImportTask(aEvent, fileChooser, selectedPath);
          } else {
            hasError = true;
          }
        }
      } while (hasError);
    } catch (final IOException ioException) {
      LOG.fatal("Unexpected error while importing the film list.", ioException);
      showError(BUNDLE_KEY_IMPORT_ERROR, MessageTypes.FATAL_ERROR);
    }
  }

  /**
   * Creates and opens the film list save dialog.
   *
   * @param aEvent The JFX event.
   */
  @FXML
  public void openSaveDialog(final Event aEvent) {
    boolean hasError = false;
    do {
      final FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle(bundle.getString(BUNDLE_KEY_TITLES_DIALOGS_SAVE));

      final List<ExtensionFilter> extensionFilters = Arrays.stream(FilmlistFormats.values())
          .map(this::toExtensionFilter).collect(Collectors.toList());
      fileChooser.getExtensionFilters().addAll(extensionFilters);

      final File selectedFile = fileChooser.showSaveDialog(eventToStage(aEvent));
      if (selectedFile == null) {
        hasError = false;
      } else {
        final Path selectedPath = selectedFile.toPath();
        if (Files.isWritable(selectedPath) || Files.isWritable(selectedPath.getParent())) {
          hasError = false;
          initializeFilmlistSaveTask(aEvent, fileChooser, selectedPath);
        } else {
          hasError = true;
          showError(BUNDLE_KEY_ERROR_NO_SAVE_RIGHTS, MessageTypes.ERROR);
        }
      }
    } while (hasError);
  }

  /**
   * Creates and opens the URL film list import dialog.
   *
   * @param aEvent
   * @throws IOException
   */
  @FXML
  public void openUrlImportDialog(final Event aEvent) {
    try {
      final ImportUrlDialog importUrlDialog = new ImportUrlDialog(bundle);
      final Optional<ImportUrlResult> result = importUrlDialog.showAndWait();

      if (result.isPresent()) {
        final FilmlistImportTask importTask = new FilmlistImportTask(eventToStage(aEvent), bundle,
            result.get().getFilmlistFormats(), result.get().getUrl());
        importTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
            (final WorkerStateEvent t) -> enableControls());
        new Thread(importTask).start();
        disableControls();
      }
    } catch (final IOException ioException) {
      LOG.fatal("Unexpected error while importing the film list.", ioException);
      showError(BUNDLE_KEY_IMPORT_ERROR, MessageTypes.FATAL_ERROR);
    }

  }

  @Override
  public void start(final Stage aPrimaryStage) throws Exception {
    aPrimaryStage.setTitle(bundle.getString(BUNDLE_KEY_TITLE_WINDOW));

    final Parent root =
        FXMLLoader.load(getClass().getClassLoader().getResource(FXML_M_SERVER_GUI), bundle);

    final Scene scene = new Scene(root);
    aPrimaryStage.setScene(scene);

    aPrimaryStage.show();
    aPrimaryStage.setOnCloseRequest((event) -> quit());
  }

  @FXML
  public void startCrawler() {
    mainTabPane.getSelectionModel().select(DETAILS_TAB_INDEX);
    final CrawlerTask crawlerTask = new CrawlerTask(bundle, pieChartData, processChartData,
        crawlerSelectionView.getTargetItems());
    crawlerTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
        (final WorkerStateEvent t) -> enableControls());
    progressBar.progressProperty().bind(crawlerTask.progressProperty());
    new Thread(crawlerTask).start();
    disableControls();
  }

  public ExtensionFilter toExtensionFilter(final FilmlistFormats aFilmlistFormats) {
    return new ExtensionFilter(aFilmlistFormats.name(),
        FILE_EXTENSION_SEPERATOR + aFilmlistFormats.getFileExtension());
  }

  private void checkStartButton() {
    startButton.setDisable(crawlerSelectionView.getTargetItems().isEmpty());
  }

  private void disableControls() {
    startButton.setDisable(true);
    saveButton.setDisable(true);
    startImport.setDisable(true);
    startImportUrl.setDisable(true);
    menuBar.setDisable(true);
  }

  private void enableControls() {
    checkStartButton();
    saveButton.setDisable(false);
    startImport.setDisable(false);
    startImportUrl.setDisable(false);
    menuBar.setDisable(false);
  }

  private void initializeCharts() {
    pieChartData = FXCollections.observableArrayList();
    statisticChart.setData(pieChartData);

    processChartData = FXCollections.observableArrayList();
    processChart.setData(processChartData);
  }

  private void initializeCrawleView() {
    crawlerSelectionView.setSourceItems(
        FXCollections.observableArrayList(crawlerManager.getAviableSenderToCrawl()));
    crawlerSelectionView
        .setSourceHeader(new Label(bundle.getString(BUNDLE_KEY_SELECTION_VIEW_SOURCE)));
    crawlerSelectionView
        .setTargetHeader(new Label(bundle.getString(BUNDLE_KEY_SELECTION_VIEW_TARGET)));

    crawlerSelectionView.getTargetItems()
        .addListener((ListChangeListener<Sender>) event -> checkStartButton());
  }

  private void initializeFilmlistSaveTask(final Event aEvent, final FileChooser fileChooser,
      final Path selectedPath) {
    final FilmlistFormats saveFormat = FilmlistFormats
        .valueOf(fileChooser.selectedExtensionFilterProperty().get().getDescription());
    try {
      final FilmlistSaveTask saveTask =
          new FilmlistSaveTask(eventToStage(aEvent), bundle, saveFormat, selectedPath);
      saveTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
          (final WorkerStateEvent t) -> enableControls());
      new Thread(saveTask).start();
    } catch (final IOException ioException) {
      LOG.fatal("Unexpected error while saving the film list.", ioException);
      showError(BUNDLE_KEY_ERROR_SAVING_FILMLIST, MessageTypes.FATAL_ERROR);
    }
  }

  private void initializeMessaging() {
    final ObservableList<String> messages = FXCollections.observableArrayList();
    messageList.setItems(messages);

    messageUpdator = new MessageUpdator(messages, debugCheckBox);
    messageTask = new MessageTask();
    messageTask.valueProperty()
        .addListener((final ObservableValue<? extends MessageWrapper> aObservable,
            final MessageWrapper aOldValue,
            final MessageWrapper aNewValue) -> messageUpdator.offerMessage(aNewValue));
    new Thread(messageUpdator).start();
    crawlerManager.addMessageListener(messageTask);
  }

  private void showError(final String bundleKey, final MessageTypes messageType) {
    messageUpdator.offerMessage(new MessageWrapper(bundle.getString(bundleKey), messageType));
    if (MessageTypes.FATAL_ERROR.equals(messageType)) {
      quit();
    }
  }

  private void startImportTask(final Event aEvent, final FileChooser fileChooser,
      final Path selectedPath) throws IOException {
    final FilmlistImportTask importTask = new FilmlistImportTask(eventToStage(aEvent), bundle,
        FilmlistFormats.valueOf(fileChooser.getSelectedExtensionFilter().getDescription()),
        selectedPath.toAbsolutePath().toString());
    importTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
        (final WorkerStateEvent t) -> enableControls());
    disableControls();
    new Thread(importTask).start();
  }

  @FXML
  protected void quit() {
    if (messageTask != null) {
      messageTask.stop();
    }
    if (messageUpdator != null) {
      messageUpdator.stop();
    }
    crawlerManager.stop();
    Platform.exit();
    System.exit(0);
  }

}
