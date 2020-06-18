package org.eti.meta.database.loaders;

import android.content.Context;
import android.database.Cursor;

import org.eti.meta.database.DatabaseFactory;
import org.eti.meta.util.AbstractCursorLoader;

public class BlockedContactsLoader extends AbstractCursorLoader {

  public BlockedContactsLoader(Context context) {
    super(context);
  }

  @Override
  public Cursor getCursor() {
    return DatabaseFactory.getRecipientDatabase(getContext()).getBlocked();
  }

}
