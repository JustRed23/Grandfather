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

    public static void register() {
        JDAUtilities.createSlashCommand("play", "Plays a song")
                .addOption(
                        new CommandOption(OptionType.STRING, "query", "A search query or URL", true, true)
                                .onAutoComplete(event -> {
                                    final String value = event.getFocusedOption().getValue();
                                    if (value.isBlank() || HttpUtils.isUrl(value))
                                        return;

                                    final List<SearchResult> search;
                                    try {
                                        search = YT.search(value);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
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
                        //TODO: use embeds and stuff to make it more nice
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
                .modifyData(data -> data.setGuildOnly(true))
                .buildAndRegister();
    }
}
