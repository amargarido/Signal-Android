package br.eti.meta.jobs;

public abstract class PushReceivedJob extends BaseJob {

  private static final String TAG = PushReceivedJob.class.getSimpleName();


  protected PushReceivedJob(Parameters parameters) {
    super(parameters);
  }

}
