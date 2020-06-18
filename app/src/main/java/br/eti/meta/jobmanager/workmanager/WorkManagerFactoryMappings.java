package br.eti.meta.jobmanager.workmanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.eti.meta.jobs.AttachmentDownloadJob;
import br.eti.meta.jobs.AttachmentUploadJob;
import br.eti.meta.jobs.AvatarGroupsV1DownloadJob;
import br.eti.meta.jobs.CleanPreKeysJob;
import br.eti.meta.jobs.CreateSignedPreKeyJob;
import br.eti.meta.jobs.DirectoryRefreshJob;
import br.eti.meta.jobs.FailingJob;
import br.eti.meta.jobs.FcmRefreshJob;
import br.eti.meta.jobs.LocalBackupJob;
import br.eti.meta.jobs.MmsDownloadJob;
import br.eti.meta.jobs.MmsReceiveJob;
import br.eti.meta.jobs.MmsSendJob;
import br.eti.meta.jobs.MultiDeviceBlockedUpdateJob;
import br.eti.meta.jobs.MultiDeviceConfigurationUpdateJob;
import br.eti.meta.jobs.MultiDeviceContactUpdateJob;
import br.eti.meta.jobs.MultiDeviceGroupUpdateJob;
import br.eti.meta.jobs.MultiDeviceProfileKeyUpdateJob;
import br.eti.meta.jobs.MultiDeviceReadUpdateJob;
import br.eti.meta.jobs.MultiDeviceVerifiedUpdateJob;
import br.eti.meta.jobs.PushDecryptMessageJob;
import br.eti.meta.jobs.PushGroupSendJob;
import br.eti.meta.jobs.PushGroupUpdateJob;
import br.eti.meta.jobs.PushMediaSendJob;
import br.eti.meta.jobs.PushNotificationReceiveJob;
import br.eti.meta.jobs.PushTextSendJob;
import br.eti.meta.jobs.RefreshAttributesJob;
import br.eti.meta.jobs.RefreshPreKeysJob;
import br.eti.meta.jobs.RequestGroupInfoJob;
import br.eti.meta.jobs.RetrieveProfileAvatarJob;
import br.eti.meta.jobs.RetrieveProfileJob;
import br.eti.meta.jobs.RotateCertificateJob;
import br.eti.meta.jobs.RotateProfileKeyJob;
import br.eti.meta.jobs.RotateSignedPreKeyJob;
import br.eti.meta.jobs.SendDeliveryReceiptJob;
import br.eti.meta.jobs.SendReadReceiptJob;
import br.eti.meta.jobs.ServiceOutageDetectionJob;
import br.eti.meta.jobs.SmsReceiveJob;
import br.eti.meta.jobs.SmsSendJob;
import br.eti.meta.jobs.SmsSentJob;
import br.eti.meta.jobs.TrimThreadJob;
import br.eti.meta.jobs.TypingSendJob;
import br.eti.meta.jobs.UpdateApkJob;

import java.util.HashMap;
import java.util.Map;

public class WorkManagerFactoryMappings {

  private static final Map<String, String> FACTORY_MAP = new HashMap<String, String>() {{
    put("AttachmentDownloadJob", AttachmentDownloadJob.KEY);
    put("AttachmentUploadJob", AttachmentUploadJob.KEY);
    put("AvatarDownloadJob", AvatarGroupsV1DownloadJob.KEY);
    put("CleanPreKeysJob", CleanPreKeysJob.KEY);
    put("CreateSignedPreKeyJob", CreateSignedPreKeyJob.KEY);
    put("DirectoryRefreshJob", DirectoryRefreshJob.KEY);
    put("FcmRefreshJob", FcmRefreshJob.KEY);
    put("LocalBackupJob", LocalBackupJob.KEY);
    put("MmsDownloadJob", MmsDownloadJob.KEY);
    put("MmsReceiveJob", MmsReceiveJob.KEY);
    put("MmsSendJob", MmsSendJob.KEY);
    put("MultiDeviceBlockedUpdateJob", MultiDeviceBlockedUpdateJob.KEY);
    put("MultiDeviceConfigurationUpdateJob", MultiDeviceConfigurationUpdateJob.KEY);
    put("MultiDeviceContactUpdateJob", MultiDeviceContactUpdateJob.KEY);
    put("MultiDeviceGroupUpdateJob", MultiDeviceGroupUpdateJob.KEY);
    put("MultiDeviceProfileKeyUpdateJob", MultiDeviceProfileKeyUpdateJob.KEY);
    put("MultiDeviceReadUpdateJob", MultiDeviceReadUpdateJob.KEY);
    put("MultiDeviceVerifiedUpdateJob", MultiDeviceVerifiedUpdateJob.KEY);
    put("PushContentReceiveJob", FailingJob.KEY);
    put("PushDecryptJob", PushDecryptMessageJob.KEY);
    put("PushGroupSendJob", PushGroupSendJob.KEY);
    put("PushGroupUpdateJob", PushGroupUpdateJob.KEY);
    put("PushMediaSendJob", PushMediaSendJob.KEY);
    put("PushNotificationReceiveJob", PushNotificationReceiveJob.KEY);
    put("PushTextSendJob", PushTextSendJob.KEY);
    put("RefreshAttributesJob", RefreshAttributesJob.KEY);
    put("RefreshPreKeysJob", RefreshPreKeysJob.KEY);
    put("RefreshUnidentifiedDeliveryAbilityJob", FailingJob.KEY);
    put("RequestGroupInfoJob", RequestGroupInfoJob.KEY);
    put("RetrieveProfileAvatarJob", RetrieveProfileAvatarJob.KEY);
    put("RetrieveProfileJob", RetrieveProfileJob.KEY);
    put("RotateCertificateJob", RotateCertificateJob.KEY);
    put("RotateProfileKeyJob", RotateProfileKeyJob.KEY);
    put("RotateSignedPreKeyJob", RotateSignedPreKeyJob.KEY);
    put("SendDeliveryReceiptJob", SendDeliveryReceiptJob.KEY);
    put("SendReadReceiptJob", SendReadReceiptJob.KEY);
    put("ServiceOutageDetectionJob", ServiceOutageDetectionJob.KEY);
    put("SmsReceiveJob", SmsReceiveJob.KEY);
    put("SmsSendJob", SmsSendJob.KEY);
    put("SmsSentJob", SmsSentJob.KEY);
    put("TrimThreadJob", TrimThreadJob.KEY);
    put("TypingSendJob", TypingSendJob.KEY);
    put("UpdateApkJob", UpdateApkJob.KEY);
  }};

  public static @Nullable String getFactoryKey(@NonNull String workManagerClass) {
    return FACTORY_MAP.get(workManagerClass);
  }
}
