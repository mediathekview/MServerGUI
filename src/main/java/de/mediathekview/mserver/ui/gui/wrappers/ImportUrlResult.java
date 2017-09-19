package de.mediathekview.mserver.ui.gui.wrappers;

import de.mediathekview.mlib.filmlisten.FilmlistFormats;

public class ImportUrlResult
{
    private final String url;
    private final FilmlistFormats filmlistFormats;

    public ImportUrlResult(final String aUrl, final FilmlistFormats aFilmlistFormats)
    {
        url = aUrl;
        filmlistFormats = aFilmlistFormats;
    }

    public String getUrl()
    {
        return url;
    }

    public FilmlistFormats getFilmlistFormats()
    {
        return filmlistFormats;
    }

}
