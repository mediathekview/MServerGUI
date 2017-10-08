package de.mediathekview.mserver.ui.gui.tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.mediathekview.mserver.ui.gui.wrappers.MessageWrapper;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.text.Text;

/**
 * A thread which waits for new external incoming messages and displays them based on their type.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *         <b>Skype:</b> Nicklas2751<br/>
 *
 */
public class MessageUpdator implements Runnable {

  private static final Logger LOG = LogManager.getLogger(MessageUpdator.class);
  private static final int ONE_SECEOND = 1000;
  private static final String CONSOLE_PATTERN = "%s - %s";

  private boolean shouldRun;
  private final ObservableList<String> messages;
  private final ConcurrentLinkedQueue<MessageWrapper> newMessageQuoe;
  private final CheckBox debugCheckBox;
  private Temporal lastMessageTime;

  public MessageUpdator(final ObservableList<String> aMessages, final CheckBox aDebugCheckBox) {
    shouldRun = true;
    messages = aMessages;
    debugCheckBox = aDebugCheckBox;
    newMessageQuoe = new ConcurrentLinkedQueue<>();
  }

  /**
   * Takes a new wrapped message and adds it to his internal quo.
   *
   * @param aMessageWrapper The new wrapped message.
   * @see ConcurrentLinkedQueue#offer(Object)
   */
  public void offerMessage(final MessageWrapper aMessageWrapper) {
    newMessageQuoe.offer(aMessageWrapper);
  }

  @Override
  public void run() {
    while (shouldRun) {
      if (!newMessageQuoe.isEmpty()) {
        Platform.runLater(this::processMessage);
      }
      try {
        Thread.sleep(ONE_SECEOND);
      } catch (final InterruptedException interruptedException) {
        LOG.debug(interruptedException);
        Thread.currentThread().interrupt();
      }
    }
  }

  public void stop() {
    shouldRun = false;
  }

  private boolean isLastDialogOlderThen5Minutes() {
    return lastMessageTime == null || Duration.between(lastMessageTime, LocalDateTime.now())
        .compareTo(Duration.of(5, ChronoUnit.MINUTES)) <= 0;
  }

  private void messageToConsole(final String aMessageText) {
    messages.add(String.format(CONSOLE_PATTERN,
        LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)),
        aMessageText));

  }

  private void messageToDialog(final String aMessage) {
    final Alert alert = new Alert(AlertType.ERROR);
    alert.setWidth(300);
    final Text text = new Text(aMessage);
    text.setWrappingWidth(alert.getWidth());
    alert.getDialogPane().setContent(text);
    alert.show();
  }

  private void printMessage(final MessageWrapper aMessageWrapper) {
    switch (aMessageWrapper.getType()) {
      case DEBUG:
        if (debugCheckBox.isSelected()) {
          messageToConsole(aMessageWrapper.getMessage());
        }
        break;
      case INFO:
      case WARNING:
        messageToConsole(aMessageWrapper.getMessage());
        break;
      default:
        if (isLastDialogOlderThen5Minutes()) {
          lastMessageTime = LocalDateTime.now();
          messageToDialog(aMessageWrapper.getMessage());
        } else {
          messageToConsole(aMessageWrapper.getMessage());
        }
    }
  }

  private void processMessage() {
    while (!newMessageQuoe.isEmpty()) {
      printMessage(newMessageQuoe.poll());
    }
  }

}
