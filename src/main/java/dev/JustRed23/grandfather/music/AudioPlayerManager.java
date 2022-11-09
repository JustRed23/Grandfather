package dev.JustRed23.grandfather.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.JustRed23.grandfather.bettertemplate.Templates;
import dev.JustRed23.grandfather.utils.TimeUtils;
import dev.JustRed23.grandfather.utils.YoutubeUtils;
import dev.JustRed23.grandfather.utils.msg.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AudioPlayerManager {

    private static AudioPlayerManager INSTANCE;

    private final com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager manager;
    private final Map<Long, MusicManager> musicManagers;

    private final Map<Long, Long> lastActive = new ConcurrentHashMap<>();

    public AudioPlayerManager() {
        this.musicManagers = new HashMap<>();
        this.manager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(manager);
        AudioSourceManagers.registerLocalSource(manager);
    }

    @NotNull
    public synchronized MusicManager getMusicManager(@NotNull Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), guildID -> {
            final MusicManager musicManager = new MusicManager(manager, guild);

            guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

            return musicManager;
        });
    }

    public synchronized void destroyMusicManager(@NotNull Guild guild) {
        MusicManager musicManager = getMusicManager(guild);
        musicManager.getScheduler().disconnect();
        musicManagers.remove(guild.getIdLong());
    }

    public void loadAndPlay(@NotNull TextChannel channel, User user, String trackURL) {
        final MusicManager musicManager = this.getMusicManager(channel.getGuild());

        //Strip <>'s that prevent discord from embedding link resources
        if (trackURL.startsWith("<") && trackURL.endsWith(">"))
            trackURL = trackURL.substring(1, trackURL.length() - 1);

        String url = trackURL;
        this.manager.loadItemOrdered(manager, url, new AudioLoadResultHandler() {

            public void trackLoaded(AudioTrack track) {
                boolean trackPlaying = musicManager.getScheduler().trackPlaying();
                boolean isPaused = musicManager.getScheduler().isPaused();

                track.setUserData(user);

                AudioTrackInfo info = track.getInfo();

                musicManager.queue(track);

                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.CYAN);

                builder.setThumbnail(YoutubeUtils.getThumbnail(YoutubeUtils.getVideoID(info.uri)));

                if (!trackPlaying && !isPaused) {
                    builder.setAuthor("Now playing");
                    builder.setTitle(info.title, info.uri);
                } else {
                    builder.setAuthor("Added to queue", null, user.getEffectiveAvatarUrl());

                    builder.addField("Channel name", info.author, true);
                    builder.addField("Song duration", TimeUtils.millisToTime(track.getDuration()), true);
                    builder.addField("Position in queue", String.valueOf(musicManager.getScheduler().getQueue().size()), false);
                }

                channel.sendMessageEmbeds(builder.build()).queue();
            }

            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();

                tracks.forEach(track -> {
                    musicManager.queue(track);
                    track.setUserData(user.getAsTag());
                });

                long totalTimeMs = tracks.stream().mapToLong(AudioTrack::getDuration).sum();

                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.CYAN);
                builder.setAuthor("Added playlist to queue", null, user.getEffectiveAvatarUrl());
                builder.setThumbnail(YoutubeUtils.getThumbnail(YoutubeUtils.getVideoID(tracks.get(0).getInfo().uri)));
                builder.addField("Playlist name", playlist.getName(), true);
                builder.addField("Total playlist time", TimeUtils.millisToTime(totalTimeMs), true);
                builder.addField("Position in queue", String.valueOf(musicManager.getScheduler().getQueue().isEmpty() ? "Now" : musicManager.getScheduler().getQueue().size()), true);
                builder.addField("Enqueued songs", String.valueOf(playlist.getTracks().size()), true);

                channel.sendMessageEmbeds(builder.build()).queue();

                getMusicManager(channel.getGuild()).showTrackInfo(null);
            }

            public void noMatches() {
                Templates.Music.no_matches.format(url).embed(channel);
            }

            public void loadFailed(FriendlyException exception) {
                EmbedUtils.sendErrorEmbed("Something went wrong: " + exception.getMessage(), channel);
            }
        });
    }

    public static AudioPlayerManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new AudioPlayerManager();
        return INSTANCE;
    }

    public void disconnect() {
        this.musicManagers.forEach((id, manager) -> manager.getScheduler().disconnect());
    }

    public void shutdown() {
        disconnect();
        this.musicManagers.clear();
    }

    public Map<Long, Long> getLastActive() {
        return lastActive;
    }
}
