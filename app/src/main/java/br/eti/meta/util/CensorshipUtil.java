package br.eti.meta.util;

import android.content.Context;

import androidx.annotation.NonNull;

import br.eti.meta.push.SignalServiceNetworkAccess;

public final class CensorshipUtil {

  private CensorshipUtil() {}

  public static boolean isCensored(@NonNull Context context) {
    return new SignalServiceNetworkAccess(context).isCensored(context);
  }
}
