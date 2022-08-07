package dev.JustRed23.grandfather.utils;

import dev.JustRed23.grandfather.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

public class Settings {

    @NotNull
    public static String getPrefix(@NotNull TextChannel channel) {
        return getPrefix(channel.getGuild().getIdLong());
    }

    @NotNull
    public static String getPrefix(@NotNull Guild guild) {
        return getPrefix(guild.getIdLong());
    }

    @NotNull
    public static String getPrefix(long guildId) {
        return Bot.prefix; //TODO: connect to database
    }
}
