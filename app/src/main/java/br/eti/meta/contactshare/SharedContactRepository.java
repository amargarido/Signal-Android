package br.eti.meta.contactshare;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import android.text.TextUtils;

import br.eti.meta.contacts.ContactsDatabase;
import br.eti.meta.contacts.avatars.ContactPhoto;
import br.eti.meta.logging.Log;
import br.eti.meta.mms.PartAuthority;
import br.eti.meta.phonenumbers.PhoneNumberFormatter;
import br.eti.meta.providers.BlobProvider;
import br.eti.meta.recipients.Recipient;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import ezvcard.Ezvcard;
import ezvcard.VCard;

public class SharedContactRepository {

  private static final String TAG = SharedContactRepository.class.getSimpleName();

  private final Context          context;
  private final Executor         executor;
  private final ContactsDatabase contactsDatabase;

  SharedContactRepository(@NonNull Context          context,
                          @NonNull Executor         executor,
                          @NonNull ContactsDatabase contactsDatabase)
  {
    this.context          = context.getApplicationContext();
    this.executor         = executor;
    this.contactsDatabase = contactsDatabase;
  }

  void getContacts(@NonNull List<Uri> contactUris, @NonNull ValueCallback<List<Contact>> callback) {
    executor.execute(() -> {
      List<Contact> contacts = new ArrayList<>(contactUris.size());
      for (Uri contactUri : contactUris) {
        Contact contact;

        if (ContactsContract.AUTHORITY.equals(contactUri.getAuthority())) {
          contact = getContactFromSystemContacts(ContactUtil.getContactIdFromUri(contactUri));
        } else {
          contact = getContactFromVcard(contactUri);
        }

        if (contact != null) {
          contacts.add(contact);
        }
      }
      callback.onComplete(contacts);
    });
  }

  @WorkerThread
  private @Nullable Contact getContactFromSystemContacts(long contactId) {
    Contact.Name name = getName(contactId);
    if (name == null) {
      Log.w(TAG, "Couldn't find a name associated with the provided contact ID.");
      return null;
    }

    List<Contact.Phone> phoneNumbers = getPhoneNumbers(contactId);
    AvatarInfo  avatarInfo   = getAvatarInfo(contactId, phoneNumbers);
    Contact.Avatar avatar       = avatarInfo != null ? new Contact.Avatar(avatarInfo.uri, avatarInfo.isProfile) : null;

    return new Contact(name, null, phoneNumbers, getEmails(contactId), getPostalAddresses(contactId), avatar);
  }

  @WorkerThread
  private @Nullable Contact getContactFromVcard(@NonNull Uri uri) {
    Contact contact = null;

    try (InputStream stream = PartAuthority.getAttachmentStream(context, uri)) {
      VCard vcard = Ezvcard.parse(stream).first();

      ezvcard.property.StructuredName  vName            = vcard.getStructuredName();
      List<ezvcard.property.Telephone> vPhones          = vcard.getTelephoneNumbers();
      List<ezvcard.property.Email>     vEmails          = vcard.getEmails();
      List<ezvcard.property.Address>   vPostalAddresses = vcard.getAddresses();

      String organization = vcard.getOrganization() != null && !vcard.getOrganization().getValues().isEmpty() ? vcard.getOrganization().getValues().get(0) : null;
      String displayName  = vcard.getFormattedName() != null ? vcard.getFormattedName().getValue() : null;

      if (displayName == null && vName != null) {
        displayName = vName.getGiven();
      }

      if (displayName == null && vcard.getOrganization() != null) {
        displayName = organization;
      }

      if (displayName == null) {
        throw new IOException("No valid name.");
      }

      Contact.Name name = new Contact.Name(displayName,
                           vName != null ? vName.getGiven() : null,
                           vName != null ? vName.getFamily() : null,
                           vName != null && !vName.getPrefixes().isEmpty() ? vName.getPrefixes().get(0) : null,
                           vName != null && !vName.getSuffixes().isEmpty() ? vName.getSuffixes().get(0) : null,
                           null);


      List<Contact.Phone> phoneNumbers = new ArrayList<>(vPhones.size());
      for (ezvcard.property.Telephone vEmail : vPhones) {
        String label = !vEmail.getTypes().isEmpty() ? getCleanedVcardType(vEmail.getTypes().get(0).getValue()) : null;

        // Phone number is stored in the uri field in v4.0 only. In other versions, it is in the text field.
        String phoneNumberFromText  = vEmail.getText();
        String extractedPhoneNumber = phoneNumberFromText == null ? vEmail.getUri().getNumber() : phoneNumberFromText;
        phoneNumbers.add(new Contact.Phone(extractedPhoneNumber, phoneTypeFromVcardType(label), label));
      }

      List<Contact.Email> emails = new ArrayList<>(vEmails.size());
      for (ezvcard.property.Email vEmail : vEmails) {
        String label = !vEmail.getTypes().isEmpty() ? getCleanedVcardType(vEmail.getTypes().get(0).getValue()) : null;
        emails.add(new Contact.Email(vEmail.getValue(), emailTypeFromVcardType(label), label));
      }

      List<Contact.PostalAddress> postalAddresses = new ArrayList<>(vPostalAddresses.size());
      for (ezvcard.property.Address vPostalAddress : vPostalAddresses) {
        String label = !vPostalAddress.getTypes().isEmpty() ? getCleanedVcardType(vPostalAddress.getTypes().get(0).getValue()) : null;
        postalAddresses.add(new Contact.PostalAddress(postalAddressTypeFromVcardType(label),
                                              label,
                                              vPostalAddress.getStreetAddress(),
                                              vPostalAddress.getPoBox(),
                                              null,
                                              vPostalAddress.getLocality(),
                                              vPostalAddress.getRegion(),
                                              vPostalAddress.getPostalCode(),
                                              vPostalAddress.getCountry()));
      }

      contact = new Contact(name, organization, phoneNumbers, emails, postalAddresses, null);
    } catch (IOException e) {
      Log.w(TAG, "Failed to parse the vcard.", e);
    }

    if (BlobProvider.AUTHORITY.equals(uri.getAuthority())) {
      BlobProvider.getInstance().delete(context, uri);
    }

    return contact;
  }

