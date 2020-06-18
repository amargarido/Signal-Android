package br.eti.meta.jobs;

import android.content.Context;

import androidx.annotation.NonNull;

import org.signal.zkgroup.profiles.ProfileKey;
import br.eti.meta.crypto.ProfileKeyUtil;
import br.eti.meta.database.DatabaseFactory;
import br.eti.meta.dependencies.ApplicationDependencies;
import br.eti.meta.jobmanager.Data;
import br.eti.meta.jobmanager.Job;
import br.eti.meta.jobmanager.impl.NetworkConstraint;
import br.eti.meta.profiles.AvatarHelper;
import br.eti.meta.profiles.ProfileName;
import br.eti.meta.recipients.Recipient;
import br.eti.meta.util.FeatureFlags;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.util.StreamDetails;

public final class ProfileUploadJob extends BaseJob {

  public static final String KEY = "ProfileUploadJob";

  public static final String QUEUE = "ProfileAlteration";

  private final Context                     context;
  private final SignalServiceAccountManager accountManager;

  public ProfileUploadJob() {
    this(new Job.Parameters.Builder()
                            .addConstraint(NetworkConstraint.KEY)
                            .setQueue(QUEUE)
                            .setLifespan(Parameters.IMMORTAL)
                            .setMaxAttempts(Parameters.UNLIMITED)
                            .setMaxInstances(1)
                            .build());
  }

  private ProfileUploadJob(@NonNull Parameters parameters) {
    super(parameters);

    this.context        = ApplicationDependencies.getApplication();
    this.accountManager = ApplicationDependencies.getSignalServiceAccountManager();
  }

  @Override
  protected void onRun() throws Exception {
    ProfileKey  profileKey  = ProfileKeyUtil.getSelfProfileKey();
    ProfileName profileName = Recipient.self().getProfileName();
    String      avatarPath  = null;

    try (StreamDetails avatar = AvatarHelper.getSelfProfileAvatarStream(context)) {
      if (FeatureFlags.versionedProfiles()) {
        avatarPath = accountManager.setVersionedProfile(Recipient.self().getUuid().get(), profileKey, profileName.serialize(), avatar).orNull();
      } else {
        accountManager.setProfileName(profileKey, profileName.serialize());
        avatarPath = accountManager.setProfileAvatar(profileKey, avatar).orNull();
      }
    }

    DatabaseFactory.getRecipientDatabase(context).setProfileAvatar(Recipient.self().getId(), avatarPath);
  }

  @Override
  protected boolean onShouldRetry(@NonNull Exception e) {
    return true;
  }

  @Override
  public @NonNull
  Data serialize() {
    return Data.EMPTY;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onFailure() {
  }

  public static class Factory implements Job.Factory {

    @NonNull
    @Override
    public Job create(@NonNull Parameters parameters, @NonNull Data data) {
      return new ProfileUploadJob(parameters);
    }
  }
}
