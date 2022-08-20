package dev.JustRed23.grandfather.command.commands.music;

import dev.JustRed23.grandfather.bettertemplate.Templates;
import dev.JustRed23.grandfather.command.CommandContext;
import dev.JustRed23.grandfather.command.types.DefaultMusicCommand;
import dev.JustRed23.grandfather.music.AudioPlayerManager;
import dev.JustRed23.grandfather.music.MusicManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.List;

public class DisconnectCommand extends DefaultMusicCommand {

    public void execute(CommandContext context, MessageReceivedEvent event) {
        leave(event.getGuild(), context);
    }

    public void execute(CommandContext context, SlashCommandInteractionEvent event) {
        leave(event.getGuild(), context);
    }

    private void leave(Guild guild, CommandContext context) {
        MusicManager musicManager = AudioPlayerManager.getInstance().getMusicManager(guild);
        musicManager.getScheduler().disconnect();

        Templates.Music.disconnected.message(context.getEvent());
    }

    public String getName() {
        return "disconnect";
    }

    public String getHelp() {
        return "Makes the bot leave its current voice channel";
    }

    public CommandData getCommandData() {
        return Commands.slash(getName(), getHelp()).setGuildOnly(true);
    }

    public List<String> getAliases() {
        return List.of("leave", "dc", "fuckoff");
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
