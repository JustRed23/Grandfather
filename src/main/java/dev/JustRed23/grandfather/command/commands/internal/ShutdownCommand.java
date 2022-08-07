package dev.JustRed23.grandfather.command.commands.internal;

import dev.JustRed23.grandfather.App;
import dev.JustRed23.grandfather.command.CommandContext;
import dev.JustRed23.grandfather.command.types.DefaultInternalCommand;
import dev.JustRed23.grandfather.utils.msg.MessageUtils;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class ShutdownCommand extends DefaultInternalCommand {

    public void execute(CommandContext context) {
        MessageUtils.sendMessage("Shutting down", context.getEvent());
        App.exit();
    }

    public String getName() {
        return "shutdown";
    }

    public String getHelp() {
        return "Shuts the bot down";
    }

    public CommandData getCommandData() {
        return Commands.slash(getName(), getHelp());
    }
}
