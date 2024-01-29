package dev.JustRed23.grandfather.command;

import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import dev.JustRed23.grandfather.Bot;
import dev.JustRed23.grandfather.utils.HttpUtils;
import dev.JustRed23.jdautils.JDAUtilities;
import dev.JustRed23.jdautils.command.CommandOption;
import dev.JustRed23.jdautils.music.AudioManager;
import dev.JustRed23.jdautils.music.TrackInfo;
import dev.JustRed23.jdautils.music.TrackLoadCallback;
import dev.JustRed23.jdautils.music.search.Search;
import dev.JustRed23.jdautils.music.search.YouTubeSource;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.io.IOException;
import java.security.GeneralSecurityException;
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

    //TODO: use embeds and stuff to make it more nice
    public static void register() {
        JDAUtilities.createSlashCommand("play", "Plays a song")
                .addAlias("p")
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
                                        e.printStackTrace();
                                        return;
                                    }

                                    final List<String> titles = search.stream().map(s -> s.getSnippet().getTitle()).toList();
                                    event.replyChoiceStrings(titles).queue();
                                })
                )
                .addCondition(IN_VOICE_CHANNEL)
                .executes(event -> {
                    //join voice channel
                    final AudioManager audioManager = AudioManager.get(event.getGuild());
                    audioManager.join(event.getMember().getVoiceState().getChannel().asVoiceChannel());

                    String query = event.getOption("query").getAsString();
                    if (!HttpUtils.isUrl(query)) query = "ytsearch:" + query;

                    audioManager.loadAndPlay(query, event.getMember(), new TrackLoadCallback() {
                        public void onTrackLoaded(TrackInfo trackInfo, boolean addedToQueue, long durationMs) {
                            event.reply("Added " + trackInfo.track().getInfo().title + " by " + trackInfo.track().getInfo().author + " to the queue").queue();
                        }

                        public void onPlaylistLoaded(AudioPlaylist playlist, List<TrackInfo> tracks, long totalDurationMs) {
                            event.reply("Added " + playlist.getTracks().size() + " tracks from playlist " + playlist.getName() + " to the queue").queue();
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

        JDAUtilities.createSlashCommand("seek", "Seeks to a position in the current song")
                .addAlias("goto")
                .addOption(new CommandOption(OptionType.INTEGER, "minutes", "The amount of minutes to seek", false))
                .addOption(new CommandOption(OptionType.INTEGER, "seconds", "The amount of seconds to seek", false))
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
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

        JDAUtilities.createSlashCommand("skip", "Skips the current song")
                .addAlias("s")
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
                })
                .setGuildOnly()
                .buildAndRegister();

        JDAUtilities.createSlashCommand("prev", "Goes back to the previous song")
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
                .addCondition(IN_VOICE_CHANNEL)
                .addCondition(BOT_NOT_PLAYING)
                .executes(event -> {
                    AudioManager.get(event.getGuild()).disconnect();
                    event.reply("Disconnected").queue();
                })
                .setGuildOnly()
                .buildAndRegister();
    }
}
