package dev.JustRed23.grandfather.command;

import dev.JustRed23.grandfather.Bot;
import dev.JustRed23.grandfather.stats.SongsPerGuild;
import dev.JustRed23.grandfather.utils.TimeUtils;
import dev.JustRed23.jdautils.JDAUtilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import java.util.List;

public class GeneralCommands {

    public static void register() {
        JDAUtilities.createSlashCommand("stats", "Get the bot's statistics")
                .addSubCommand("general", "General statistics, such as uptime and memory usage")
                    .executes(GeneralCommands::generalStats)
                    .build()
                .addSubCommand("music", "Music statistics, such as the total amount of songs played and top songs/users")
                    .executes(GeneralCommands::musicStats)
                    .build()
                .setGuildOnly()
                .buildAndRegister();
    }

    private static EmbedBuilder getStatBuilder(SlashCommandInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(event.getGuild().getSelfMember().getColor());
        builder.setFooter("Requested by " + event.getUser().getEffectiveName(), event.getUser().getEffectiveAvatarUrl());
        return builder;
    }

    private static void generalStats(SlashCommandInteractionEvent event) {
        event.deferReply().queue(hook -> {
            EmbedBuilder builder = getStatBuilder(event);
            builder.setTitle("Bot Statistics");
            builder.setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

            //First row
            long uptimeMs = System.currentTimeMillis() - Bot.START_TIME;
            builder.addField("Uptime", TimeUtils.msToFormatted(uptimeMs, TimeUtils.TimeFormat.FULL), true);

            double div = 1_048_576d;
            double maxMemory = Runtime.getRuntime().totalMemory() / div;
            double currentMemory = maxMemory - (Runtime.getRuntime().freeMemory() / div);
            builder.addField("Memory Usage", String.format("%.2f MB / %.2f MB", currentMemory, maxMemory), true);

            builder.addBlankField(true); //evenly spaced, like all things should be

            //Second row
            boolean sharded = event.getJDA().getShardInfo().getShardTotal() > 1;
            if (sharded) builder.addField("Shard", event.getJDA().getShardInfo().getShardString(), false);

            long totalGuilds = event.getJDA().getGuildCache().size();
            long totalUsers = event.getJDA().getUserCache().size();
            builder.addField("Cached Guilds", String.valueOf(totalGuilds), true);
            builder.addField("Cached Users", String.valueOf(totalUsers), true);

            builder.addBlankField(true); //evenly spaced, like all things should be

            //Send it
            hook.editOriginalEmbeds(builder.build()).queue();
        });
    }

    private static void musicStats(SlashCommandInteractionEvent event) {
        if (SongsPerGuild.has(event.getGuild().getIdLong()))
            event.deferReply().queue(hook -> {
                EmbedBuilder builder = getStatBuilder(event);
                builder.setTitle("Music Statistics");

                SongsPerGuild stats = SongsPerGuild.get(event.getGuild().getIdLong());
                builder.addField("Songs played", String.valueOf(stats.getSongsPlayed()), true);
                builder.addField("Songs skipped", String.valueOf(stats.getSongsSkipped()), true);
                builder.addBlankField(true); //evenly spaced, like all things should be

                //Top 5 songs
                final List<SongsPerGuild.SongStat> songs = stats.getTopSongs(5);

                StringBuilder topSongs = new StringBuilder();
                for (int i = 0; i < songs.size(); i++) {
                    final SongsPerGuild.SongStat song = songs.get(i);
                    topSongs.append("**").append(i + 1).append("**. ")
                            .append(MarkdownSanitizer.escape(song.title()))
                            .append("\n")
                            .append("`played ").append(song.plays()).append(song.plays() == 1 ? " time" : " times").append("`")
                            .append("\n\n");
                }

                builder.addField("Top Songs", topSongs.toString(), true);

                //Top 5 users
                final List<SongsPerGuild.UserStat> users = stats.getTopUsers(5);

                StringBuilder topUsers = new StringBuilder();
                for (int i = 0; i < users.size(); i++) {
                    final SongsPerGuild.UserStat user = users.get(i);
                    int totalSongsPlayed = user.songsPlayed().size();
                    topUsers.append("**").append(i + 1).append("**. ")
                            .append(event.getGuild().retrieveMemberById(user.userID()).complete().getEffectiveName())
                            .append("\n")
                            .append("`").append(totalSongsPlayed).append(totalSongsPlayed == 1 ? " song" : " songs").append(" played`")
                            .append("\n\n");
                }

                builder.addField("Top Users", topUsers.toString(), true);

                builder.addBlankField(true); //evenly spaced, like all things should be

                //Send it
                hook.editOriginalEmbeds(builder.build()).queue();
            });
        else event.reply("No music statistics available for this guild!").setEphemeral(true).queue();
    }
}
