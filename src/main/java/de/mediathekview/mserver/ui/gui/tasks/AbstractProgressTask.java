package de.mediathekview.mserver.ui.gui.tasks;

import java.io.IOException;
import java.util.ResourceBundle;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public abstract class AbstractProgressTask extends Task<Void>
{
    private static final String FXML_PROGRESS_FXML = "fxml/Progress.fxml";
    private Stage stage;
    private final Stage parentStage;
    private final ResourceBundle resourceBundle;

    public AbstractProgressTask(final Stage aParentStage, final ResourceBundle aResourceBundle) throws IOException
    {
        parentStage = aParentStage;
        resourceBundle = aResourceBundle;
        this.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (final WorkerStateEvent t) -> stopDialog());

        initDialog();
    }

    private void initDialog() throws IOException
    {
        final VBox progressBox =
                FXMLLoader.load(getClass().getClassLoader().getResource(FXML_PROGRESS_FXML), resourceBundle);

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
        doWork();
        updateProgress(1, 1);
        return null;
    }

    protected abstract void doWork();
}
