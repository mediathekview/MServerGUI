package de.mediathekview.mserver.ui.gui.tasks;

import static de.mediathekview.mserver.ui.gui.Consts.BUNDLE_KEY_PROGRESS_IMPORT;
import java.io.IOException;
import java.util.ResourceBundle;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import de.mediathekview.mserver.crawler.CrawlerManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

/**
 * A task for the film list import process.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *         <b>Skype:</b> Nicklas2751<br/>
 *
 */
public class FilmlistImportTask extends AbstractProgressTask {

  private final ObservableList<Film> filmlist;
  private final FilmlistFormats filmlistFormat;
  private final String url;

  public FilmlistImportTask(final Stage aParentStage, final ResourceBundle aResourceBundle,
      final FilmlistFormats aFilmlistFormat, final String aUrl) throws IOException {
    super(aParentStage, aResourceBundle);
    filmlistFormat = aFilmlistFormat;
    url = aUrl;
    filmlist = FXCollections.emptyObservableList();

  }

  public ObservableList<Film> getFilmlist() {
    return filmlist;
  }

  @Override
  protected void doWork() {
    CrawlerManager.getInstance().importFilmlist(filmlistFormat, url);
    updateProgress(1, 1);
  }

  @Override
  protected String getProgressTextKey() {
    return BUNDLE_KEY_PROGRESS_IMPORT;
  }

}
