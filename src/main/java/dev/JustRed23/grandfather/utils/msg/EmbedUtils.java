package dev.JustRed23.grandfather.utils.msg;

import dev.JustRed23.grandfather.template.Template;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class EmbedUtils {

    private static final Color INFO = Color.CYAN;
    private static final Color WARNING = Color.ORANGE;
    private static final Color ERROR = Color.RED;

    private EmbedUtils() {}

    public static void sendTemplateEmbed(@NotNull Template template, net.dv8tion.jda.api.events.Event event) {
        Checks.notNull(template.getTemplateType(), "Template Type");
        switch (template.getTemplateType()) {
            case INFO -> sendInfoEmbed(template, event);
            case WARNING -> sendWarningEmbed(template, event);
            case ERROR -> sendErrorEmbed(template, event);
        }
    }

    public static void sendTemplateEmbed(@NotNull Template template, @NotNull TextChannel channel) {
        Checks.notNull(template.getTemplateType(), "Template Type");
        switch (template.getTemplateType()) {
            case INFO -> sendInfoEmbed(template, channel);
            case WARNING -> sendWarningEmbed(template, channel);
            case ERROR -> sendErrorEmbed(template, channel);
        }
    }

    public static void sendInfoEmbed(@NotNull CharSequence message, net.dv8tion.jda.api.events.Event event) {
        sendEmbed(new EmbedBuilder().setColor(INFO).setDescription(message), event);
    }

    public static void sendInfoEmbed(@NotNull CharSequence message, @NotNull TextChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder().setColor(INFO).setDescription(message).build()).queue();
    }

    public static void sendWarningEmbed(@NotNull CharSequence message, net.dv8tion.jda.api.events.Event event) {
        sendEmbed(new EmbedBuilder().setColor(WARNING).setDescription(message), event);
    }

    public static void sendWarningEmbed(@NotNull CharSequence message, @NotNull TextChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder().setColor(WARNING).setDescription(message).build()).queue();
    }

    public static void sendErrorEmbed(@NotNull CharSequence message, net.dv8tion.jda.api.events.Event event) {
        sendEmbed(new EmbedBuilder().setColor(ERROR).setDescription(message), event);
    }

    public static void sendErrorEmbed(@NotNull CharSequence message, @NotNull TextChannel channel) {
        channel.sendMessageEmbeds(new EmbedBuilder().setColor(ERROR).setDescription(message).build()).queue();
    }

    public static void sendEmbed(EmbedBuilder embedBuilder, net.dv8tion.jda.api.events.Event event) {
        switch (EventType.getEventType(event)) {
            case PRIVATE -> ((MessageReceivedEvent) event).getChannel().asPrivateChannel().sendMessageEmbeds(embedBuilder.build()).queue();
            case GUILD -> ((MessageReceivedEvent) event).getChannel().asGuildMessageChannel().sendMessageEmbeds(embedBuilder.build()).queue();
            case GUILD_SLASH -> ((SlashCommandInteractionEvent) event).deferReply().queue(response -> response.sendMessageEmbeds(embedBuilder.build()).queue());
            case UNKNOWN -> throw new IllegalArgumentException("Event type can only be PrivateMessageReceivedEvent, GuildMessageReceivedEvent or SlashCommandEvent");
        }
    }

    public static void sendEmbedWithActionRows(EmbedBuilder embedBuilder, Event event, ActionRow... actionRows) {
        switch (EventType.getEventType(event)) {
            case PRIVATE -> ((MessageReceivedEvent) event).getChannel().asPrivateChannel().sendMessageEmbeds(embedBuilder.build()).setActionRows(actionRows).queue();
            case GUILD -> ((MessageReceivedEvent) event).getChannel().asGuildMessageChannel().sendMessageEmbeds(embedBuilder.build()).setActionRows(actionRows).queue();
            case GUILD_SLASH -> ((SlashCommandInteractionEvent) event).deferReply().queue(response -> response.sendMessageEmbeds(embedBuilder.build()).addActionRows(actionRows).queue());
            case UNKNOWN -> throw new IllegalArgumentException("Event type can only be PrivateMessageReceivedEvent, GuildMessageReceivedEvent or SlashCommandEvent");
        }
    }
}
