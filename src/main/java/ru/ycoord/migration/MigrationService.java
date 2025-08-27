package ru.ycoord.migration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.ycoord.YcoordCore;
import ru.ycoord.YcoordMenus;
import ru.ycoord.core.messages.ChatMessage;
import ru.ycoord.core.messages.MessageBase;
import ru.ycoord.core.messages.MessagePlaceholders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class MigrationService {

    public CompletableFuture<Boolean> processFileAsync(File file, String name, File resultFile) {
        return CompletableFuture.supplyAsync(() -> {
            YamlConfiguration menuConfig = YamlConfiguration.loadConfiguration(file);
            YamlConfiguration resultMenu = YamlConfiguration.loadConfiguration(resultFile);
            resultMenu.set("name", name);

            resultMenu.set("title", menuConfig.getString("menu_title", "ЗАГОЛОВОК НЕ НАЙДЕН"));
            int size = menuConfig.getInt("size", 54);
            resultMenu.set("size", size);
            int w = 9;
            int h = size / w;

            HashMap<String, Character> nameToCharacter = new HashMap<>();
            HashMap<String, List<Integer>> nameToSlots = new HashMap<>();

            ConfigurationSection menuItems = menuConfig.getConfigurationSection("items");
            ConfigurationSection newMenuItems = resultMenu.createSection("items");

            assert menuItems != null;
            int counter = 0;
            for (String itemName : menuItems.getKeys(false)) {
                nameToCharacter.put(itemName, (char) ('a' + counter));
                ConfigurationSection itemSection = menuItems.getConfigurationSection(itemName);

                assert itemSection != null;
                int slot = itemSection.getInt("slot", -1);
                if (slot >= 0) {
                    nameToSlots.computeIfAbsent(itemName, k -> new ArrayList<>()).add(slot);
                }

                List<Integer> slots = itemSection.getIntegerList("slots");
                for (int i = 0; i < slots.size(); i++) {
                    nameToSlots.computeIfAbsent(itemName, k -> new ArrayList<>()).add(slots.get(i));
                }

                counter++;
            }

            List<StringBuilder> pattern = new ArrayList<>();
            for (int i = 0; i < h; i++) {
                pattern.add(new StringBuilder("         "));
            }

            for (String itemName : menuItems.getKeys(false)) {
                ConfigurationSection itemSection = menuConfig.getConfigurationSection(itemName);

                List<Integer> itemSlots = nameToSlots.get(itemName);
                for (Integer slot : itemSlots) {
                    int y = slot / w;
                    int x = slot % w;

                    StringBuilder sb = pattern.get(y);
                    sb.setCharAt(x, nameToCharacter.get(itemName));
                }
            }

            List<String> result = new LinkedList<>();
            for (StringBuilder stringBuilder : pattern) {
                result.add(stringBuilder.toString());
            }

            resultMenu.set("pattern", result);

            for (String itemName : menuItems.getKeys(false)) {
                ConfigurationSection itemSection = menuItems.getConfigurationSection(itemName);

                assert itemSection != null;
                ConfigurationSection newItemSection = newMenuItems.createSection(itemName);
                newItemSection.set("symbol", nameToCharacter.get(itemName));
                newItemSection.set("type", "ITEM");

                newItemSection.set("lore", itemSection.getStringList("lore"));
                newItemSection.set("name", itemSection.getString("display_name", "БЕЗ НАЗВАНИЯ"));

                newItemSection.set("material", itemSection.getString("material", "BARRIER").toUpperCase());
            }

            try {
                resultMenu.save(resultFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        });
    }

    public CompletableFuture<Boolean> migrateAsync(Player initiator) {
        return CompletableFuture.supplyAsync(() -> {

            File currentFolder = YcoordMenus.getInstance().getDataFolder();
            File deluxeMenusDir = currentFolder.toPath().resolveSibling("DeluxeMenus").normalize().toFile();
            File newMenuDir = currentFolder.toPath().resolve("menus").normalize().toFile();
            newMenuDir.mkdirs();
            YamlConfiguration dmConfig = YamlConfiguration.loadConfiguration(new File(deluxeMenusDir, "config.yml"));

            @NotNull Logger logger = YcoordCore.getInstance().getLogger();
            logger.info("Обработка директории: " + deluxeMenusDir.getAbsolutePath());


            File guiMenusDir = new File(deluxeMenusDir, "gui_menus");


            ConfigurationSection menus = dmConfig.getConfigurationSection("gui_menus");
            if (menus != null) {
                for (String key : menus.getKeys(false)) {
                    ConfigurationSection fileConfig = menus.getConfigurationSection(key);
                    if (fileConfig == null) {
                        continue;
                    }
                    String fileName = fileConfig.getString("file", null);
                    if (fileName == null) {
                        continue;
                    }

                    File menuFile = guiMenusDir.toPath().resolve(fileName).normalize().toFile();

                    logger.info("Обработка файла: " + menuFile.getAbsolutePath());

                    File newMenuFile = newMenuDir.toPath().resolve(key + ".yml").normalize().toFile();

                    processFileAsync(menuFile, key, newMenuFile).thenAccept(result -> {
                        if(result){
                            ChatMessage cm = YcoordCore.getInstance().getChatMessage();

                            MessagePlaceholders placeholders = new MessagePlaceholders(initiator);
                            placeholders.put("%menu-name%", key);
                            cm.sendMessageIdAsync(MessageBase.Level.SUCCESS, initiator, "messages.ym-loaded", placeholders);
                        }
                    });
                }
            }


            return true;
        });
    }
}
