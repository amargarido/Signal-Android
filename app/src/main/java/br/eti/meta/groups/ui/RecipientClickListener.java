package br.eti.meta.groups.ui;

import androidx.annotation.NonNull;

import br.eti.meta.recipients.Recipient;

public interface RecipientClickListener {
  void onClick(@NonNull Recipient recipient);
}
