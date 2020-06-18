package br.eti.meta.lock.v2;

import android.content.Context;

import androidx.annotation.NonNull;

import br.eti.meta.keyvalue.SignalStore;
import br.eti.meta.util.TextSecurePreferences;

public final class RegistrationLockUtil {

  private RegistrationLockUtil() {}

  public static boolean userHasRegistrationLock(@NonNull Context context) {
    return TextSecurePreferences.isV1RegistrationLockEnabled(context) || SignalStore.kbsValues().isV2RegistrationLockEnabled();
  }
}
