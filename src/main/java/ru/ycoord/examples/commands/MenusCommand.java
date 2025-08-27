package ru.ycoord.examples.commands;

import org.bukkit.command.CommandSender;
import ru.ycoord.YcoordCore;
import ru.ycoord.core.commands.AdminCommand;
import ru.ycoord.core.commands.requirements.Requirement;
import ru.ycoord.core.commands.requirements.SubcommandRequirement;
import ru.ycoord.core.messages.MessageBase;
import ru.ycoord.core.messages.MessagePlaceholders;
import ru.ycoord.examples.commands.subcommands.MigrateCommand;
import ru.ycoord.examples.commands.subcommands.OpenCommand;

import java.util.List;

public class MenusCommand extends AdminCommand {
    @Override
    public String getName() {
        return "ym";
    }

    @Override
    public List<Requirement> getRequirements(CommandSender sender) {
        return List.of(new SubcommandRequirement(this, List.of(
                new MigrateCommand(),
                new OpenCommand()
        )));
    }

    @Override
    public String getDescription(CommandSender commandSender) {
        return YcoordCore.getInstance().getChatMessage().makeMessageId(MessageBase.Level.NONE, "messages.ym-desc", new MessagePlaceholders(null));
    }
}
