package dev.JustRed23.grandfather.command.commands.general;

import dev.JustRed23.grandfather.bettertemplate.Templates;
import dev.JustRed23.grandfather.command.Category;
import dev.JustRed23.grandfather.command.CommandContext;
import dev.JustRed23.grandfather.command.ICommand;
import dev.JustRed23.grandfather.command.handler.CommandHandler;
import dev.JustRed23.grandfather.command.types.DefaultGuildCommand;
import dev.JustRed23.grandfather.utils.msg.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class HelpCommand extends DefaultGuildCommand {

    public void execute(CommandContext context, MessageReceivedEvent event) {
        List<String> args = context.getArgs();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.MAGENTA);

        if (args.isEmpty()) {
            builder.setTitle("Showing list of all commands");
            Arrays.stream(Category.values()).filter(Category::canShow).forEach(category -> {
                List<String> categoryCommands = CommandHandler.getCommandsByCategory(category)
                        .stream()
                        .map(ICommand::getName)
                        .sorted()
                        .toList();

                String escape = !category.equals(Category.values()[Category.values().length - 1]) ? "\u200E" : "";

                StringBuilder strBuilder = new StringBuilder();

                for (String command : categoryCommands)
                    strBuilder.append("> ").append(command).append("\n");

                builder.addField(category.name(), strBuilder + escape, false);
            });

            EmbedUtils.sendEmbed(builder, event);
            return;
        }

        String searchTerm = args.get(0);

        Arrays.stream(Category.values()).filter(Category::canShow).forEach(category -> {
            if (searchTerm.equalsIgnoreCase(category.name())) {
                builder.setTitle("Showing commands of category '" + category.toString().toLowerCase() + "'");

                List<String> categoryCommands = CommandHandler.getCommandsByCategory(category)
                        .stream()
                        .map(ICommand::getName)
                        .sorted()
                        .toList();

                StringBuilder strBuilder = new StringBuilder();

                for (String command : categoryCommands)
                    strBuilder.append("> ").append(command).append("\n");

                builder.addField(category.name(), strBuilder.toString(), false);
            } else return;

            EmbedUtils.sendEmbed(builder, event);
        });

        ICommand command = CommandHandler.getCommand(searchTerm);

        if (command == null) {
            Templates.Help.no_command_or_category.format(searchTerm).embed(event);
            return;
        }

        builder.setTitle("Showing help for command '" + command.getName().toLowerCase() + "'");
        builder.setDescription(command.getHelp());
        if (!command.getAliases().isEmpty())
            builder.setFooter("Aliases: " + command.getAliases());

        EmbedUtils.sendEmbed(builder, event);
    }

    public void execute(CommandContext context, SlashCommandInteractionEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.MAGENTA);

        switch (event.getSubcommandName()) {
            case "show" -> {
                builder.setTitle("Showing list of all commands");
                Arrays.stream(Category.values()).filter(Category::canShow).forEach(category -> {
                    List<String> categoryCommands = CommandHandler.getCommandsByCategory(category)
                            .stream()
                            .map(ICommand::getName)
                            .sorted()
                            .toList();

                    String escape = !category.equals(Category.values()[Category.values().length - 1]) ? "\u200E" : "";

                    StringBuilder strBuilder = new StringBuilder();

                    for (String command : categoryCommands)
                        strBuilder.append("> ").append(command).append("\n");

                    builder.addField(category.name(), strBuilder + escape, false);
                });

                EmbedUtils.sendEmbed(builder, event);
            }
            case "command" -> {
                final String search = event.getOption("command-name").getAsString();

                ICommand command = CommandHandler.getCommand(search);

                if (command == null) {
                    Templates.Help.no_command.format(search).embed(event);
                    return;
                }

                builder.setTitle("Showing help for command '" + command.getName().toLowerCase() + "'");
                builder.setDescription(command.getHelp());
                if (!command.getAliases().isEmpty())
                    builder.setFooter("Aliases: " + command.getAliases());

                EmbedUtils.sendEmbed(builder, event);
            }
            case "category" -> {
                String search = event.getOption("category-name").getAsString();

                Arrays.stream(Category.values()).filter(Category::canShow).forEach(category -> {
                    if (search.equalsIgnoreCase(category.name())) {
                        builder.setTitle("Showing commands of category '" + category.toString().toLowerCase() + "'");

                        List<String> categoryCommands = CommandHandler.getCommandsByCategory(category)
                                .stream()
                                .map(ICommand::getName)
                                .sorted()
                                .toList();

                        StringBuilder strBuilder = new StringBuilder();

                        for (String command : categoryCommands)
                            strBuilder.append("> ").append(command).append("\n");

                        builder.addField(category.name(), strBuilder.toString(), false);
                    } else return;

                    EmbedUtils.sendEmbed(builder, event);
                });
            }
        }
    }

    public String getName() {
        return "help";
    }

    public String getHelp() {
        return "Shows a list of all available commands";
    }

    public CommandData getCommandData() {
        return Commands.slash(getName(), getHelp())
                .addSubcommands(
                        new SubcommandData("show", getHelp()),
                        new SubcommandData("command", "Shows the command description")
                                .addOption(OptionType.STRING, "command-name", "The name of the command", true),
                        new SubcommandData("category", "Shows all the commands in a specific category")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "category-name", "The name of the category", true)
                                                .addChoices(
                                                        Arrays.stream(Category.values())
                                                                .filter(Category::canShow)
                                                                .map(category -> new Command.Choice(category.name().toLowerCase(), category.name().toLowerCase()))
                                                                .toList()
                                                )
                                )
                ).setGuildOnly(true);
    }

    public Category getCategory() {
        return Category.GENERAL;
    }
}
