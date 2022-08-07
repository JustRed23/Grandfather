package dev.JustRed23.grandfather.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.Event;
import org.jetbrains.annotations.Nullable;

public class MusicManager {

    private final Guild guild;
    private final AudioPlayer player;
    private final Scheduler scheduler;
    private final SendHandler sendHandler;
    private final TrackModifier trackModifier;

    public MusicManager(AudioPlayerManager manager, Guild guild) {
        this.guild = guild;
        this.player = manager.createPlayer();
        this.scheduler = new Scheduler(guild.getAudioManager(), player, guild);
        this.player.addListener(scheduler);
        this.sendHandler = new SendHandler(player);
        this.trackModifier = new TrackModifier(player);
    }

    public void queue(AudioTrack track) {
        this.scheduler.queue(track);
    }

    public void nextTrack() {
        this.scheduler.nextTrack();
    }

    public boolean prevTrack() {
        return this.scheduler.prevTrack();
    }

    public void stop() {
        this.scheduler.stop();
    }

    public void pause() {
        this.scheduler.pause(false);
    }

    public void resume() {
        this.scheduler.pause(true);
    }

    public void restart() {
        this.scheduler.restart();
    }

    public void showTrackInfo(@Nullable Event event) {
        this.scheduler.showTrackInfo(event);
    }

    public SendHandler getSendHandler() {
        return sendHandler;
    }

    public Guild getGuild() {
        return guild;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public TrackModifier getTrackModifier() {
        return trackModifier;
    }
}
