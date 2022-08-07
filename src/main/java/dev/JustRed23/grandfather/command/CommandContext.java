package dev.JustRed23.grandfather.command;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandContext {

    private MessageReceivedEvent messageReceivedEvent;
    private boolean privateMessage;
    private SlashCommandInteractionEvent slashCommandEvent;

    private List<String> args;

    public MessageReceivedEvent getMessageReceivedEvent() {
        return messageReceivedEvent;
    }

    public CommandContext setMessageReceivedEvent(MessageReceivedEvent messageReceivedEvent, boolean privateMessage) {
        this.privateMessage = privateMessage;
        this.messageReceivedEvent = messageReceivedEvent;
        return this;
    }

    public SlashCommandInteractionEvent getSlashCommandEvent() {
        return slashCommandEvent;
    }

    public CommandContext setSlashCommandEvent(SlashCommandInteractionEvent slashCommandEvent) {
        this.slashCommandEvent = slashCommandEvent;
        return this;
    }

    @Nullable
    public Event getEvent() {
        if (getMessageReceivedEvent() != null)
            return getMessageReceivedEvent();
        else if (getSlashCommandEvent() != null)
            return getSlashCommandEvent();
        return null;
    }

    public boolean isPrivateMessage() {
        return privateMessage;
    }

    public List<String> getArgs() {
        return args;
    }

    public CommandContext setArgs(List<String> args) {
        this.args = args;
        return this;
    }
}
