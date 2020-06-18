package org.eti.meta.groups.ui;

import androidx.annotation.NonNull;

import org.eti.meta.recipients.Recipient;

public interface RecipientClickListener {
  void onClick(@NonNull Recipient recipient);
}
