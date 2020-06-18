package br.eti.meta.jobs;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import br.eti.meta.BuildConfig;
import br.eti.meta.events.ReminderUpdateEvent;
import br.eti.meta.jobmanager.Data;
import br.eti.meta.jobmanager.Job;
import br.eti.meta.jobmanager.impl.NetworkConstraint;
import br.eti.meta.logging.Log;
import br.eti.meta.transport.RetryLaterException;
import br.eti.meta.util.TextSecurePreferences;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServiceOutageDetectionJob extends BaseJob {

  public static final String KEY = "ServiceOutageDetectionJob";

  private static final String TAG = ServiceOutageDetectionJob.class.getSimpleName();

  private static final String IP_SUCCESS = "127.0.0.1";
  private static final String IP_FAILURE = "127.0.0.2";
  private static final long   CHECK_TIME = 1000 * 60;

  public ServiceOutageDetectionJob() {
    this(new Parameters.Builder()
                           .setQueue("ServiceOutageDetectionJob")
                           .addConstraint(NetworkConstraint.KEY)
                           .setMaxAttempts(5)
                           .setMaxInstances(1)
                           .build());
  }

  private ServiceOutageDetectionJob(@NonNull Parameters parameters) {
    super(parameters);
  }

  @Override
  public @NonNull
  Data serialize() {
    return Data.EMPTY;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onRun() throws RetryLaterException {
    Log.i(TAG, "onRun()");

    long timeSinceLastCheck = System.currentTimeMillis() - TextSecurePreferences.getLastOutageCheckTime(context);
    if (timeSinceLastCheck < CHECK_TIME) {
      Log.w(TAG, "Skipping service outage check. Too soon.");
      return;
    }

    try {
      InetAddress address = InetAddress.getByName(BuildConfig.SIGNAL_SERVICE_STATUS_URL);
      Log.i(TAG, "Received outage check address: " + address.getHostAddress());

      if (IP_SUCCESS.equals(address.getHostAddress())) {
        Log.i(TAG, "Service is available.");
        TextSecurePreferences.setServiceOutage(context, false);
      } else if (IP_FAILURE.equals(address.getHostAddress())) {
        Log.w(TAG, "Service is down.");
        TextSecurePreferences.setServiceOutage(context, true);
      } else {
        Log.w(TAG, "Service status check returned an unrecognized IP address. Could be a weird network state. Prompting retry.");
        throw new RetryLaterException(new Exception("Unrecognized service outage IP address."));
      }

      TextSecurePreferences.setLastOutageCheckTime(context, System.currentTimeMillis());
      EventBus.getDefault().post(new ReminderUpdateEvent());
    } catch (UnknownHostException e) {
      throw new RetryLaterException(e);
    }
  }

  @Override
  public boolean onShouldRetry(@NonNull Exception e) {
    return e instanceof RetryLaterException;
  }

  @Override
  public void onFailure() {
    Log.i(TAG, "Service status check could not complete. Assuming success to avoid false positives due to bad network.");
    TextSecurePreferences.setServiceOutage(context, false);
    TextSecurePreferences.setLastOutageCheckTime(context, System.currentTimeMillis());
    EventBus.getDefault().post(new ReminderUpdateEvent());
  }

  public static final class Factory implements Job.Factory<ServiceOutageDetectionJob> {
    @Override
    public @NonNull ServiceOutageDetectionJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new ServiceOutageDetectionJob(parameters);
    }
  }
}