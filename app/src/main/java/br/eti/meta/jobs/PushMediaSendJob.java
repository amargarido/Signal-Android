package br.eti.meta.jobs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.annimon.stream.Stream;

import br.eti.meta.ApplicationContext;
import br.eti.meta.attachments.Attachment;
import br.eti.meta.crypto.UnidentifiedAccessUtil;
import br.eti.meta.database.DatabaseFactory;
import br.eti.meta.database.MmsDatabase;
import br.eti.meta.database.NoSuchMessageException;
import br.eti.meta.dependencies.ApplicationDependencies;
import br.eti.meta.jobmanager.Data;
import br.eti.meta.jobmanager.Job;
import br.eti.meta.jobmanager.JobManager;
import br.eti.meta.logging.Log;
import br.eti.meta.mms.MmsException;
import br.eti.meta.mms.OutgoingMediaMessage;
import br.eti.meta.recipients.Recipient;
import br.eti.meta.recipients.RecipientUtil;
import br.eti.meta.service.ExpiringMessageManager;
import br.eti.meta.transport.InsecureFallbackApprovalException;
import br.eti.meta.transport.RetryLaterException;
import br.eti.meta.transport.UndeliverableMessageException;
import br.eti.meta.util.TextSecurePreferences;
import br.eti.meta.util.Util;
import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.crypto.UnidentifiedAccessPair;
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachment;
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage.Preview;
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage;
import org.whispersystems.signalservice.api.messages.shared.SharedContact;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;
import org.whispersystems.signalservice.api.push.exceptions.UnregisteredUserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import br.eti.meta.database.MessagingDatabase;
import br.eti.meta.database.RecipientDatabase;

public class PushMediaSendJob extends PushSendJob {

  public static final String KEY = "PushMediaSendJob";

  private static final String TAG = PushMediaSendJob.class.getSimpleName();

  private static final String KEY_MESSAGE_ID = "message_id";

  private long messageId;

  public PushMediaSendJob(long messageId, @NonNull Recipient recipient) {
    this(constructParameters(recipient, true), messageId);
  }

  private PushMediaSendJob(Job.Parameters parameters, long messageId) {
    super(parameters);
    this.messageId = messageId;
  }

  @WorkerThread
  public static void enqueue(@NonNull Context context, @NonNull JobManager jobManager, long messageId, @NonNull Recipient recipient) {
    try {
      if (!recipient.hasServiceIdentifier()) {
        throw new AssertionError();
      }

      MmsDatabase database            = DatabaseFactory.getMmsDatabase(context);
      OutgoingMediaMessage message             = database.getOutgoingMessage(messageId);
      Set<String>          attachmentUploadIds = enqueueCompressingAndUploadAttachmentsChains(jobManager, message);

      jobManager.add(new PushMediaSendJob(messageId, recipient), attachmentUploadIds, recipient.getId().toQueueKey());

    } catch (NoSuchMessageException | MmsException e) {
      Log.w(TAG, "Failed to enqueue message.", e);
      DatabaseFactory.getMmsDatabase(context).markAsSentFailed(messageId);
      notifyMediaMessageDeliveryFailed(context, messageId);
    }
  }

