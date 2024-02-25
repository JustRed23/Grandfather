package dev.JustRed23.grandfather.utils;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.JustRed23.jdautils.music.AudioManager;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class InactivityTimer {

    private static final long INACTIVITY_THRESHOLD = TimeUnit.SECONDS.toMillis(30);

    private static final Map<Guild, AudioEventAdapter> adapters = new ConcurrentHashMap<>();
    private static final Map<Guild, Long> lastActive = new ConcurrentHashMap<>();

    public static AudioEventAdapter create(Guild guild) {
        if (adapters.containsKey(guild)) return adapters.get(guild);

        final AudioEventAdapter audioEventAdapter = new AudioEventAdapter() {
            public void onTrackStart(AudioPlayer player, AudioTrack track) {
                lastActive.remove(guild);
            }

            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                System.out.println(endReason.name() + " " + endReason.mayStartNext);
                lastActive.put(guild, System.currentTimeMillis());
            }
        };
        adapters.put(guild, audioEventAdapter);
        return audioEventAdapter;
    }

    @ApiStatus.Internal
    public static void check() {
        long now = System.currentTimeMillis();
        for (Map.Entry<Guild, Long> entry : new HashMap<>(lastActive).entrySet()) {
            final Guild guild = entry.getKey();
            if (!AudioManager.has(guild) || !AudioManager.get(guild).isConnected()) {
                lastActive.remove(guild);
                adapters.remove(guild);
                continue;
            }

            if (now - entry.getValue() > INACTIVITY_THRESHOLD) {
                AudioManager.get(guild).disconnect();
                lastActive.remove(guild);
                adapters.remove(guild);
            }
        }
    }
}
