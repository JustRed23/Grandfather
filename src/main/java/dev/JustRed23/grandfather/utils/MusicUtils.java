package dev.JustRed23.grandfather.utils;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.JustRed23.grandfather.music.MusicManager;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class MusicUtils {

    private MusicUtils() {}

    public static EmbedBuilder displayMusicInfo(MusicManager guildAudioPlayer) {
        AudioTrack track = guildAudioPlayer.getScheduler().getPlayingTrack();
        AudioTrackInfo info = track.getInfo();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.CYAN);
        builder.setAuthor("Now playing");
        builder.setThumbnail(YoutubeUtils.getThumbnail(YoutubeUtils.getVideoID(info.uri)));

        builder.setTitle(info.title, info.uri);

        return builder;
    }

    public static EmbedBuilder displayMusicInfoWithTimestamp(MusicManager guildAudioPlayer) {
        AudioTrack track = guildAudioPlayer.getScheduler().getPlayingTrack();
        AudioTrackInfo info = track.getInfo();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.CYAN);
        builder.setAuthor("Now playing");
        builder.setThumbnail(YoutubeUtils.getThumbnail(YoutubeUtils.getVideoID(info.uri)));

        long timeStamp = TimeUnit.MILLISECONDS.toSeconds(track.getPosition());

        String uriWithTimestamp = info.uri + "&t=" + timeStamp;

        builder.setTitle(info.title, uriWithTimestamp);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append("`")
                .append(TimeUtils.millisToTime(track.getPosition()))
                .append("/")
                .append(TimeUtils.millisToTime(track.getDuration()))
                .append("`")
                .append(guildAudioPlayer.getScheduler().isPaused() ? " " + EmojiUtils.Music.PAUSE : "")
                .append(guildAudioPlayer.getScheduler().trackLooping() ? " " + EmojiUtils.Music.REPEAT : "")
                .append("\n\n`")
                .append("Requested by:` ")
                .append(track.getUserData());

        builder.addField(EmojiUtils.makeProgressbar(TimeUnit.MILLISECONDS.toSeconds(track.getDuration()), TimeUnit.MILLISECONDS.toSeconds(track.getPosition())), stringBuilder.toString(), false);

        return builder;
    }
}
