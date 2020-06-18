package br.eti.meta.reactions.any;

import android.content.Context;

import androidx.annotation.NonNull;

import br.eti.meta.components.emoji.EmojiPageModel;
import br.eti.meta.components.emoji.EmojiUtil;
import br.eti.meta.components.emoji.RecentEmojiPageModel;
import br.eti.meta.sms.MessageSender;
import br.eti.meta.util.concurrent.SignalExecutors;

import java.util.LinkedList;
import java.util.List;

final class ReactWithAnyEmojiRepository {

  private static final String RECENT_STORAGE_KEY = "reactions_recent_emoji";

  private final Context              context;
  private final RecentEmojiPageModel recentEmojiPageModel;
  private final List<EmojiPageModel> emojiPageModels;

  ReactWithAnyEmojiRepository(@NonNull Context context) {
    this.context              = context;
    this.recentEmojiPageModel = new RecentEmojiPageModel(context, RECENT_STORAGE_KEY);
    this.emojiPageModels      = new LinkedList<>();

    emojiPageModels.add(recentEmojiPageModel);
    emojiPageModels.addAll(EmojiUtil.getDisplayPages());
    emojiPageModels.remove(emojiPageModels.size() - 1);
  }

  List<EmojiPageModel> getEmojiPageModels() {
    return emojiPageModels;
  }

  void addEmojiToMessage(@NonNull String emoji, long messageId, boolean isMms) {
    recentEmojiPageModel.onCodePointSelected(emoji);

    SignalExecutors.BOUNDED.execute(() -> MessageSender.sendNewReaction(context, messageId, isMms, emoji));
  }
}
