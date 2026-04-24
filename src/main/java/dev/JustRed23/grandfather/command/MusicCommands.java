package dev.JustRed23.grandfather.command;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.model.SearchResult;
import dev.JustRed23.grandfather.App;
import dev.JustRed23.grandfather.Bot;
import dev.JustRed23.grandfather.ex.ErrorHandler;
import dev.JustRed23.grandfather.ui.MusicEmbeds;
import dev.JustRed23.grandfather.ui.QueueComponent;
import dev.JustRed23.grandfather.utils.HttpUtils;
import dev.JustRed23.jdautils.JDAUtilities;
import dev.JustRed23.jdautils.command.CommandOption;
import dev.JustRed23.jdautils.component.SendableComponent;
import dev.JustRed23.jdautils.music.GuildMusicManager;
import dev.JustRed23.jdautils.music.PlaybackState;
import dev.JustRed23.jdautils.music.RepeatMode;
import dev.JustRed23.jdautils.music.event.*;
import dev.JustRed23.jdautils.music.search.Search;
import dev.JustRed23.jdautils.music.search.YouTubeSource;
import dev.JustRed23.jdautils.utils.TimeUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.function.Function;

public class MusicCommands {

    private static final YouTubeSource YT;

    static {
        try {
            YT = Search.YouTube(Bot.youtube_api_key);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Could not set up search sources", e);
        }
    }

    //<editor-fold desc="Conditions">
    private static final Function<SlashCommandInteractionEvent, Boolean> IN_VOICE_CHANNEL = event -> {
        assert event.getMember() != null; // This command is guild-only, so this should never be null
        if (event.getMember().getVoiceState() == null || event.getMember().getVoiceState().getChannel() == null) {
            event.reply("You must be in a voice channel to use this command!").setEphemeral(true).queue();
            return false;
        }
        return true;
    };

    private static final Function<SlashCommandInteractionEvent, Boolean> IN_SAME_VOICE_CHANNEL = event -> {
        // This command is guild-only, so these should never be null
        assert event.getGuild() != null;
        assert event.getMember() != null;

        if (!IN_VOICE_CHANNEL.apply(event)) return false;

        assert event.getMember().getVoiceState() != null; // We know for sure that the voice state is not null here

        AudioChannel channel = gmm(event).getCurrentChannel();

        if (channel != null && !channel.equals(event.getMember().getVoiceState().getChannel())) {
            event.reply("You must be in the same voice channel as the bot to use this command!").setEphemeral(true).queue();
            return false;
        } else if (channel == null) {
            event.reply("The bot is not in a voice channel! Use /music join to make the bot join your voice channel.").setEphemeral(true).queue();
            return false;
        }

        return true;
    };

    private static final Function<SlashCommandInteractionEvent, Boolean> BOT_IS_PLAYING = event -> {
        assert event.getGuild() != null;
        final GuildMusicManager musicManager = JDAUtilities.getGuildMusicManager(event.getGuild());
        if (!List.of(PlaybackState.LOADING, PlaybackState.PLAYING, PlaybackState.PAUSED).contains(musicManager.getPlaybackState())) {
            event.reply("The bot is not currently playing any music!").setEphemeral(true).queue();
            return false;
        }
        return true;
    };

    private static final Function<SlashCommandInteractionEvent, Boolean> QUEUE_NOT_EMPTY = event -> {
        assert event.getGuild() != null;
        final GuildMusicManager musicManager = JDAUtilities.getGuildMusicManager(event.getGuild());
        try {
            if (musicManager.queue().getQueue().isEmpty()) {
                event.reply("The music queue is currently empty!").setEphemeral(true).queue();
                return false;
            }
        } catch (UnsupportedOperationException e) {
            event.reply(e.getMessage()).queue();
            return false;
        }
        return true;
    };
    //</editor-fold>

