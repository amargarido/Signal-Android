package br.eti.meta.recipients.ui.bottomsheet;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import br.eti.meta.contacts.sync.DirectoryHelper;
import br.eti.meta.database.DatabaseFactory;
import br.eti.meta.database.GroupDatabase;
import br.eti.meta.database.IdentityDatabase;
import br.eti.meta.groups.GroupChangeBusyException;
import br.eti.meta.groups.GroupChangeFailedException;
import br.eti.meta.groups.GroupId;
import br.eti.meta.groups.GroupInsufficientRightsException;
import br.eti.meta.groups.GroupManager;
import br.eti.meta.groups.GroupNotAMemberException;
import br.eti.meta.groups.ui.GroupChangeErrorCallback;
import br.eti.meta.groups.ui.GroupChangeFailureReason;
import br.eti.meta.logging.Log;
import br.eti.meta.recipients.Recipient;
import br.eti.meta.recipients.RecipientId;
import br.eti.meta.util.concurrent.SignalExecutors;
import br.eti.meta.util.concurrent.SimpleTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class RecipientDialogRepository {

  private static final String TAG = Log.tag(RecipientDialogRepository.class);

  @NonNull  private final Context     context;
  @NonNull  private final RecipientId recipientId;
  @Nullable private final GroupId     groupId;

  RecipientDialogRepository(@NonNull Context context,
                            @NonNull RecipientId recipientId,
                            @Nullable GroupId groupId)
  {
    this.context     = context;
    this.recipientId = recipientId;
    this.groupId     = groupId;
  }

  @NonNull RecipientId getRecipientId() {
    return recipientId;
  }

  @Nullable GroupId getGroupId() {
    return groupId;
  }

  void getIdentity(@NonNull Consumer<IdentityDatabase.IdentityRecord> callback) {
    SignalExecutors.BOUNDED.execute(
      () -> callback.accept(DatabaseFactory.getIdentityDatabase(context)
                                           .getIdentity(recipientId)
                                           .orNull()));
  }

  void getRecipient(@NonNull RecipientCallback recipientCallback) {
    SimpleTask.run(SignalExecutors.BOUNDED,
                   () -> Recipient.resolved(recipientId),
                   recipientCallback::onRecipient);
  }

  void refreshRecipient() {
    SignalExecutors.UNBOUNDED.execute(() -> {
      try {
        DirectoryHelper.refreshDirectoryFor(context, Recipient.resolved(recipientId), false);
      } catch (IOException e) {
        Log.w(TAG, "Failed to refresh user after adding to contacts.");
      }
    });
  }

  void getGroupName(@NonNull Consumer<String> stringConsumer) {
    SimpleTask.run(SignalExecutors.BOUNDED,
                   () -> DatabaseFactory.getGroupDatabase(context).requireGroup(Objects.requireNonNull(groupId)).getTitle(),
                   stringConsumer::accept);
  }

  void removeMember(@NonNull Consumer<Boolean> onComplete, @NonNull GroupChangeErrorCallback error) {
    SimpleTask.run(SignalExecutors.UNBOUNDED,
                   () -> {
                     try {
                       GroupManager.ejectFromGroup(context, Objects.requireNonNull(groupId).requireV2(), Recipient.resolved(recipientId));
                       return true;
                     } catch (GroupInsufficientRightsException | GroupNotAMemberException e) {
                       Log.w(TAG, e);
                       error.onError(GroupChangeFailureReason.NO_RIGHTS);
                     } catch (GroupChangeFailedException | GroupChangeBusyException | IOException e) {
                       Log.w(TAG, e);
                       error.onError(GroupChangeFailureReason.OTHER);
                     }
                     return false;
                   },
                   onComplete::accept);
  }

  void setMemberAdmin(boolean admin, @NonNull Consumer<Boolean> onComplete, @NonNull GroupChangeErrorCallback error) {
    SimpleTask.run(SignalExecutors.UNBOUNDED,
                   () -> {
                     try {
                       GroupManager.setMemberAdmin(context, Objects.requireNonNull(groupId).requireV2(), recipientId, admin);
                       return true;
                     } catch (GroupInsufficientRightsException | GroupNotAMemberException e) {
                       Log.w(TAG, e);
                       error.onError(GroupChangeFailureReason.NO_RIGHTS);
                     } catch (GroupChangeFailedException | GroupChangeBusyException | IOException e) {
                       Log.w(TAG, e);
                       error.onError(GroupChangeFailureReason.OTHER);
                     }
                     return false;
                   },
                   onComplete::accept);
  }

  void getGroupMembership(@NonNull Consumer<List<RecipientId>> onComplete) {
    SimpleTask.run(SignalExecutors.UNBOUNDED,
                   () -> {
                     GroupDatabase                   groupDatabase   = DatabaseFactory.getGroupDatabase(context);
                     List<GroupDatabase.GroupRecord> groupRecords    = groupDatabase.getPushGroupsContainingMember(recipientId);
                     ArrayList<RecipientId>          groupRecipients = new ArrayList<>(groupRecords.size());

                     for (GroupDatabase.GroupRecord groupRecord : groupRecords) {
                       groupRecipients.add(groupRecord.getRecipientId());
                     }

                     return groupRecipients;
                   },
                   onComplete::accept);
  }

  interface RecipientCallback {
    void onRecipient(@NonNull Recipient recipient);
  }
}
