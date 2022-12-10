package dev.JustRed23.grandfather.utils;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.JustRed23.grandfather.music.MusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MusicUtils {

    private static final Map<Long, Integer> pages = new HashMap<>();

    private MusicUtils() {}

    public static EmbedBuilder displayMusicInfo(MusicManager guildAudioPlayer) {
        AudioTrack track = guildAudioPlayer.getScheduler().getPlayingTrack();
        AudioTrackInfo info = track.getInfo();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.CYAN);
        builder.setAuthor("Now playing");
        builder.setThumbnail(YoutubeUtils.getThumbnail(YoutubeUtils.getVideoID(info.uri)));

        User userData = track.getUserData(User.class);
        if (userData != null)
            builder.setFooter("Requested by " + userData.getAsTag(), userData.getEffectiveAvatarUrl());

        builder.setTitle(MarkdownSanitizer.escape(info.title, true), info.uri);

        return builder;
    }

    public static EmbedBuilder displayMusicInfoWithTimestamp(MusicManager guildAudioPlayer) {
        AudioTrack track = guildAudioPlayer.getScheduler().getPlayingTrack();
        AudioTrackInfo info = track.getInfo();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.CYAN);
        builder.setAuthor("Now playing");
        builder.setThumbnail(YoutubeUtils.getThumbnail(YoutubeUtils.getVideoID(info.uri)));

        User userData = track.getUserData(User.class);
        if (userData != null)
            builder.setFooter("Requested by " + userData.getAsTag(), userData.getEffectiveAvatarUrl());

        long timeStamp = TimeUnit.MILLISECONDS.toSeconds(track.getPosition());

        String uriWithTimestamp = info.uri + "&t=" + timeStamp;

        builder.setTitle(MarkdownSanitizer.escape(info.title, true), uriWithTimestamp);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append("`")
                .append(TimeUtils.millisToTime(track.getPosition()))
                .append("/")
                .append(TimeUtils.millisToTime(track.getDuration()))
                .append("`")
                .append(guildAudioPlayer.getScheduler().isPaused() ? " " + EmojiUtils.Music.PAUSE : "")
                .append(guildAudioPlayer.getScheduler().trackLooping() ? " " + EmojiUtils.Music.REPEAT : "");

        builder.addField(EmojiUtils.makeProgressbar(TimeUnit.MILLISECONDS.toSeconds(track.getDuration()), TimeUnit.MILLISECONDS.toSeconds(track.getPosition())), stringBuilder.toString(), false);

        return builder;
    }

    public static EmbedBuilder buildQueue(MusicManager guildMusicManager, int page) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.CYAN);
        builder.setAuthor("Queue");

        if (page == 1) {
            AudioTrack curTrack = guildMusicManager.getScheduler().getPlayingTrack();
            AudioTrackInfo curInfo = curTrack.getInfo();
            builder.setThumbnail(YoutubeUtils.getThumbnail(YoutubeUtils.getVideoID(curInfo.uri)));

            boolean paused = guildMusicManager.getScheduler().isPaused();
            boolean looping = guildMusicManager.getScheduler().trackLooping();

            String str = HttpUtils.linkText(MarkdownSanitizer.escape(curInfo.title, true), curInfo.uri) +
                    (paused ? " " + EmojiUtils.Music.PAUSE : "") +
                    (looping ? " " + EmojiUtils.Music.REPEAT : "");

            try {
                if (YoutubeUtils.isLive(YoutubeUtils.getVideoID(curInfo.uri)))
                    str += System.lineSeparator() + "`" + EmojiUtils.Music.LIVE + "Live" + "`";
                else
                    str += System.lineSeparator() + "`" + TimeUtils.millisToTime(curTrack.getPosition()) + "/" + TimeUtils.millisToTime(curTrack.getDuration()) + "`";
            } catch (IOException e) {
                str += System.lineSeparator() + "`Unknown`";
            }

            builder.addField("Now playing", str, false);
            builder.addField("", "**Next up**", false);

            List<AudioTrack> q = guildMusicManager.getScheduler().getQueue();

            for (int i = 0; i < 4; i++) {
                if (i >= q.size())
                    break;

                AudioTrack audioTrack = q.get(i);
                AudioTrackInfo trackInfo = audioTrack.getInfo();

                String info = "**" + (i + 1) + ".** " + HttpUtils.linkText(MarkdownSanitizer.escape(trackInfo.title, true), trackInfo.uri);
                String time = "`(" + TimeUtils.millisToTime(audioTrack.getDuration()) + ")`";

                builder.addField("", info + System.lineSeparator() + time,false);
            }
        } else {
            int track = (page - 1) * 5;
            List<AudioTrack> q = guildMusicManager.getScheduler().getQueue();

            for (int i = 0; i < 5; i++) {
                if (track - 1 >= q.size())
                    break;

                AudioTrack audioTrack = q.get(track - 1);
                AudioTrackInfo trackInfo = audioTrack.getInfo();

                String info = "**" + track + ".** " + HttpUtils.linkText(MarkdownSanitizer.escape(trackInfo.title, true), trackInfo.uri);
                String time = "`(" + TimeUtils.millisToTime(audioTrack.getDuration()) + ")`";

                builder.addField("", info + System.lineSeparator() + time,false);
                track++;
            }
        }

        int size = guildMusicManager.getScheduler().getQueue().size() - 4; //-4 for first page
        int totalPages = 1 + (int) Math.ceil(size / 5.0);

        builder.setFooter("Page " + page + " of " + totalPages);

        return builder;
    }

    public static int getPage(long msgID) {
        if (pages.containsKey(msgID))
            return pages.get(msgID);

        pages.put(msgID, 1);
        return 1;
    }

    public static int nextPage(long msgID) {
        int page = getPage(msgID);
        page++;
        pages.replace(msgID, page);
        return page;
    }

    public static int prevPage(long msgID) {
        int page = getPage(msgID);
        page--;
        pages.replace(msgID, page);
        return page;
    }

    public static void invalidatePage(long msgID) {
        pages.remove(msgID);
    }
}
