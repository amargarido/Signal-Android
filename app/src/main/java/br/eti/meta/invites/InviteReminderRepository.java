package br.eti.meta.invites;

import android.content.Context;

import br.eti.meta.database.DatabaseFactory;
import br.eti.meta.database.MmsSmsDatabase;
import br.eti.meta.database.RecipientDatabase;
import br.eti.meta.recipients.Recipient;

public final class InviteReminderRepository implements InviteReminderModel.Repository {

  private final Context context;

  public InviteReminderRepository(Context context) {
    this.context = context;
  }

  @Override
  public void setHasSeenFirstInviteReminder(Recipient recipient) {
    RecipientDatabase recipientDatabase = DatabaseFactory.getRecipientDatabase(context);
    recipientDatabase.setSeenFirstInviteReminder(recipient.getId());
  }

  @Override
  public void setHasSeenSecondInviteReminder(Recipient recipient) {
    RecipientDatabase recipientDatabase = DatabaseFactory.getRecipientDatabase(context);
    recipientDatabase.setSeenSecondInviteReminder(recipient.getId());
  }

  @Override
  public int getPercentOfInsecureMessages(int insecureCount) {
    MmsSmsDatabase mmsSmsDatabase = DatabaseFactory.getMmsSmsDatabase(context);
    int            insecure       = mmsSmsDatabase.getInsecureMessageCountForInsights();
    int            secure         = mmsSmsDatabase.getSecureMessageCountForInsights();

    if (insecure + secure == 0) return 0;
    return Math.round(100f * (insecureCount / (float) (insecure + secure)));
  }
}