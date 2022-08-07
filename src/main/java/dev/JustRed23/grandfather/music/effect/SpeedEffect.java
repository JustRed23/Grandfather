package dev.JustRed23.grandfather.music.effect;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Collections;
import java.util.List;

public class SpeedEffect extends AbstractEffect {

    private final float speed;

    public SpeedEffect(AudioPlayer player, float speed) {
        super(player);
        this.speed = speed;
    }

    List<AudioFilter> getEffect(AudioTrack track, AudioDataFormat format, UniversalPcmAudioFilter output) {
        if (speed <= 0 || speed == 1)
            return Collections.emptyList();

        TimescalePcmAudioFilter timescale = new TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate);
        timescale.setSpeed(speed);
        return Collections.singletonList(timescale);
    }

    public String getEffectName() {
        return "speed";
    }
}
