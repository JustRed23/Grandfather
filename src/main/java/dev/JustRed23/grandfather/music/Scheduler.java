package dev.JustRed23.grandfather.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import dev.JustRed23.grandfather.template.Templates;
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
        AudioTrack playing = getPlayingTrack();

        if (playing != null)
            return false;

        this.audioPlayer.startTrack(this.prev.poll(), false);
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
        this.queue.clear();
        this.prev.clear();
        AudioPlayerManager.getInstance().getLastActive().remove(guild.getIdLong());
        stop();
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
            EmbedUtils.sendTemplateEmbed(Templates.music.not_playing, event);
            return;
        }

        EmbedBuilder builder;

        long timeStamp = TimeUnit.MILLISECONDS.toSeconds(getPlayingTrack().getPosition());

        if (timeStamp > 0)
            builder = MusicUtils.displayMusicInfoWithTimestamp(musicManager);
        else
            builder = MusicUtils.displayMusicInfo(musicManager);

        if (event != null)
            EmbedUtils.sendEmbed(builder, event);
        else
            this.channel.sendMessageEmbeds(builder.build()).queue();
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

            audioPlayer.startTrack(this.queue.poll(), false);
        }
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
