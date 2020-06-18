package br.eti.meta.megaphone;

import br.eti.meta.dependencies.ApplicationDependencies;
import br.eti.meta.keyvalue.SignalStore;
import br.eti.meta.util.TextSecurePreferences;

final class SignalPinReminderSchedule implements MegaphoneSchedule {

  @Override
  public boolean shouldDisplay(int seenCount, long lastSeen, long firstVisible, long currentTime) {
    if (!SignalStore.kbsValues().hasPin()) {
      return false;
    }

    if (!SignalStore.pinValues().arePinRemindersEnabled()) {
      return false;
    }

    if (!TextSecurePreferences.isPushRegistered(ApplicationDependencies.getApplication())) {
      return false;
    }

    long lastSuccessTime = SignalStore.pinValues().getLastSuccessfulEntryTime();
    long interval        = SignalStore.pinValues().getCurrentInterval();

    return currentTime - lastSuccessTime >= interval;
  }
}
