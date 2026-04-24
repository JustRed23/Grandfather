package dev.JustRed23.grandfather.ui;

import dev.JustRed23.jdautils.music.GuildMusicManager;
import dev.JustRed23.jdautils.music.PlayableTrack;
import dev.JustRed23.jdautils.music.event.QueueUpdateEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.dv8tion.jda.api.utils.MarkdownSanitizer.escape;

public final class MusicEmbeds {

    public static EmbedBuilder createDefault() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(0x9b59b6);
        return builder;
    }

    /*
    public static EmbedBuilder onPlay(PlayableTrack track, boolean addedToQueue, int positionInQueue, User requester) {
        EmbedBuilder builder = createDefault();
        builder.setAuthor(addedToQueue ? "Added to queue" : "Now playing");
        builder.setTitle(escape(track.getDisplayName()), track.url());
        builder.setThumbnail(track.thumbnailUrl());

        if (track.hasAuthor()) builder.addField("Author", escape(track.author()), true);
        builder.addField("Duration", track.isLiveStream() ? "LIVE" : TimeUtils.millisToTime(track.durationMillis()), true);
        builder.addField("Position in queue", String.valueOf(positionInQueue), true);

        builder.setFooter("Requested by " + requester.getEffectiveName(), requester.getEffectiveAvatarUrl());
        return builder;
    }
    */

    /*
    public static EmbedBuilder onPlaylistAdded(TrackList playlist, long positionInQueue, User requester) {
        EmbedBuilder builder = createDefault();
        builder.setAuthor("Added playlist to queue");
        builder.setTitle(escape(playlist.getName()));
        builder.setThumbnail(playlist.get(0).thumbnailUrl());

        builder.addField("Tracks", String.valueOf(playlist.size()), true);
        builder.addField("Total duration", TimeUtils.millisToTime(playlist.getTotalDuration()), true);
        if (positionInQueue > 0) builder.addField("Position in queue", String.valueOf(positionInQueue), true);

        builder.setFooter("Requested by " + requester.getEffectiveName(), requester.getEffectiveAvatarUrl());
        return builder;
    }
    */

    public static EmbedBuilder onNowPlaying(PlayableTrack track) {
        Member requester = track.member();
        EmbedBuilder builder = createDefault();
        builder.setAuthor("Now playing");
        builder.setTitle(escape(track.getDisplayName()), track.url());
        builder.setThumbnail(track.thumbnailUrl());

        builder.setFooter("Requested by " + requester.getEffectiveName(), requester.getEffectiveAvatarUrl());
        return builder;
    }

    public static EmbedBuilder onStart(PlayableTrack track) {
        Member requester = track.member();
        EmbedBuilder builder = createDefault();
        builder.setAuthor("Started playing");
        builder.setTitle(escape(track.getDisplayName()), track.url());
        builder.setThumbnail(track.thumbnailUrl());

        builder.setFooter("Requested by " + requester.getEffectiveName(), requester.getEffectiveAvatarUrl());
        return builder;
    }

    public static EmbedBuilder onError(@Nullable PlayableTrack track) {
        EmbedBuilder builder = createDefault();
        builder.setColor(0xe74c3c);
        builder.setAuthor("Track error");
        if (track != null) {
            Member requester = track.member();
            builder.setTitle(track.getDisplayName(), track.url());
            builder.setThumbnail(track.thumbnailUrl());
            builder.setFooter("Requested by " + requester.getEffectiveName(), requester.getEffectiveAvatarUrl());
        }

        builder.setDescription("An error occurred while trying to play the track.");
        return builder;
    }

    public static EmbedBuilder onQueueUpdate(@NotNull QueueUpdateEvent event, GuildMusicManager manager) {
        EmbedBuilder builder = createDefault();
        //TODO
        return builder;
    }

    public static EmbedBuilder onNotFound(String url) {
        EmbedBuilder builder = createDefault();
        builder.setAuthor("Track not found");
        builder.setDescription("Could not find a track for the provided URL: " + escape(url).replace("@", ""));
        return builder;
    }
}
