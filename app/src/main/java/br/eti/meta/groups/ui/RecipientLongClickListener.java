package br.eti.meta.groups.ui;

import androidx.annotation.NonNull;

import br.eti.meta.recipients.Recipient;

public interface RecipientLongClickListener {
  boolean onLongClick(@NonNull Recipient recipient);
}
