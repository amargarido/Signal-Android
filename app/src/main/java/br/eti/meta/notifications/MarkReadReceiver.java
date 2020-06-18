package br.eti.meta.notifications;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import br.eti.meta.ApplicationContext;
import br.eti.meta.database.DatabaseFactory;
import br.eti.meta.dependencies.ApplicationDependencies;
import br.eti.meta.jobs.MultiDeviceReadUpdateJob;
import br.eti.meta.jobs.SendReadReceiptJob;
import br.eti.meta.logging.Log;
import br.eti.meta.recipients.RecipientId;
import br.eti.meta.service.ExpiringMessageManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import br.eti.meta.database.MessagingDatabase;

public class MarkReadReceiver extends BroadcastReceiver {

  private static final String TAG                   = MarkReadReceiver.class.getSimpleName();
  public static final  String CLEAR_ACTION          = "br.eti.meta.notifications.CLEAR";
  public static final  String THREAD_IDS_EXTRA      = "thread_ids";
  public static final  String NOTIFICATION_ID_EXTRA = "notification_id";

  @SuppressLint("StaticFieldLeak")
  @Override
  public void onReceive(final Context context, Intent intent) {
    if (!CLEAR_ACTION.equals(intent.getAction()))
      return;

    final long[] threadIds = intent.getLongArrayExtra(THREAD_IDS_EXTRA);

    if (threadIds != null) {
      NotificationManagerCompat.from(context).cancel(intent.getIntExtra(NOTIFICATION_ID_EXTRA, -1));

      new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
          List<MessagingDatabase.MarkedMessageInfo> messageIdsCollection = new LinkedList<>();

          for (long threadId : threadIds) {
            Log.i(TAG, "Marking as read: " + threadId);
            List<MessagingDatabase.MarkedMessageInfo> messageIds = DatabaseFactory.getThreadDatabase(context).setRead(threadId, true);
            messageIdsCollection.addAll(messageIds);
          }

          process(context, messageIdsCollection);

          ApplicationDependencies.getMessageNotifier().updateNotification(context);

          return null;
        }
      }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
  }

  public static void process(@NonNull Context context, @NonNull List<MessagingDatabase.MarkedMessageInfo> markedReadMessages) {
    if (markedReadMessages.isEmpty()) return;

    List<MessagingDatabase.SyncMessageId>  syncMessageIds    = Stream.of(markedReadMessages)
                                                   .map(MessagingDatabase.MarkedMessageInfo::getSyncMessageId)
                                                   .toList();
    List<MessagingDatabase.ExpirationInfo> mmsExpirationInfo = Stream.of(markedReadMessages)
                                                   .map(MessagingDatabase.MarkedMessageInfo::getExpirationInfo)
                                                   .filter(MessagingDatabase.ExpirationInfo::isMms)
                                                   .filter(info -> info.getExpiresIn() > 0 && info.getExpireStarted() <= 0)
                                                   .toList();
    List<MessagingDatabase.ExpirationInfo> smsExpirationInfo = Stream.of(markedReadMessages)
                                                   .map(MessagingDatabase.MarkedMessageInfo::getExpirationInfo)
                                                   .filterNot(MessagingDatabase.ExpirationInfo::isMms)
                                                   .filter(info -> info.getExpiresIn() > 0 && info.getExpireStarted() <= 0)
                                                   .toList();

    scheduleDeletion(context, smsExpirationInfo, mmsExpirationInfo);

    ApplicationDependencies.getJobManager().add(new MultiDeviceReadUpdateJob(syncMessageIds));

    Map<Long, List<MessagingDatabase.MarkedMessageInfo>> threadToInfo = Stream.of(markedReadMessages)
                                                            .collect(Collectors.groupingBy(MessagingDatabase.MarkedMessageInfo::getThreadId));

    Stream.of(threadToInfo).forEach(threadToInfoEntry -> {
      Map<RecipientId, List<MessagingDatabase.SyncMessageId>> idMapForThread = Stream.of(threadToInfoEntry.getValue())
                                                                   .map(MessagingDatabase.MarkedMessageInfo::getSyncMessageId)
                                                                   .collect(Collectors.groupingBy(MessagingDatabase.SyncMessageId::getRecipientId));

      Stream.of(idMapForThread).forEach(entry -> {
        List<Long> timestamps = Stream.of(entry.getValue()).map(MessagingDatabase.SyncMessageId::getTimetamp).toList();

        ApplicationDependencies.getJobManager().add(new SendReadReceiptJob(threadToInfoEntry.getKey(), entry.getKey(), timestamps));
      });
    });
  }

  private static void scheduleDeletion(@NonNull Context context,
                                       @NonNull List<MessagingDatabase.ExpirationInfo> smsExpirationInfo,
                                       @NonNull List<MessagingDatabase.ExpirationInfo> mmsExpirationInfo)
  {
    if (smsExpirationInfo.size() > 0) {
      DatabaseFactory.getSmsDatabase(context).markExpireStarted(Stream.of(smsExpirationInfo).map(MessagingDatabase.ExpirationInfo::getId).toList(), System.currentTimeMillis());
    }

    if (mmsExpirationInfo.size() > 0) {
      DatabaseFactory.getMmsDatabase(context).markExpireStarted(Stream.of(mmsExpirationInfo).map(MessagingDatabase.ExpirationInfo::getId).toList(), System.currentTimeMillis());
    }

    if (smsExpirationInfo.size() + mmsExpirationInfo.size() > 0) {
      ExpiringMessageManager expirationManager = ApplicationContext.getInstance(context).getExpiringMessageManager();

      Stream.concat(Stream.of(smsExpirationInfo), Stream.of(mmsExpirationInfo))
            .forEach(info -> expirationManager.scheduleDeletion(info.getId(), info.isMms(), info.getExpiresIn()));
    }
  }
}
