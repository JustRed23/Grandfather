package dev.JustRed23.grandfather.event;

import dev.JustRed23.grandfather.App;
import dev.JustRed23.grandfather.command.handler.CommandHandler;
import dev.JustRed23.grandfather.utils.MusicUtils;
import dev.JustRed23.grandfather.utils.msg.ReactionHandler;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BasicEventListener extends ListenerAdapter {

    public void onReady(@NotNull ReadyEvent event) {
        App.LOGGER.info("Bot is ready! Running version " + App.version.gitHash());
    }

    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        //TODO
        event.getGuild().updateCommands().addCommands(CommandHandler.getCommandData()).queue();
    }

    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        //TODO
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        CommandHandler.handle(event.getChannel(), event.getMessage().getContentRaw(), event);
    }

    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        CommandHandler.getButtonHandler(event.getGuild()).handleRemove(event.getMessageIdLong());
        MusicUtils.invalidatePage(event.getMessageIdLong());
    }

    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        CommandHandler.handle(event.getChannel(), null, event);
    }

    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getUser().isBot())
            return;

        CommandHandler.getButtonHandler(event.getGuild()).handle(event);
    }

    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot() || !event.isFromGuild())
            return;

        ReactionHandler handler = CommandHandler.getReactionHandler(event.getGuild());

        if (handler.canHandle(event.getGuild().getIdLong(), event.getMessageIdLong()))
            handler.handle(event.getChannel().asTextChannel(), event.getMessageIdLong(), event.getUserIdLong(), event.getReaction());
    }
}
