package dev.JustRed23.grandfather.utils.msg;

import dev.JustRed23.grandfather.bettertemplate.FormattedTemplate;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class EmbedUtils {

    private static final Color INFO = Color.CYAN;
    private static final Color WARNING = Color.ORANGE;
    private static final Color ERROR = Color.RED;

    private EmbedUtils() {}

    public static void sendTemplateEmbed(@NotNull FormattedTemplate template, Event event) {
        switch (template.getType()) {
            case INFO -> sendInfoEmbed(template.getMessage(), event);
            case WARNING -> sendWarningEmbed(template.getMessage(), event);
            case ERROR -> sendErrorEmbed(template.getMessage(), event);
        }
    }

    public static void sendTemplateEmbed(@NotNull FormattedTemplate template, @NotNull TextChannel channel) {
        switch (template.getType()) {
            case INFO -> sendInfoEmbed(template.getMessage(), channel);
            case WARNING -> sendWarningEmbed(template.getMessage(), channel);
            case ERROR -> sendErrorEmbed(template.getMessage(), channel);
        }
    }

    public static void sendTemplateEmbed(@NotNull FormattedTemplate template, @Nullable Event event, @NotNull TextChannel channel) {
        if (event != null)
            sendTemplateEmbed(template, event);
        else
            sendTemplateEmbed(template, channel);
    }

    public static void sendInfoEmbed(@NotNull String message, Event event) {
        sendEmbed(new EmbedBuilder().setColor(INFO).setDescription(message), event);
    }

    public static void sendInfoEmbed(@NotNull String message, @NotNull TextChannel channel) {
        sendEmbed(new EmbedBuilder().setColor(INFO).setDescription(message), channel);
    }

    public static void sendWarningEmbed(@NotNull String message, Event event) {
        sendEmbed(new EmbedBuilder().setColor(WARNING).setDescription(message), event);
    }

    public static void sendWarningEmbed(@NotNull String message, @NotNull TextChannel channel) {
        sendEmbed(new EmbedBuilder().setColor(WARNING).setDescription(message), channel);
    }

    public static void sendErrorEmbed(@NotNull String message, Event event) {
        sendEmbed(new EmbedBuilder().setColor(ERROR).setDescription(message), event);
    }

    public static void sendErrorEmbed(@NotNull String message, @NotNull TextChannel channel) {
        sendEmbed(new EmbedBuilder().setColor(ERROR).setDescription(message), channel);
    }

    public static void sendEmbed(EmbedBuilder embedBuilder, Event event) {
        switch (EventType.getEventType(event)) {
            case PRIVATE -> ((MessageReceivedEvent) event).getChannel().asPrivateChannel().sendMessageEmbeds(embedBuilder.build()).queue();
            case GUILD -> ((MessageReceivedEvent) event).getChannel().asGuildMessageChannel().sendMessageEmbeds(embedBuilder.build()).queue();
            case GUILD_SLASH -> ((SlashCommandInteractionEvent) event).deferReply().queue(response -> response.sendMessageEmbeds(embedBuilder.build()).queue());
            case UNKNOWN -> throw new IllegalArgumentException("Event type can only be one of the following: PRIVATE, GUILD, GUILD_SLASH");
        }
    }

    public static void sendEmbed(EmbedBuilder embedBuilder, @NotNull TextChannel channel) {
        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public static void sendEmbed(EmbedBuilder embedBuilder, @Nullable Event event, @NotNull TextChannel channel) {
        if (event != null)
            sendEmbed(embedBuilder, event);
        else
            sendEmbed(embedBuilder, channel);
    }

    public static void sendEmbedWithActionRows(EmbedBuilder embedBuilder, Event event, ActionRow... actionRows) {
        switch (EventType.getEventType(event)) {
            case PRIVATE -> ((MessageReceivedEvent) event).getChannel().asPrivateChannel().sendMessageEmbeds(embedBuilder.build()).setActionRows(actionRows).queue();
            case GUILD -> ((MessageReceivedEvent) event).getChannel().asGuildMessageChannel().sendMessageEmbeds(embedBuilder.build()).setActionRows(actionRows).queue();
            case GUILD_SLASH -> ((SlashCommandInteractionEvent) event).deferReply().queue(response -> response.sendMessageEmbeds(embedBuilder.build()).addActionRows(actionRows).queue());
            case UNKNOWN -> throw new IllegalArgumentException("Event type can only be one of the following: PRIVATE, GUILD, GUILD_SLASH");
        }
    }
}
