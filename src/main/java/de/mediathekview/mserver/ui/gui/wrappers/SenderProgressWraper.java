package de.mediathekview.mserver.ui.gui.wrappers;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.progress.Progress;

/**
 * A wrapper class to combine sender and progress information.
 * 
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *         <b>Skype:</b> Nicklas2751<br/>
 *
 */
public class SenderProgressWraper {
  private Progress progress;
  private Sender sender;

  public SenderProgressWraper(final Progress aProgress, final Sender aSender) {
    super();
    progress = aProgress;
    sender = aSender;
  }

  public Progress getProgress() {
    return progress;
  }

  public Sender getSender() {
    return sender;
  }

  public void setProgress(final Progress aProgress) {
    progress = aProgress;
  }

  public void setSender(final Sender aSender) {
    sender = aSender;
  }

}
