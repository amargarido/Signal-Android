package br.eti.meta.profiles.edit;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Consumer;

import br.eti.meta.database.DatabaseFactory;
import br.eti.meta.groups.GroupChangeBusyException;
import br.eti.meta.groups.GroupChangeFailedException;
import br.eti.meta.groups.GroupId;
import br.eti.meta.groups.GroupInsufficientRightsException;
import br.eti.meta.groups.GroupManager;
import br.eti.meta.groups.GroupNotAMemberException;
import br.eti.meta.logging.Log;
import br.eti.meta.profiles.AvatarHelper;
import br.eti.meta.profiles.ProfileName;
import br.eti.meta.recipients.Recipient;
import br.eti.meta.recipients.RecipientId;
import br.eti.meta.util.Util;
import br.eti.meta.util.concurrent.SimpleTask;
import org.whispersystems.libsignal.util.guava.Optional;

import java.io.IOException;

class EditPushGroupProfileRepository implements EditProfileRepository {

  private static final String TAG = Log.tag(EditPushGroupProfileRepository.class);

  private final Context      context;
  private final GroupId.Push groupId;

  EditPushGroupProfileRepository(@NonNull Context context, @NonNull GroupId.Push groupId) {
    this.context = context.getApplicationContext();
    this.groupId = groupId;
  }

  @Override
  public void getCurrentProfileName(@NonNull Consumer<ProfileName> profileNameConsumer) {
    profileNameConsumer.accept(ProfileName.EMPTY);
  }

  @Override
  public void getCurrentAvatar(@NonNull Consumer<byte[]> avatarConsumer) {
    SimpleTask.run(() -> {
      final RecipientId recipientId = getRecipientId();

      if (AvatarHelper.hasAvatar(context, recipientId)) {
        try {
          return Util.readFully(AvatarHelper.getAvatar(context, recipientId));
        } catch (IOException e) {
          Log.w(TAG, e);
          return null;
        }
      } else {
        return null;
      }
    }, avatarConsumer::accept);
  }

  @Override
  public void getCurrentDisplayName(@NonNull Consumer<String> displayNameConsumer) {
    SimpleTask.run(() -> Recipient.resolved(getRecipientId()).getDisplayName(context), displayNameConsumer::accept);
  }

  @Override
  public void getCurrentName(@NonNull Consumer<String> nameConsumer) {
    SimpleTask.run(() -> Recipient.resolved(getRecipientId()).getName(context), nameConsumer::accept);
  }

  @Override
  public void uploadProfile(@NonNull ProfileName profileName,
                            @NonNull String displayName,
                            boolean displayNameChanged,
                            @Nullable byte[] avatar,
                            boolean avatarChanged,
                            @NonNull Consumer<UploadResult> uploadResultConsumer)
  {
    SimpleTask.run(() -> {
      try {
        GroupManager.updateGroup(context, groupId, avatar, avatarChanged, displayName, displayNameChanged);

        return UploadResult.SUCCESS;
      } catch (GroupChangeFailedException | GroupInsufficientRightsException | IOException | GroupNotAMemberException | GroupChangeBusyException e) {
        return UploadResult.ERROR_IO;
      }

    }, uploadResultConsumer::accept);
  }

  @Override
  public void getCurrentUsername(@NonNull Consumer<Optional<String>> callback) {
    callback.accept(Optional.absent());
  }

  @WorkerThread
  private RecipientId getRecipientId() {
    return DatabaseFactory.getRecipientDatabase(context).getByGroupId(groupId.toString())
                          .or(() -> {
                            throw new AssertionError("Recipient ID for Group ID does not exist.");
                          });
  }
}
