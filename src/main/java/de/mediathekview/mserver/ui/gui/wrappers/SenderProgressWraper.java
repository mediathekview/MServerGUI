package de.mediathekview.mserver.ui.gui.wrappers;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.progress.Progress;

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
