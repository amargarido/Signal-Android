package br.eti.meta.util;

import androidx.annotation.NonNull;

import com.annimon.stream.Stream;

import br.eti.meta.database.model.MediaMmsMessageRecord;
import br.eti.meta.database.model.MessageRecord;
import br.eti.meta.database.model.MmsMessageRecord;
import br.eti.meta.mms.Slide;

public final class MessageRecordUtil {

  private MessageRecordUtil() {
  }

  public static boolean isMediaMessage(@NonNull MessageRecord messageRecord) {
    return messageRecord.isMms()                                    &&
        !messageRecord.isMmsNotification()                          &&
        ((MediaMmsMessageRecord)messageRecord).containsMediaSlide() &&
        ((MediaMmsMessageRecord)messageRecord).getSlideDeck().getStickerSlide() == null;
  }

  public static boolean hasSticker(@NonNull MessageRecord messageRecord) {
    return messageRecord.isMms() && ((MmsMessageRecord)messageRecord).getSlideDeck().getStickerSlide() != null;
  }

  public static boolean hasSharedContact(@NonNull MessageRecord messageRecord) {
    return messageRecord.isMms() && !((MmsMessageRecord)messageRecord).getSharedContacts().isEmpty();
  }

  public static boolean hasLocation(@NonNull MessageRecord messageRecord) {
    return messageRecord.isMms() && Stream.of(((MmsMessageRecord) messageRecord).getSlideDeck().getSlides())
                                          .anyMatch(Slide::hasLocation);
  }
}
