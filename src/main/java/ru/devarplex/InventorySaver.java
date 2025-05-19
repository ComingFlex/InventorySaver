package ru.devarplex;

import org.bukkit.plugin.java.JavaPlugin;
import ru.devarplex.commands.ReloadCommand;
import ru.devarplex.commands.SaveInventoryCommand;
import ru.devarplex.database.Database;
import ru.devarplex.listeners.PlayerDeathListener;

import java.util.List;

public class InventorySaver extends JavaPlugin {
    private Database database;
    private static InventorySaver instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Проверяем конфиг на наличие необходимых параметров
        checkConfig();

        // Инициализируем базу данных
        database = new Database();
        database.initialize();

        // Регистрируем команды и слушатели
        getCommand("save-inventory").setExecutor(new SaveInventoryCommand());
        getCommand("inventorysaver").setExecutor(new ReloadCommand());
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);

        getLogger().info("InventorySaver успешно запущен!");
    }

    @Override
    public void onDisable() {
        database.closeConnection();
        getLogger().info("InventorySaver отключен!");
    }

    private void checkConfig() {
        // Создаем список миров по умолчанию, если его нет
        if (!getConfig().contains("disabled-worlds")) {
            getConfig().set("disabled-worlds", List.of("creative", "minigames"));
            saveConfig();
        }

        // Добавляем сообщение о перезагрузке, если его нет
        if (!getConfig().contains("messages.reload-success")) {
            getConfig().set("messages.reload-success", "&aКонфигурация успешно перезагружена!");
            saveConfig();
        }

        // Добавляем сообщение о правах, если его нет
        if (!getConfig().contains("messages.no-permission")) {
            getConfig().set("messages.no-permission", "&cУ вас нет прав на использование этой команды!");
            saveConfig();
        }
    }

    public static InventorySaver getInstance() {
        return instance;
    }

    public Database getDatabase() {
        return database;
    }
}