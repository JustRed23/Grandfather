package dev.JustRed23.grandfather.utils;

import dev.JustRed23.jdautils.music.TrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import java.awt.*;

public final class LyricsProvider {

    public static EmbedBuilder getLyrics(TrackInfo track) {
        //TODO: Implement lyrics provider
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.ORANGE);
        builder.setTitle("Lyrics for " + MarkdownSanitizer.escape(track.track().getInfo().title));
        builder.setDescription("Lyrics are not available at the moment. This feature will be available soon:tm:.");
        return builder;
    }
}
