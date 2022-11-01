package dev.JustRed23.grandfather.utils.msg;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.TimeUnit;

public class MessageUtils {

    private MessageUtils() {}

    public static void sendMessage(String text, Event event) {
        switch (EventType.getEventType(event)) {
            case PRIVATE -> ((MessageReceivedEvent) event).getChannel().asPrivateChannel().sendMessage(text).queue();
            case GUILD -> ((MessageReceivedEvent) event).getChannel().asGuildMessageChannel().sendMessage(text).queue();
            case GUILD_SLASH -> ((SlashCommandInteractionEvent) event).deferReply().queue(response -> response.sendMessage(text).queue());
            case UNKNOWN -> throw new IllegalArgumentException("Event type can only be one of the following: PRIVATE, GUILD, GUILD_SLASH");
        }
    }

    public static void sendMessageAndDeleteAfter(String text, Event event, int time, TimeUnit unit) {
        switch (EventType.getEventType(event)) {
            case PRIVATE -> ((MessageReceivedEvent) event).getChannel().asPrivateChannel().sendMessage(text).queue(message -> message.delete().queueAfter(time, unit));
            case GUILD -> ((MessageReceivedEvent) event).getChannel().asGuildMessageChannel().sendMessage(text).queue(message -> message.delete().queueAfter(time, unit));
            case GUILD_SLASH -> ((SlashCommandInteractionEvent) event).deferReply().queue(response -> response.sendMessage(text).queue(message -> message.delete().queueAfter(time, unit)));
            case UNKNOWN -> throw new IllegalArgumentException("Event type can only be one of the following: PRIVATE, GUILD, GUILD_SLASH");
        }
    }
}
