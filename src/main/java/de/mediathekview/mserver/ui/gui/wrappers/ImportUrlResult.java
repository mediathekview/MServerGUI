package de.mediathekview.mserver.ui.gui.wrappers;

import de.mediathekview.mlib.filmlisten.FilmlistFormats;

/**
 * A wrapper class for the film list URL import information.
 * 
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br>
 *         <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 *
 */
public class ImportUrlResult {
  private final String url;
  private final FilmlistFormats filmlistFormats;

  public ImportUrlResult(final String aUrl, final FilmlistFormats aFilmlistFormats) {
    url = aUrl;
    filmlistFormats = aFilmlistFormats;
  }

  public FilmlistFormats getFilmlistFormats() {
    return filmlistFormats;
  }

  public String getUrl() {
    return url;
  }

}
