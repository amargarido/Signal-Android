package br.eti.meta.messagerequests;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import br.eti.meta.recipients.Recipient;
import br.eti.meta.recipients.RecipientId;

public class CalleeMustAcceptMessageRequestViewModel extends ViewModel {

  private final LiveData<Recipient> recipient;

  private CalleeMustAcceptMessageRequestViewModel(@NonNull RecipientId recipientId) {
    recipient = Recipient.live(recipientId).getLiveData();
  }

  public LiveData<Recipient> getRecipient() {
    return recipient;
  }

  public static class Factory implements ViewModelProvider.Factory {

    private final RecipientId recipientId;

    public Factory(@NonNull RecipientId recipientId) {
      this.recipientId = recipientId;
    }

    @Override
    public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      //noinspection ConstantConditions
      return modelClass.cast(new CalleeMustAcceptMessageRequestViewModel(recipientId));
    }
  }
}
