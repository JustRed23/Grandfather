package dev.JustRed23.grandfather.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserUtils {

    private static final Pattern mentionUserPattern = Pattern.compile("<@!?([0-9]{8,})>");
    private static final Pattern aMention = Pattern.compile("<[@#][&!]?([0-9]{4,})>");

    private UserUtils() {}

    @Nullable
    public static User getUserFromMention(@NotNull String mention, @NotNull JDA jda) {
        if (isUserMention(mention))
            return jda.retrieveUserById(mentionToId(mention)).complete();

        return null;
    }

    @Nullable
    public static Member getMemberFromMention(@NotNull String mention, @NotNull Guild guild, @NotNull JDA jda) {
        User user = getUserFromMention(mention, jda);
        return user != null ? userToMember(guild, user) : null;
    }

    public static boolean isUserMention(String input) {
        return mentionUserPattern.matcher(input).find();
    }

    public static String mentionToId(String mention) {
        String id = "";
        Matcher matcher = aMention.matcher(mention);

        if (matcher.find())
            id = matcher.group(1);

        return id;
    }

    public static Member userToMember(Guild guild, User user) {
        return guild.getMember(user);
    }
}
