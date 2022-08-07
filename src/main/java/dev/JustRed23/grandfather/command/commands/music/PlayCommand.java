package dev.JustRed23.grandfather.command.commands.music;

import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.VideoContentDetails;
import dev.JustRed23.grandfather.command.CommandContext;
import dev.JustRed23.grandfather.command.types.DefaultMusicCommand;
import dev.JustRed23.grandfather.music.AudioPlayerManager;
import dev.JustRed23.grandfather.music.MusicManager;
import dev.JustRed23.grandfather.template.Templates;
import dev.JustRed23.grandfather.utils.EmojiUtils;
import dev.JustRed23.grandfather.utils.HttpUtils;
import dev.JustRed23.grandfather.utils.TimeUtils;
import dev.JustRed23.grandfather.utils.YoutubeUtils;
import dev.JustRed23.grandfather.utils.btn.BetterButton;
import dev.JustRed23.grandfather.utils.msg.EmbedUtils;
import dev.JustRed23.grandfather.utils.msg.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayCommand extends DefaultMusicCommand {

    public void execute(CommandContext context, MessageReceivedEvent event) {
        List<String> args = context.getArgs();
        TextChannel channel = event.getChannel().asTextChannel();

        Guild guild = event.getGuild();
        Member bot = guild.getSelfMember();
        Member author = event.getMember();

        GuildVoiceState botState = bot.getVoiceState();
        GuildVoiceState memberState = author.getVoiceState();

        MusicManager manager = AudioPlayerManager.getInstance().getMusicManager(guild);
        manager.getScheduler().setChannel(channel);

        if (args.isEmpty() && !manager.getScheduler().isPaused()) {
            EmbedUtils.sendTemplateEmbed(Templates.music.no_link, event);
            return;
        }

        if (!memberState.inAudioChannel()) {
            EmbedUtils.sendTemplateEmbed(Templates.music.user_not_connected, event);
            return;
        }

        if (!botState.inAudioChannel()) {
            JoinCommand.join(author, bot, channel, context, false);
            playSong(args, channel, author.getUser(), event);
            return;
        }

        if (!memberState.getChannel().equals(botState.getChannel())) {
            EmbedUtils.sendTemplateEmbed(Templates.music.in_different_channel, event);
            return;
        }

        if (args.isEmpty() && manager.getScheduler().isPaused()) {
            manager.getScheduler().pause(true);
            MessageUtils.sendTemplateMessage(Templates.music.resumed, event);
            return;
        }

        playSong(args, channel, author.getUser(), event);
    }

    public void execute(CommandContext context, SlashCommandInteractionEvent event) {
        OptionMapping queryOption = event.getOption("query");
        String query = queryOption != null ? queryOption.getAsString() : "";

        TextChannel channel = event.getChannel().asTextChannel();

        Guild guild = event.getGuild();
        Member bot = guild.getSelfMember();
        Member author = event.getMember();

        GuildVoiceState botState = bot.getVoiceState();
        GuildVoiceState memberState = author.getVoiceState();

        MusicManager manager = AudioPlayerManager.getInstance().getMusicManager(guild);
        manager.getScheduler().setChannel(channel);

        if (query.isEmpty() && !manager.getScheduler().isPaused()) {
            EmbedUtils.sendTemplateEmbed(Templates.music.no_link, event);
            return;
        }

        if (!memberState.inAudioChannel()) {
            EmbedUtils.sendTemplateEmbed(Templates.music.user_not_connected, event);
            return;
        }

        if (!botState.inAudioChannel()) {
            JoinCommand.join(author, bot, channel, context, true);
            playSong(Collections.singletonList(query), channel, author.getUser(), event);
            return;
        }

        if (!memberState.getChannel().equals(botState.getChannel())) {
            EmbedUtils.sendTemplateEmbed(Templates.music.in_different_channel, event);
            return;
        }

        if (query.isEmpty() && manager.getScheduler().isPaused()) {
            manager.getScheduler().pause(true);
            MessageUtils.sendMessage(EmojiUtils.Music.PLAY + " Resumed currently playing track.", event);
            return;
        }

        playSong(Collections.singletonList(query), channel, author.getUser(), event);
    }

    private void playSong(List<String> args, TextChannel channel, User user, Event event) {
        String url = String.join(" ", args);

        if (!HttpUtils.isUrl(url)) {
            List<SearchResult> searched;

            try {
                searched = YoutubeUtils.ytSearch(url, 5);

                if (searched == null) {
                    MessageUtils.sendMessage(EmojiUtils.Misc.MAGNIFYING_GLASS + " No results found for `" + url + "`", event);
                    return;
                }

                showResults(url, channel, user, searched, event);
            } catch (IOException e) {
                MessageUtils.sendMessage(EmojiUtils.General.NO + " Something went wrong. Try again later.", event);
                e.printStackTrace();
            }
        } else
            AudioPlayerManager.getInstance().loadAndPlay(channel, user, url);
    }

    private void showResults(String url, TextChannel channel, User user, List<SearchResult> searched, Event event) throws IOException {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.YELLOW);
        builder.setTitle("Showing results for: `" + url + "`");
        builder.setThumbnail(YoutubeUtils.getThumbnail(searched.get(0).getId().getVideoId()));

        List<VideoContentDetails> durations = YoutubeUtils.getVideoDetails(searched.stream().map(searchResult -> searchResult.getId().getVideoId()).toList());

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < searched.size(); i++) {
            SearchResult result = searched.get(i);
            stringBuilder.append("`#")
                    .append(i + 1)
                    .append(".` ")
                    .append(result.getSnippet().getTitle())
                    .append("\n")
                    .append("**(")
                    .append(TimeUtils.youtubeTime(durations.get(i).getDuration()))
                    .append(")**")
                    .append("\n\n");
        }

        builder.addField("", stringBuilder.toString(), false);

        List<Button> buttons = new ArrayList<>();

        for (int i = 1; i <= searched.size(); i++) {
            int finalI = i;
            buttons.add(new BetterButton().primary("grandfather:play:option-" + i, String.valueOf(i)).onEvent(
                    channel.getGuild(), user, unused -> {},
                    e -> {
                        e.deferEdit().queue(interactionHook -> interactionHook.deleteOriginal().queue());
                        AudioPlayerManager.getInstance().loadAndPlay(channel, user, YoutubeUtils.getVideo(searched.get(finalI - 1).getId().getVideoId()));
                    }
            ).build());
        }

        EmbedUtils.sendEmbedWithActionRows(builder, event, ActionRow.of(buttons));
    }

    public String getName() {
        return "play";
    }

    public String getHelp() {
        return "Plays the requested song";
    }

    public CommandData getCommandData() {
        return Commands.slash(getName(), getHelp())
                .addOption(OptionType.STRING, "query", "A search term or url for the bot to play", false)
                .setGuildOnly(true);
    }

    public List<String> getAliases() {
        return Collections.singletonList("p");
    }

    public boolean needConnectedVoice() {
        //if this would be true, the bot would not be able to join the current channel
        return false;
    }

    public boolean sameVoice() {
        return false;
    }

    public boolean activeQueue() {
        return false;
    }

    public boolean activePlayer() {
        return false;
    }
}
