package dev.JustRed23.grandfather.command;

import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.JustRed23.grandfather.Bot;
import dev.JustRed23.grandfather.ex.ErrorHandler;
import dev.JustRed23.grandfather.stats.SongsPerGuild;
import dev.JustRed23.grandfather.utils.HttpUtils;
import dev.JustRed23.grandfather.utils.LyricsProvider;
import dev.JustRed23.jdautils.JDAUtilities;
import dev.JustRed23.jdautils.command.CommandOption;
import dev.JustRed23.jdautils.data.DataStore;
import dev.JustRed23.jdautils.data.InteractionResult;
import dev.JustRed23.jdautils.music.AudioManager;
import dev.JustRed23.jdautils.music.TrackInfo;
import dev.JustRed23.jdautils.music.TrackLoadCallback;
import dev.JustRed23.jdautils.music.search.Search;
import dev.JustRed23.jdautils.music.search.YouTubeSource;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class MusicCommands {

    private static final YouTubeSource YT;

    static {
        AudioSourceManagers.registerRemoteSources(new DefaultAudioPlayerManager());

        try {
            YT = Search.YouTube(Bot.youtube_api_key);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Could not set up search sources", e);
        }
    }

    private static final Function<SlashCommandInteractionEvent, Boolean> IN_VOICE_CHANNEL = event -> {
        if (event.getMember().getVoiceState() == null || event.getMember().getVoiceState().getChannel() == null) {
            event.reply("You must be in a voice channel to use this command!").setEphemeral(true).queue();
            return false;
        }

        if (AudioManager.has(event.getGuild()) &&
                AudioManager.get(event.getGuild()).isConnected() &&
                !AudioManager.get(event.getGuild()).getConnectedChannel().equals(event.getMember().getVoiceState().getChannel())
        ) {
            event.reply("You must be in the same voice channel as the bot to use this command!").setEphemeral(true).queue();
            return false;
        }

        return true;
    };

    private static final Function<SlashCommandInteractionEvent, Boolean> BOT_NOT_PLAYING = event -> {
        if (!AudioManager.get(event.getGuild()).isConnected()) {
            event.reply("The bot is not playing music!").setEphemeral(true).queue();
            return false;
        }

        return true;
    };

    private static final Function<SlashCommandInteractionEvent, Boolean> EMPTY_QUEUE = event -> {
        if (AudioManager.get(event.getGuild()).getScheduler().getQueue().isEmpty()) {
            event.reply("The queue is empty!").setEphemeral(true).queue();
            return false;
        }

        return true;
    };

    private static boolean cacheIsUpToDate = false;
    private static final List<Long> knownBannedUsers = new LinkedList<>();

    private static final Function<SlashCommandInteractionEvent, Boolean> NOT_BANNED = event -> {
        if (!cacheIsUpToDate) {
            knownBannedUsers.clear();

            String bannedUsers = DataStore.GUILD.get(event.getGuild().getIdLong(), "music_banned_users").orElse("");

            if (bannedUsers.isBlank()) {
                cacheIsUpToDate = true;
                return true;
            }

            for (String s : bannedUsers.split(",")) {
                if (s.isBlank()) continue;
                knownBannedUsers.add(Long.parseLong(s));
            }

            cacheIsUpToDate = true;
        }

        if (knownBannedUsers.isEmpty()) return true;

        if (knownBannedUsers.contains(event.getMember().getIdLong())) {
            event.reply("You are currently banned from using music commands!").queue();
            return false;
        }
        return true;
    };

    public static void clearKnownBannedUsers() {
        cacheIsUpToDate = false;
    }

    //TODO: use embeds and stuff to make it more nice
    public static void register() {
        JDAUtilities.createSlashCommand("play", "Plays a song")
                .addAlias("p")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addOption(
                        new CommandOption(OptionType.STRING, "query", "A search query or URL", true, true)
                                .onAutoComplete(event -> {
                                    final String value = event.getFocusedOption().getValue();
                                    if (value.isBlank() || HttpUtils.isUrl(value)) {
                                        event.replyChoices(List.of()).queue();
                                        return;
                                    }

                                    final List<SearchResult> search;
                                    try {
                                        search = YT.search(value);
                                    } catch (IOException e) {
                                        event.replyChoices(List.of()).queue();
                                        ErrorHandler.handleException("youtube-search-request", e);
                                        return;
                                    }

                                    final List<String> titles = search.stream().map(s -> s.getSnippet().getTitle()).toList();
                                    event.replyChoiceStrings(titles.stream().map(s -> s.length() > OptionData.MAX_CHOICE_NAME_LENGTH ? s.substring(0, OptionData.MAX_CHOICE_NAME_LENGTH - 3) + "..." : s).toList()).queue();
                                })
                )
                .executes(event -> {
                    //join voice channel
                    final AudioManager audioManager = AudioManager.get(event.getGuild());
                    audioManager.join(event.getMember().getVoiceState().getChannel().asVoiceChannel());

                    String query = event.getOption("query").getAsString();
                    if (!HttpUtils.isUrl(query)) query = "ytsearch:" + query;

                    final SongsPerGuild stats = SongsPerGuild.get(event.getGuild().getIdLong());
                    audioManager.loadAndPlay(query, event.getMember(), new TrackLoadCallback() {
                        public void onTrackLoaded(TrackInfo trackInfo, boolean addedToQueue, long durationMs) {
                            event.reply("Added " + trackInfo.track().getInfo().title + " by " + trackInfo.track().getInfo().author + " to the queue").queue();

                            //stats
                            stats.play(event.getMember().getIdLong(), trackInfo.track().getInfo().title);
                        }

                        public void onPlaylistLoaded(AudioPlaylist playlist, List<TrackInfo> tracks, long totalDurationMs) {
                            event.reply("Added " + playlist.getTracks().size() + " tracks from playlist " + playlist.getName() + " to the queue").queue();

                            //stats
                            tracks.forEach(trackInfo -> stats.play(event.getMember().getIdLong(), trackInfo.track().getInfo().title));
                        }

                        public void onNoMatches() {
                            event.reply("No matches found").queue();
                        }

                        public void onTrackLoadError(Exception exception) {
                            event.reply("Could not load track: " + exception.getMessage()).queue();
                        }
                    });
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("pause", "Pauses the current song")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .addCondition(event -> {
                    if (!AudioManager.get(event.getGuild()).getScheduler().isPaused()) {
                        event.reply("The bot is already paused!").setEphemeral(true).queue();
                        return false;
                    }
                    return true;
                })
                .executes(event -> {
                    AudioManager.get(event.getGuild()).getControls().pause();
                    event.reply("Paused").queue();
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("resume", "Resumes the current song")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .addCondition(event -> {
                    if (!AudioManager.get(event.getGuild()).getScheduler().isPaused()) {
                        event.reply("The bot is not paused!").setEphemeral(true).queue();
                        return false;
                    }
                    return true;
                })
                .executes(event -> {
                    AudioManager.get(event.getGuild()).getControls().resume();
                    event.reply("Resumed").queue();
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("skip", "Skips the current song")
                .addAlias("s")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .addCondition(event -> {
                    if (!AudioManager.get(event.getGuild()).getScheduler().isPlaying() && AudioManager.get(event.getGuild()).getScheduler().getQueue().isEmpty()) {
                        event.reply("There are no songs in the queue!").setEphemeral(true).queue();
                        return false;
                    }
                    return true;
                })
                .executes(event -> {
                    final TrackInfo next = AudioManager.get(event.getGuild()).getControls().skip();
                    if (next == null)
                        event.reply("There are no more songs in the queue, stopped playing").queue();
                    else
                        event.reply("Skipped to " + next.track().getInfo().title + " by " + next.track().getInfo().author).queue();

                    //stats
                    SongsPerGuild.get(event.getGuild().getIdLong()).skip();
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("prev", "Goes back to the previous song")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .executes(event -> {
                    final TrackInfo prev = AudioManager.get(event.getGuild()).getControls().prev();
                    if (prev == null)
                        event.reply("There are no more songs in the queue, stopped playing").queue();
                    else
                        event.reply("Went back to " + prev.track().getInfo().title + " by " + prev.track().getInfo().author).queue();
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("stop", "Stops playing music")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .executes(event -> {
                    AudioManager.get(event.getGuild()).getControls().stop();
                    event.reply("Stopped playing").queue();
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("disconnect", "Disconnects the bot from the voice channel")
                .addAlias("dc")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .executes(event -> {
                    AudioManager.get(event.getGuild()).disconnect();
                    event.reply("Disconnected").queue();
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("repeat", "Repeats the current song")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .addCondition(event -> {
                    if (!AudioManager.get(event.getGuild()).getScheduler().isPlaying()) {
                        event.reply("There is no song currently playing!").setEphemeral(true).queue();
                        return false;
                    }
                    return true;
                })
                .executes(event -> {
                    AudioManager.get(event.getGuild()).getControls().seek(0);
                    event.reply("Repeating the current song").queue();
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("seek", "Seeks to a position in the current song")
                .addAlias("goto")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .addOption(new CommandOption(OptionType.INTEGER, "minutes", "The amount of minutes to seek", false))
                .addOption(new CommandOption(OptionType.INTEGER, "seconds", "The amount of seconds to seek", false))
                .executes(event -> {
                    final OptionMapping minutes = event.getOption("minutes");
                    final OptionMapping seconds = event.getOption("seconds");
                    int m = minutes != null ? minutes.getAsInt() : 0;
                    int s = seconds != null ? seconds.getAsInt() : 0;
                    int ms = ((m * 60) + s) * 1000;

                    if (ms <= 0) {
                        event.reply("You must specify a valid time to seek to!").setEphemeral(true).queue();
                        return;
                    }

                    AudioManager.get(event.getGuild()).getControls().seek(ms);
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("volume", "Sets the volume of the bot")
                .addAlias("vol")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .addOption(new CommandOption(OptionType.INTEGER, "volume", "The volume to set"))
                .executes(event -> {
                    final OptionMapping volume = event.getOption("volume");
                    if (volume == null) {
                        event.reply("The current volume is " + AudioManager.get(event.getGuild()).getAudioModifier().getVolume()).queue();
                        return;
                    }

                    final int vol = volume.getAsInt();
                    final InteractionResult val = AudioManager.get(event.getGuild()).getAudioModifier().setVolume(vol);
                    if (val == null) {
                        event.reply("The volume must be between 0 and 100!").setEphemeral(true).queue();
                        return;
                    }

                    if (val == InteractionResult.ERROR)
                        ErrorHandler.handleException("db-change-volume", val.getError());

                    event.reply("Set the volume to " + vol).queue();
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("queue", "Shows the current queue")
                .addAlias("q")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .addCondition(EMPTY_QUEUE)
                .executes(event -> {
                    //concurrency
                    final LinkedList<AudioTrack> queue = new LinkedList<>(AudioManager.get(event.getGuild()).getScheduler().getQueue());

                    final StringBuilder builder = new StringBuilder();
                    builder.append("Current queue:\n");
                    for (int i = 0; i < queue.size(); i++) {
                        final AudioTrack track = queue.get(i);
                        builder.append(i + 1).append(". ").append(track.getInfo().title).append(" by ").append(track.getInfo().author).append("\n");
                    }

                    event.reply(builder.toString()).queue();
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("clear", "Clears the current queue")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .addCondition(EMPTY_QUEUE)
                .executes(event -> {
                    AudioManager.get(event.getGuild()).getScheduler().getQueue().clear();
                    event.reply("Cleared the queue").queue();
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("shuffle", "Shuffles the current queue")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .addCondition(EMPTY_QUEUE)
                .executes(event -> {
                    AudioManager.get(event.getGuild()).getScheduler().shuffle();
                    event.reply("Shuffled the queue").queue();
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("loop", "Loops the current song")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .executes(event -> {
                    final AudioManager audioManager = AudioManager.get(event.getGuild());
                    boolean looping = audioManager.getControls().loop();
                    event.reply("Looping is now " + (looping ? "enabled" : "disabled")).queue();
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("remove", "Removes a song from the queue")
                .addAlias("rm")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .addCondition(EMPTY_QUEUE)
                .addOption(new CommandOption(OptionType.INTEGER, "index", "The index of the song to remove", true))
                .executes(event -> {
                    int index = event.getOption("index").getAsInt();
                    index--;

                    final LinkedList<AudioTrack> queue = AudioManager.get(event.getGuild()).getScheduler().getQueue();

                    if (index < 0 || index >= queue.size()) {
                        event.reply("The index must be between 1 and " + queue.size()).setEphemeral(true).queue();
                        return;
                    }

                    final AudioTrack track = queue.get(index);

                    try {
                        queue.remove(index);
                    } catch (ConcurrentModificationException c) {
                        event.reply("An error occurred while removing the song from the queue, please try again").setEphemeral(true).queue();
                        ErrorHandler.handleException("queue-remove", c);
                        return;
                    }

                    event.reply("Removed " + track.getInfo().title + " by " + track.getInfo().author + " from the queue").queue();
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("lyrics", "Shows the lyrics of the current song")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .addCondition(event -> {
                    final TrackInfo current = AudioManager.get(event.getGuild()).getScheduler().getPlayingTrackInfo();
                    if (current == null) {
                        event.reply("There is no song currently playing!").setEphemeral(true).queue();
                        return false;
                    }
                    return true;
                })
                .executes(event -> {
                    final TrackInfo current = AudioManager.get(event.getGuild()).getScheduler().getPlayingTrackInfo();

                    event.deferReply().queue(hook -> {
                        EmbedBuilder builder;
                        try {
                            builder = LyricsProvider.getLyrics(current);
                        } catch (Exception e) {
                            hook.sendMessage("An error occurred while trying to get the lyrics!").queue();
                            ErrorHandler.handleException("lyrics-search", e);
                            return;
                        }

                        hook.sendMessageEmbeds(builder.build()).queue();
                    });
                })
                .setGuildOnly()
                .buildAndRegister();

        //TODO: optional - playlist
        //TODO: optional - effect

        JDAUtilities.createSlashCommand("nowplaying", "Shows the currently playing song")
                .addAlias("np")
                .addCondition(NOT_BANNED)
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .executes(event -> {
                    final TrackInfo current = AudioManager.get(event.getGuild()).getScheduler().getPlayingTrackInfo();
                    if (current == null)
                        event.reply("There is no song currently playing!").queue();
                    else
                        event.reply("Currently playing " + current.track().getInfo().title + " by " + current.track().getInfo().author).queue();
                })
                .setGuildOnly()
                .buildAndRegister();
    }
}
