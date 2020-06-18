package br.eti.meta.jobs;

import androidx.annotation.NonNull;

import br.eti.meta.dependencies.ApplicationDependencies;
import br.eti.meta.jobmanager.Data;
import br.eti.meta.jobmanager.Job;
import br.eti.meta.jobmanager.impl.NetworkConstraint;
import br.eti.meta.logging.Log;
import br.eti.meta.util.FeatureFlags;
import org.whispersystems.signalservice.internal.push.http.ResumableUploadSpec;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ResumableUploadSpecJob extends BaseJob {

  private static final String TAG = Log.tag(ResumableUploadSpecJob.class);

  static final String KEY_RESUME_SPEC = "resume_spec";

  public static final String KEY = "ResumableUploadSpecJob";

  public ResumableUploadSpecJob() {
    this(new Job.Parameters.Builder()
                           .addConstraint(NetworkConstraint.KEY)
                           .setLifespan(TimeUnit.DAYS.toMillis(1))
                           .setMaxAttempts(Parameters.UNLIMITED)
                           .build());
  }

  private ResumableUploadSpecJob(@NonNull Parameters parameters) {
    super(parameters);
  }

  @Override
  protected void onRun() throws Exception {
    if (!FeatureFlags.attachmentsV3()) {
      Log.i(TAG, "Attachments V3 is not enabled so there is nothing to do!");
      return;
    }

    ResumableUploadSpec resumableUploadSpec = ApplicationDependencies.getSignalServiceMessageSender()
                                                                     .getResumableUploadSpec();

    setOutputData(new Data.Builder()
                          .putString(KEY_RESUME_SPEC, resumableUploadSpec.serialize())
                          .build());
  }

  @Override
  protected boolean onShouldRetry(@NonNull Exception e) {
    return e instanceof IOException;
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
  public void onFailure() {

  }

  public static class Factory implements Job.Factory<ResumableUploadSpecJob> {

    @Override
    public @NonNull ResumableUploadSpecJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new ResumableUploadSpecJob(parameters);
    }
  }
}