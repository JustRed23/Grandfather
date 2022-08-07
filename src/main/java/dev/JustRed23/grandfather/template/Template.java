package dev.JustRed23.grandfather.template;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Template implements CharSequence {

    private String message;
    private TemplateTypes templateType;

    public Template(String message) {
        this.message = message;
    }

    public Template(String unformatted, Object... args) {
        String msg = unformatted;

        for (Object arg : args)
            msg = msg.replaceFirst("%s", arg.toString());

        this.message = msg;
    }

    public Template(String unformatted, TemplateTypes templateType) {
        this.message = unformatted.replaceFirst("%s", templateType.getEmoji());
        this.templateType = templateType;
    }

    public String format(Object... args) {
        return String.format(message, args);
    }

    public int length() {
        return message.length();
    }

    public char charAt(int index) {
        return message.charAt(index);
    }

    @NotNull
    public CharSequence subSequence(int start, int end) {
        return message.subSequence(start, end);
    }

    public String toString() {
        return message;
    }

    @Nullable
    public TemplateTypes getTemplateType() {
        return templateType;
    }
}
