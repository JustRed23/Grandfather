package dev.JustRed23.grandfather.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiUtils {

    private static final Pattern patternGuildEmote = Pattern.compile("<:.*:(\\d+)>");

    public static boolean isGuildEmote(String emote) {
        return patternGuildEmote.matcher(emote).matches();
    }

    public static String getGuildEmoteId(String emote) {
        Matcher matcher = patternGuildEmote.matcher(emote);

        if (matcher.find())
            return matcher.group(1);

        if (emote.matches("^\\d+$"))
            return emote;
        return null;
    }

    public static String makeProgressbar(long max, long current) {
        int parts = 10;
        StringBuilder bar = new StringBuilder();
        int activeBLock = Math.min(parts - 1, (int) ((float) current / (float) max * parts));
        for (int i = 0; i < parts; i++)
            bar.append(i == activeBLock ? ":white_circle:" : "\u25AC");
        return bar.toString();
    }

    public static String getNumberReaction(int number) {
        if (number < 0 || number > 9)
            throw new IllegalArgumentException("Number must be between 0 and 9");

        return number + "\uFE0F\u20E3";
    }

    public class General {
        public static final String YES = "\u2705",
        NO = "\u274C",
        INFO = "\u2139",
        WARNING = "\u26A0",
        ERROR = "\uD83D\uDEAB";
    }

    public class Games {
        public static final String DICE = "\uD83C\uDFB2";
    }

    public class Music {
        public static final String PAUSE = "\u23F8\uFE0F",
        PLAY = " \u25B6\uFE0F",
        STOP = "\u23F9\uFE0F",
        REPEAT = "\uD83D\uDD01",
        EJECT = "\u23CF\uFE0F",
        PREV_TRACK = "\u23EE",
        NEXT_TRACK = "\u23ED",
        MUSIC_NOTE = "\uD83C\uDFB5",
        SPEAKER = "\uD83D\uDD0A",
        LIVE = "\uD83D\uDD34";
    }

    public class Misc {
        public static final String WAVE_GOODBYE = "\uD83D\uDC4B",
        MAGNIFYING_GLASS = "\uD83D\uDD0E",
        NEXT = "\u23E9",
        PREVIOUS = "\u23EA",
        NOTEPAD = "\uD83D\uDDD2",
        CALENDAR = "\uD83D\uDDD3";
    }
}
