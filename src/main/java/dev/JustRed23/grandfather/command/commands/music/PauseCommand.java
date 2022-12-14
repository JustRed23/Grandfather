package dev.JustRed23.grandfather.command.commands.music;

import dev.JustRed23.grandfather.bettertemplate.Templates;
import dev.JustRed23.grandfather.command.CommandContext;
import dev.JustRed23.grandfather.command.types.DefaultMusicCommand;
import dev.JustRed23.grandfather.music.AudioPlayerManager;
import dev.JustRed23.grandfather.music.MusicManager;
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
            Templates.Music.already_paused.message(event);
            return;
        }

        manager.pause();
        Templates.Music.paused.message(event);
    }

    public String getName() {
        return "pause";
    }

    public String getHelp() {
        return "Pauses the current song";
    }

    public CommandData getCommandData() {
        return Commands.slash(getName(), getHelp()).setGuildOnly(true);
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
