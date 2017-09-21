package de.mediathekview.mserver.ui.gui;

/**
 * Contains the constants for MServerGUI.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *         <b>Skype:</b> Nicklas2751<br/>
 *
 */
public final class Consts {
  public static final String BUNDLE_NAME = "MServerGUI";
  public static final String BUNDLE_KEY_IMPORT_ERROR = "error.importError";
  public static final String BUNDLE_KEY_SELECTION_VIEW_TARGET = "selectionView.target";
  public static final String BUNDLE_KEY_SELECTION_VIEW_SOURCE = "selectionView.source";
  public static final String BUNDLE_KEY_ERROR_FXML_IO = "error.fxmlIoError";
  public static final String BUNDLE_KEY_ERROR_NO_SAVE_RIGHTS = "error.noSaveRights";
  public static final String BUNDLE_KEY_TITLES_DIALOGS_SAVE = "titles.dialogs.save";
  public static final String BUNDLE_KEY_TITLES_DIALOGS_IMPORT = "titles.dialogs.import";
  public static final String BUNDLE_KEY_ERROR_SAVING_FILMLIST = "error.savingFilmlist";
  public static final String BUNDLE_KEY_BUTTON_CANCEL = "button.cancel";
  public static final String BUNDLE_KEY_BUTTON_IMPORT = "button.import";
  public static final String BUNDLE_KEY_ERROR_URL_INVALID = "error.urlInvalid";
  public static final String BUNDLE_KEY_CHART_WORKING = "chart.working";
  public static final String BUNDLE_KEY_CHART_FINISHED = "chart.finished";
  public static final String BUNDLE_KEY_CHART_ERROR = "chart.error";
  public static final String BUNDLE_KEY_PROGRESS_IMPORT = "progress.importText";
  public static final String BUNDLE_KEY_PROGRESS_SAVE = "progress.saveText";
  public static final String BUNDLE_KEY_TITLE_WINDOW = "titles.window";


  public static final String FXML_M_SERVER_GUI = "fxml/MServerGUI.fxml";
  public static final String FXML_ABOUT_DIALOG_FXML = "fxml/AboutDialog.fxml";
  public static final String FXML_IMPORT_URL_DIALOG_FXML = "fxml/ImportUrlDialog.fxml";
  public static final String FXML_PROGRESS_FXML = "fxml/Progress.fxml";

  public static final String FILE_NAME_LICENSE = "license.txt";
  public static final String FILE_NAME_VERSION_PROPERTIES = "version.properties";

  public static final String PROPERTY_VERSION = "version";

  public static final String FX_ID_VERSION_LABEL = "#versionLabel";
  public static final String FX_ID_FORMAT_COMBO_BOX = "#formatComboBox";
  public static final String FX_ID_URL_INPUT = "#urlInput";
  public static final String FX_ID_PROGRESS = "#progress";
  public static final String FX_ID_PROGESS_TEST = "#progressText";

  private Consts() {
    super();
  }
}
