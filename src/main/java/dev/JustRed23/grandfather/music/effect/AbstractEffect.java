package dev.JustRed23.grandfather.music.effect;

import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.List;

public abstract class AbstractEffect {

    private final AudioPlayer player;

    protected AbstractEffect(AudioPlayer player) {
        this.player = player;
    }

    abstract List<AudioFilter> getEffect(AudioTrack track, AudioDataFormat format, UniversalPcmAudioFilter output);

    public void enable() {
        player.setFilterFactory(this::getEffect);
    }

    public void disable() {
        player.setFilterFactory(null);
    }

    public abstract String getEffectName();
}
