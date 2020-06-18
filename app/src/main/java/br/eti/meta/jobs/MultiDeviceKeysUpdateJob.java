package br.eti.meta.jobs;


import androidx.annotation.NonNull;

import br.eti.meta.crypto.UnidentifiedAccessUtil;
import br.eti.meta.dependencies.ApplicationDependencies;
import br.eti.meta.jobmanager.Data;
import br.eti.meta.jobmanager.Job;
import br.eti.meta.jobmanager.impl.NetworkConstraint;
import br.eti.meta.keyvalue.SignalStore;
import br.eti.meta.logging.Log;
import br.eti.meta.util.TextSecurePreferences;
import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException;
import org.whispersystems.signalservice.api.storage.StorageKey;
import org.whispersystems.signalservice.api.messages.multidevice.KeysMessage;
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;

import java.io.IOException;

public class MultiDeviceKeysUpdateJob extends BaseJob {

  public static final String KEY = "MultiDeviceKeysUpdateJob";

  private static final String TAG = MultiDeviceKeysUpdateJob.class.getSimpleName();

  public MultiDeviceKeysUpdateJob() {
    this(new Job.Parameters.Builder()
                           .setQueue("MultiDeviceKeysUpdateJob")
                           .setMaxInstances(2)
                           .addConstraint(NetworkConstraint.KEY)
                           .setMaxAttempts(10)
                           .build());

  }

  private MultiDeviceKeysUpdateJob(@NonNull Job.Parameters parameters) {
    super(parameters);
  }

  @Override
  public @NonNull
  Data serialize() {
    return Data.EMPTY;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onRun() throws IOException, UntrustedIdentityException {
    if (!TextSecurePreferences.isMultiDevice(context)) {
      Log.i(TAG, "Not multi device, aborting...");
      return;
    }

    SignalServiceMessageSender messageSender     = ApplicationDependencies.getSignalServiceMessageSender();
    StorageKey                 storageServiceKey = SignalStore.storageServiceValues().getOrCreateStorageKey();

    messageSender.sendMessage(SignalServiceSyncMessage.forKeys(new KeysMessage(Optional.fromNullable(storageServiceKey))),
                              UnidentifiedAccessUtil.getAccessForSync(context));
  }

  @Override
  public boolean onShouldRetry(@NonNull Exception e) {
    return e instanceof PushNetworkException;
  }

  @Override
  public void onFailure() {
  }

  public static final class Factory implements Job.Factory<MultiDeviceKeysUpdateJob> {
    @Override
    public @NonNull MultiDeviceKeysUpdateJob create(@NonNull Job.Parameters parameters, @NonNull Data data) {
      return new MultiDeviceKeysUpdateJob(parameters);
    }
  }
}
