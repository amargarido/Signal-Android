package org.eti.meta.conversationlist;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eti.meta.BindableConversationListItem;
import org.eti.meta.R;
import org.eti.meta.database.model.ThreadRecord;
import org.eti.meta.mms.GlideRequests;
import org.eti.meta.util.ViewUtil;

import java.util.Locale;
import java.util.Set;

public class ConversationListItemAction extends LinearLayout implements BindableConversationListItem {

  private TextView description;

  public ConversationListItemAction(Context context) {
    super(context);
  }

  public ConversationListItemAction(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public ConversationListItemAction(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void onFinishInflate() {
    super.onFinishInflate();
    this.description = ViewUtil.findById(this, R.id.description);
  }

  @Override
  public void bind(@NonNull ThreadRecord thread,
                   @NonNull GlideRequests glideRequests,
                   @NonNull Locale locale,
                   @NonNull Set<Long> typingThreads,
                   @NonNull Set<Long> selectedThreads,
                   boolean batchMode)
  {
    this.description.setText(getContext().getString(R.string.ConversationListItemAction_archived_conversations_d, thread.getCount()));
  }

  @Override
  public void unbind() {

  }

  @Override
  public void setBatchMode(boolean batchMode) {

  }

  @Override
  public void updateTypingIndicator(@NonNull Set<Long> typingThreads) {

  }
}
