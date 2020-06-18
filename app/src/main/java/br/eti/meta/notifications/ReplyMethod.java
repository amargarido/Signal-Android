package br.eti.meta.notifications;

import android.content.Context;
import androidx.annotation.NonNull;

import br.eti.meta.database.RecipientDatabase;
import br.eti.meta.recipients.Recipient;
import br.eti.meta.util.TextSecurePreferences;

public enum ReplyMethod {

  GroupMessage,
  SecureMessage,
  UnsecuredSmsMessage;

  public static @NonNull ReplyMethod forRecipient(Context context, Recipient recipient) {
    if (recipient.isGroup()) {
      return ReplyMethod.GroupMessage;
    } else if (TextSecurePreferences.isPushRegistered(context) && recipient.getRegistered() == RecipientDatabase.RegisteredState.REGISTERED && !recipient.isForceSmsSelection()) {
      return ReplyMethod.SecureMessage;
    } else {
      return ReplyMethod.UnsecuredSmsMessage;
    }
  }
}
