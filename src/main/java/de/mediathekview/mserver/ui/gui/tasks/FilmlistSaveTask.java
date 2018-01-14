package de.mediathekview.mserver.ui.gui.tasks;

import static de.mediathekview.mserver.ui.gui.Consts.BUNDLE_KEY_PROGRESS_SAVE;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ResourceBundle;
import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import de.mediathekview.mserver.crawler.CrawlerManager;
import javafx.stage.Stage;

/**
 * A Task for the film list import process.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br>
 *         <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 *
 */
public class FilmlistSaveTask extends AbstractProgressTask {

  private final FilmlistFormats filmlistFormat;
  private final Path savePath;

  public FilmlistSaveTask(final Stage aParentStage, final ResourceBundle aResourceBundle,
      final FilmlistFormats aFilmlistFormat, final Path aSavePath) throws IOException {
    super(aParentStage, aResourceBundle);
    filmlistFormat = aFilmlistFormat;
    savePath = aSavePath;
  }

  @Override
  protected void doWork() {
    CrawlerManager.getInstance().saveFilmlist(savePath, filmlistFormat);
    updateProgress(1, 1);
  }

  @Override
  protected String getProgressTextKey() {
    return BUNDLE_KEY_PROGRESS_SAVE;
  }

}
