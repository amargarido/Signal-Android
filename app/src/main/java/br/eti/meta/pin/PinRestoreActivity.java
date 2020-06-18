package br.eti.meta.pin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import br.eti.meta.MainActivity;
import br.eti.meta.PassphraseRequiredActionBarActivity;
import br.eti.meta.R;
import br.eti.meta.lock.v2.CreateKbsPinActivity;

public final class PinRestoreActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.pin_restore_activity);
  }

  void navigateToPinCreation() {
    final Intent main      = new Intent(this, MainActivity.class);
    final Intent createPin = CreateKbsPinActivity.getIntentForPinCreate(this);
    final Intent chained   = PassphraseRequiredActionBarActivity.chainIntent(createPin, main);

    startActivity(chained);
  }
}
