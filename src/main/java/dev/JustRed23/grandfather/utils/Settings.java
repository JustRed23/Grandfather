package dev.JustRed23.grandfather.utils;

import dev.JustRed23.grandfather.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class Settings {

    private static final List<String> sameAsTrue = Arrays.asList("yep", "yes", "yup", "y", "true");
    private static final List<String> sameAsFalse = Arrays.asList("nope", "no", "n", "false");

    public static boolean isSameAsTrue(String value) {
        return sameAsTrue.contains(value.toLowerCase());
    }

    public static boolean isSameAsFalse(String value) {
        return sameAsFalse.contains(value.toLowerCase());
    }

    @NotNull
    public static String getPrefix(@NotNull GuildMessageChannel channel) {
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
