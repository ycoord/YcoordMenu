package ru.ycoord.examples.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import ru.ycoord.YcoordCore;
import ru.ycoord.core.commands.AdminCommand;
import ru.ycoord.core.commands.Command;
import ru.ycoord.core.commands.requirements.Requirement;
import ru.ycoord.core.commands.requirements.StringRequirement;
import ru.ycoord.core.gui.GuiBase;
import ru.ycoord.core.messages.MessageBase;
import ru.ycoord.core.messages.MessagePlaceholders;

import java.util.List;
import java.util.Map;

public class OpenCommand extends AdminCommand {
    @Override
    public String getName() {
        return "open";
    }

    private static class GuiRequirement extends StringRequirement {

        public GuiRequirement(Command command) {
            super(command);
        }

        @Override
        public List<String> subComplete() {
            YcoordCore core = YcoordCore.getInstance();
            Map<String, ConfigurationSection> menus = core.getMenus();
            return menus.keySet().stream().toList();
        }

        @Override
        public void failed(CommandSender sender) {
            if (sender instanceof Player player) {
                YcoordCore.getInstance().getChatMessage().sendMessageIdAsync(MessageBase.Level.ERROR, player, "messages.ym-gui-requirement-failed");
            }
        }

        public Object validate(CommandSender player, String param) {
            YcoordCore core = YcoordCore.getInstance();
            Map<String, ConfigurationSection> menus = core.getMenus();
            ConfigurationSection guiConfig = menus.getOrDefault(param, null);
            if (guiConfig == null) {
                throw new IllegalArgumentException();
            }
            return new GuiBase(guiConfig);
        }
    }

    @Override
    public List<Requirement> getRequirements(CommandSender sender) {
        return List.of(new GuiRequirement(this));
    }

    @Override
    public boolean execute(CommandSender sender, List<String> args, List<Object> params) {
        if (!super.execute(sender, args, params))
            return false;

        if (sender instanceof Player player) {
            GuiBase gui = getParam();

            gui.open(player);
        }

        return true;
    }

    @Override
    public String getDescription(CommandSender commandSender) {
        return YcoordCore.getInstance().getChatMessage().makeMessageId(MessageBase.Level.NONE, "messages.ym-open-desc", new MessagePlaceholders(null));
    }
}
