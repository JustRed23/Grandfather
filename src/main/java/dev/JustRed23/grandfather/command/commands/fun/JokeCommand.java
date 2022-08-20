package dev.JustRed23.grandfather.command.commands.fun;

import dev.JustRed23.grandfather.bettertemplate.FormattedTemplate;
import dev.JustRed23.grandfather.bettertemplate.TemplateType;
import dev.JustRed23.grandfather.bettertemplate.UnformattedTemplate;
import dev.JustRed23.grandfather.command.Category;
import dev.JustRed23.grandfather.command.CommandContext;
import dev.JustRed23.grandfather.command.types.DefaultGuildCommand;
import dev.JustRed23.grandfather.utils.msg.EmbedUtils;
import dev.JustRed23.grandfather.utils.msg.MessageUtils;
import dev.JustRed23.stonebrick.exceptions.NetRequestException;
import dev.JustRed23.stonebrick.net.Callback;
import dev.JustRed23.stonebrick.net.NetworkManager;
import dev.JustRed23.stonebrick.net.Request;
import dev.JustRed23.stonebrick.net.Response;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class JokeCommand extends DefaultGuildCommand {

    public void execute(CommandContext context) {
        NetworkManager.get("https://icanhazdadjoke.com/").header("Accept", "application/json").async(new Callback() {
            public void response(Request request, Response response, NetRequestException e) {
                if (e != null || !response.isSuccess()) {
                    FormattedTemplate.from("An unexpected error occurred: " + response.responseCode(), TemplateType.ERROR).embed(context.getEvent());
                    return;
                }
                MessageUtils.sendMessage(response.asJSONObject().getString("joke"), context.getEvent());
            }
        });
    }

    public String getName() {
        return "joke";
    }

    public String getHelp() {
        return "Tells a random dad joke";
    }

    public CommandData getCommandData() {
        return Commands.slash(getName(), getHelp());
    }

    public Category getCategory() {
        return Category.FUN;
    }
}
