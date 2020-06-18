package org.eti.meta.registration;

import org.eti.meta.dependencies.ApplicationDependencies;
import org.eti.meta.jobs.DirectoryRefreshJob;
import org.eti.meta.jobs.StorageSyncJob;
import org.eti.meta.keyvalue.SignalStore;
import org.eti.meta.logging.Log;
import org.eti.meta.recipients.Recipient;
import org.whispersystems.signalservice.internal.storage.protos.SignalStorage;

public final class RegistrationUtil {

  private static final String TAG = Log.tag(RegistrationUtil.class);

  private RegistrationUtil() {}

  /**
   * There's several events where a registration may or may not be considered complete based on what
   * path a user has taken. This will only truly mark registration as complete if all of the
   * requirements are met.
   */
  public static void markRegistrationPossiblyComplete() {
    if (!SignalStore.registrationValues().isRegistrationComplete() && SignalStore.kbsValues().hasPin() && !Recipient.self().getProfileName().isEmpty()) {
      Log.i(TAG, "Marking registration completed.", new Throwable());
      SignalStore.registrationValues().setRegistrationComplete();
      ApplicationDependencies.getJobManager().startChain(new StorageSyncJob())
                                             .then(new DirectoryRefreshJob(false))
                                             .enqueue();
    } else if (!SignalStore.registrationValues().isRegistrationComplete()) {
      Log.i(TAG, "Registration is not yet complete.", new Throwable());
    }
  }
}
