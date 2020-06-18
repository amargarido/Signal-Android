package br.eti.meta.components.reminder;

import android.content.Context;
import androidx.annotation.NonNull;

import br.eti.meta.R;
import br.eti.meta.util.TextSecurePreferences;

public class ServiceOutageReminder extends Reminder {

  public ServiceOutageReminder(@NonNull Context context) {
    super(null,
          context.getString(R.string.reminder_header_service_outage_text));
  }

  public static boolean isEligible(@NonNull Context context) {
    return TextSecurePreferences.getServiceOutage(context);
  }

  @Override
  public boolean isDismissable() {
    return false;
  }

  @NonNull
  @Override
  public Importance getImportance() {
    return Importance.ERROR;
  }
}