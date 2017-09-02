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

import org.controlsfx.control.ListSelectionView;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mlib.progress.Progress;
import de.mediathekview.mserver.crawler.CrawlerManager;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class MServerGUI extends Application implements MessageListener
{

    private static final String FILE_EXTENSION_SEPERATOR = ".";

    private static final String BUNDLE_KEY_SELECTION_VIEW_TARGET = "selectionView.target";

    private static final String BUNDLE_KEY_SELECTION_VIEW_SOURCE = "selectionView.source";

    private final CrawlerManager crawlerManager;

    @FXML
    private ListSelectionView<Sender> crawlerSelectionView;

    @FXML
    ProgressBar progressBar;

    @FXML
    PieChart statisticChart;

    @FXML
    ListView<String> messageList;

    @FXML
    Button startButton;

    @FXML
    Button saveButton;

    @FXML
    Button startImport;

    @FXML
    Button startImportUrl;

    private final ResourceBundle bundle;

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

        // crawlerManager.addMessageListener(this);
    }

    @FXML
    protected void quit()
    {
        Platform.exit();
    }

    @FXML
    public void startCrawler()
    {

    }

    @FXML
    public void openSaveDialog()
    {

    }

    @FXML
    public void openFileImportDialog(final Event aEvent)
    {
        boolean hasError = false;
        do
        {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");

            final List<ExtensionFilter> extensionFilters =
                    Arrays.stream(FilmlistFormats.values()).map(this::toExtensionFilter).collect(Collectors.toList());
            fileChooser.getExtensionFilters().addAll(extensionFilters);

            final File selectedFile = fileChooser.showOpenDialog(eventToStage(aEvent));
            if (selectedFile != null)
            {
                final Path selectedPath = selectedFile.toPath();
                if (Files.exists(selectedPath) && Files.isReadable(selectedPath))
                {
                    hasError = false;
                    new Thread(new FilmlistLoadTask(
                            FilmlistFormats.valueOf(fileChooser.getSelectedExtensionFilter().getDescription()),
                            selectedPath.toAbsolutePath().toString(), 1)).start();
                }
                else
                {
                    hasError = true;
                }
            }
        }
        while (hasError);
    }

    @FXML
    public void openUrlImportDialog(final Event aEvent) throws IOException
    {
        final ImportUrlDialog importUrlDialog = new ImportUrlDialog(bundle);
        final Optional<ImportUrlResult> result = importUrlDialog.showAndWait();

        if (result.isPresent())
        {
            new Thread(new FilmlistLoadTask(result.get().getFilmlistFormats(), result.get().getUrl(), 1)).start();
        }
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

    @Override
    public void consumeMessage(final Message aArg0, final Object... aArg1)
    {
        // TODO Auto-generated method stub

    }

    class FilmlistLoadTask extends Task<Void>
    {
        private final FilmlistFormats filmlistFormat;
        private final String location;
        private final int crawlerCount;

        public FilmlistLoadTask(final FilmlistFormats aFilmlistFormat, final String aLocation, final int aCrawlerCount)
        {
            filmlistFormat = aFilmlistFormat;
            location = aLocation;
            crawlerCount = aCrawlerCount;
        }

        @Override
        protected Void call()
        {
            crawlerManager.addSenderProgressListener(new LoadListener());
            crawlerManager.importFilmlist(filmlistFormat, location);
            return null;
        }

        class LoadListener implements SenderProgressListener
        {

            @Override
            public void updateProgess(final Sender aSender, final Progress aProgress)
            {
                updateProgress(aProgress.calcProgressInPercent() / crawlerCount, 100);
            }

        }
    }

}