  @WorkerThread
  private @Nullable
  Contact.Name getName(long contactId) {
    try (Cursor cursor = contactsDatabase.getNameDetails(contactId)) {
      if (cursor != null && cursor.moveToFirst()) {
        String cursorDisplayName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
        String cursorGivenName   = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
        String cursorFamilyName  = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
        String cursorPrefix      = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.PREFIX));
        String cursorSuffix      = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.SUFFIX));
        String cursorMiddleName  = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));

        Contact.Name name = new Contact.Name(cursorDisplayName, cursorGivenName, cursorFamilyName, cursorPrefix, cursorSuffix, cursorMiddleName);
        if (!name.isEmpty()) {
          return name;
        }
      }
    }

    String org = contactsDatabase.getOrganizationName(contactId);
    if (!TextUtils.isEmpty(org)) {
      return new Contact.Name(org, org, null, null, null, null);
    }

    return null;
  }

  @WorkerThread
  private @NonNull List<Contact.Phone> getPhoneNumbers(long contactId) {
    Map<String, Contact.Phone> numberMap = new HashMap<>();
    try (Cursor cursor = contactsDatabase.getPhoneDetails(contactId)) {
      while (cursor != null && cursor.moveToNext()) {
        String cursorNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
        int    cursorType   = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE));
        String cursorLabel  = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LABEL));

        String number    = ContactUtil.getNormalizedPhoneNumber(context, cursorNumber);
        Contact.Phone existing  = numberMap.get(number);
        Contact.Phone candidate = new Contact.Phone(number, phoneTypeFromContactType(cursorType), cursorLabel);

        if (existing == null || (existing.getType() == Contact.Phone.Type.CUSTOM && existing.getLabel() == null)) {
          numberMap.put(number, candidate);
        }
      }
    }

    List<Contact.Phone> numbers = new ArrayList<>(numberMap.size());
    numbers.addAll(numberMap.values());
    return numbers;
  }

  @WorkerThread
  private @NonNull List<Contact.Email> getEmails(long contactId) {
    List<Contact.Email> emails = new LinkedList<>();

    try (Cursor cursor = contactsDatabase.getEmailDetails(contactId)) {
      while (cursor != null && cursor.moveToNext()) {
        String cursorEmail = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS));
        int    cursorType  = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.TYPE));
        String cursorLabel = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.LABEL));

        emails.add(new Contact.Email(cursorEmail, emailTypeFromContactType(cursorType), cursorLabel));
      }
    }

    return emails;
  }

  @WorkerThread
  private @NonNull List<Contact.PostalAddress> getPostalAddresses(long contactId) {
    List<Contact.PostalAddress> postalAddresses = new LinkedList<>();

    try (Cursor cursor = contactsDatabase.getPostalAddressDetails(contactId)) {
      while (cursor != null && cursor.moveToNext()) {
        int    cursorType         = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
        String cursorLabel        = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.LABEL));
        String cursorStreet       = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
        String cursorPoBox        = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
        String cursorNeighborhood = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD));
        String cursorCity         = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
        String cursorRegion       = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
        String cursorPostal       = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
        String cursorCountry      = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));

        postalAddresses.add(new Contact.PostalAddress(postalAddressTypeFromContactType(cursorType),
                                              cursorLabel,
                                              cursorStreet,
                                              cursorPoBox,
                                              cursorNeighborhood,
                                              cursorCity,
                                              cursorRegion,
                                              cursorPostal,
                                              cursorCountry));
      }
    }

    return postalAddresses;
  }

  @WorkerThread
  private @Nullable AvatarInfo getAvatarInfo(long contactId, List<Contact.Phone> phoneNumbers) {
    AvatarInfo systemAvatar = getSystemAvatarInfo(contactId);

    if (systemAvatar != null) {
      return systemAvatar;
    }

    for (Contact.Phone phoneNumber : phoneNumbers) {
      AvatarInfo recipientAvatar = getRecipientAvatarInfo(PhoneNumberFormatter.get(context).format(phoneNumber.getNumber()));
      if (recipientAvatar != null) {
        return recipientAvatar;
      }
    }
    return null;
  }

  @WorkerThread
  private @Nullable AvatarInfo getSystemAvatarInfo(long contactId) {
    Uri uri = contactsDatabase.getAvatarUri(contactId);
    if (uri != null) {
      return new AvatarInfo(uri, false);
    }

    return null;
  }

  @WorkerThread
  private @Nullable AvatarInfo getRecipientAvatarInfo(String address) {
    Recipient recipient    = Recipient.external(context, address);
    ContactPhoto contactPhoto = recipient.getContactPhoto();

    if (contactPhoto != null) {
      Uri avatarUri = contactPhoto.getUri(context);
      if (avatarUri != null) {
        return new AvatarInfo(avatarUri, contactPhoto.isProfilePhoto());
      }
    }

    return null;
  }

  private Contact.Phone.Type phoneTypeFromContactType(int type) {
    switch (type) {
      case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
        return Contact.Phone.Type.HOME;
      case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
        return Contact.Phone.Type.MOBILE;
      case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
        return Contact.Phone.Type.WORK;
    }
    return Contact.Phone.Type.CUSTOM;
  }

  private Contact.Phone.Type phoneTypeFromVcardType(@Nullable String type) {
    if      ("home".equalsIgnoreCase(type)) return Contact.Phone.Type.HOME;
    else if ("cell".equalsIgnoreCase(type)) return Contact.Phone.Type.MOBILE;
    else if ("work".equalsIgnoreCase(type)) return Contact.Phone.Type.WORK;
    else                                    return Contact.Phone.Type.CUSTOM;
  }

  private Contact.Email.Type emailTypeFromContactType(int type) {
    switch (type) {
      case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
        return Contact.Email.Type.HOME;
      case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
        return Contact.Email.Type.MOBILE;
      case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
        return Contact.Email.Type.WORK;
    }
    return Contact.Email.Type.CUSTOM;
  }

  private Contact.Email.Type emailTypeFromVcardType(@Nullable String type) {
    if      ("home".equalsIgnoreCase(type)) return Contact.Email.Type.HOME;
    else if ("cell".equalsIgnoreCase(type)) return Contact.Email.Type.MOBILE;
    else if ("work".equalsIgnoreCase(type)) return Contact.Email.Type.WORK;
    else                                    return Contact.Email.Type.CUSTOM;
  }

  private Contact.PostalAddress.Type postalAddressTypeFromContactType(int type) {
    switch (type) {
      case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME:
        return Contact.PostalAddress.Type.HOME;
      case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK:
        return Contact.PostalAddress.Type.WORK;
    }
    return Contact.PostalAddress.Type.CUSTOM;
  }

  private Contact.PostalAddress.Type postalAddressTypeFromVcardType(@Nullable String type) {
    if      ("home".equalsIgnoreCase(type)) return Contact.PostalAddress.Type.HOME;
    else if ("work".equalsIgnoreCase(type)) return Contact.PostalAddress.Type.WORK;
    else                                    return Contact.PostalAddress.Type.CUSTOM;
  }

  private String getCleanedVcardType(@Nullable String type) {
    if (TextUtils.isEmpty(type)) return "";

    if (type.startsWith("x-") && type.length() > 2) {
      return type.substring(2);
    }

    return type;
  }

  interface ValueCallback<T> {
    void onComplete(@NonNull T value);
  }

  private static class AvatarInfo {

    private final Uri     uri;
    private final boolean isProfile;

    private AvatarInfo(Uri uri, boolean isProfile) {
      this.uri = uri;
      this.isProfile = isProfile;
    }

    public Uri getUri() {
      return uri;
    }

    public boolean isProfile() {
      return isProfile;
    }
  }
}
