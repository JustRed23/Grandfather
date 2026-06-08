package dev.JustRed23.grandfather.command;

import dev.JustRed23.grandfather.App;
import dev.JustRed23.grandfather.Bot;
import dev.JustRed23.jdautils.JDAUtilities;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

import java.util.function.Function;

public class AdminCommands {

    private static final Function<SlashCommandInteractionEvent, Boolean> REQUIRE_OWNER = event -> {
        if (event.getMember() == null || !(event.getMember().getIdLong() == Bot.owner_id)) {
            event.reply("You need to be the bot owner to use this command!").setEphemeral(true).queue();
            return false;
        }
        return true;
    };

    private static final Function<SlashCommandInteractionEvent, Boolean> REQUIRE_ADMIN = event -> {
        if (event.getMember() == null || !event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("You need to be an administrator to use this command!").setEphemeral(true).queue();
            return false;
        }
        return true;
    };

    public static void register() {
        JDAUtilities.createSlashCommand("refreshnodes", "Refreshes lavalink nodes")
                .addCondition(REQUIRE_OWNER)
                .executes(event -> {
                    event.deferReply(true).queue();
                    App.refreshNodes();
                    event.getHook().sendMessage("Lavalink nodes refreshed!").queue();
                })
                .modifyData(data -> data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)))
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("shutdown", "Shut down the bot")
                .addCondition(REQUIRE_OWNER)
                .executes(event -> {
                    event.reply("Shutting down...").setEphemeral(true).complete();
                    App.exit();
                })
                .modifyData(data -> data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)))
                .setGuildOnly()
                .buildAndRegister();
    }
}
