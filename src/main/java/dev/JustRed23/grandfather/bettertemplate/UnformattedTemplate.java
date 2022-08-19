package dev.JustRed23.grandfather.bettertemplate;

import org.jetbrains.annotations.NotNull;

public class UnformattedTemplate implements Template {

    private final TemplateType type;
    private String message;

    UnformattedTemplate(@NotNull String message, @NotNull TemplateType type, boolean formatType) {
        this.message = formatType ? message.replaceFirst("%s", type.getEmoji()) : message;
        this.type = type;
    }

    UnformattedTemplate(@NotNull String message, @NotNull TemplateType type) {
        this(message, type, true);
    }

    public FormattedTemplate format(Object... args) {
        return FormattedTemplate.from(formatRaw(args));
    }

    public UnformattedTemplate formatRaw(Object... args) {
        for (Object arg : args)
            this.message = message.replaceFirst("%s", arg.toString());

        return this;
    }

    public String getMessage() {
        return message;
    }

    public TemplateType getType() {
        return type;
    }
}
