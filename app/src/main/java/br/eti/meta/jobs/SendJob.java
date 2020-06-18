package br.eti.meta.jobs;

import androidx.annotation.NonNull;

import br.eti.meta.BuildConfig;
import br.eti.meta.TextSecureExpiredException;
import br.eti.meta.attachments.Attachment;
import br.eti.meta.database.AttachmentDatabase;
import br.eti.meta.database.DatabaseFactory;
import br.eti.meta.jobmanager.Job;
import br.eti.meta.logging.Log;
import br.eti.meta.util.Util;

import java.util.List;

public abstract class SendJob extends BaseJob {

  @SuppressWarnings("unused")
  private final static String TAG = SendJob.class.getSimpleName();

  public SendJob(Job.Parameters parameters) {
    super(parameters);
  }

  @Override
  public final void onRun() throws Exception {
    if (Util.getDaysTillBuildExpiry() <= 0) {
      throw new TextSecureExpiredException(String.format("TextSecure expired (build %d, now %d)",
                                                         BuildConfig.BUILD_TIMESTAMP,
                                                         System.currentTimeMillis()));
    }

    Log.i(TAG, "Starting message send attempt");
    onSend();
    Log.i(TAG, "Message send completed");
  }

  protected abstract void onSend() throws Exception;

  protected void markAttachmentsUploaded(long messageId, @NonNull List<Attachment> attachments) {
    AttachmentDatabase database = DatabaseFactory.getAttachmentDatabase(context);

    for (Attachment attachment : attachments) {
      database.markAttachmentUploaded(messageId, attachment);
    }
  }
}
