package br.eti.meta.groups.ui.addmembers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import br.eti.meta.contacts.SelectedContact;
import br.eti.meta.dependencies.ApplicationDependencies;
import br.eti.meta.recipients.RecipientId;
import br.eti.meta.util.concurrent.SignalExecutors;

class AddMembersRepository {

  private final Context context;

  AddMembersRepository() {
    this.context = ApplicationDependencies.getApplication();
  }

  void getOrCreateRecipientId(@NonNull SelectedContact selectedContact, @NonNull Consumer<RecipientId> consumer) {
    SignalExecutors.BOUNDED.execute(() -> consumer.accept(selectedContact.getOrCreateRecipientId(context)));
  }
}
