package dev.JustRed23.grandfather.command.commands.admin;

import dev.JustRed23.grandfather.bettertemplate.Templates;
import dev.JustRed23.grandfather.command.CommandContext;
import dev.JustRed23.grandfather.command.types.DefaultAdminCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CleanCommand extends DefaultAdminCommand {

    public void execute(CommandContext context, MessageReceivedEvent event) {
        List<String> args = context.getArgs();
        int amount;

        if (args.size() > 0) {
            try {
                amount = Integer.parseInt(args.get(0));
            } catch (NumberFormatException e) {
                Templates.Clean.provide_amount.message(event);
                return;
            }

            event.getMessage().delete().complete();
            clean(event, event.getChannel().asTextChannel(), amount);
        } else Templates.Clean.provide_amount.message(event);
    }

    public void execute(CommandContext context, SlashCommandInteractionEvent event) {
        clean(event, event.getChannel().asTextChannel(), event.getOption("amount").getAsInt());
    }

    private void clean(Event event, TextChannel channel, int amount) {
        if (amount > 1000 || amount < 1) {
            Templates.Clean.invalid_amount.messageAndDelete(event, 5, TimeUnit.SECONDS);
            return;
        }

        channel.getIterableHistory().takeAsync(amount).thenAcceptAsync(messages -> {
            int size = messages.stream().filter(m -> m.getType().canDelete()).toList().size();

            if (size == 0) {
                Templates.Clean.no_messages.message(event);
                return;
            }

            try {
                List<CompletableFuture<Void>> msgs2delete = channel.purgeMessages(messages).stream().toList();
                CompletableFuture.allOf(msgs2delete.toArray(CompletableFuture[]::new)).get();
            } catch (Exception e) {
                Templates.Clean.fail.format(e.getMessage()).message(event);
                e.printStackTrace();
            }

            Templates.Clean.success.format(size).messageAndDelete(event, 5, TimeUnit.SECONDS);
        });
    }

    public String getName() {
        return "clean";
    }

    public List<String> getAliases() {
        return List.of("cleanup", "purge");
    }

    public String getHelp() {
        return "Deletes a specified amount of messages";
    }

    public CommandData getCommandData() {
        return Commands.slash(getName(), getHelp())
                .addOption(OptionType.INTEGER, "amount", "The amount of messages to delete (1-1000)", true)
                .setGuildOnly(true);
    }

    public List<Permission> getUserPermissions() {
        return List.of(Permission.MESSAGE_MANAGE);
    }

    public List<Permission> getBotPermissions() {
        return List.of(Permission.MESSAGE_MANAGE);
    }
}
