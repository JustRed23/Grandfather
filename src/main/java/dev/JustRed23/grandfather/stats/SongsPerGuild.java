package dev.JustRed23.grandfather.stats;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.JustRed23.grandfather.GFS;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SongsPerGuild {

    private static final StatStorage storage = new StatStorage(GFS.stats.getDirectory(), "song-stats.json");
    public static final Map<Long, SongsPerGuild> stats = new HashMap<>();
    public static SongsPerGuild get(long guildID) {
        return stats.computeIfAbsent(guildID, k -> new SongsPerGuild());
    }

    public static void save() {
        JsonObject object = new JsonObject();
        stats.forEach((guildID, songsPerGuild) -> object.add(String.valueOf(guildID), songsPerGuild.toJsonObject()));
        storage.save(object);
    }

    public static void load() {
        JsonObject data = storage.data;
        if (data == null) return;
        data.entrySet().forEach(entry -> stats.put(Long.parseLong(entry.getKey()), new SongsPerGuild(entry.getValue().getAsJsonObject())));
    }



    public record SongStat(String title, int plays) {}
    public record UserStat(long userID, List<String> songsPlayed) {}



    private int songsPlayed;
    private int songsSkipped;

    private final Map<String, Integer> songsPlayedCount = new HashMap<>();
    private final Map<Long, List<String>> songsPlayedByUser = new HashMap<>();

    private SongsPerGuild() {}

    private SongsPerGuild(JsonObject fromData) {
        fromJsonObject(fromData);
    }

    public void play(long userID, String title) {
        songsPlayed++;
        songsPlayedCount.put(title, songsPlayedCount.getOrDefault(title, 0) + 1);

        final List<String> strings = songsPlayedByUser.computeIfAbsent(userID, k -> new ArrayList<>());
        if (!strings.contains(title))
            strings.add(title);
    }

    public void skip() {
        songsSkipped++;
    }



    public int getSongsPlayed() {
        return songsPlayed;
    }

    public int getSongsSkipped() {
        return songsSkipped;
    }

    public int getSongPlays(String title) {
        return songsPlayedCount.getOrDefault(title, 0);
    }

    public List<SongStat> getTopSongs(int amount) {
        return songsPlayedCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(amount)
                .map(e -> new SongStat(e.getKey(), e.getValue()))
                .toList();
    }

    public List<String> getSongsPlayedByUser(long userID) {
        return songsPlayedByUser.getOrDefault(userID, new ArrayList<>());
    }

    public List<UserStat> getTopUsers(int amount) {
        return songsPlayedByUser.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .limit(amount)
                .map(e -> new UserStat(e.getKey(), e.getValue()))
                .toList();
    }

    @ApiStatus.Internal
    private void fromJsonObject(JsonObject data) {
        songsPlayed = data.get("songsPlayed").getAsInt();
        songsSkipped = data.get("songsSkipped").getAsInt();

        data.getAsJsonArray("songsPlayedCount").forEach(element -> {
            JsonObject songObject = element.getAsJsonObject();
            songsPlayedCount.put(songObject.get("title").getAsString(), songObject.get("plays").getAsInt());
        });

        data.getAsJsonObject("songsPlayedByUser").entrySet().forEach(entry -> {
            List<String> songs = new ArrayList<>();
            entry.getValue().getAsJsonArray().forEach(element -> songs.add(element.getAsString()));
            songsPlayedByUser.put(Long.parseLong(entry.getKey()), songs);
        });
    }

    @ApiStatus.Internal
    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("songsPlayed", songsPlayed);
        object.addProperty("songsSkipped", songsSkipped);

        JsonArray songsPlayedCountArray = new JsonArray();
        songsPlayedCount.forEach((title, plays) -> {
            JsonObject songObject = new JsonObject();
            songObject.addProperty("title", title);
            songObject.addProperty("plays", plays);
            songsPlayedCountArray.add(songObject);
        });
        object.add("songsPlayedCount", songsPlayedCountArray);

        JsonObject songsPlayedByUserObject = new JsonObject();
        songsPlayedByUser.forEach((userID, songs) -> {
            JsonArray songsArray = new JsonArray();
            songs.forEach(songsArray::add);
            songsPlayedByUserObject.add(String.valueOf(userID), songsArray);
        });
        object.add("songsPlayedByUser", songsPlayedByUserObject);

        return object;
    }
}
