package br.eti.meta.groups.ui.creategroup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.annimon.stream.Stream;

import br.eti.meta.ContactSelectionActivity;
import br.eti.meta.ContactSelectionListFragment;
import br.eti.meta.R;
import br.eti.meta.contacts.ContactsCursorLoader;
import br.eti.meta.groups.ui.creategroup.details.AddGroupDetailsActivity;
import br.eti.meta.recipients.RecipientId;
import br.eti.meta.util.FeatureFlags;
import br.eti.meta.util.TextSecurePreferences;
import org.whispersystems.libsignal.util.guava.Optional;

public class CreateGroupActivity extends ContactSelectionActivity {

  private static final int   MINIMUM_GROUP_SIZE       = 1;
  private static final short REQUEST_CODE_ADD_DETAILS = 17275;

  private View next;

  public static Intent newIntent(@NonNull Context context) {
    Intent intent = new Intent(context, CreateGroupActivity.class);

    intent.putExtra(ContactSelectionListFragment.MULTI_SELECT, true);
    intent.putExtra(ContactSelectionListFragment.REFRESHABLE, false);
    intent.putExtra(ContactSelectionActivity.EXTRA_LAYOUT_RES_ID, R.layout.create_group_activity);

    int displayMode = TextSecurePreferences.isSmsEnabled(context) ? ContactsCursorLoader.DisplayMode.FLAG_SMS | ContactsCursorLoader.DisplayMode.FLAG_PUSH
                                                                  : ContactsCursorLoader.DisplayMode.FLAG_PUSH;

    intent.putExtra(ContactSelectionListFragment.DISPLAY_MODE, displayMode);
    intent.putExtra(ContactSelectionListFragment.TOTAL_CAPACITY, FeatureFlags.groupsV2create() ? FeatureFlags.gv2GroupCapacity() - 1
                                                                                               : ContactSelectionListFragment.NO_LIMIT);

    return intent;
  }

  @Override
  public void onCreate(Bundle bundle, boolean ready) {
    super.onCreate(bundle, ready);
    assert getSupportActionBar() != null;
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    next = findViewById(R.id.next);

    disableNext();
    next.setOnClickListener(v -> handleNextPressed());
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (requestCode == REQUEST_CODE_ADD_DETAILS && resultCode == RESULT_OK) {
      finish();
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override
  public void onContactSelected(Optional<RecipientId> recipientId, String number) {
    if (contactsFragment.hasQueryFilter()) {
      getToolbar().clear();
    }

    if (contactsFragment.getSelectedContactsCount() >= MINIMUM_GROUP_SIZE) {
      enableNext();
    }
  }

  @Override
  public void onContactDeselected(Optional<RecipientId> recipientId, String number) {
    if (contactsFragment.hasQueryFilter()) {
      getToolbar().clear();
    }

    if (contactsFragment.getSelectedContactsCount() < MINIMUM_GROUP_SIZE) {
      disableNext();
    }
  }

  private void enableNext() {
    next.setEnabled(true);
    next.animate().alpha(1f);
  }

  private void disableNext() {
    next.setEnabled(false);
    next.animate().alpha(0.5f);
  }

  private void handleNextPressed() {
    RecipientId[] ids = Stream.of(contactsFragment.getSelectedContacts())
                              .map(selectedContact -> selectedContact.getOrCreateRecipientId(this))
                              .toArray(RecipientId[]::new);

    startActivityForResult(AddGroupDetailsActivity.newIntent(this, ids), REQUEST_CODE_ADD_DETAILS);
  }
}
