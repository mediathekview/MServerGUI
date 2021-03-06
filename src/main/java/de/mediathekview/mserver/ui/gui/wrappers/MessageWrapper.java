package de.mediathekview.mserver.ui.gui.wrappers;

import de.mediathekview.mlib.messages.MessageTypes;

/**
 * A wrapper class for the external incoming message information.
 * 
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *         <b>Skype:</b> Nicklas2751<br/>
 *
 */
public class MessageWrapper {
  private String message;
  private MessageTypes type;

  public MessageWrapper(final String aMessage, final MessageTypes aType) {
    super();
    message = aMessage;
    type = aType;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MessageWrapper other = (MessageWrapper) obj;
    if (message == null) {
      if (other.message != null) {
        return false;
      }
    } else if (!message.equals(other.message)) {
      return false;
    }
    if (type != other.type) {
      return false;
    }
    return true;
  }

  public String getMessage() {
    return message;
  }

  public MessageTypes getType() {
    return type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (message == null ? 0 : message.hashCode());
    result = prime * result + (type == null ? 0 : type.hashCode());
    return result;
  }

  public void setMessage(final String aMessage) {
    message = aMessage;
  }

  public void setType(final MessageTypes aType) {
    type = aType;
  }

}
