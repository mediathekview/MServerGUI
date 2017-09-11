package de.mediathekview.mserver.ui.gui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.text.Text;

public class MessageUpdator implements Runnable
{

    private static final Logger LOG = LogManager.getLogger(MessageUpdator.class);
    private static final int ONE_SECEOND = 1000;
    private static final String CONSOLE_PATTERN = "%s - %s";

    private boolean shouldRun;
    private final ObservableList<String> messages;
    private final ConcurrentLinkedQueue<MessageWrapper> newMessageQuoe;
    private final CheckBox debugCheckBox;

    public MessageUpdator(final ObservableList<String> aMessages, final CheckBox aDebugCheckBox)
    {
        shouldRun = true;
        messages = aMessages;
        debugCheckBox = aDebugCheckBox;
        newMessageQuoe = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void run()
    {
        while (shouldRun)
        {
            if (!newMessageQuoe.isEmpty())
            {
                Platform.runLater(() -> processMessage());
            }
            try
            {
                Thread.sleep(ONE_SECEOND);
            }
            catch (final InterruptedException interruptedException)
            {
                LOG.debug(interruptedException);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processMessage()
    {
        while (!newMessageQuoe.isEmpty())
        {
            printMessage(newMessageQuoe.poll());
        }
    }

    public void offerMessage(final MessageWrapper aMessageWrapper)
    {
        newMessageQuoe.offer(aMessageWrapper);
    }

    public void stop()
    {
        shouldRun = false;
    }

    private void printMessage(final MessageWrapper aMessageWrapper)
    {
        switch (aMessageWrapper.getType())
        {
        case DEBUG:
            if (debugCheckBox.isSelected())
            {
                messageToConsole(aMessageWrapper.getMessage());
            }
            break;
        case INFO:
        case WARNING:
            messageToConsole(aMessageWrapper.getMessage());
            break;
        default:
            messageToDialog(aMessageWrapper.getMessage());
        }
    }

    private void messageToDialog(final String aMessage)
    {
        final Alert alert = new Alert(AlertType.ERROR);
        alert.setWidth(300);
        final Text text = new Text(aMessage);
        text.setWrappingWidth(alert.getWidth());
        alert.getDialogPane().setContent(text);
        alert.show();
    }

    private void messageToConsole(final String aMessageText)
    {
        messages.add(String.format(CONSOLE_PATTERN,
                LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)), aMessageText));

    }

}
