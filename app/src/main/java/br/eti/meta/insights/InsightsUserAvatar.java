package br.eti.meta.insights;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import br.eti.meta.color.MaterialColor;
import br.eti.meta.contacts.avatars.FallbackContactPhoto;
import br.eti.meta.contacts.avatars.ProfileContactPhoto;
import br.eti.meta.mms.GlideApp;
import br.eti.meta.mms.GlideRequests;

class InsightsUserAvatar {
  private final ProfileContactPhoto  profileContactPhoto;
  private final MaterialColor        fallbackColor;
  private final FallbackContactPhoto fallbackContactPhoto;

  InsightsUserAvatar(@NonNull ProfileContactPhoto profileContactPhoto, @NonNull MaterialColor fallbackColor, @NonNull FallbackContactPhoto fallbackContactPhoto) {
    this.profileContactPhoto  = profileContactPhoto;
    this.fallbackColor        = fallbackColor;
    this.fallbackContactPhoto = fallbackContactPhoto;
  }

  private Drawable fallbackDrawable(@NonNull Context context) {
    return fallbackContactPhoto.asDrawable(context, fallbackColor.toAvatarColor(context));
  }

  void load(ImageView into) {
    GlideApp.with(into)
            .load(profileContactPhoto)
            .error(fallbackDrawable(into.getContext()))
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(into);
  }
}
