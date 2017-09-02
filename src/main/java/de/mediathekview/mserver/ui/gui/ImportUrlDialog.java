package de.mediathekview.mserver.ui.gui;

import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;

import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class ImportUrlDialog extends Dialog<ImportUrlResult>
{

    public ImportUrlDialog(final ResourceBundle aResourcebundle) throws IOException
    {
        super();

        final DialogPane dialogPane =
                FXMLLoader.load(getClass().getClassLoader().getResource("fxml/ImportUrlDialog.fxml"), aResourcebundle);
        setDialogPane(dialogPane);
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        final TextField urlInput = (TextField) dialogPane.lookup("#urlInput");
        final ComboBox<FilmlistFormats> formatComboBox =
                (ComboBox<FilmlistFormats>) dialogPane.lookup("#formatComboBox");

        setResultConverter((dialogButton) -> {
            final ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonData.OK_DONE ? new ImportUrlResult(urlInput.getText(), formatComboBox.getValue())
                    : null;
        });
        formatComboBox.setItems(FXCollections.observableArrayList(FilmlistFormats.values()));

        final Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        okButton.setText(aResourcebundle.getString("button.import"));

        final Button cancleButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancleButton.setText(aResourcebundle.getString("button.cancel"));

        formatComboBox.setConverter(new StringConverter<FilmlistFormats>()
        {

            @Override
            public String toString(final FilmlistFormats aFilmlistFormat)
            {
                return aFilmlistFormat.getDescription();
            }

            @Override
            public FilmlistFormats fromString(final String aFilmlistFormat)
            {
                final Optional<FilmlistFormats> format = FilmlistFormats.getByDescription(aFilmlistFormat);
                if (format.isPresent())
                {
                    return format.get();
                }
                else
                {
                    throw new IllegalArgumentException(aFilmlistFormat + " is not a FilmlistFormat");
                }
            }
        });

        urlInput.textProperty()
                .addListener((event, oldValue, newValue) -> okButton.setDisable(newValue.trim().isEmpty()));

    }

}
