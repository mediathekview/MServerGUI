package de.mediathekview.mserver.ui.gui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

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

    private static final String MESSAGE_KEY_ERROR_URL_INVALID = "error.urlInvalid";
    private static final Logger LOG = LogManager.getLogger(ImportUrlDialog.class);

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
        okButton.setText(aResourcebundle.getString("button.import"));
        okButton.setDisable(true);

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

        final ValidationSupport support = new ValidationSupport();

        final Validator<String> validator = (control, value) -> ValidationResult.fromErrorIf(control,
                aResourcebundle.getString(MESSAGE_KEY_ERROR_URL_INVALID), !validateUrl(value));

        support.registerValidator(urlInput, true, validator);

        urlInput.textProperty().addListener(
                (event, oldValue, newValue) -> okButton.setDisable(validateData(newValue, formatComboBox)));
        formatComboBox.getSelectionModel().selectedItemProperty().addListener(
                (event, oldValue, newValue) -> okButton.setDisable(validateData(urlInput.getText(), formatComboBox)));

    }

    private boolean validateData(final String aNewValue, final ComboBox<FilmlistFormats> aFormatComboBox)
    {
        return !validateUrl(aNewValue) || aFormatComboBox.getSelectionModel().isEmpty();
    }

    private boolean validateUrl(final String aNewValue)
    {
        try
        {
            new URL(aNewValue);
            return !StringUtils.isEmpty(aNewValue);
        }
        catch (final MalformedURLException malformedURLException)
        {
            LOG.debug(malformedURLException);
            return false;
        }
    }

}
