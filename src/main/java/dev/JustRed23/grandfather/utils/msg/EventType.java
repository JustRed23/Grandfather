package dev.JustRed23.grandfather.utils.msg;

import io.r2dbc.spi.Result;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public enum EventType {
    PRIVATE, GUILD, GUILD_SLASH, UNKNOWN;

    public static EventType getEventType(Event event) {
        if (event instanceof MessageReceivedEvent messageReceivedEvent)
            if (messageReceivedEvent.isFromGuild())
                return GUILD;
            else
                return PRIVATE;
        else if (event instanceof SlashCommandInteractionEvent)
            return GUILD_SLASH;
        return UNKNOWN;
    }
}
