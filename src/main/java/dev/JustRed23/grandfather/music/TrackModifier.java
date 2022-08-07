package dev.JustRed23.grandfather.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import dev.JustRed23.grandfather.music.effect.AbstractEffect;

public class TrackModifier {

    private final AudioPlayer audioPlayer;

    private AbstractEffect effect;

    public TrackModifier(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    public void setVolume(int volume) {
        this.audioPlayer.setVolume(volume);
    }

    public int getVolume() {
        return this.audioPlayer.getVolume();
    }

    public void setEffect(AbstractEffect effect) {
        AbstractEffect previousEffect = getEffect();

        if (previousEffect != null)
            previousEffect.disable();

        effect.enable();

        this.effect = effect;
    }

    private AbstractEffect getEffect() {
        return effect;
    }

    public void resetEffect() {
        if (effect != null) {
            effect.disable();
            effect = null;
        }
    }
}
