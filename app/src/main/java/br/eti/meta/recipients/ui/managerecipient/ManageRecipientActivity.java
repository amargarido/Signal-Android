package br.eti.meta.recipients.ui.managerecipient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import br.eti.meta.PassphraseRequiredActionBarActivity;
import br.eti.meta.R;
import br.eti.meta.recipients.RecipientId;
import br.eti.meta.util.DynamicNoActionBarTheme;
import br.eti.meta.util.DynamicTheme;

public class ManageRecipientActivity extends PassphraseRequiredActionBarActivity {

  private static final String RECIPIENT_ID = "RECIPIENT_ID";

  private final DynamicTheme dynamicTheme = new DynamicNoActionBarTheme();

  public static Intent newIntent(@NonNull Context context, @NonNull RecipientId recipientId) {
    Intent intent = new Intent(context, ManageRecipientActivity.class);
    intent.putExtra(RECIPIENT_ID, recipientId);
    return intent;
  }

  public static @Nullable Bundle createTransitionBundle(@NonNull Context activityContext, @NonNull View from) {
    if (activityContext instanceof Activity) {
      return ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) activityContext, from, "avatar").toBundle();
    } else {
      return null;
    }
  }

  @Override
  protected void onPreCreate() {
    dynamicTheme.onCreate(this);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState, boolean ready) {
    super.onCreate(savedInstanceState, ready);
    setContentView(R.layout.recipient_manage_activity);
    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
                                 .replace(R.id.container, ManageRecipientFragment.newInstance(getIntent().getParcelableExtra(RECIPIENT_ID)))
                                 .commitNow();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);
  }
}
