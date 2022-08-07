package dev.JustRed23.grandfather.music.effect;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Collections;
import java.util.List;

public class NightcoreEffect extends AbstractEffect {

    public NightcoreEffect(AudioPlayer player) {
        super(player);
    }

    List<AudioFilter> getEffect(AudioTrack track, AudioDataFormat format, UniversalPcmAudioFilter output) {
        TimescalePcmAudioFilter timescale = new TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate);
        timescale.setSpeed(1.3);
        timescale.setPitch(1.25);
        return Collections.singletonList(timescale);
    }

    public String getEffectName() {
        return "nightcore";
    }
}
