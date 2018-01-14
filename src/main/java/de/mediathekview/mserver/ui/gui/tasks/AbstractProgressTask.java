package de.mediathekview.mserver.ui.gui.tasks;

import static de.mediathekview.mserver.ui.gui.Consts.FXML_PROGRESS_FXML;
import static de.mediathekview.mserver.ui.gui.Consts.FX_ID_PROGESS_TEST;
import static de.mediathekview.mserver.ui.gui.Consts.FX_ID_PROGRESS;
import java.io.IOException;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A abstract class which defines the basics for a tasks which works with simple progress.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br>
 *         <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 *
 */
public abstract class AbstractProgressTask extends Task<Void> {

  private Stage stage;
  private final Stage parentStage;
  private final ResourceBundle resourceBundle;

  public AbstractProgressTask(final Stage aParentStage, final ResourceBundle aResourceBundle)
      throws IOException {
    parentStage = aParentStage;
    resourceBundle = aResourceBundle;
    this.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
        (final WorkerStateEvent t) -> stopDialog());

    initDialog();
  }

  private void initDialog() throws IOException {
    final VBox progressBox = FXMLLoader
        .load(getClass().getClassLoader().getResource(FXML_PROGRESS_FXML), resourceBundle);

    final Label progressText = (Label) progressBox.lookup(FX_ID_PROGESS_TEST);
    progressText.setText(resourceBundle.getString(getProgressTextKey()));

    final ProgressBar progressBar = (ProgressBar) progressBox.lookup(FX_ID_PROGRESS);
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

  private void stopDialog() {
    stage.close();
  }

  @Override
  protected Void call() throws Exception {
    doWork();
    return null;
  }

  /**
   * This method should do the work.
   */
  protected abstract void doWork();

  /**
   * A method to gather the resource bundle key for the text to show in the dialog.
   */
  protected abstract String getProgressTextKey();
}
