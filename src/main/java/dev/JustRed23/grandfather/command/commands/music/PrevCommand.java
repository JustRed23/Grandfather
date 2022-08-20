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

public class PrevCommand extends DefaultMusicCommand {

    public void execute(CommandContext context, MessageReceivedEvent event) {
        skip(event.getGuild(), event);
    }

    public void execute(CommandContext context, SlashCommandInteractionEvent event) {
        skip(event.getGuild(), event);
    }

    private void skip(Guild guild, Event event) {
        MusicManager manager = AudioPlayerManager.getInstance().getMusicManager(guild);
        if (manager.prevTrack())
            Templates.Music.previous_track.message(event);
        else
            Templates.Music.no_previous_tracks.message(event);
    }

    public String getName() {
        return "prev";
    }

    public String getHelp() {
        return "Goes back to the previous song";
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
        return false;
    }
}
