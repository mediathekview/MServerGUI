package de.mediathekview.mserver.ui.gui.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;

import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.MessageUtil;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.ui.gui.wrappers.MessageWrapper;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class MessageTask extends Task<MessageWrapper> implements MessageListener
{

    private boolean shouldRun;
    private final ConcurrentLinkedQueue<MessageWrapper> messageQue;

    public MessageTask()
    {
        messageQue = new ConcurrentLinkedQueue<>();
        shouldRun = true;
    }

    @Override
    protected MessageWrapper call() throws Exception
    {
        while (shouldRun)
        {
            // This should never end
        }
        return null;
    }

    @Override
    public void consumeMessage(final Message aMessage, final Object... aParameter)
    {
        messageQue.offer(
                new MessageWrapper(String.format(MessageUtil.getInstance().loadMessageText(aMessage), aParameter),
                        aMessage.getMessageType()));

        Platform.runLater(() -> updateValue(messageQue.poll()));
    }

    public void stop()
    {
        shouldRun = false;

    }

}
