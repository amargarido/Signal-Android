package br.eti.meta.revealable;

import androidx.annotation.NonNull;

import br.eti.meta.database.AttachmentDatabase;
import br.eti.meta.database.model.MmsMessageRecord;

import java.util.concurrent.TimeUnit;

public class ViewOnceUtil {

  public static final long MAX_LIFESPAN = TimeUnit.DAYS.toMillis(30);

  public static boolean isViewable(@NonNull MmsMessageRecord message) {
    if (!message.isViewOnce()) {
      return true;
    }

    if (message.isOutgoing()) {
      return false;
    }

    if (message.getSlideDeck().getThumbnailSlide() == null) {
      return false;
    }

    if (message.getSlideDeck().getThumbnailSlide().getUri() == null) {
      return false;
    }

    if (message.getSlideDeck().getThumbnailSlide().getTransferState() != AttachmentDatabase.TRANSFER_PROGRESS_DONE) {
      return false;
    }

    if (isViewed(message)) {
      return false;
    }

    return true;
  }

  public static boolean isViewed(@NonNull MmsMessageRecord message) {
    if (!message.isViewOnce()) {
      return false;
    }

    if (message.getDateReceived() + MAX_LIFESPAN <= System.currentTimeMillis()) {
      return true;
    }

    if (message.getSlideDeck().getThumbnailSlide() != null && message.getSlideDeck().getThumbnailSlide().getTransferState() != AttachmentDatabase.TRANSFER_PROGRESS_DONE) {
      return false;
    }

    if (message.getSlideDeck().getThumbnailSlide() == null) {
      return true;
    }

    if (message.getSlideDeck().getThumbnailSlide().getUri() == null) {
      return true;
    }

    if (message.isOutgoing()) {
      return true;
    }

    return false;
  }
}