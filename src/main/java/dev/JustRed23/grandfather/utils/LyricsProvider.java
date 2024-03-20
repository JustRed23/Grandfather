package dev.JustRed23.grandfather.utils;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import core.GLA;
import dev.JustRed23.jdautils.music.TrackInfo;
import genius.SongSearch;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

public final class LyricsProvider {

    private static final GLA client = new GLA();
    private static final HashMap<String, SongSearch.Hit> cache = new HashMap<>();

    public static EmbedBuilder getLyrics(TrackInfo track) throws IOException {
        final AudioTrackInfo info = track.track().getInfo();
        final EmbedBuilder builder = new EmbedBuilder();

        if (cache.containsKey(info.title)) {
            createEmbed(builder, cache.get(info.title));
            return builder;
        }

        final LinkedList<SongSearch.Hit> hits = search(info.title);

        if (hits.isEmpty()) {
            //Try again by removing parenthesis
            final String title = info.title.replaceAll("\\(.*?\\)", "").trim();
            search(title).stream().findFirst().ifPresent(hits::add);
        }

        if (!hits.isEmpty()) {
            final SongSearch.Hit hit = hits.get(0);
            cache.put(info.title, hit);
            createEmbed(builder, hit);
            return builder;
        }

        builder.setColor(Color.RED);
        builder.setTitle("No lyrics found for " + MarkdownSanitizer.escape(info.title));
        return builder;
    }

    private static LinkedList<SongSearch.Hit> search(String query) throws IOException {
        try {
            return client.search(query).getHits();
        } catch (NullPointerException e) { //If the search fails, return an empty list
            return new LinkedList<>();
        }
    }

    private static void createEmbed(EmbedBuilder builder, SongSearch.Hit hit) {
        builder.setColor(0xF0E68C);
        builder.setTitle("Lyrics for " + MarkdownSanitizer.escape(hit.getTitle() + " by " + hit.getArtist().getName()), hit.getUrl());
        builder.setThumbnail(hit.getThumbnailUrl());

        String content = hit.fetchLyrics();
        if (content.length() > MessageEmbed.DESCRIPTION_MAX_LENGTH)
            content = content.substring(0, MessageEmbed.DESCRIPTION_MAX_LENGTH - 3) + "...";
        builder.setDescription(content);

        builder.setFooter("Lyrics provided by Genius, may not be accurate");
    }
}
