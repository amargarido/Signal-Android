package org.eti.meta.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.eti.meta.dependencies.ApplicationDependencies;
import org.eti.meta.jobs.PushNotificationReceiveJob;

public class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    ApplicationDependencies.getJobManager().add(new PushNotificationReceiveJob(context));
  }
}
