package dev.JustRed23.grandfather.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.JustRed23.grandfather.GFS;
import dev.JustRed23.jdautils.music.PlayableTrack;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SongsPerGuild {

    private static final Logger log = LoggerFactory.getLogger(SongsPerGuild.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Path STATS_FILE = GFS.statsFile.getPath();
    private static final Path STATS_TEMP = GFS.statsTemp.getPath();

    private static final Map<Long, SongsPerGuild> stats = new ConcurrentHashMap<>();

    public static SongsPerGuild get(long guildID) {
        return stats.computeIfAbsent(guildID, k -> new SongsPerGuild());
    }

    public static boolean has(long guildID) {
        return stats.containsKey(guildID);
    }

    public static void track(long guildId, PlayableTrack track) {
        get(guildId).play(track);
    }

    public static void save() {
        JsonObject root = new JsonObject();
        stats.forEach((guildID, guild) -> root.add(String.valueOf(guildID), guild.toJson()));
        try {
            Files.writeString(STATS_TEMP, GSON.toJson(root));
            Files.move(STATS_TEMP, STATS_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            log.error("Failed to save song stats", e);
        }
    }

    public static void load() {
        try {
            JsonObject root = GSON.fromJson(Files.readString(STATS_FILE), JsonObject.class);
            if (root == null) return;
            root.entrySet().forEach(entry -> {
                try {
                    stats.put(Long.parseLong(entry.getKey()), SongsPerGuild.fromJson(entry.getValue().getAsJsonObject()));
                } catch (Exception e) {
                    log.warn("Skipping malformed entry '{}': {}", entry.getKey(), e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Failed to load song stats", e);
        }
    }

    public record SongStat(String title, int plays) {}
    public record UserStat(long userID, int plays) {}

    private final Map<String, AtomicInteger> songsPlayed = new ConcurrentHashMap<>();
    private final Map<Long, AtomicInteger> songsPerUser  = new ConcurrentHashMap<>();

    private SongsPerGuild() {}

    private void play(PlayableTrack track) {
        songsPlayed.computeIfAbsent(track.title(), k -> new AtomicInteger()).incrementAndGet();
        songsPerUser.computeIfAbsent(track.member().getIdLong(), k -> new AtomicInteger()).incrementAndGet();
    }

    public int getPlays(String title) {
        AtomicInteger count = songsPlayed.get(title);
        return count == null ? 0 : count.get();
    }

    public int getPlays(long userID) {
        AtomicInteger count = songsPerUser.get(userID);
        return count == null ? 0 : count.get();
    }

    public int getTotalPlays() {
        return songsPlayed.values().stream().mapToInt(AtomicInteger::get).sum();
    }

    public int getUniqueSongCount() {
        return songsPlayed.size();
    }

    public int getUniqueUserCount() {
        return songsPerUser.size();
    }

    public List<SongStat> getTopSongs(int limit) {
        return songsPlayed.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().get(), a.getValue().get()))
                .limit(limit)
                .map(e -> new SongStat(e.getKey(), e.getValue().get()))
                .toList();
    }

    public List<UserStat> getTopUsers(int limit) {
        return songsPerUser.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().get(), a.getValue().get()))
                .limit(limit)
                .map(e -> new UserStat(e.getKey(), e.getValue().get()))
                .toList();
    }

    public boolean hasPlayed(String title) {
        return songsPlayed.containsKey(title);
    }

    public boolean hasPlayed(long userID) {
        return songsPerUser.containsKey(userID);
    }

    public SongStat getMostPlayedSong() {
        return songsPlayed.entrySet().stream()
                .max(Comparator.comparingInt(a -> a.getValue().get()))
                .map(e -> new SongStat(e.getKey(), e.getValue().get()))
                .orElse(null);
    }

    public UserStat getMostActiveUser() {
        return songsPerUser.entrySet().stream()
                .max(Comparator.comparingInt(a -> a.getValue().get()))
                .map(e -> new UserStat(e.getKey(), e.getValue().get()))
                .orElse(null);
    }

    @ApiStatus.Internal
    private static SongsPerGuild fromJson(JsonObject data) {
        SongsPerGuild instance = new SongsPerGuild();

        JsonObject played = data.getAsJsonObject("songsPlayed");
        if (played != null)
            played.entrySet().forEach(entry -> instance.songsPlayed.put(entry.getKey(), new AtomicInteger(entry.getValue().getAsInt())));

        JsonObject perUser = data.getAsJsonObject("songsPerUser");
        if (perUser != null)
            perUser.entrySet().forEach(entry -> instance.songsPerUser.put(Long.parseLong(entry.getKey()), new AtomicInteger(entry.getValue().getAsInt())));

        return instance;
    }

    @ApiStatus.Internal
    private JsonObject toJson() {
        JsonObject object = new JsonObject();

        JsonObject played = new JsonObject();
        songsPlayed.forEach((title, count) -> played.addProperty(title, count.get()));
        object.add("songsPlayed", played);

        JsonObject perUser = new JsonObject();
        songsPerUser.forEach((userID, count) -> perUser.addProperty(String.valueOf(userID), count.get()));
        object.add("songsPerUser", perUser);

        return object;
    }
}
