package org.eti.meta.jobmanager.impl;

import androidx.annotation.NonNull;

import org.eti.meta.dependencies.ApplicationDependencies;
import org.eti.meta.jobmanager.ConstraintObserver;

/**
 * An observer for {@link WebsocketDrainedConstraint}. Will fire when the
 * {@link org.eti.meta.messages.InitialMessageRetriever} is caught up.
 */
public class WebsocketDrainedConstraintObserver implements ConstraintObserver {

  private static final String REASON = WebsocketDrainedConstraintObserver.class.getSimpleName();

  @Override
  public void register(@NonNull Notifier notifier) {
    ApplicationDependencies.getInitialMessageRetriever().addListener(() -> {
      notifier.onConstraintMet(REASON);
    });
  }
}
