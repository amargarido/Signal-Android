package br.eti.meta.mms;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.Nullable;

import br.eti.meta.attachments.Attachment;
import br.eti.meta.util.MediaUtil;

public class GifSlide extends ImageSlide {

  public GifSlide(Context context, Attachment attachment) {
    super(context, attachment);
  }


  public GifSlide(Context context, Uri uri, long size, int width, int height) {
    this(context, uri, size, width, height, null);
  }

  public GifSlide(Context context, Uri uri, long size, int width, int height, @Nullable String caption) {
    super(context, constructAttachmentFromUri(context, uri, MediaUtil.IMAGE_GIF, size, width, height, true, null, caption, null, null, null, false, false));
  }

  @Override
  @Nullable
  public Uri getThumbnailUri() {
    return getUri();
  }
}
