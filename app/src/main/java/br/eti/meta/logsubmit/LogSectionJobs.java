package br.eti.meta.logsubmit;

import android.content.Context;

import androidx.annotation.NonNull;

import br.eti.meta.dependencies.ApplicationDependencies;

public class LogSectionJobs implements LogSection {

  @Override
  public @NonNull String getTitle() {
    return "JOBS";
  }

  @Override
  public @NonNull CharSequence getContent(@NonNull Context context) {
    return ApplicationDependencies.getJobManager().getDebugInfo();
  }
}
