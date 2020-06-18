package br.eti.meta.net;

public interface RequestController {

  /**
   * Best-effort cancellation of any outstanding requests. Will also release any resources held by
   * the underlying request.
   */
  void cancel();
}