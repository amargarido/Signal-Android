package br.eti.meta.ringrtc;

import androidx.annotation.NonNull;

public interface CameraEventListener {
  void onCameraSwitchCompleted(@NonNull CameraState newCameraState);
}
