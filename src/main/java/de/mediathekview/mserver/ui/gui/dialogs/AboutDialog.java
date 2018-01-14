package de.mediathekview.mserver.ui.gui.dialogs;

import static de.mediathekview.mserver.ui.gui.Consts.FILE_NAME_LICENSE;
import static de.mediathekview.mserver.ui.gui.Consts.FILE_NAME_VERSION_PROPERTIES;
import static de.mediathekview.mserver.ui.gui.Consts.FXML_ABOUT_DIALOG_FXML;
import static de.mediathekview.mserver.ui.gui.Consts.FX_ID_VERSION_LABEL;
import static de.mediathekview.mserver.ui.gui.Consts.PROPERTY_VERSION;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * The controller and UI for the about dialog.
 * 
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br>
 *         <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 *
 */
public class AboutDialog extends Dialog<Void> {


  public AboutDialog(final ResourceBundle aResourcebundle) throws IOException {
    super();

    final DialogPane dialogPane = FXMLLoader
        .load(getClass().getClassLoader().getResource(FXML_ABOUT_DIALOG_FXML), aResourcebundle);
    setDialogPane(dialogPane);
    dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);

    final Label versionLabel = (Label) dialogPane.lookup(FX_ID_VERSION_LABEL);
    loadVersion(versionLabel);
    loadLicense(dialogPane);

  }

  private void loadLicense(final DialogPane dialogPane) throws IOException {
    final StringBuilder licenseTextBuilder = new StringBuilder();
    try (BufferedReader licenseReader = new BufferedReader(new InputStreamReader(
        getClass().getClassLoader().getResource(FILE_NAME_LICENSE).openStream(),
        StandardCharsets.UTF_8))) {
      licenseReader.lines()
          .forEach(t -> licenseTextBuilder.append(t).append(System.lineSeparator()));

      final TextArea licensTextArea = new TextArea();
      licensTextArea.setEditable(false);
      licensTextArea.setText(licenseTextBuilder.toString());

      dialogPane.setExpandableContent(licensTextArea);
      dialogPane.setExpanded(true);
    }
  }

  private void loadVersion(final Label versionLabel) throws IOException {
    try (InputStream propertiesInputStream =
        getClass().getClassLoader().getResource(FILE_NAME_VERSION_PROPERTIES).openStream()) {
      final Properties versionProperties = new Properties();
      versionProperties.load(propertiesInputStream);
      if (versionProperties.containsKey(PROPERTY_VERSION)) {
        versionLabel.setText(
            String.format(versionLabel.getText(), versionProperties.getProperty(PROPERTY_VERSION)));
      }
    }
  }

}
