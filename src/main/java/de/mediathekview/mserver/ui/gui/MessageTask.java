package de.mediathekview.mserver.ui.gui;

import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.MessageUtil;
import de.mediathekview.mlib.messages.listener.MessageListener;
import javafx.concurrent.Task;

public class MessageTask extends Task<MessageWrapper> implements MessageListener
{

    private boolean shouldRun = true;

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
        updateValue(new MessageWrapper(String.format(MessageUtil.getInstance().loadMessageText(aMessage), aParameter),
                aMessage.getMessageType()));
    }

    public boolean isShouldRun()
    {
        return shouldRun;
    }

    public void setShouldRun(final boolean aShouldRun)
    {
        shouldRun = aShouldRun;
    }

}
