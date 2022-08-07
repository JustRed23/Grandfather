package dev.JustRed23.grandfather.command.types;

import dev.JustRed23.grandfather.command.Category;
import dev.JustRed23.grandfather.command.CommandContext;
import dev.JustRed23.grandfather.command.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class DefaultMusicCommand implements ICommand {

    public void execute(CommandContext context) {
        if (context.isPrivateMessage())
            return;

        if (context.getSlashCommandEvent() != null)
            execute(context, context.getSlashCommandEvent());
        else if (context.getMessageReceivedEvent() != null)
            execute(context, context.getMessageReceivedEvent());
    }

    public void execute(CommandContext context, SlashCommandInteractionEvent event) {}
    public void execute(CommandContext context, MessageReceivedEvent event) {}

    public Category getCategory() {
        return Category.MUSIC;
    }

    public abstract boolean needConnectedVoice();
    public abstract boolean sameVoice();
    public abstract boolean activeQueue();
    public abstract boolean activePlayer();
}
