package ru.ycoord.examples.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.ycoord.YcoordCore;
import ru.ycoord.core.commands.AdminCommand;
import ru.ycoord.core.messages.MessageBase;
import ru.ycoord.core.messages.MessagePlaceholders;
import ru.ycoord.migration.MigrationService;

import java.util.List;

public class MigrateCommand extends AdminCommand {
    @Override
    public String getName() {
        return "migrate";
    }

    @Override
    public boolean execute(CommandSender sender, List<String> args, List<Object> params) {
        if (!super.execute(sender, args, params))
            return false;

        if (sender instanceof Player player)
            new MigrationService().migrateAsync(player);

        return true;
    }

    @Override
    public String getDescription(CommandSender commandSender) {
        return YcoordCore.getInstance().getChatMessage().makeMessageId(MessageBase.Level.NONE, "messages.ym-migrate-desc", new MessagePlaceholders(null));
    }
}
