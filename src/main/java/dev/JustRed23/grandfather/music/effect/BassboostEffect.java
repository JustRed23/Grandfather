package dev.JustRed23.grandfather.music.effect;

import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Collections;
import java.util.List;

public class BassboostEffect extends AbstractEffect {

    private final float[] freqGain = {-0.05f, 0.07f, 0.16f, 0.03f, -0.05f, -0.11f};

    private final float multiplier;

    public BassboostEffect(AudioPlayer player, float multiplier) {
        super(player);
        this.multiplier = multiplier;
    }

    List<AudioFilter> getEffect(AudioTrack track, AudioDataFormat format, UniversalPcmAudioFilter output) {
        if (multiplier == 1)
            return Collections.emptyList();
        else {
            EqualizerFactory factory = new EqualizerFactory();

            for (int i = 0; i < freqGain.length; i++)
                factory.setGain(i, freqGain[i] * multiplier);

            return factory.buildChain(track, format, output);
        }
    }

    public String getEffectName() {
        return "bassboost";
    }
}
