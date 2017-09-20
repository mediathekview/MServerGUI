package de.mediathekview.mserver.ui.gui.tasks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ResourceBundle;
import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import de.mediathekview.mserver.crawler.CrawlerManager;
import javafx.stage.Stage;

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
  }

}
