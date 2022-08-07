package dev.JustRed23.grandfather.command.commands.music;

import dev.JustRed23.grandfather.command.CommandContext;
import dev.JustRed23.grandfather.command.types.DefaultMusicCommand;
import dev.JustRed23.grandfather.music.AudioPlayerManager;
import dev.JustRed23.grandfather.music.MusicManager;
import dev.JustRed23.grandfather.template.Templates;
import dev.JustRed23.grandfather.utils.msg.MessageUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class PauseCommand extends DefaultMusicCommand {

    public void execute(CommandContext context, MessageReceivedEvent event) {
        pause(event.getGuild(), event);
    }

    public void execute(CommandContext context, SlashCommandInteractionEvent event) {
        pause(event.getGuild(), event);
    }

    private void pause(Guild guild, Event event) {
        MusicManager manager = AudioPlayerManager.getInstance().getMusicManager(guild);

        if (manager.getScheduler().isPaused()) {
            MessageUtils.sendTemplateMessage(Templates.music.already_paused, event);
            return;
        }

        manager.pause();
        MessageUtils.sendTemplateMessage(Templates.music.paused, event);
    }

    public String getName() {
        return "pause";
    }

    public String getHelp() {
        return "Pauses the current song";
    }

    public CommandData getCommandData() {
        return Commands.slash(getName(), getHelp());
    }

    public boolean needConnectedVoice() {
        return true;
    }

    public boolean sameVoice() {
        return true;
    }

    public boolean activeQueue() {
        return false;
    }

    public boolean activePlayer() {
        return true;
    }
}
