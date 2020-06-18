package org.eti.meta.jobmanager.impl;

import android.app.job.JobInfo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.eti.meta.dependencies.ApplicationDependencies;
import org.eti.meta.jobmanager.Constraint;

/**
 * A constraint that is met once we have pulled down all messages from the websocket during initial
 * load. See {@link org.eti.meta.messages.InitialMessageRetriever}.
 */
public final class WebsocketDrainedConstraint implements Constraint {

  public static final String KEY = "WebsocketDrainedConstraint";

  private WebsocketDrainedConstraint() {
  }

  @Override
  public boolean isMet() {
    return ApplicationDependencies.getInitialMessageRetriever().isCaughtUp();
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @RequiresApi(26)
  @Override
  public void applyToJobInfo(@NonNull JobInfo.Builder jobInfoBuilder) {
  }

  public static final class Factory implements Constraint.Factory<WebsocketDrainedConstraint> {

    @Override
    public WebsocketDrainedConstraint create() {
      return new WebsocketDrainedConstraint();
    }
  }
}
