package de.mediathekview.mserver.ui.gui;

import java.io.IOException;
import java.util.ResourceBundle;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import de.mediathekview.mserver.crawler.CrawlerManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FilmlistImportTask extends Task<Void>
{
    private Stage stage;

    private final ObservableList<Film> filmlist;

    private final Stage parentStage;

    private final FilmlistFormats filmlistFormat;

    private final String url;

    private final ResourceBundle resourceBundle;

    public FilmlistImportTask(final Stage aParentStage, final ResourceBundle aResourceBundle,
            final FilmlistFormats aFilmlistFormat, final String aUrl) throws IOException
    {
        parentStage = aParentStage;
        resourceBundle = aResourceBundle;
        filmlistFormat = aFilmlistFormat;
        url = aUrl;
        filmlist = FXCollections.emptyObservableList();

        this.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (final WorkerStateEvent t) -> {
            stopDialog();
        });

        initDialog();
    }

    public ObservableList<Film> getFilmlist()
    {
        return filmlist;
    }

    private void initDialog() throws IOException
    {
        final VBox progressBox =
                FXMLLoader.load(getClass().getClassLoader().getResource("fxml/ImportProgress.fxml"), resourceBundle);

        final ProgressBar progressBar = (ProgressBar) progressBox.lookup("#progress");
        progressBar.progressProperty().bind(progressProperty());

        final Scene scene = new Scene(progressBox);
        final double centerXPosition = parentStage.getX() + parentStage.getWidth() / 2d;
        final double centerYPosition = parentStage.getY() + parentStage.getHeight() / 2d;

        stage = new Stage();
        stage.setX(centerXPosition);
        stage.setY(centerYPosition);
        stage.initOwner(parentStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.show();
    }

    private void stopDialog()
    {
        stage.close();
    }

    @Override
    protected Void call() throws Exception
    {
        CrawlerManager.getInstance().importFilmlist(filmlistFormat, url);
        updateProgress(1, 1);
        return null;
    }

}
