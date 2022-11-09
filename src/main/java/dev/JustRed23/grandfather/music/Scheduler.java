package dev.JustRed23.grandfather.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.JustRed23.grandfather.bettertemplate.Templates;
import dev.JustRed23.grandfather.utils.MusicUtils;
import dev.JustRed23.grandfather.utils.msg.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Scheduler extends AudioEventAdapter {

    private final LinkedList<AudioTrack> queue, prev;

    private final AudioManager audioManager;
    private final AudioPlayer audioPlayer;

    private final Guild guild;
    private TextChannel channel;

    private TrackModes trackMode = TrackModes.NORMAL;

    public Scheduler(AudioManager audioManager, AudioPlayer audioPlayer, Guild guild) {
        this.queue = new LinkedList<>();
        this.prev = new LinkedList<>();
        this.audioManager = audioManager;
        this.audioPlayer = audioPlayer;
        this.guild = guild;
    }

    public void queue(AudioTrack track) {
        if (!audioPlayer.startTrack(track, true))
            queue.offer(track);
    }

    public boolean prevTrack() {
        if (this.prev.isEmpty())
            return false;

        this.audioPlayer.startTrack(this.prev.poll(), false);
        showTrackInfo(null);
        return true;
    }

    public void nextTrack() {
        trackMode = TrackModes.NORMAL;

        if (isPaused())
            pause(true);

        if (queue.isEmpty()) {
            stop();
            return;
        }

        this.audioPlayer.startTrack(queue.poll(), false);
        showTrackInfo(null);
    }

    public void stop() {
        this.audioPlayer.stopTrack();
        if (isPaused())
            pause(true);
        AudioPlayerManager.getInstance().getLastActive().put(guild.getIdLong(), System.currentTimeMillis());
        AudioPlayerManager.getInstance().getMusicManager(guild).getTrackModifier().resetEffect();
    }

    public void pause(boolean resume) {
        this.audioPlayer.setPaused(!resume);
    }

    public void restart() {
        this.audioPlayer.startTrack(getPlayingTrack().makeClone(), false);
    }

    public void stopAndClear() {
        stop();
        this.queue.clear();
        this.prev.clear();
        AudioPlayerManager.getInstance().getLastActive().remove(guild.getIdLong());
    }

    public void disconnect() {
        stopAndClear();
        audioManager.closeAudioConnection();
        AudioPlayerManager.getInstance().getLastActive().remove(guild.getIdLong());
        trackMode = TrackModes.NORMAL;
        setChannel(null);
    }

    public void showTrackInfo(@Nullable Event event) {
        if (this.channel == null)
            return;

        MusicManager musicManager = AudioPlayerManager.getInstance().getMusicManager(guild);

        if (!trackPlaying()) {
            Templates.Music.not_playing.embed(channel);
            return;
        }

        EmbedBuilder builder;

        long timeStamp = TimeUnit.MILLISECONDS.toSeconds(getPlayingTrack().getPosition());

        if (timeStamp > 0)
            builder = MusicUtils.displayMusicInfoWithTimestamp(musicManager);
        else
            builder = MusicUtils.displayMusicInfo(musicManager);

        EmbedUtils.sendEmbed(builder, event, channel);
    }


    //Queue modification
    public void swapTracks(int a, int b) throws IndexOutOfBoundsException {
        Collections.swap(queue, a, b);
    }

    public void shuffle() {
        Collections.shuffle(queue);
    }


    //Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (trackLooping())
                this.queue.add(track.makeClone());
            else
                this.prev.add(track.makeClone());

            AudioTrack poll = this.queue.poll();
            audioPlayer.startTrack(poll, false);
            if (poll != null)
                showTrackInfo(null);
        }
        AudioPlayerManager.getInstance().getLastActive().put(guild.getIdLong(), System.currentTimeMillis());
    }

    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        AudioPlayerManager.getInstance().getLastActive().put(guild.getIdLong(), System.currentTimeMillis());
    }

    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        AudioPlayerManager.getInstance().getLastActive().put(guild.getIdLong(), System.currentTimeMillis());
    }

    //GETTERS & SETTERS
    public TrackModes getTrackMode() {
        return trackMode;
    }

    public AudioTrack getPlayingTrack() {
        return this.audioPlayer.getPlayingTrack();
    }

    public List<AudioTrack> getQueue() {
        return queue;
    }

    public Guild getGuild() {
        return this.guild;
    }

    public TextChannel getChannel() {
        return channel;
    }

    public boolean trackPlaying() {
        return this.audioPlayer.getPlayingTrack() != null;
    }

    public boolean isPaused() {
        return this.audioPlayer.isPaused();
    }

    public boolean toggleLoop() {
        this.trackMode = trackLooping() ? TrackModes.NORMAL : TrackModes.LOOPING;
        return trackLooping();
    }

    public boolean trackLooping() {
        return this.trackMode == TrackModes.LOOPING;
    }

    public void setChannel(TextChannel channel) {
        this.channel = channel;
    }

    public boolean isSameChannel(TextChannel channel) {
        return this.channel.equals(channel);
    }
}