  @Override
  public @NonNull
  Data serialize() {
    return new Data.Builder().putLong(KEY_MESSAGE_ID, messageId).build();
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onAdded() {
    DatabaseFactory.getMmsDatabase(context).markAsSending(messageId);
  }

  @Override
  public void onPushSend()
      throws RetryLaterException, MmsException, NoSuchMessageException,
          UndeliverableMessageException
  {
    ExpiringMessageManager expirationManager = ApplicationContext.getInstance(context).getExpiringMessageManager();
    MmsDatabase            database          = DatabaseFactory.getMmsDatabase(context);
    OutgoingMediaMessage   message           = database.getOutgoingMessage(messageId);

    if (database.isSent(messageId)) {
      warn(TAG, "Message " + messageId + " was already sent. Ignoring.");
      return;
    }

    try {
      log(TAG, "Sending message: " + messageId);

      RecipientUtil.shareProfileIfFirstSecureMessage(context, message.getRecipient());

      Recipient              recipient  = message.getRecipient().fresh();
      byte[]                 profileKey = recipient.getProfileKey();
      RecipientDatabase.UnidentifiedAccessMode accessMode = recipient.getUnidentifiedAccessMode();

      boolean unidentified = deliver(message);

      database.markAsSent(messageId, true);
      markAttachmentsUploaded(messageId, message.getAttachments());
      database.markUnidentified(messageId, unidentified);

      if (recipient.isLocalNumber()) {
        MessagingDatabase.SyncMessageId id = new MessagingDatabase.SyncMessageId(recipient.getId(), message.getSentTimeMillis());
        DatabaseFactory.getMmsSmsDatabase(context).incrementDeliveryReceiptCount(id, System.currentTimeMillis());
        DatabaseFactory.getMmsSmsDatabase(context).incrementReadReceiptCount(id, System.currentTimeMillis());
      }

      if (unidentified && accessMode == RecipientDatabase.UnidentifiedAccessMode.UNKNOWN && profileKey == null) {
        log(TAG, "Marking recipient as UD-unrestricted following a UD send.");
        DatabaseFactory.getRecipientDatabase(context).setUnidentifiedAccessMode(recipient.getId(), RecipientDatabase.UnidentifiedAccessMode.UNRESTRICTED);
      } else if (unidentified && accessMode == RecipientDatabase.UnidentifiedAccessMode.UNKNOWN) {
        log(TAG, "Marking recipient as UD-enabled following a UD send.");
        DatabaseFactory.getRecipientDatabase(context).setUnidentifiedAccessMode(recipient.getId(), RecipientDatabase.UnidentifiedAccessMode.ENABLED);
      } else if (!unidentified && accessMode != RecipientDatabase.UnidentifiedAccessMode.DISABLED) {
        log(TAG, "Marking recipient as UD-disabled following a non-UD send.");
        DatabaseFactory.getRecipientDatabase(context).setUnidentifiedAccessMode(recipient.getId(), RecipientDatabase.UnidentifiedAccessMode.DISABLED);
      }

      if (message.getExpiresIn() > 0 && !message.isExpirationUpdate()) {
        database.markExpireStarted(messageId);
        expirationManager.scheduleDeletion(messageId, true, message.getExpiresIn());
      }

      if (message.isViewOnce()) {
        DatabaseFactory.getAttachmentDatabase(context).deleteAttachmentFilesForViewOnceMessage(messageId);
      }

      log(TAG, "Sent message: " + messageId);

    } catch (InsecureFallbackApprovalException ifae) {
      warn(TAG, "Failure", ifae);
      database.markAsPendingInsecureSmsFallback(messageId);
      notifyMediaMessageDeliveryFailed(context, messageId);
      ApplicationDependencies.getJobManager().add(new DirectoryRefreshJob(false));
    } catch (UntrustedIdentityException uie) {
      warn(TAG, "Failure", uie);
      database.addMismatchedIdentity(messageId, Recipient.external(context, uie.getIdentifier()).getId(), uie.getIdentityKey());
      database.markAsSentFailed(messageId);
    }
  }

  @Override
  public boolean onShouldRetry(@NonNull Exception exception) {
    if (exception instanceof RetryLaterException) return true;
    return false;
  }

  @Override
  public void onFailure() {
    DatabaseFactory.getMmsDatabase(context).markAsSentFailed(messageId);
    notifyMediaMessageDeliveryFailed(context, messageId);
  }

  private boolean deliver(OutgoingMediaMessage message)
      throws RetryLaterException, InsecureFallbackApprovalException, UntrustedIdentityException,
             UndeliverableMessageException
  {
    if (message.getRecipient() == null) {
      throw new UndeliverableMessageException("No destination address.");
    }

    try {
      rotateSenderCertificateIfNecessary();

      Recipient                                  messageRecipient   = message.getRecipient().fresh();
      SignalServiceMessageSender                 messageSender      = ApplicationDependencies.getSignalServiceMessageSender();
      SignalServiceAddress                       address            = getPushAddress(messageRecipient);
      List<Attachment>                           attachments        = Stream.of(message.getAttachments()).filterNot(Attachment::isSticker).toList();
      List<SignalServiceAttachment>              serviceAttachments = getAttachmentPointersFor(attachments);
      Optional<byte[]>                           profileKey         = getProfileKey(messageRecipient);
      Optional<SignalServiceDataMessage.Quote>   quote              = getQuoteFor(message);
      Optional<SignalServiceDataMessage.Sticker> sticker            = getStickerFor(message);
      List<SharedContact>                        sharedContacts     = getSharedContactsFor(message);
      List<Preview>                              previews           = getPreviewsFor(message);
      SignalServiceDataMessage                   mediaMessage       = SignalServiceDataMessage.newBuilder()
                                                                                            .withBody(message.getBody())
                                                                                            .withAttachments(serviceAttachments)
                                                                                            .withTimestamp(message.getSentTimeMillis())
                                                                                            .withExpiration((int)(message.getExpiresIn() / 1000))
                                                                                            .withViewOnce(message.isViewOnce())
                                                                                            .withProfileKey(profileKey.orNull())
                                                                                            .withQuote(quote.orNull())
                                                                                            .withSticker(sticker.orNull())
                                                                                            .withSharedContacts(sharedContacts)
                                                                                            .withPreviews(previews)
                                                                                            .asExpirationUpdate(message.isExpirationUpdate())
                                                                                            .build();

      if (Util.equals(TextSecurePreferences.getLocalUuid(context), address.getUuid().orNull())) {
        Optional<UnidentifiedAccessPair> syncAccess  = UnidentifiedAccessUtil.getAccessForSync(context);
        SignalServiceSyncMessage         syncMessage = buildSelfSendSyncMessage(context, mediaMessage, syncAccess);

        messageSender.sendMessage(syncMessage, syncAccess);
        return syncAccess.isPresent();
      } else {
        return messageSender.sendMessage(address, UnidentifiedAccessUtil.getAccessFor(context, messageRecipient), mediaMessage).getSuccess().isUnidentified();
      }
    } catch (UnregisteredUserException e) {
      warn(TAG, e);
      throw new InsecureFallbackApprovalException(e);
    } catch (FileNotFoundException e) {
      warn(TAG, e);
      throw new UndeliverableMessageException(e);
    } catch (IOException e) {
      warn(TAG, e);
      throw new RetryLaterException(e);
    }
  }

  public static final class Factory implements Job.Factory<PushMediaSendJob> {
    @Override
    public @NonNull PushMediaSendJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new PushMediaSendJob(parameters, data.getLong(KEY_MESSAGE_ID));
    }
  }
}