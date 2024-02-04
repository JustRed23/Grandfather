package dev.JustRed23.grandfather.command;

import dev.JustRed23.grandfather.ex.ErrorHandler;
import dev.JustRed23.jdautils.JDAUtilities;
import dev.JustRed23.jdautils.command.CommandOption;
import dev.JustRed23.jdautils.settings.ConfigReturnValue;
import dev.JustRed23.jdautils.settings.GuildSettingManager;
import dev.JustRed23.jdautils.settings.Setting;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.function.Function;

public class AdminCommands {

    private static final Function<SlashCommandInteractionEvent, Boolean> REQUIRE_ADMIN = event -> {
        if (event.getMember() == null || !event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("You need to be an administrator to use this command!").setEphemeral(true).queue();
            return false;
        }
        return true;
    };

    public static void register() {
        JDAUtilities.createSlashCommand("musicban", "Ban a user from using music commands")
                .addCondition(REQUIRE_ADMIN)
                .addOption(new CommandOption(OptionType.USER, "user", "The user to ban", true))
                .executes(event -> {
                    final User user = event.getOption("user").getAsUser();
                    final long userID = user.getIdLong();

                    final GuildSettingManager manager = JDAUtilities.getGuildSettingManager();
                    final Setting currentlyBanned = manager.getOrDefault(event.getGuild().getIdLong(), "music_banned_users", "");

                    if (currentlyBanned.stringValue().contains(String.valueOf(userID))) {
                        event.reply("This user is already banned from using music commands!").setEphemeral(true).queue();
                        return;
                    }

                    final ConfigReturnValue status = manager.set(event.getGuild().getIdLong(), "music_banned_users", currentlyBanned.stringValue() + userID + ",");
                    if (status == ConfigReturnValue.ERROR) {
                        event.reply("An error occurred while trying to ban the user!").setEphemeral(true).queue();
                        ErrorHandler.handleException("musicban", status.getException());
                        return;
                    }

                    event.reply(user.getEffectiveName() + " has been banned from using music commands!").setEphemeral(true).queue();
                    MusicCommands.clearKnownBannedUsers();
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("musicunban", "Unban a user from using music commands")
                .addCondition(REQUIRE_ADMIN)
                .addOption(new CommandOption(OptionType.USER, "user", "The user to unban", true))
                .executes(event -> {
                    final User user = event.getOption("user").getAsUser();
                    final long userID = user.getIdLong();

                    final GuildSettingManager manager = JDAUtilities.getGuildSettingManager();
                    final Setting currentlyBanned = manager.getOrDefault(event.getGuild().getIdLong(), "music_banned_users", "");

                    if (!currentlyBanned.stringValue().contains(String.valueOf(userID))) {
                        event.reply("This user is not banned from using music commands!").setEphemeral(true).queue();
                        return;
                    }

                    final ConfigReturnValue status = manager.set(event.getGuild().getIdLong(), "music_banned_users", currentlyBanned.stringValue().replace(userID + ",", ""));
                    if (status == ConfigReturnValue.ERROR) {
                        event.reply("An error occurred while trying to unban the user!").setEphemeral(true).queue();
                        ErrorHandler.handleException("musicunban", status.getException());
                        return;
                    }

                    event.reply(user.getEffectiveName() + " has been unbanned from using music commands!").setEphemeral(true).queue();
                    MusicCommands.clearKnownBannedUsers();
                })
                .setGuildOnly()
                .buildAndRegister();
    }
}
