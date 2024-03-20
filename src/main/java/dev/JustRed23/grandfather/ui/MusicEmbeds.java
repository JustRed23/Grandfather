package dev.JustRed23.grandfather.ui;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.JustRed23.grandfather.utils.TimeUtils;
import dev.JustRed23.jdautils.music.TrackInfo;
import dev.JustRed23.jdautils.music.search.YouTubeSource;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.util.List;

import static net.dv8tion.jda.api.utils.MarkdownSanitizer.escape;

public final class MusicEmbeds {

    public static EmbedBuilder createDefault() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(0x9b59b6);
        builder.setTimestamp(Instant.now());
        return builder;
    }

    public static EmbedBuilder onPlay(TrackInfo trackInfo, long durationMs, boolean addedToQueue, int positionInQueue) {
        EmbedBuilder builder = createDefault();
        builder.setAuthor(addedToQueue ? "Added to queue" : "Now playing");

        final AudioTrackInfo info = trackInfo.track().getInfo();
        builder.setTitle(escape(info.title), info.uri);
        builder.setThumbnail(YouTubeSource.getThumbnail(info.identifier));

        if (addedToQueue) {
            builder.addField("Author", escape(info.author), true);
            builder.addField("Duration", info.isStream ? "LIVE" : TimeUtils.msToFormatted(durationMs, TimeUtils.TimeFormat.CLOCK), true);
            builder.addField("Position in queue", String.valueOf(positionInQueue), true);
        }

        final Object user = trackInfo.track().getUserData();
        if (user instanceof User requester)
            builder.setFooter("Requested by " + requester.getEffectiveName(), requester.getEffectiveAvatarUrl());

        return builder;
    }

    public static EmbedBuilder onPlaylistAdded(List<TrackInfo> tracks, long totalDurationMs, AudioPlaylist playlist, int positionInQueue) {
        EmbedBuilder builder = createDefault();
        builder.setAuthor("Added playlist to queue");
        builder.setTitle(escape(playlist.getName()));

        if (playlist.getSelectedTrack() != null)
            builder.setThumbnail(YouTubeSource.getThumbnail(playlist.getSelectedTrack().getInfo().identifier));
        else
            builder.setThumbnail(YouTubeSource.getThumbnail(tracks.get(0).track().getInfo().identifier));

        builder.addField("Tracks", String.valueOf(tracks.size()), true);
        builder.addField("Total duration", TimeUtils.msToFormatted(totalDurationMs, TimeUtils.TimeFormat.CLOCK), true);
        builder.addField("Position in queue", positionInQueue > 0 ? String.valueOf(positionInQueue) : "Now", true);

        final Object user = tracks.get(0).track().getUserData();
        if (user instanceof User requester)
            builder.setFooter("Requested by " + requester.getEffectiveName(), requester.getEffectiveAvatarUrl());

        return builder;
    }

    private static EmbedBuilder trackInfo(AudioTrack track, String action) {
        EmbedBuilder builder = createDefault();
        builder.setAuthor(action);

        final AudioTrackInfo info = track.getInfo();
        builder.setTitle(escape(info.title), info.uri);
        builder.setThumbnail(YouTubeSource.getThumbnail(info.identifier));

        return builder;
    }

    public static EmbedBuilder onSkip(AudioTrack track) {
        return trackInfo(track, "Skipped track, now playing");
    }

    public static EmbedBuilder onPrev(AudioTrack track) {
        return trackInfo(track, "Playing previous track");
    }

    public static EmbedBuilder onNowPlaying(AudioTrack track) {
        return trackInfo(track, "Now playing");
    }
}
