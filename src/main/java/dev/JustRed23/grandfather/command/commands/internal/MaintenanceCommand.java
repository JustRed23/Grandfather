package dev.JustRed23.grandfather.command.commands.internal;

import dev.JustRed23.grandfather.App;
import dev.JustRed23.grandfather.command.CommandContext;
import dev.JustRed23.grandfather.command.handler.CommandChecker;
import dev.JustRed23.grandfather.command.types.DefaultInternalCommand;
import dev.JustRed23.grandfather.music.AudioPlayerManager;
import dev.JustRed23.grandfather.utils.msg.MessageUtils;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.sharding.ShardManager;

public class MaintenanceCommand extends DefaultInternalCommand {

    private boolean maintenance = false;

    public void execute(CommandContext context) {
        maintenance = !maintenance;
        MessageUtils.sendMessage("Maintenance mode " + (maintenance ? "enabled" : "disabled"), context.getEvent());
        maintenance();
    }

    private void maintenance() {
        final ShardManager shardManager = App.getShardManager();
        if (maintenance) {
            CommandChecker.disable();
            AudioPlayerManager.getInstance().disconnect();
            shardManager.setStatus(OnlineStatus.IDLE);
            shardManager.setActivity(Activity.listening("MAINTENANCE MODE"));
        } else {
            CommandChecker.enable();
            shardManager.setStatus(OnlineStatus.DO_NOT_DISTURB);
            shardManager.setActivity(App.getDefaultActivity());
        }
    }

    public String getName() {
        return "maintenance";
    }

    public String getHelp() {
        return "Enables maintenance mode, which will prevent the bot from responding to commands";
    }

    public CommandData getCommandData() {
        return Commands.slash(getName(), getHelp());
    }
}
