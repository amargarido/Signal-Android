package org.eti.meta.migrations;

public class MigrationCompleteEvent {

  private final int version;

  public MigrationCompleteEvent(int version) {
    this.version = version;
  }

  public int getVersion() {
    return version;
  }
}
