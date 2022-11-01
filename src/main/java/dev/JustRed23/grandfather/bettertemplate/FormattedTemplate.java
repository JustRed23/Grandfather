package dev.JustRed23.grandfather.bettertemplate;

import dev.JustRed23.grandfather.utils.msg.EmbedUtils;
import dev.JustRed23.grandfather.utils.msg.MessageUtils;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class FormattedTemplate implements Template {

    private final TemplateType type;
    private final String message;

    public FormattedTemplate(@NotNull String formatted, @NotNull TemplateType type) {
        this.message = formatted;
        this.type = type;
    }

    public void message(Event event) {
        MessageUtils.sendMessage(getMessage(), event);
    }

    public void messageAndDelete(Event event, int delay, TimeUnit unit) {
        MessageUtils.sendMessageAndDeleteAfter(getMessage(), event, delay, unit);
    }

    public void message(@NotNull TextChannel channel) {
        channel.sendMessage(getMessage()).queue();
    }

    public void messageAndDelete(@NotNull TextChannel channel, int delay, TimeUnit unit) {
        channel.sendMessage(getMessage()).queue(message -> message.delete().queueAfter(delay, unit));
    }

    public void embed(Event event) {
        EmbedUtils.sendTemplateEmbed(this, event);
    }

    public void embed(@NotNull TextChannel channel) {
        EmbedUtils.sendTemplateEmbed(this, channel);
    }

    @Contract("_ -> new")
    public static @NotNull FormattedTemplate from(@NotNull UnformattedTemplate template) {
        return from(template.getMessage(), template.getType());
    }

    @Contract("_, _ -> new")
    public static @NotNull FormattedTemplate from(@NotNull String message, @NotNull TemplateType type) {
        return new FormattedTemplate(message, type);
    }

    public String getMessage() {
        return message;
    }

    public TemplateType getType() {
        return type;
    }
}
