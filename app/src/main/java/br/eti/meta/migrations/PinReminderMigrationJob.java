package br.eti.meta.migrations;

import androidx.annotation.NonNull;

import br.eti.meta.jobmanager.Data;
import br.eti.meta.jobmanager.Job;
import br.eti.meta.keyvalue.SignalStore;

import java.util.concurrent.TimeUnit;

public class PinReminderMigrationJob extends MigrationJob {

  public static final String KEY = "PinReminderMigrationJob";

  PinReminderMigrationJob() {
    this(new Job.Parameters.Builder().build());
  }

  private PinReminderMigrationJob(@NonNull Parameters parameters) {
    super(parameters);
  }

  @Override
  boolean isUiBlocking() {
    return false;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  void performMigration() {
    SignalStore.pinValues().setNextReminderIntervalToAtMost(TimeUnit.DAYS.toMillis(3));
  }

  @Override
  boolean shouldRetry(@NonNull Exception e) {
    return false;
  }

  public static class Factory implements Job.Factory<PinReminderMigrationJob> {

    @Override
    public @NonNull PinReminderMigrationJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new PinReminderMigrationJob(parameters);
    }
  }
}