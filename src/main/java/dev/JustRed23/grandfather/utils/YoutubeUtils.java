package dev.JustRed23.grandfather.utils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoContentDetails;
import dev.JustRed23.grandfather.Bot;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class YoutubeUtils {

    private static final YouTube youTube;

    static {
        YouTube tmp = null;

        try {
            tmp = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    null
            )
                    .setApplicationName("Grandfather#2911")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        youTube = tmp;
    }

    private YoutubeUtils() {}

    @Nullable
    public static List<SearchResult> ytSearch(String input, long maxResults) throws IOException {
        List<SearchResult> results = youTube.search()
                .list(Collections.singletonList("id,snippet"))
                .setQ(input)
                .setMaxResults(maxResults)
                .setType(Collections.singletonList("video"))
                .setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)")
                .setKey(Bot.youtube_api_key)
                .execute()
                .getItems();

        if (!results.isEmpty())
            return results;
        return null;
    }

    @Nullable
    public static List<VideoContentDetails> getVideoDetails(List<String> videoIDs) throws IOException {
        List<Video> details = youTube.videos()
                .list(Collections.singletonList("contentDetails"))
                .setId(videoIDs)
                .setKey(Bot.youtube_api_key)
                .execute()
                .getItems();

        if (!details.isEmpty())
            return details.stream().map(Video::getContentDetails).toList();

        return null;
    }

    public static String getThumbnail(String videoID) {
        return "http://img.youtube.com/vi/" + videoID +"/0.jpg";
    }

    public static String getVideo(String videoID) {
        return "https://www.youtube.com/watch?v=" + videoID;
    }

    public static String getVideoID(String youtube_link) {
        return youtube_link.replace("https://www.youtube.com/watch?v=", "");
    }
}
