package dev.JustRed23.grandfather.ui;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.JustRed23.grandfather.utils.TimeUtils;
import dev.JustRed23.jdautils.component.Component;
import dev.JustRed23.jdautils.component.SendableComponent;
import dev.JustRed23.jdautils.component.interact.SmartButton;
import dev.JustRed23.jdautils.music.AudioManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.InteractionCallbackAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.dv8tion.jda.api.utils.MarkdownSanitizer.escape;

public class QueueComponent extends SendableComponent {

    private static final int TRACKS_PER_PAGE = 5;

    private final Guild guild;

    private EmbedBuilder builder;
    private SmartButton back, next, close;

    private int currentPage = 0;

    public QueueComponent(Guild guild) {
        super("queue");
        this.guild = guild;
    }

    protected MessageCreateAction onSend(@NotNull MessageReceivedEvent event) {
        return null;
    }

    protected InteractionCallbackAction onReply(@NotNull SlashCommandInteractionEvent event) {
        return event.replyEmbeds(builder.build())
                .addActionRow(back.build(), next.build(), close.build());
    }

    protected WebhookMessageEditAction<Message> onEdit(@NotNull InteractionHook hook) {
        return hook.editOriginalEmbeds(builder.build());
    }

    protected List<Component> getChildren() {
        return List.of(back, next, close);
    }

    protected void onCreate() {
        fillEmbed();

        back = SmartButton.primary(Emoji.fromFormatted("U+2B05 U+FE0F"))
                .withListener(event -> {
                    int page = Math.max(0, currentPage - 1);

                    if (page != currentPage) {
                        currentPage = page;
                        fillEmbed();
                        event.deferEdit().complete().editOriginalEmbeds(builder.build()).queue();
                    } else event.deferEdit().queue();
                });

        next = SmartButton.primary(Emoji.fromFormatted("U+27A1 U+FE0F"))
                .withListener(event -> {
                    int page = Math.min((int) Math.ceil((double) AudioManager.get(guild).getScheduler().getQueue().size() / TRACKS_PER_PAGE) - 1, currentPage + 1);

                    if (page != currentPage) {
                        currentPage = page;
                        fillEmbed();
                        event.deferEdit().complete().editOriginalEmbeds(builder.build()).queue();
                    } else event.deferEdit().queue();
                });

        close = SmartButton.danger(Emoji.fromFormatted("U+1F5D1 U+FE0F"))
                .withListener(event -> event.deferEdit().complete().deleteOriginal().queue());
    }

    private void fillEmbed() {
        AudioManager manager = AudioManager.get(guild);
        List<AudioTrack> queue = List.copyOf(manager.getScheduler().getQueue());

        builder = MusicEmbeds.createDefault();

        if (currentPage == 0) {
            AudioTrack currentTrack = manager.getScheduler().getPlayingTrack();
            if (currentTrack != null) {
                StringBuilder description = new StringBuilder();
                description.append("\u23AF".repeat(30)).append("\n\n");

                description.append(escape(currentTrack.getInfo().title));
                if (manager.getScheduler().isPaused())
                    description.append(" ***(Paused)***");

                if (manager.getScheduler().isLooping())
                    description.append(" ***(Looping)***");

                description.append("\n");

                description.append("`")
                        .append(TimeUtils.msToFormatted(currentTrack.getPosition(), TimeUtils.TimeFormat.CLOCK))
                        .append("` / `")
                        .append(TimeUtils.msToFormatted(currentTrack.getDuration(), TimeUtils.TimeFormat.CLOCK))
                        .append("`")
                        .append("\n");

                description.append(EmbedBuilder.ZERO_WIDTH_SPACE).append("\n");

                builder.addField("Now playing", description.toString(), false);
            }
        }

        int start = currentPage * TRACKS_PER_PAGE;
        int end = Math.min(start + TRACKS_PER_PAGE, queue.size());

        StringBuilder queueBuilder = new StringBuilder();
        queueBuilder.append("\u23AF".repeat(30));

        for (int i = start; i < end; i++) {
            AudioTrack track = queue.get(i);
            queueBuilder.append("\n\n").append(i + 1).append(". ").append(escape(track.getInfo().title));
            queueBuilder.append("\n").append("`")
                    .append(TimeUtils.msToFormatted(track.getDuration(), TimeUtils.TimeFormat.CLOCK))
                    .append("`");
        }

        builder.addField("Queue", queueBuilder.toString(), false);

        builder.setFooter("Page " + (currentPage + 1) + " / " + (int) Math.ceil((double) queue.size() / TRACKS_PER_PAGE));
    }

    protected void onRemove() {
        back = null;
        next = null;
        close = null;
        builder = null;
    }
}
