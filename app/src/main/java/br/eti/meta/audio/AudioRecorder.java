package br.eti.meta.audio;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import androidx.annotation.NonNull;
import br.eti.meta.logging.Log;

import br.eti.meta.providers.BlobProvider;
import br.eti.meta.util.MediaUtil;
import br.eti.meta.util.Util;
import br.eti.meta.util.concurrent.ListenableFuture;
import br.eti.meta.util.concurrent.SettableFuture;
import br.eti.meta.util.concurrent.SignalExecutors;
import org.whispersystems.libsignal.util.Pair;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class AudioRecorder {

  private static final String TAG = AudioRecorder.class.getSimpleName();

  private static final ExecutorService executor = SignalExecutors.newCachedSingleThreadExecutor("signal-AudioRecorder");

  private final Context context;

  private AudioCodec audioCodec;
  private Uri        captureUri;

  public AudioRecorder(@NonNull Context context) {
    this.context = context;
  }

  public void startRecording() {
    Log.i(TAG, "startRecording()");

    executor.execute(() -> {
      Log.i(TAG, "Running startRecording() + " + Thread.currentThread().getId());
      try {
        if (audioCodec != null) {
          throw new AssertionError("We can only record once at a time.");
        }

        ParcelFileDescriptor fds[] = ParcelFileDescriptor.createPipe();

        captureUri = BlobProvider.getInstance()
                                 .forData(new ParcelFileDescriptor.AutoCloseInputStream(fds[0]), 0)
                                 .withMimeType(MediaUtil.AUDIO_AAC)
                                 .createForSingleSessionOnDiskAsync(context, () -> Log.i(TAG, "Write successful."), e -> Log.w(TAG, "Error during recording", e));
        audioCodec = new AudioCodec();

        audioCodec.start(new ParcelFileDescriptor.AutoCloseOutputStream(fds[1]));
      } catch (IOException e) {
        Log.w(TAG, e);
      }
    });
  }

  public @NonNull ListenableFuture<Pair<Uri, Long>> stopRecording() {
    Log.i(TAG, "stopRecording()");

    final SettableFuture<Pair<Uri, Long>> future = new SettableFuture<>();

    executor.execute(() -> {
      if (audioCodec == null) {
        sendToFuture(future, new IOException("MediaRecorder was never initialized successfully!"));
        return;
      }

      audioCodec.stop();

      try {
        long size = MediaUtil.getMediaSize(context, captureUri);
        sendToFuture(future, new Pair<>(captureUri, size));
      } catch (IOException ioe) {
        Log.w(TAG, ioe);
        sendToFuture(future, ioe);
      }

      audioCodec = null;
      captureUri = null;
    });

    return future;
  }

  private <T> void sendToFuture(final SettableFuture<T> future, final Exception exception) {
    Util.runOnMain(() -> future.setException(exception));
  }

  private <T> void sendToFuture(final SettableFuture<T> future, final T result) {
    Util.runOnMain(() -> future.set(result));
  }
}