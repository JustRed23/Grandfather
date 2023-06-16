package dev.JustRed23.grandfather.bettertemplate;

import dev.JustRed23.grandfather.utils.EmojiUtils;

public enum TemplateType {
    INFO(":information_source:"), WARNING(":warning:"), ERROR(":octagonal_sign:");

    private final String emoji;

    TemplateType(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }
}
