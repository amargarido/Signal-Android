package org.eti.meta.groups.ui.addtogroup;

import android.content.Context;

import androidx.annotation.NonNull;

import org.eti.meta.dependencies.ApplicationDependencies;
import org.eti.meta.groups.GroupChangeBusyException;
import org.eti.meta.groups.GroupChangeFailedException;
import org.eti.meta.groups.GroupId;
import org.eti.meta.groups.GroupInsufficientRightsException;
import org.eti.meta.groups.GroupManager;
import org.eti.meta.groups.GroupNotAMemberException;
import org.eti.meta.groups.MembershipNotSuitableForV2Exception;
import org.eti.meta.groups.ui.GroupChangeErrorCallback;
import org.eti.meta.groups.ui.GroupChangeFailureReason;
import org.eti.meta.logging.Log;
import org.eti.meta.recipients.Recipient;
import org.eti.meta.recipients.RecipientId;
import org.eti.meta.util.concurrent.SignalExecutors;

import java.io.IOException;
import java.util.Collections;

final class AddToGroupRepository {

  private static final String TAG = Log.tag(AddToGroupRepository.class);

  private final Context context;

  AddToGroupRepository() {
    this.context = ApplicationDependencies.getApplication();
  }

  public void add(@NonNull RecipientId recipientId,
                  @NonNull Recipient groupRecipient,
                  @NonNull GroupChangeErrorCallback error,
                  @NonNull Runnable success)
  {
    SignalExecutors.UNBOUNDED.execute(() -> {
      try {
        GroupId.Push pushGroupId = groupRecipient.requireGroupId().requirePush();

        GroupManager.addMembers(context, pushGroupId, Collections.singletonList(recipientId));

        success.run();
      } catch (GroupInsufficientRightsException | GroupNotAMemberException e) {
        Log.w(TAG, e);
        error.onError(GroupChangeFailureReason.NO_RIGHTS);
      } catch (GroupChangeFailedException | GroupChangeBusyException | IOException e) {
        Log.w(TAG, e);
        error.onError(GroupChangeFailureReason.OTHER);
      } catch (MembershipNotSuitableForV2Exception e) {
        Log.w(TAG, e);
        error.onError(GroupChangeFailureReason.NOT_CAPABLE);
      }
    });
  }
}
