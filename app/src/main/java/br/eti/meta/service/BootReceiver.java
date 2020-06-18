package br.eti.meta.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import br.eti.meta.dependencies.ApplicationDependencies;
import br.eti.meta.jobs.PushNotificationReceiveJob;

public class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    ApplicationDependencies.getJobManager().add(new PushNotificationReceiveJob(context));
  }
}
