package br.eti.meta.logging;

import androidx.annotation.NonNull;

import br.eti.meta.keyvalue.SignalStore;

public class SignalUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

  private static final String TAG = SignalUncaughtExceptionHandler.class.getSimpleName();

  private final Thread.UncaughtExceptionHandler originalHandler;

  public SignalUncaughtExceptionHandler(@NonNull Thread.UncaughtExceptionHandler originalHandler) {
    this.originalHandler = originalHandler;
  }

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    Log.e(TAG, "", e);
    SignalStore.blockUntilAllWritesFinished();
    Log.blockUntilAllWritesFinished();
    originalHandler.uncaughtException(t, e);
  }
}