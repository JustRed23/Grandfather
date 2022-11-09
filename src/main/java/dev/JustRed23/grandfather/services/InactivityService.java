package dev.JustRed23.grandfather.services;

import dev.JustRed23.grandfather.App;
import dev.JustRed23.grandfather.Bot;
import dev.JustRed23.grandfather.music.AudioPlayerManager;
import dev.JustRed23.grandfather.music.MusicManager;
import dev.JustRed23.stonebrick.service.Service;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class InactivityService extends Service {
    private final long TIME_UNTIL_DISCONNECT = TimeUnit.SECONDS.toMillis(30);

    private final Map<AudioChannel, Long> botAlone = new ConcurrentHashMap<>();

    public String getServiceName() {
        return "music_channel_inactivity_checker";
    }

    public long delayBetweenRuns() {
        return TimeUnit.SECONDS.toMillis(10);
    }

    public boolean shouldRun() {
        return Bot.enabled;
    }

    public void run() throws Exception {
        getConnectedVoiceChannels().forEach(channel -> {
            MusicManager musicManager = AudioPlayerManager.getInstance().getMusicManager(channel.getGuild());

            if (!isMemberPresent(channel)) {
                if (!botAlone.containsKey(channel)) {
                    botAlone.put(channel, System.currentTimeMillis());
                    return;
                } else if (System.currentTimeMillis() >= botAlone.get(channel) + TIME_UNTIL_DISCONNECT) {
                    musicManager.getScheduler().disconnect();
                    botAlone.remove(channel);
                    return;
                }
            } else botAlone.remove(channel);

            if (musicManager.getScheduler().trackPlaying() && !musicManager.getScheduler().isPaused()) {
                AudioPlayerManager.getInstance().getLastActive().remove(channel.getGuild().getIdLong());
                return;
            }

            if (AudioPlayerManager.getInstance().getLastActive().containsKey(channel.getGuild().getIdLong())
                    && System.currentTimeMillis() >= AudioPlayerManager.getInstance().getLastActive().get(channel.getGuild().getIdLong()) + TIME_UNTIL_DISCONNECT) {
                musicManager.getScheduler().disconnect();
                botAlone.remove(channel);
            }
        });
    }

    public void onComplete() {
        LOGGER.debug("Inactivity service completed");
    }

    public void onError(Exception e) {
        LOGGER.error("Inactivity service encountered an error", e);
    }

    private boolean isMemberPresent(AudioChannel channel) {
        boolean memberPresent = false;

        for (Member member : channel.getMembers()) {
            if (member.getUser().isBot())
                continue;

            memberPresent = true;
        }

        return memberPresent;
    }

    private Set<AudioChannel> getConnectedVoiceChannels() {
        Set<AudioChannel> channels = new HashSet<>();

        for (JDA shard : App.getShardManager().getShards()) {
            channels.addAll(shard.getGuilds().stream()
                    .filter(g -> g.getAudioManager().getConnectedChannel() != null)
                    .map(g -> g.getAudioManager().getConnectedChannel())
                    .toList());
        }

        return channels;
    }
}
