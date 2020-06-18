package br.eti.meta.groups;

import androidx.annotation.StringRes;

import br.eti.meta.R;

public enum GroupAccessControl {
  ALL_MEMBERS(R.string.GroupManagement_access_level_all_members),
  ONLY_ADMINS(R.string.GroupManagement_access_level_only_admins);

  private final @StringRes int string;

  GroupAccessControl(@StringRes int string) {
    this.string = string;
  }

  public @StringRes int getString() {
    return string;
  }
}
