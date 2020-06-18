package br.eti.meta.contacts.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import br.eti.meta.logging.Log;
import br.eti.meta.recipients.Recipient;
import br.eti.meta.storage.StorageSyncHelper;
import br.eti.meta.util.FeatureFlags;

import java.io.IOException;

import br.eti.meta.database.RecipientDatabase;

public class DirectoryHelper {

  private static final String TAG = Log.tag(DirectoryHelper.class);

  @WorkerThread
  public static void refreshDirectory(@NonNull Context context, boolean notifyOfNewUsers) throws IOException {
    if (FeatureFlags.uuids()) {
      // TODO [greyson] Create a DirectoryHelperV2 when appropriate.
      DirectoryHelperV1.refreshDirectory(context, notifyOfNewUsers);
    } else {
      DirectoryHelperV1.refreshDirectory(context, notifyOfNewUsers);
    }

    StorageSyncHelper.scheduleSyncForDataChange();
  }

  @WorkerThread
  public static RecipientDatabase.RegisteredState refreshDirectoryFor(@NonNull Context context, @NonNull Recipient recipient, boolean notifyOfNewUsers) throws IOException {
    RecipientDatabase.RegisteredState originalRegisteredState = recipient.resolve().getRegistered();
    RecipientDatabase.RegisteredState newRegisteredState      = null;

    if (FeatureFlags.uuids()) {
      // TODO [greyson] Create a DirectoryHelperV2 when appropriate.
      newRegisteredState = DirectoryHelperV1.refreshDirectoryFor(context, recipient, notifyOfNewUsers);
    } else {
      newRegisteredState = DirectoryHelperV1.refreshDirectoryFor(context, recipient, notifyOfNewUsers);
    }

    if (newRegisteredState != originalRegisteredState) {
      StorageSyncHelper.scheduleSyncForDataChange();
    }

    return newRegisteredState;
  }
}
