package br.eti.meta.jobs;

import androidx.annotation.NonNull;

import br.eti.meta.crypto.UnidentifiedAccessUtil;
import br.eti.meta.database.DatabaseFactory;
import br.eti.meta.database.GroupDatabase;
import br.eti.meta.dependencies.ApplicationDependencies;
import br.eti.meta.jobmanager.Data;
import br.eti.meta.jobmanager.Job;
import br.eti.meta.jobmanager.impl.NetworkConstraint;
import br.eti.meta.logging.Log;
import br.eti.meta.profiles.AvatarHelper;
import br.eti.meta.recipients.Recipient;
import br.eti.meta.recipients.RecipientId;
import br.eti.meta.recipients.RecipientUtil;
import br.eti.meta.util.TextSecurePreferences;
import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachment;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachmentStream;
import org.whispersystems.signalservice.api.messages.multidevice.DeviceGroup;
import org.whispersystems.signalservice.api.messages.multidevice.DeviceGroupsOutputStream;
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MultiDeviceGroupUpdateJob extends BaseJob {

  public static final String KEY = "MultiDeviceGroupUpdateJob";

  private static final String TAG = MultiDeviceGroupUpdateJob.class.getSimpleName();

  public MultiDeviceGroupUpdateJob() {
    this(new Parameters.Builder()
                           .addConstraint(NetworkConstraint.KEY)
                           .setQueue("MultiDeviceGroupUpdateJob")
                           .setLifespan(TimeUnit.DAYS.toMillis(1))
                           .setMaxAttempts(Parameters.UNLIMITED)
                           .build());
  }

  private MultiDeviceGroupUpdateJob(@NonNull Parameters parameters) {
    super(parameters);
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public @NonNull
  Data serialize() {
    return Data.EMPTY;
  }

  @Override
  public void onRun() throws Exception {
    if (!TextSecurePreferences.isMultiDevice(context)) {
      Log.i(TAG, "Not multi device, aborting...");
      return;
    }

    File                 contactDataFile = createTempFile("multidevice-contact-update");
    GroupDatabase.Reader reader          = null;

    GroupDatabase.GroupRecord record;

    try {
      DeviceGroupsOutputStream out = new DeviceGroupsOutputStream(new FileOutputStream(contactDataFile));

      reader = DatabaseFactory.getGroupDatabase(context).getGroups();

      while ((record = reader.getNext()) != null) {
        if (!record.isMms()) {
          List<SignalServiceAddress> members = new LinkedList<>();

          for (RecipientId member : record.getMembers()) {
            members.add(RecipientUtil.toSignalServiceAddress(context, Recipient.resolved(member)));
          }

          RecipientId               recipientId     = DatabaseFactory.getRecipientDatabase(context).getOrInsertFromGroupId(record.getId());
          Recipient                 recipient       = Recipient.resolved(recipientId);
          Optional<Integer>         expirationTimer = recipient.getExpireMessages() > 0 ? Optional.of(recipient.getExpireMessages()) : Optional.absent();
          Map<RecipientId, Integer> inboxPositions  = DatabaseFactory.getThreadDatabase(context).getInboxPositions();
          Set<RecipientId>          archived        = DatabaseFactory.getThreadDatabase(context).getArchivedRecipients();

          out.write(new DeviceGroup(record.getId().getDecodedId(),
                                    Optional.fromNullable(record.getTitle()),
                                    members,
                                    getAvatar(record.getRecipientId()),
                                    record.isActive(),
                                    expirationTimer,
                                    Optional.of(recipient.getColor().serialize()),
                                    recipient.isBlocked(),
                                    Optional.fromNullable(inboxPositions.get(recipientId)),
                                    archived.contains(recipientId)));
        }
      }

      out.close();

      if (contactDataFile.exists() && contactDataFile.length() > 0) {
        sendUpdate(ApplicationDependencies.getSignalServiceMessageSender(), contactDataFile);
      } else {
        Log.w(TAG, "No groups present for sync message...");
      }

    } finally {
      if (contactDataFile != null) contactDataFile.delete();
      if (reader != null)          reader.close();
    }

  }

  @Override
  public boolean onShouldRetry(@NonNull Exception exception) {
    if (exception instanceof PushNetworkException) return true;
    return false;
  }

  @Override
  public void onFailure() {

  }

  private void sendUpdate(SignalServiceMessageSender messageSender, File contactsFile)
      throws IOException, UntrustedIdentityException
  {
    FileInputStream               contactsFileStream = new FileInputStream(contactsFile);
    SignalServiceAttachmentStream attachmentStream   = SignalServiceAttachment.newStreamBuilder()
                                                                              .withStream(contactsFileStream)
                                                                              .withContentType("application/octet-stream")
                                                                              .withLength(contactsFile.length())
                                                                              .build();

    messageSender.sendMessage(SignalServiceSyncMessage.forGroups(attachmentStream),
                              UnidentifiedAccessUtil.getAccessForSync(context));
  }


  private Optional<SignalServiceAttachmentStream> getAvatar(@NonNull RecipientId recipientId) throws IOException {
    if (!AvatarHelper.hasAvatar(context, recipientId)) return Optional.absent();

    return Optional.of(SignalServiceAttachment.newStreamBuilder()
                                              .withStream(AvatarHelper.getAvatar(context, recipientId))
                                              .withContentType("image/*")
                                              .withLength(AvatarHelper.getAvatarLength(context, recipientId))
                                              .build());
  }

  private File createTempFile(String prefix) throws IOException {
    File file = File.createTempFile(prefix, "tmp", context.getCacheDir());
    file.deleteOnExit();

    return file;
  }

  public static final class Factory implements Job.Factory<MultiDeviceGroupUpdateJob> {
    @Override
    public @NonNull MultiDeviceGroupUpdateJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new MultiDeviceGroupUpdateJob(parameters);
    }
  }
}
