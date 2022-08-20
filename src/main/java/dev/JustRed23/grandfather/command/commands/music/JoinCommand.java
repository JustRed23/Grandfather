package dev.JustRed23.grandfather.command.commands.music;

import dev.JustRed23.grandfather.bettertemplate.Templates;
import dev.JustRed23.grandfather.command.CommandContext;
import dev.JustRed23.grandfather.command.types.DefaultMusicCommand;
import dev.JustRed23.grandfather.music.AudioPlayerManager;
import dev.JustRed23.grandfather.music.MusicManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Collections;
import java.util.List;

public class JoinCommand extends DefaultMusicCommand {

    public void execute(CommandContext context, MessageReceivedEvent event) {
        join(event, event.getChannel().asTextChannel(), event.getMember(), event.getGuild().getSelfMember());
    }

    public void execute(CommandContext context, SlashCommandInteractionEvent event) {
        join(event, event.getChannel().asTextChannel(), event.getMember(), event.getGuild().getSelfMember());
    }

    public static void join(Event event, TextChannel channel, Member user, Member bot) {
        Guild guild = channel.getGuild();
        GuildVoiceState userVoiceState = user.getVoiceState();
        GuildVoiceState botVoiceState = bot.getVoiceState();

        if (botVoiceState.inAudioChannel()) {
            Templates.Music.already_in_channel.message(event);
            return;
        }

        guild.getAudioManager().openAudioConnection(userVoiceState.getChannel());

        MusicManager musicManager = AudioPlayerManager.getInstance().getMusicManager(guild);
        musicManager.getScheduler().setChannel(channel);

        Templates.Music.joined.format(userVoiceState.getChannel().getName(), channel).message(event);
    }

    public String getName() {
        return "join";
    }

    public String getHelp() {
        return "Makes the bot join your current voice channel";
    }

    public CommandData getCommandData() {
        return Commands.slash(getName(), getHelp()).setGuildOnly(true);
    }

    public List<Permission> getBotPermissions() {
        return Collections.singletonList(Permission.VOICE_CONNECT);
    }

    public boolean needConnectedVoice() {
        return false;
    }

    public boolean sameVoice() {
        return false;
    }

    public boolean activeQueue() {
        return false;
    }

    public boolean activePlayer() {
        return false;
    }
}
