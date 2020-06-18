package br.eti.meta.jobs;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.signal.zkgroup.profiles.ProfileKey;
import org.signal.zkgroup.profiles.ProfileKeyCredential;
import br.eti.meta.crypto.ProfileKeyUtil;
import br.eti.meta.database.DatabaseFactory;
import br.eti.meta.database.RecipientDatabase;
import br.eti.meta.dependencies.ApplicationDependencies;
import br.eti.meta.jobmanager.Data;
import br.eti.meta.jobmanager.Job;
import br.eti.meta.jobmanager.impl.NetworkConstraint;
import br.eti.meta.logging.Log;
import br.eti.meta.profiles.ProfileName;
import br.eti.meta.recipients.Recipient;
import br.eti.meta.util.FeatureFlags;
import br.eti.meta.util.ProfileUtil;
import br.eti.meta.util.TextSecurePreferences;
import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.crypto.InvalidCiphertextException;
import org.whispersystems.signalservice.api.profiles.ProfileAndCredential;
import org.whispersystems.signalservice.api.profiles.SignalServiceProfile;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;

import java.io.IOException;


/**
 * Refreshes the profile of the local user. Different from {@link RetrieveProfileJob} in that we
 * have to sometimes look at/set different data stores, and we will *always* do the fetch regardless
 * of caching.
 */
public class RefreshOwnProfileJob extends BaseJob {

  public static final String KEY = "RefreshOwnProfileJob";

  private static final String TAG = Log.tag(RefreshOwnProfileJob.class);

  public RefreshOwnProfileJob() {
    this(new Parameters.Builder()
                       .addConstraint(NetworkConstraint.KEY)
                       .setQueue(ProfileUploadJob.QUEUE)
                       .setMaxInstances(1)
                       .setMaxAttempts(10)
                       .build());
  }


  private RefreshOwnProfileJob(@NonNull Parameters parameters) {
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
  protected void onRun() throws Exception {
    if (!TextSecurePreferences.isPushRegistered(context) || TextUtils.isEmpty(TextSecurePreferences.getLocalNumber(context))) {
      Log.w(TAG, "Not yet registered!");
      return;
    }

    Recipient self                 = Recipient.self();
    ProfileAndCredential profileAndCredential = ProfileUtil.retrieveProfileSync(context, self, getRequestType(self));
    SignalServiceProfile profile              = profileAndCredential.getProfile();

    setProfileName(profile.getName());
    setProfileAvatar(profile.getAvatar());
    setProfileCapabilities(profile.getCapabilities());
    Optional<ProfileKeyCredential> profileKeyCredential = profileAndCredential.getProfileKeyCredential();
    if (profileKeyCredential.isPresent()) {
      setProfileKeyCredential(self, ProfileKeyUtil.getSelfProfileKey(), profileKeyCredential.get());
    }
  }

  private void setProfileKeyCredential(@NonNull Recipient recipient,
                                       @NonNull ProfileKey recipientProfileKey,
                                       @NonNull ProfileKeyCredential credential)
  {
    RecipientDatabase recipientDatabase = DatabaseFactory.getRecipientDatabase(context);
    recipientDatabase.setProfileKeyCredential(recipient.getId(), recipientProfileKey, credential);
  }

  private static SignalServiceProfile.RequestType getRequestType(@NonNull Recipient recipient) {
    return FeatureFlags.versionedProfiles() && !recipient.hasProfileKeyCredential()
           ? SignalServiceProfile.RequestType.PROFILE_AND_CREDENTIAL
           : SignalServiceProfile.RequestType.PROFILE;
  }

  @Override
  protected boolean onShouldRetry(@NonNull Exception e) {
    return e instanceof PushNetworkException;
  }

  @Override
  public void onFailure() { }

  private void setProfileName(@Nullable String encryptedName) {
    try {
      ProfileKey  profileKey    = ProfileKeyUtil.getSelfProfileKey();
      String      plaintextName = ProfileUtil.decryptName(profileKey, encryptedName);
      ProfileName profileName   = ProfileName.fromSerialized(plaintextName);

      DatabaseFactory.getRecipientDatabase(context).setProfileName(Recipient.self().getId(), profileName);
    } catch (InvalidCiphertextException | IOException e) {
      Log.w(TAG, e);
    }
  }

  private static void setProfileAvatar(@Nullable String avatar) {
    ApplicationDependencies.getJobManager().add(new RetrieveProfileAvatarJob(Recipient.self(), avatar));
  }

  private void setProfileCapabilities(@Nullable SignalServiceProfile.Capabilities capabilities) {
    if (capabilities == null) {
      return;
    }

    DatabaseFactory.getRecipientDatabase(context).setCapabilities(Recipient.self().getId(), capabilities);
  }

  public static final class Factory implements Job.Factory<RefreshOwnProfileJob> {

    @Override
    public @NonNull RefreshOwnProfileJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new RefreshOwnProfileJob(parameters);
    }
  }
}