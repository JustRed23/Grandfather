package dev.JustRed23.grandfather.bettertemplate;

import dev.JustRed23.grandfather.utils.EmojiUtils;

public enum TemplateType {
    INFO(EmojiUtils.General.INFO), WARNING(EmojiUtils.General.WARNING), ERROR(EmojiUtils.General.ERROR);

    private final String emoji;

    TemplateType(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }
}
