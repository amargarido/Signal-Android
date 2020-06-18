package br.eti.meta.mms;

import br.eti.meta.attachments.Attachment;
import br.eti.meta.database.ThreadDatabase;
import br.eti.meta.recipients.Recipient;

import java.util.Collections;
import java.util.LinkedList;

public class OutgoingExpirationUpdateMessage extends OutgoingSecureMediaMessage {

  public OutgoingExpirationUpdateMessage(Recipient recipient, long sentTimeMillis, long expiresIn) {
    super(recipient, "", new LinkedList<Attachment>(), sentTimeMillis,
          ThreadDatabase.DistributionTypes.CONVERSATION, expiresIn, false, null, Collections.emptyList(),
          Collections.emptyList());
  }

  @Override
  public boolean isExpirationUpdate() {
    return true;
  }

}