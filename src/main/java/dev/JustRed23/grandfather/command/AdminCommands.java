package dev.JustRed23.grandfather.command;

import dev.JustRed23.grandfather.App;
import dev.JustRed23.grandfather.Bot;
import dev.JustRed23.jdautils.JDAUtilities;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.protocol.v4.Info;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

import java.awt.*;
import java.util.List;
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
        JDAUtilities.createSlashCommand("nodes", "Refreshes lavalink nodes")
                .addCondition(REQUIRE_OWNER)
                .addSubCommand("refresh", "Refreshes lavalink nodes")
                    .executes(event -> {
                        event.deferReply(true).queue();
                        App.refreshNodes();
                        event.getHook().sendMessage("Lavalink nodes refreshed!").queue();
                    })
                    .build()
                .addSubCommand("list", "Lists all currently active nodes")
                    .executes(event -> {
                        event.deferReply(true).queue();
                        List<LavalinkNode> nodes = App.getNodes();
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setTitle("Lavalink nodes");
                        eb.setColor(Color.GREEN);

                        nodes.forEach(node -> {
                            boolean available = node.getAvailable();
                            StringBuilder sb = new StringBuilder();
                            sb.append("**Address:** ").append(node.getBaseUri()).append("\n");
                            sb.append("**Active:** ").append(available ? "✅ Yes" : "❌ No").append("\n");

                            if (available) {
                                Info info = node.getNodeInfo().block();
                                sb.append("**Version:** ").append(info != null ? info.getVersion().getSemver() : "Unknown");
                            }

                            eb.addField(node.getName(), sb.toString(), false);
                        });

                        event.getHook().sendMessageEmbeds(eb.build()).queue();
                    })
                    .build()
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
