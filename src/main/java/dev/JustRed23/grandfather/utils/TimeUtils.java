package dev.JustRed23.grandfather.utils;

public final class TimeUtils {

    public enum TimeFormat {
        FULL, CLOCK, SHORT
    }

    public static String msToFormatted(long ms, TimeFormat format) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        hours %= 24;
        minutes %= 60;
        seconds %= 60;

        return switch (format) {
            case FULL -> days + " days, " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds";
            case CLOCK ->
                    days > 0 ? String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds) : String.format("%02d:%02d:%02d", hours, minutes, seconds);
            case SHORT -> days + "d " + hours + "h";
        };
    }
}
