package dev.JustRed23.grandfather.command.handler;

import dev.JustRed23.grandfather.Bot;
import dev.JustRed23.grandfather.command.ICommand;
import dev.JustRed23.grandfather.command.types.DefaultAdminCommand;
import dev.JustRed23.grandfather.command.types.DefaultInternalCommand;
import dev.JustRed23.grandfather.command.types.DefaultMusicCommand;
import dev.JustRed23.grandfather.music.AudioPlayerManager;
import dev.JustRed23.grandfather.music.MusicManager;
import dev.JustRed23.grandfather.utils.Settings;
import dev.JustRed23.grandfather.utils.UserUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import static dev.JustRed23.grandfather.command.handler.CheckTypes.*;

public class CommandChecker {

    private static boolean enabled = true;

    private CommandChecker() {}

    public static CommandTypes isCommand(MessageChannel channel, String message) {
        if (channel instanceof TextChannel textChannel)
            return isGuildCommand(textChannel, message);
        else if (channel instanceof PrivateChannel privateChannel)
            return isPrivateCommand(privateChannel, message);
        return CommandTypes.INVALID;
    }

    public static CheckTypes doChecks(ICommand command, User u, MessageChannel c, boolean isPrivateMessage) {
        if (command == null)
            return enabled ? COMMAND_NOT_FOUND : SUPPRESSED_FAIL;

        if (!enabled && !(command instanceof DefaultInternalCommand))
            return SUPPRESSED_FAIL;

        if (command instanceof DefaultInternalCommand && !u.getId().equals(Bot.owner_id))
            return COMMAND_NO_PERMISSION;
        else if (command instanceof DefaultInternalCommand && u.getId().equals(Bot.owner_id))
            return SUCCESS;

        //If we want we could add some private channel only commands here

        if (isPrivateMessage)
            return SUPPRESSED_FAIL;

        TextChannel channel = ((TextChannel) c);
        Member author = UserUtils.userToMember(channel.getGuild(), u);

        if (command instanceof DefaultAdminCommand && !author.hasPermission(Permission.ADMINISTRATOR))
            return COMMAND_NO_PERMISSION;

        if (!author.hasPermission(command.getUserPermissions()))
            return COMMAND_NO_PERMISSION;

        if (!channel.getGuild().getSelfMember().hasPermission(command.getBotPermissions()))
            return BOT_NO_PERMISSION;

        if (command instanceof DefaultMusicCommand defaultMusicCommand) {
            if (u.getIdLong() == 527593263067168770L)
                return BLOCKED_USER;

            MusicManager manager = AudioPlayerManager.getInstance().getMusicManager(channel.getGuild());

            if (manager.getScheduler().getChannel() != null && !manager.getScheduler().isSameChannel(channel))
                return SUPPRESSED_FAIL;

            GuildVoiceState botState = channel.getGuild().getSelfMember().getVoiceState();
            GuildVoiceState userState = author.getVoiceState();

            if (defaultMusicCommand.needConnectedVoice()) {
                if (botState == null || userState == null)
                    return CANNOT_DETECT_STATE;

                if (!botState.inAudioChannel())
                    return BOT_NOT_CONNECTED;

                if (!userState.inAudioChannel())
                    return USER_NOT_CONNECTED;
            }

            if (defaultMusicCommand.sameVoice()) {
                if (botState == null || userState == null)
                    return CANNOT_DETECT_STATE;

                if (!botState.inAudioChannel())
                    return BOT_NOT_CONNECTED;

                if (!userState.inAudioChannel())
                    return USER_NOT_CONNECTED;

                if (botState.getChannel() != userState.getChannel())
                    return IN_DIFFERENT_CHANNEL;
            }

            if (defaultMusicCommand.activePlayer() && !manager.getScheduler().trackPlaying())
                return NOT_PLAYING;

            if (defaultMusicCommand.activeQueue() && manager.getScheduler().getQueue().isEmpty())
                return EMPTY_QUEUE;
        }
        return SUCCESS;
    }

    private static CommandTypes isGuildCommand(TextChannel channel, String message) {
        return message.startsWith(Settings.getPrefix(channel))
                || message.startsWith("<@" + channel.getJDA().getSelfUser().getId() + ">")
                || message.startsWith("<@!" + channel.getJDA().getSelfUser().getId() + ">")
                ? CommandTypes.GUILD : CommandTypes.INVALID;
    }

    private static CommandTypes isPrivateCommand(PrivateChannel channel, String message) {
        return message.startsWith(Bot.prefix)
                || message.startsWith("<@" + channel.getJDA().getSelfUser().getId() + ">")
                ? CommandTypes.PRIVATE : CommandTypes.INVALID;
    }

    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }
}
