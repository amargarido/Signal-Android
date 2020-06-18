package org.eti.meta.jobmanager.workmanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.eti.meta.jobs.AttachmentDownloadJob;
import org.eti.meta.jobs.AttachmentUploadJob;
import org.eti.meta.jobs.AvatarGroupsV1DownloadJob;
import org.eti.meta.jobs.CleanPreKeysJob;
import org.eti.meta.jobs.CreateSignedPreKeyJob;
import org.eti.meta.jobs.DirectoryRefreshJob;
import org.eti.meta.jobs.FailingJob;
import org.eti.meta.jobs.FcmRefreshJob;
import org.eti.meta.jobs.LocalBackupJob;
import org.eti.meta.jobs.MmsDownloadJob;
import org.eti.meta.jobs.MmsReceiveJob;
import org.eti.meta.jobs.MmsSendJob;
import org.eti.meta.jobs.MultiDeviceBlockedUpdateJob;
import org.eti.meta.jobs.MultiDeviceConfigurationUpdateJob;
import org.eti.meta.jobs.MultiDeviceContactUpdateJob;
import org.eti.meta.jobs.MultiDeviceGroupUpdateJob;
import org.eti.meta.jobs.MultiDeviceProfileKeyUpdateJob;
import org.eti.meta.jobs.MultiDeviceReadUpdateJob;
import org.eti.meta.jobs.MultiDeviceVerifiedUpdateJob;
import org.eti.meta.jobs.PushDecryptMessageJob;
import org.eti.meta.jobs.PushGroupSendJob;
import org.eti.meta.jobs.PushGroupUpdateJob;
import org.eti.meta.jobs.PushMediaSendJob;
import org.eti.meta.jobs.PushNotificationReceiveJob;
import org.eti.meta.jobs.PushTextSendJob;
import org.eti.meta.jobs.RefreshAttributesJob;
import org.eti.meta.jobs.RefreshPreKeysJob;
import org.eti.meta.jobs.RequestGroupInfoJob;
import org.eti.meta.jobs.RetrieveProfileAvatarJob;
import org.eti.meta.jobs.RetrieveProfileJob;
import org.eti.meta.jobs.RotateCertificateJob;
import org.eti.meta.jobs.RotateProfileKeyJob;
import org.eti.meta.jobs.RotateSignedPreKeyJob;
import org.eti.meta.jobs.SendDeliveryReceiptJob;
import org.eti.meta.jobs.SendReadReceiptJob;
import org.eti.meta.jobs.ServiceOutageDetectionJob;
import org.eti.meta.jobs.SmsReceiveJob;
import org.eti.meta.jobs.SmsSendJob;
import org.eti.meta.jobs.SmsSentJob;
import org.eti.meta.jobs.TrimThreadJob;
import org.eti.meta.jobs.TypingSendJob;
import org.eti.meta.jobs.UpdateApkJob;

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
