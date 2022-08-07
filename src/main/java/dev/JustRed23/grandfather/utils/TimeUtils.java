package dev.JustRed23.grandfather.utils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    private TimeUtils() {}

    public static String millisToTime(long ms) {
        int days = (int) (ms / (60 * 60 * 24 * 1000));
        int hours = (int) (ms / (60 * 60 * 1000));
        int minutes = (int) (ms / (60 * 1000)) % 60;
        int seconds = (int) (ms / 1000) % 60;

        String time = "";

        if (days > 0)
            time += String.format("%02d:", days);

        if (hours > 0)
            time += String.format("%02d:", hours);

        time += String.format("%02d:%02d", minutes, seconds);

        return time;
    }

    public static long timeToMillis(String time) {
        int days, hours, minutes = 0, seconds = 0;

        String[] t = time.split(":");
        long ms = 0;

        if (t.length == 4) {
            days = Integer.parseInt(t[0]);
            hours = Integer.parseInt(t[1]);
            minutes = Integer.parseInt(t[2]);
            seconds = Integer.parseInt(t[3]);

            if (days < 0)
                throw new IllegalArgumentException("Days must be more than -1");

            ms += TimeUnit.DAYS.toMillis(days);

            if (hours < 0 || hours > 24)
                throw new IllegalArgumentException("Hours must be more than -1 and less than 24");

            ms += TimeUnit.HOURS.toMillis(hours);
        } else if (t.length == 3) {
            hours = Integer.parseInt(t[0]);
            minutes = Integer.parseInt(t[1]);
            seconds = Integer.parseInt(t[2]);

            if (hours < 0 || hours > 24)
                throw new IllegalArgumentException("Hours must be more than -1 and less than 24");

            ms += TimeUnit.HOURS.toMillis(hours);
        } else if (t.length == 2) {
            minutes = Integer.parseInt(t[0]);
            seconds = Integer.parseInt(t[1]);
        }

        if (minutes < 0 || minutes > 60)
            throw new IllegalArgumentException("Minutes must be more than -1 and less than 60");

        if (seconds < 0 || seconds > 60)
            throw new IllegalArgumentException("Seconds must be more than -1 and less than 60");

        ms += TimeUnit.MINUTES.toMillis(minutes);
        ms += TimeUnit.SECONDS.toMillis(seconds);

        return ms;
    }

    public static String youtubeTime(String ytTime) {
        return millisToTime(Duration.parse(ytTime).toMillis());
    }
}
