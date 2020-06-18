package br.eti.meta.attachments;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.eti.meta.audio.AudioHash;
import br.eti.meta.blurhash.BlurHash;
import br.eti.meta.database.AttachmentDatabase.TransformProperties;
import br.eti.meta.stickers.StickerLocator;

public class UriAttachment extends Attachment {

  private final @NonNull  Uri dataUri;
  private final @Nullable Uri thumbnailUri;

  public UriAttachment(@NonNull Uri uri, @NonNull String contentType, int transferState, long size,
                       @Nullable String fileName, boolean voiceNote, boolean quote, @Nullable String caption,
                       @Nullable StickerLocator stickerLocator, @Nullable BlurHash blurHash, @Nullable AudioHash audioHash, @Nullable TransformProperties transformProperties)
  {
    this(uri, uri, contentType, transferState, size, 0, 0, fileName, null, voiceNote, quote, caption, stickerLocator, blurHash, audioHash, transformProperties);
  }

  public UriAttachment(@NonNull Uri dataUri, @Nullable Uri thumbnailUri,
                       @NonNull String contentType, int transferState, long size, int width, int height,
                       @Nullable String fileName, @Nullable String fastPreflightId,
                       boolean voiceNote, boolean quote, @Nullable String caption, @Nullable StickerLocator stickerLocator,
                       @Nullable BlurHash blurHash, @Nullable AudioHash audioHash, @Nullable TransformProperties transformProperties)
  {
    super(contentType, transferState, size, fileName, 0, null, null, null, null, fastPreflightId, voiceNote, width, height, quote, 0, caption, stickerLocator, blurHash, audioHash, transformProperties);
    this.dataUri      = dataUri;
    this.thumbnailUri = thumbnailUri;
  }

  @Override
  @NonNull
  public Uri getDataUri() {
    return dataUri;
  }

  @Override
  @Nullable
  public Uri getThumbnailUri() {
    return thumbnailUri;
  }

  @Override
  public boolean equals(Object other) {
    return other != null && other instanceof UriAttachment && ((UriAttachment) other).dataUri.equals(this.dataUri);
  }

  @Override
  public int hashCode() {
    return dataUri.hashCode();
  }
}