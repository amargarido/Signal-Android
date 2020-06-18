package org.eti.meta.jobs;

import android.content.Context;
import androidx.annotation.NonNull;

import org.eti.meta.crypto.IdentityKeyUtil;
import org.eti.meta.crypto.PreKeyUtil;
import org.eti.meta.dependencies.ApplicationDependencies;
import org.eti.meta.jobmanager.Data;
import org.eti.meta.jobmanager.Job;
import org.eti.meta.jobmanager.impl.NetworkConstraint;
import org.eti.meta.logging.Log;
import org.eti.meta.util.TextSecurePreferences;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;

import java.io.IOException;

public class CreateSignedPreKeyJob extends BaseJob {

  public static final String KEY = "CreateSignedPreKeyJob";

  private static final String TAG = CreateSignedPreKeyJob.class.getSimpleName();

  public CreateSignedPreKeyJob(Context context) {
    this(new Job.Parameters.Builder()
                           .addConstraint(NetworkConstraint.KEY)
                           .setQueue("CreateSignedPreKeyJob")
                           .setMaxAttempts(25)
                           .build());
  }

  private CreateSignedPreKeyJob(@NonNull Job.Parameters parameters) {
    super(parameters);
  }

  @Override
  public @NonNull Data serialize() {
    return Data.EMPTY;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onRun() throws IOException {
    if (TextSecurePreferences.isSignedPreKeyRegistered(context)) {
      Log.w(TAG, "Signed prekey already registered...");
      return;
    }

    if (!TextSecurePreferences.isPushRegistered(context)) {
      Log.w(TAG, "Not yet registered...");
      return;
    }

    SignalServiceAccountManager accountManager     = ApplicationDependencies.getSignalServiceAccountManager();
    IdentityKeyPair             identityKeyPair    = IdentityKeyUtil.getIdentityKeyPair(context);
    SignedPreKeyRecord          signedPreKeyRecord = PreKeyUtil.generateSignedPreKey(context, identityKeyPair, true);

    accountManager.setSignedPreKey(signedPreKeyRecord);
    TextSecurePreferences.setSignedPreKeyRegistered(context, true);
  }

  @Override
  public void onFailure() {}

  @Override
  public boolean onShouldRetry(@NonNull Exception exception) {
    if (exception instanceof PushNetworkException) return true;
    return false;
  }

  public static final class Factory implements Job.Factory<CreateSignedPreKeyJob> {
    @Override
    public @NonNull CreateSignedPreKeyJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new CreateSignedPreKeyJob(parameters);
    }
  }
}
