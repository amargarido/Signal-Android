package br.eti.meta.components.webrtc;

import android.media.AudioManager;

import br.eti.meta.dependencies.ApplicationDependencies;
import br.eti.meta.util.ServiceUtil;

class WebRtcCallRepository {

  private final AudioManager audioManager;

  WebRtcCallRepository() {
    this.audioManager = ServiceUtil.getAudioManager(ApplicationDependencies.getApplication());
  }

  WebRtcAudioOutput getAudioOutput() {
    if (audioManager.isBluetoothScoOn()) {
      return WebRtcAudioOutput.HEADSET;
    } else if (audioManager.isSpeakerphoneOn()) {
      return WebRtcAudioOutput.SPEAKER;
    } else {
      return WebRtcAudioOutput.HANDSET;
    }
  }
}