    @SuppressWarnings("ConstantConditions") //suppress null warnings, as conditions handle those checks
    public static void register() {
        addListener();

        JDAUtilities.createSlashCommand("music", "All music commands")
                .addSubCommand("join", "Make the bot join your voice channel")
                    .addCondition(IN_VOICE_CHANNEL)
                    .executes(event -> {
                        event.deferReply().queue();
                        gmm(event).bind(event.getChannel().asTextChannel());
                        gmm(event).join(event.getMember().getVoiceState().getChannel());
                        event.getInteraction().getHook().sendMessage("Joined your voice channel!").queue();
                    })
                    .build()

                .addSubCommand("disconnect", "Make the bot leave the voice channel")
                    .addCondition(IN_SAME_VOICE_CHANNEL)
                    .executes(event -> {
                        gmm(event).disconnect();
                        event.getInteraction().getHook().sendMessage("Left the voice channel!").queue();
                    })
                    .build()

                .addSubCommand("play", "Play a song or playlist from YouTube")
                    .addOption(new CommandOption(OptionType.STRING, "query", "The search query or URL of the song/playlist to play", true)
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

                                    if (e instanceof GoogleJsonResponseException && e.getMessage().contains("quotaExceeded")) {
                                        App.LOGGER.warn("YouTube API request limit reached");
                                        return;
                                    }

                                    ErrorHandler.handleException("youtube-search-request", e);
                                    return;
                                }

                                if (search == null || search.isEmpty()) {
                                    event.replyChoices(List.of()).queue();
                                    return;
                                }

                                final List<Command.Choice> choices = search.stream()
                                        .map(e -> {
                                            String title = e.getSnippet().getTitle();
                                            if (title.length() > OptionData.MAX_CHOICE_NAME_LENGTH)
                                                title = title.substring(0, OptionData.MAX_CHOICE_NAME_LENGTH - 3) + "...";

                                            return new Command.Choice(title, YouTubeSource.getVideo(e.getId().getVideoId()));
                                        })
                                        .toList();

                                event.replyChoices(choices).queue();
                            })
                    )
                    .addCondition(IN_SAME_VOICE_CHANNEL)
                    .executes(event -> {
                        gmm(event).bind(event.getChannel().asTextChannel());
                        gmm(event).play(event.getOption("query").getAsString(), event.getMember().getVoiceState().getChannel(), event.getMember());
                    })
                    .build()

                .addSubCommand("pause", "Pause the currently playing song")
                    .addCondition(IN_SAME_VOICE_CHANNEL)
                    .addCondition(BOT_IS_PLAYING)
                    .executes(event -> gmm(event).pause())
                    .build()

                .addSubCommand("resume", "Resume the currently paused song")
                    .addCondition(IN_SAME_VOICE_CHANNEL)
                    .addCondition(BOT_IS_PLAYING)
                    .executes(event -> gmm(event).resume())
                    .build()

                .addSubCommand("stop", "Stop the music and clear the queue")
                    .addCondition(IN_SAME_VOICE_CHANNEL)
                    .addCondition(BOT_IS_PLAYING)
                    .executes(event -> gmm(event).stop())
                    .build()

                .addSubCommand("skip", "Skip the currently playing song")
                    .addCondition(IN_SAME_VOICE_CHANNEL)
                    .addCondition(BOT_IS_PLAYING)
                    .executes(event -> softCatch(event, () -> {
                        if (gmm(event).queue().skip())
                            ;//TODO: reply with a fancy embed
                        else
                            event.reply("There are no more songs in the queue, stopped playing").queue();
                    }))
                    .build()

                .addSubCommand("back", "Go back to the previous song")
                    .addCondition(IN_SAME_VOICE_CHANNEL)
                    .addCondition(BOT_IS_PLAYING)
                    .executes(event -> softCatch(event, () -> {
                        if (gmm(event).queue().back())
                            ;//TODO: reply with a fancy embed
                        else
                            event.reply("There is nothing to go back to").queue();
                    }))
                    .build()

                .addSubCommand("clear", "Clear the music queue")
                    .addCondition(IN_SAME_VOICE_CHANNEL)
                    .addCondition(QUEUE_NOT_EMPTY)
                    .executes(event -> softCatch(event, () -> gmm(event).queue().clear()))
                    .build()

                .addSubCommand("queue", "Show the current music queue")
                    .addCondition(QUEUE_NOT_EMPTY)
                    .executes(event -> {
                        SendableComponent component = JDAUtilities.createComponent(QueueComponent.class, new Class[]{Guild.class}, event.getGuild());
                        component.reply(event);
                    })
                    .build()

                .addSubCommand("seek", "Seek to a specific position in the currently playing song")
                    .addOption(new CommandOption(OptionType.STRING, "position", "The position to seek to (MM:SS or HH:MM:SS)", true))
                    .addCondition(IN_SAME_VOICE_CHANNEL)
                    .addCondition(BOT_IS_PLAYING)
                    .executes(event -> softCatch(event, () -> {
                        String posStr = event.getOption("position").getAsString();
                        gmm(event).seek(TimeUtils.timeToMillis(posStr));
                        event.getInteraction().getHook().sendMessage("Seeked to " + posStr + "!").queue();
                    }))
                    .build()

                .addSubCommand("shuffle", "Shuffle the current music queue")
                    .addCondition(IN_SAME_VOICE_CHANNEL)
                    .addCondition(QUEUE_NOT_EMPTY)
                    .executes(event -> softCatch(event, () -> {
                        gmm(event).queue().shuffle();
                        event.getInteraction().getHook().sendMessage("Shuffled music queue!").queue();
                    }))
                    .build()

                .addSubCommand("remove", "Remove a song from the queue by its position")
                    .addOption(new CommandOption(OptionType.INTEGER, "position", "The position of the song to remove (starting from 1)", true))
                    .addCondition(IN_SAME_VOICE_CHANNEL)
                    .addCondition(QUEUE_NOT_EMPTY)
                    .executes(event -> softCatch(event, () -> {
                        int pos = event.getOption("position").getAsInt() - 1;
                        gmm(event).queue().remove(pos);
                        event.getInteraction().getHook().sendMessage("Removed song at position " + (pos + 1) + " from the queue!").queue();
                    }))
                    .build()

                .addSubCommand("move", "Move a song in the queue from one position to another")
                    .addOption(new CommandOption(OptionType.INTEGER, "from", "The current position of the song (starting from 1)", true))
                    .addOption(new CommandOption(OptionType.INTEGER, "to", "The new position of the song (starting from 1)", true))
                    .addCondition(IN_SAME_VOICE_CHANNEL)
                    .addCondition(QUEUE_NOT_EMPTY)
                    .executes(event -> softCatch(event, () -> {
                        int from = event.getOption("from").getAsInt() - 1;
                        int to = event.getOption("to").getAsInt() - 1;
                        gmm(event).queue().move(from, to);
                        event.getInteraction().getHook().sendMessage("Moved song from position " + (from + 1) + " to position " + (to + 1) + " in the queue!").queue();
                    }))
                    .build()

                .addSubCommand("nowplaying", "Show the currently playing song")
                    .executes(event -> {
                        //TODO: reply with a fancy embed, reply with an embed saying no track playing if getCurrentTrack is empty
                    })
                    .build()

                .addSubCommand("volume", "Set the music volume (0-100)")
                    .addOption(new CommandOption(OptionType.INTEGER, "level", "The volume level to set (0-100)", true))
                    .addCondition(IN_SAME_VOICE_CHANNEL)
                    .executes(event -> softCatch(event, () -> {
                        int level = event.getOption("level").getAsInt();
                        gmm(event).options().setVolume(level / 100f);
                    }))
                    .build()

                .addSubCommand("repeat", "Toggle repeating the currently playing song")
                    .addOption(new CommandOption(OptionType.STRING, "mode", "The repeat mode (off, one, all)", true)
                            .addChoice("off", "off")
                            .addChoice("one", "one")
                            .addChoice("all", "all")
                    )
                    .addCondition(IN_SAME_VOICE_CHANNEL)
                    .executes(event -> softCatch(event, () -> {
                        String mode = event.getOption("mode").getAsString();
                        RepeatMode repeatMode = RepeatMode.get(mode);
                        gmm(event).options().setRepeatMode(repeatMode);
                        event.getInteraction().getHook().sendMessage("Set repeat mode to " + mode + "!").queue();
                    }))
                    .build()

                .setGuildOnly()
                .buildAndRegister();
    }

    private static void addListener() {
        JDAUtilities.getMusicManager().addEventListener(new MusicEventListener() { //TODO
            public void onTrackStart(@NotNull TrackStartEvent event) {
                sendEmbedInBoundChannel(event.guild(), MusicEmbeds.onStart(event.track()));
            }

            public void onTrackError(@NotNull TrackErrorEvent event) {
                sendEmbedInBoundChannel(event.guild(), MusicEmbeds.onError(event.track()));
                ErrorHandler.handleException("music-track-error", event.error());
            }

            public void onTrackNotFound(@NotNull TrackNotFoundEvent event) {
                sendEmbedInBoundChannel(event.guild(), MusicEmbeds.onNotFound(event.url()));
            }

            public void onQueueUpdate(@NotNull QueueUpdateEvent event) {
                sendEmbedInBoundChannel(event.guild(), MusicEmbeds.onQueueUpdate(event, gmm(event.guild())));
            }
        });
    }

    private static void sendEmbedInBoundChannel(Guild guild, EmbedBuilder embed) {
        TextChannel channel = gmm(guild).getBoundChannel();
        if (channel != null) {
            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    private static GuildMusicManager gmm(SlashCommandInteractionEvent event) {
        assert event.getGuild() != null; // This command is guild-only, so this should never be null
        return gmm(event.getGuild());
    }

    private static GuildMusicManager gmm(Guild guild) {
        return JDAUtilities.getGuildMusicManager(guild);
    }

    private static void softCatch(SlashCommandInteractionEvent event, Runnable runnable) {
        event.deferReply().queue();
        try {
            runnable.run();
        } catch (RuntimeException e) {
            event.getInteraction().getHook().sendMessage(e.getMessage()).queue();
        }
    }
}
