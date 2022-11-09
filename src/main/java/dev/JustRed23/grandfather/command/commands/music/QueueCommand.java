package dev.JustRed23.grandfather.command.commands.music;

import dev.JustRed23.grandfather.command.CommandContext;
import dev.JustRed23.grandfather.command.types.DefaultMusicCommand;
import dev.JustRed23.grandfather.music.AudioPlayerManager;
import dev.JustRed23.grandfather.music.MusicManager;
import dev.JustRed23.grandfather.utils.EmojiUtils;
import dev.JustRed23.grandfather.utils.MusicUtils;
import dev.JustRed23.grandfather.utils.btn.BetterButton;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Collections;
import java.util.List;

public class QueueCommand extends DefaultMusicCommand {

    public void execute(CommandContext context, MessageReceivedEvent event) {
        queue(event.getGuild(), event.getChannel().asTextChannel(), event.getAuthor());
    }

    public void execute(CommandContext context, SlashCommandInteractionEvent event) {
        queue(event.getGuild(), event.getChannel().asTextChannel(), event.getUser());
    }

    private void queue(Guild guild, TextChannel channel, User author) {
        MusicManager manager = AudioPlayerManager.getInstance().getMusicManager(guild);

        channel.sendMessageEmbeds(MusicUtils.buildQueue(manager, 1).build()).queue(message -> {
            Button[] buttons = new Button[] {
                    new BetterButton().primary("grandfather:queue:prev", Emoji.fromUnicode(EmojiUtils.Music.PREV_TRACK))
                            .onEvent(guild, author, trigger -> {
                                if (MusicUtils.getPage(message.getIdLong()) == 1) {
                                    trigger.deferEdit().queue();
                                    return;
                                }

                                trigger.deferEdit().queue(hook -> hook.editOriginalEmbeds(MusicUtils.buildQueue(manager, MusicUtils.prevPage(message.getIdLong())).build()).queue());
                            }, complete -> {})
                            .noInvalidateAfterUse()
                            .build(message.getIdLong()),
                    new BetterButton().primary("grandfather:queue:next", Emoji.fromUnicode(EmojiUtils.Music.NEXT_TRACK))
                            .onEvent(guild, author, trigger -> {
                                int size = manager.getScheduler().getQueue().size() - 4; //-4 for first page
                                if (size <= 0) {
                                    trigger.deferEdit().queue();
                                    return;
                                }

                                int totalPages = 1 + (int) Math.ceil(size / 5.0);

                                if (totalPages == 1 || MusicUtils.getPage(message.getIdLong()) == totalPages) {
                                    trigger.deferEdit().queue();
                                    return;
                                }

                                trigger.deferEdit().queue(hook -> hook.editOriginalEmbeds(MusicUtils.buildQueue(manager, MusicUtils.nextPage(message.getIdLong())).build()).queue());
                            }, complete -> {})
                            .noInvalidateAfterUse()
                            .build(message.getIdLong())
            };

            BetterButton del = new BetterButton()
                    .danger("grandfather:queue:delete", Emoji.fromUnicode(EmojiUtils.General.GRAY_NO))
                    .onEvent(channel.getGuild(), author, unused -> {}, e -> e.deferEdit().queue(interactionHook -> interactionHook.deleteOriginal().queue()));

            message.editMessageComponents(ActionRow.of(buttons), ActionRow.of(del.build(message.getIdLong()))).queue();
        });
    }

    public String getName() {
        return "queue";
    }

    public List<String> getAliases() {
        return Collections.singletonList("q");
    }

    public String getHelp() {
        return "Shows the queue and the currently playing song";
    }

    public CommandData getCommandData() {
        return Commands.slash(getName(), getHelp()).setGuildOnly(true);
    }

    public boolean needConnectedVoice() {
        return true;
    }

    public boolean sameVoice() {
        return true;
    }

    public boolean activeQueue() {
        return true;
    }

    public boolean activePlayer() {
        return true;
    }
}
