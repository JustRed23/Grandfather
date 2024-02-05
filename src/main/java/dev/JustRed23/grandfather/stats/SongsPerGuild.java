package dev.JustRed23.grandfather.stats;

import dev.JustRed23.jdautils.utils.ValueStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SongsPerGuild {

    public static final Map<Long, SongsPerGuild> stats = new HashMap<>();
    public static SongsPerGuild get(long guildID) {
        return stats.computeIfAbsent(guildID, k -> new SongsPerGuild());
    }

    public static void save() {
        ValueStore store = ValueStore.loadOrCreate("song-stats");
        //TODO
        store.save();
    }

    public static void load() {
        ValueStore store = ValueStore.loadOrCreate("song-stats");
        //TODO
    }



    public record SongStat(String title, int plays) {}
    public record UserStat(long userID, List<String> songsPlayed) {}



    private int songsPlayed;
    private int songsSkipped;

    private final Map<String, Integer> songsPlayedCount = new HashMap<>();
    private final Map<Long, List<String>> songsPlayedByUser = new HashMap<>();

    private SongsPerGuild() {}

    public void play(long userID, String title) {
        songsPlayed++;
        songsPlayedCount.put(title, songsPlayedCount.getOrDefault(title, 0) + 1);
        songsPlayedByUser.computeIfAbsent(userID, k -> new ArrayList<>()).add(title);
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
}
