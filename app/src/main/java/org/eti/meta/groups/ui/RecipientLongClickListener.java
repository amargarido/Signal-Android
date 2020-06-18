package org.eti.meta.groups.ui;

import androidx.annotation.NonNull;

import org.eti.meta.recipients.Recipient;

public interface RecipientLongClickListener {
  boolean onLongClick(@NonNull Recipient recipient);
}
