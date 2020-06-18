package br.eti.meta.jobmanager.impl;

import androidx.annotation.NonNull;

import br.eti.meta.dependencies.ApplicationDependencies;
import br.eti.meta.jobmanager.ConstraintObserver;
import br.eti.meta.messages.InitialMessageRetriever;

/**
 * An observer for {@link WebsocketDrainedConstraint}. Will fire when the
 * {@link InitialMessageRetriever} is caught up.
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
