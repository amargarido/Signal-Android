package br.eti.meta.jobs;


import android.Manifest;

import androidx.annotation.NonNull;

import br.eti.meta.R;
import br.eti.meta.backup.BackupPassphrase;
import br.eti.meta.backup.FullBackupExporter;
import br.eti.meta.crypto.AttachmentSecretProvider;
import br.eti.meta.database.DatabaseFactory;
import br.eti.meta.database.NoExternalStorageException;
import br.eti.meta.jobmanager.Data;
import br.eti.meta.jobmanager.Job;
import br.eti.meta.logging.Log;
import br.eti.meta.notifications.NotificationChannels;
import br.eti.meta.permissions.Permissions;
import br.eti.meta.service.GenericForegroundService;
import br.eti.meta.service.NotificationController;
import br.eti.meta.util.BackupUtil;
import br.eti.meta.util.StorageUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocalBackupJob extends BaseJob {

  public static final String KEY = "LocalBackupJob";

  private static final String TAG = LocalBackupJob.class.getSimpleName();

  public LocalBackupJob() {
    this(new Parameters.Builder()
                           .setQueue("__LOCAL_BACKUP__")
                           .setMaxInstances(1)
                           .setMaxAttempts(3)
                           .build());
  }

  private LocalBackupJob(@NonNull Parameters parameters) {
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
  public void onRun() throws NoExternalStorageException, IOException {
    Log.i(TAG, "Executing backup job...");

    if (!Permissions.hasAll(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      throw new IOException("No external storage permission!");
    }

    try (NotificationController notification = GenericForegroundService.startForegroundTask(context,
                                                                     context.getString(R.string.LocalBackupJob_creating_backup),
                                                                     NotificationChannels.BACKUPS,
                                                                     R.drawable.ic_signal_backup))
    {
      notification.setIndeterminateProgress();

      String backupPassword  = BackupPassphrase.get(context);
      File   backupDirectory = StorageUtil.getBackupDirectory();
      String timestamp       = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(new Date());
      String fileName        = String.format("signal-%s.backup", timestamp);
      File   backupFile      = new File(backupDirectory, fileName);

      if (backupFile.exists()) {
        throw new IOException("Backup file already exists?");
      }

      if (backupPassword == null) {
        throw new IOException("Backup password is null");
      }

      File tempFile = File.createTempFile("backup", "tmp", StorageUtil.getBackupCacheDirectory(context));

      try {
        FullBackupExporter.export(context,
                                  AttachmentSecretProvider.getInstance(context).getOrCreateAttachmentSecret(),
                                  DatabaseFactory.getBackupDatabase(context),
                                  tempFile,
                                  backupPassword);

        if (!tempFile.renameTo(backupFile)) {
          Log.w(TAG, "Failed to rename temp file");
          throw new IOException("Renaming temporary backup file failed!");
        }
      } finally {
        if (tempFile.exists()) {
          if (tempFile.delete()) {
            Log.w(TAG, "Backup failed. Deleted temp file");
          } else {
            Log.w(TAG, "Backup failed. Failed to delete temp file " + tempFile);
          }
        }
      }

      BackupUtil.deleteOldBackups();
    }
  }

  @Override
  public boolean onShouldRetry(@NonNull Exception e) {
    return false;
  }

  @Override
  public void onFailure() {
  }

  public static class Factory implements Job.Factory<LocalBackupJob> {
    @Override
    public @NonNull LocalBackupJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new LocalBackupJob(parameters);
    }
  }
}
