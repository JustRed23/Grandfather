package dev.JustRed23.grandfather.ui;

import dev.JustRed23.jdautils.music.GuildMusicManager;
import dev.JustRed23.jdautils.music.PlayableTrack;
import dev.JustRed23.jdautils.music.event.QueueUpdateEvent;
import dev.JustRed23.jdautils.utils.TimeUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.JustRed23.jdautils.utils.TimeUtils.millisToTime;
import static net.dv8tion.jda.api.utils.MarkdownSanitizer.escape;
import static net.dv8tion.jda.api.utils.MarkdownSanitizer.sanitize;

public final class MusicEmbeds {

    public static EmbedBuilder createDefault() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(0x9b59b6);
        return builder;
    }

    public static EmbedBuilder onNowPlaying(GuildMusicManager manager) {
        EmbedBuilder builder = createDefault();
        PlayableTrack track = manager.getCurrentTrack().orElse(null);

        if (track == null) {
            builder.setAuthor("No track is currently playing");
            builder.setDescription("Use the play command to start playing music.");
            return builder;
        }

        builder.setAuthor("Now Playing 🎵");
        builder.setTitle(escape(track.getDisplayName()), track.url());
        builder.setThumbnail(track.thumbnailUrl());

        long duration = track.durationMillis();
        if (duration == 0) {
            builder.addField("Duration", "🔴 LIVE", false);
        } else if (duration == -1) {
            builder.addField("Duration", "Unknown", false);
        } else {
            long position = manager.getTrackPosition();
            builder.addField("Duration", millisToTime(position) + " / " + millisToTime(duration), false);
            builder.addField("Progress", buildProgressBar(position, duration), false);
        }

        Member requester = track.member();
        builder.setFooter("Requested by " + requester.getEffectiveName(), requester.getEffectiveAvatarUrl());
        return builder;
    }

    private static String buildProgressBar(long position, long duration) {
        int length = 10;
        int filled = (int) ((position / (double) duration) * length);
        filled = Math.clamp(filled, 0, length - 1);

        String before = "▬".repeat(filled);
        String after = "╌".repeat(Math.max(0, length - filled - 1));
        return before + "🔘" + after;
    }

    public static EmbedBuilder onStart(PlayableTrack track) {
        Member requester = track.member();
        EmbedBuilder builder = createDefault();
        builder.setAuthor("▶️ Started Playing");
        builder.setTitle(escape(track.getDisplayName()), track.url());
        builder.setThumbnail(track.thumbnailUrl());

        long duration = track.durationMillis();
        if (duration == 0)
            builder.addField("Duration", "🔴 LIVE", true);
        else if (duration != -1)
            builder.addField("Duration", millisToTime(duration), true);

        builder.setFooter("Requested by " + requester.getEffectiveName(), requester.getEffectiveAvatarUrl());
        return builder;
    }

    public static EmbedBuilder onError(@Nullable PlayableTrack track) {
        EmbedBuilder builder = createDefault();
        builder.setColor(0xe74c3c);
        builder.setAuthor("⚠️ Playback Error");
        builder.setDescription("An error occurred while trying to play this track. Please try again later.");
        if (track != null) {
            Member requester = track.member();
            builder.setTitle(escape(track.getDisplayName()), track.url());
            builder.setThumbnail(track.thumbnailUrl());
            builder.setFooter("Requested by " + requester.getEffectiveName(), requester.getEffectiveAvatarUrl());
        } else {
            builder.setFooter("No track information available.");
        }
        return builder;
    }

    public static EmbedBuilder onQueueUpdate(@NotNull QueueUpdateEvent event, GuildMusicManager manager) {
        EmbedBuilder builder = createDefault();
        switch (event.type()) {
            case ADDED -> {
                assert event.affectedTracks() != null;
                PlayableTrack track = event.affectedTracks().getFirst();
                builder.setTitle("Track Added to Queue");
                builder.setDescription("[" + sanitize(track.getDisplayName()) + "](" + track.url() + ")");
                builder.addField("Position in Queue", "#" + (event.index() + 1), true);
                builder.addField("Duration", millisToTime(track.durationMillis()), true);
                builder.setThumbnail(track.thumbnailUrl());
                builder.setFooter("Requested by " + track.member().getEffectiveName(), track.member().getEffectiveAvatarUrl());
            }
            case ADDED_PLAYLIST -> {
                assert event.affectedTracks() != null;
                int count = event.affectedTracks().size();
                builder.setTitle("Playlist Added to Queue");
                builder.setDescription("Added **" + count + " tracks** to the queue.");
                builder.addField("In queue at Position", "#" + (event.index() + 1), true);
                builder.setThumbnail(event.affectedTracks().getFirst().thumbnailUrl());

                Member requester = event.affectedTracks().getFirst().member();
                builder.setFooter("Requested by " + requester.getEffectiveName(), requester.getEffectiveAvatarUrl());
            }
            case REMOVED -> {
                assert event.affectedTracks() != null;
                PlayableTrack track = event.affectedTracks().getFirst();
                builder.setTitle("Track Removed from Queue");
                builder.setDescription("[" + sanitize(track.getDisplayName()) + "](" + track.url() + ")");
                builder.addField("Was at Position", "#" + (event.index() + 1), true);
            }
            case MOVED -> {
                assert event.affectedTracks() != null;
                PlayableTrack track = event.affectedTracks().getFirst();
                builder.setTitle("Track Moved in Queue");
                builder.setDescription("[" + sanitize(track.getDisplayName()) + "](" + track.url() + ")");
                builder.addField("New Position", "#" + (event.index() + 1), true);
            }
            case SHUFFLED -> {
                builder.setTitle("Queue Shuffled");
                builder.setDescription("The queue has been randomized.");
                builder.addField("Tracks in Queue", String.valueOf(manager.queue().getQueue().size()), true);
            }
            case CLEARED -> {
                builder.setTitle("Queue Cleared");
                builder.setDescription("All tracks have been removed from the queue.");
            }
            default -> throw new IllegalStateException("Unknown event type: " + event.type());
        }
        return builder;
    }

    public static EmbedBuilder onNotFound(String url) {
        EmbedBuilder builder = createDefault();
        builder.setColor(0xe74c3c);
        builder.setAuthor("❌ Track Not Found");
        builder.setDescription("No results found for:\n```\n" + escape(url).replace("@", "") + "\n```");
        builder.setFooter("Try a different URL or search term.");
        return builder;
    }

    public static EmbedBuilder onVolumeChange(float oldVolume, float newVolume) {
        EmbedBuilder builder = createDefault();
        String direction = newVolume > oldVolume ? "🔊 Volume Increased" : newVolume < oldVolume ? "🔉 Volume Decreased" : "🔈 Volume Unchanged";
        builder.setTitle(direction);
        builder.addField("Previous", (int) oldVolume + "%", true);
        builder.addField("Current", (int) newVolume + "%", true);
        return builder;
    }
}
