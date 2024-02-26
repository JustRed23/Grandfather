package dev.JustRed23.grandfather.utils;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.JustRed23.jdautils.music.AudioManager;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class InactivityTimer {

    private static final long INACTIVITY_THRESHOLD = TimeUnit.SECONDS.toMillis(30);

    private static final Map<Guild, AudioEventAdapter> adapters = new ConcurrentHashMap<>();
    private static final Map<Guild, Long> lastActive = new ConcurrentHashMap<>();
    private static final List<Guild> pendingRemoval = new CopyOnWriteArrayList<>();

    public static AudioEventAdapter create(Guild guild) {
        if (adapters.containsKey(guild)) return adapters.get(guild);

        final AudioEventAdapter audioEventAdapter = new AudioEventAdapter() {
            public void onTrackStart(AudioPlayer player, AudioTrack track) {
                pendingRemoval.add(guild);
            }

            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                pendingRemoval.remove(guild);
                lastActive.put(guild, System.currentTimeMillis());
            }
        };
        adapters.put(guild, audioEventAdapter);
        return audioEventAdapter;
    }

    @ApiStatus.Internal
    public static void check() {
        long now = System.currentTimeMillis();

        lastActive.entrySet().removeIf(entry -> {
            Guild key = entry.getKey();

            if (pendingRemoval.contains(key)) {
                pendingRemoval.remove(key);
                return true;
            }

            if (now - entry.getValue() > INACTIVITY_THRESHOLD) {
                if (AudioManager.has(key) && AudioManager.get(key).isConnected())
                    AudioManager.get(key).disconnect();
                adapters.remove(key);
                pendingRemoval.remove(key);
                return true;
            }
            return false;
        });
    }
}
