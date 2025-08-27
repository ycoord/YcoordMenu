package ru.ycoord;

import org.bukkit.configuration.ConfigurationSection;
import ru.ycoord.core.commands.Command;
import ru.ycoord.examples.commands.MenusCommand;

import java.util.List;

public class YcoordMenus extends YcoordPlugin {
    private static YcoordMenus instance;

    public static YcoordMenus getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        super.onEnable();
    }

    @Override
    public void load(ConfigurationSection cfg, boolean reload) {
        super.load(cfg, reload);
    }

    @Override
    public List<Command> getRootCommands() {
        return List.of(new MenusCommand());
    }
}