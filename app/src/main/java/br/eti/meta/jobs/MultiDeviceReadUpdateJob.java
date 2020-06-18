package br.eti.meta.jobs;

import androidx.annotation.NonNull;

import com.annimon.stream.Stream;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.eti.meta.dependencies.ApplicationDependencies;
import br.eti.meta.jobmanager.Data;
import br.eti.meta.jobmanager.Job;
import br.eti.meta.jobmanager.impl.NetworkConstraint;
import br.eti.meta.logging.Log;

import br.eti.meta.crypto.UnidentifiedAccessUtil;
import br.eti.meta.recipients.Recipient;
import br.eti.meta.recipients.RecipientId;
import br.eti.meta.recipients.RecipientUtil;
import br.eti.meta.util.JsonUtils;
import br.eti.meta.util.TextSecurePreferences;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException;
import org.whispersystems.signalservice.api.messages.multidevice.ReadMessage;
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import br.eti.meta.database.MessagingDatabase;

public class MultiDeviceReadUpdateJob extends BaseJob {

  public static final String KEY = "MultiDeviceReadUpdateJob";

  private static final String TAG = MultiDeviceReadUpdateJob.class.getSimpleName();

  private static final String KEY_MESSAGE_IDS = "message_ids";

  private List<SerializableSyncMessageId> messageIds;

  public MultiDeviceReadUpdateJob(List<MessagingDatabase.SyncMessageId> messageIds) {
    this(new Job.Parameters.Builder()
                           .addConstraint(NetworkConstraint.KEY)
                           .setLifespan(TimeUnit.DAYS.toMillis(1))
                           .setMaxAttempts(Parameters.UNLIMITED)
                           .build(),
         messageIds);
  }

  private MultiDeviceReadUpdateJob(@NonNull Job.Parameters parameters, @NonNull List<MessagingDatabase.SyncMessageId> messageIds) {
    super(parameters);

    this.messageIds = new LinkedList<>();

    for (MessagingDatabase.SyncMessageId messageId : messageIds) {
      this.messageIds.add(new SerializableSyncMessageId(messageId.getRecipientId().serialize(), messageId.getTimetamp()));
    }
  }

  @Override
  public @NonNull
  Data serialize() {
    String[] ids = new String[messageIds.size()];

    for (int i = 0; i < ids.length; i++) {
      try {
        ids[i] = JsonUtils.toJson(messageIds.get(i));
      } catch (IOException e) {
        throw new AssertionError(e);
      }
    }

    return new Data.Builder().putStringArray(KEY_MESSAGE_IDS, ids).build();
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onRun() throws IOException, UntrustedIdentityException {
    if (!TextSecurePreferences.isMultiDevice(context)) {
      Log.i(TAG, "Not multi device...");
      return;
    }

    List<ReadMessage> readMessages = new LinkedList<>();

    for (SerializableSyncMessageId messageId : messageIds) {
      Recipient recipient = Recipient.resolved(RecipientId.from(messageId.recipientId));
      readMessages.add(new ReadMessage(RecipientUtil.toSignalServiceAddress(context, recipient), messageId.timestamp));
    }

    SignalServiceMessageSender messageSender = ApplicationDependencies.getSignalServiceMessageSender();
    messageSender.sendMessage(SignalServiceSyncMessage.forRead(readMessages), UnidentifiedAccessUtil.getAccessForSync(context));
  }

  @Override
  public boolean onShouldRetry(@NonNull Exception exception) {
    return exception instanceof PushNetworkException;
  }

  @Override
  public void onFailure() {

  }

  private static class SerializableSyncMessageId implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private final String recipientId;

    @JsonProperty
    private final long   timestamp;

    private SerializableSyncMessageId(@JsonProperty("recipientId") String recipientId, @JsonProperty("timestamp") long timestamp) {
      this.recipientId = recipientId;
      this.timestamp   = timestamp;
    }
  }

  public static final class Factory implements Job.Factory<MultiDeviceReadUpdateJob> {
    @Override
    public @NonNull MultiDeviceReadUpdateJob create(@NonNull Parameters parameters, @NonNull Data data) {
      List<MessagingDatabase.SyncMessageId> ids = Stream.of(data.getStringArray(KEY_MESSAGE_IDS))
                                      .map(id -> {
                                        try {
                                          return JsonUtils.fromJson(id, SerializableSyncMessageId.class);
                                        } catch (IOException e) {
                                          throw new AssertionError(e);
                                        }
                                      })
                                      .map(id -> new MessagingDatabase.SyncMessageId(RecipientId.from(id.recipientId), id.timestamp))
                                      .toList();

      return new MultiDeviceReadUpdateJob(parameters, ids);
    }
  }
}
