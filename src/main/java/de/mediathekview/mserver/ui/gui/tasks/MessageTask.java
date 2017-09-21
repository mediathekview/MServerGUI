package de.mediathekview.mserver.ui.gui.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.MessageUtil;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.ui.gui.wrappers.MessageWrapper;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * A task which takes and encapsulates external incoming messages.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *         <b>Skype:</b> Nicklas2751<br/>
 *
 */
public class MessageTask extends Task<MessageWrapper> implements MessageListener {

  private boolean shouldRun;
  private final ConcurrentLinkedQueue<MessageWrapper> messageQue;

  public MessageTask() {
    messageQue = new ConcurrentLinkedQueue<>();
    shouldRun = true;
  }

  @Override
  public void consumeMessage(final Message aMessage, final Object... aParameter) {
    messageQue.offer(new MessageWrapper(
        String.format(MessageUtil.getInstance().loadMessageText(aMessage), aParameter),
        aMessage.getMessageType()));

    Platform.runLater(() -> updateValue(messageQue.poll()));
  }

  /**
   * Stops the loop which keeps this task alive.
   */
  public void stop() {
    shouldRun = false;

  }

  @Override
  protected MessageWrapper call() throws Exception {
    while (shouldRun) {
      // This should never end
    }
    return null;
  }

}
