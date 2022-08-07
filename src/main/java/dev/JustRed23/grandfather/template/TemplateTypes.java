package dev.JustRed23.grandfather.template;

import dev.JustRed23.grandfather.utils.EmojiUtils;

public enum TemplateTypes {
    INFO(EmojiUtils.General.INFO), WARNING(EmojiUtils.General.WARNING), ERROR(EmojiUtils.General.ERROR);

    private String emoji;

    TemplateTypes(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }
}
