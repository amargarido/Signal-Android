package br.eti.meta.jobs;

import androidx.annotation.NonNull;

import org.signal.zkgroup.profiles.ProfileKey;
import br.eti.meta.crypto.ProfileKeyUtil;
import br.eti.meta.database.DatabaseFactory;
import br.eti.meta.database.RecipientDatabase;
import br.eti.meta.dependencies.ApplicationDependencies;
import br.eti.meta.groups.GroupId;
import br.eti.meta.jobmanager.Data;
import br.eti.meta.jobmanager.Job;
import br.eti.meta.jobmanager.impl.NetworkConstraint;
import br.eti.meta.profiles.AvatarHelper;
import br.eti.meta.recipients.Recipient;
import br.eti.meta.util.FeatureFlags;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;
import org.whispersystems.signalservice.api.util.StreamDetails;

import java.util.List;

public class RotateProfileKeyJob extends BaseJob {

  public static String KEY = "RotateProfileKeyJob";

  public RotateProfileKeyJob() {
    this(new Job.Parameters.Builder()
                           .setQueue("__ROTATE_PROFILE_KEY__")
                           .addConstraint(NetworkConstraint.KEY)
                           .setMaxAttempts(25)
                           .setMaxInstances(1)
                           .build());
  }

  private RotateProfileKeyJob(@NonNull Job.Parameters parameters) {
    super(parameters);
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
  public void onRun() throws Exception {
    SignalServiceAccountManager accountManager    = ApplicationDependencies.getSignalServiceAccountManager();
    RecipientDatabase recipientDatabase = DatabaseFactory.getRecipientDatabase(context);
    ProfileKey                  profileKey        = ProfileKeyUtil.createNew();
    Recipient self              = Recipient.self();

    recipientDatabase.setProfileKey(self.getId(), profileKey);

     try (StreamDetails avatarStream = AvatarHelper.getSelfProfileAvatarStream(context)) {
      if (FeatureFlags.versionedProfiles()) {
        accountManager.setVersionedProfile(self.getUuid().get(),
                                           profileKey,
                                           Recipient.self().getProfileName().serialize(),
                                           avatarStream);
      } else {
        accountManager.setProfileName(profileKey, Recipient.self().getProfileName().serialize());
        accountManager.setProfileAvatar(profileKey, avatarStream);
      }
    }

    ApplicationDependencies.getJobManager().add(new RefreshAttributesJob());

    updateProfileKeyOnAllV2Groups();
  }

  private void updateProfileKeyOnAllV2Groups() {
    List<GroupId.V2> allGv2Groups = DatabaseFactory.getGroupDatabase(context).getAllGroupV2Ids();

    for (GroupId.V2 groupId : allGv2Groups) {
      ApplicationDependencies.getJobManager().add(new GroupV2UpdateSelfProfileKeyJob(groupId));
    }
  }

  @Override
  public void onFailure() {

  }

  @Override
  protected boolean onShouldRetry(@NonNull Exception exception) {
    return exception instanceof PushNetworkException;
  }

  public static final class Factory implements Job.Factory<RotateProfileKeyJob> {
    @Override
    public @NonNull RotateProfileKeyJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new RotateProfileKeyJob(parameters);
    }
  }
}
