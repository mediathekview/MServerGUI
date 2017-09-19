package de.mediathekview.mserver.ui.gui.dialogs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class AboutDialog extends Dialog<Void>
{

    private static final String FXML_ABOUT_DIALOG_FXML = "fxml/AboutDialog.fxml";
    private static final String FILE_NAME_LICENSE = "license.txt";
    private static final String FX_ID_VERSION_LABEL = "#versionLabel";
    private static final String PROPERTY_VERSION = "version";
    private static final String FILE_NAME_VERSION_PROPERTIES = "version.properties";

    public AboutDialog(final ResourceBundle aResourcebundle) throws IOException, URISyntaxException
    {
        super();

        final DialogPane dialogPane =
                FXMLLoader.load(getClass().getClassLoader().getResource(FXML_ABOUT_DIALOG_FXML), aResourcebundle);
        setDialogPane(dialogPane);
        dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);

        final Label versionLabel = (Label) dialogPane.lookup(FX_ID_VERSION_LABEL);

        try (InputStream propertiesInputStream =
                getClass().getClassLoader().getResource(FILE_NAME_VERSION_PROPERTIES).openStream())
        {
            final Properties versionProperties = new Properties();
            versionProperties.load(propertiesInputStream);
            if (versionProperties.containsKey(PROPERTY_VERSION))
            {
                versionLabel.setText(
                        String.format(versionLabel.getText(), versionProperties.getProperty(PROPERTY_VERSION)));
            }
        }

        final StringBuilder licenseTextBuilder = new StringBuilder();
        try (BufferedReader licenseReader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResource(FILE_NAME_LICENSE).openStream(), StandardCharsets.UTF_8)))
        {
            licenseReader.lines().forEach(t -> licenseTextBuilder.append(t).append(System.lineSeparator()));

            final TextArea licensTextArea = new TextArea();
            licensTextArea.setEditable(false);
            licensTextArea.setText(licenseTextBuilder.toString());

            dialogPane.setExpandableContent(licensTextArea);
            dialogPane.setExpanded(true);
        }

    }

}
