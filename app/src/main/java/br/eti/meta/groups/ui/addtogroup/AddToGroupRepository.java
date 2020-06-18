package br.eti.meta.groups.ui.addtogroup;

import android.content.Context;

import androidx.annotation.NonNull;

import br.eti.meta.dependencies.ApplicationDependencies;
import br.eti.meta.groups.GroupChangeBusyException;
import br.eti.meta.groups.GroupChangeFailedException;
import br.eti.meta.groups.GroupId;
import br.eti.meta.groups.GroupInsufficientRightsException;
import br.eti.meta.groups.GroupManager;
import br.eti.meta.groups.GroupNotAMemberException;
import br.eti.meta.groups.MembershipNotSuitableForV2Exception;
import br.eti.meta.groups.ui.GroupChangeErrorCallback;
import br.eti.meta.groups.ui.GroupChangeFailureReason;
import br.eti.meta.logging.Log;
import br.eti.meta.recipients.Recipient;
import br.eti.meta.recipients.RecipientId;
import br.eti.meta.util.concurrent.SignalExecutors;

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
