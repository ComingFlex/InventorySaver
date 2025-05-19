package ru.devarplex.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.devarplex.InventorySaver;
import ru.devarplex.utils.ColorUtils;

public class ReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Проверка прав
        if (!sender.hasPermission("inventorysaver.reload")) {
            sender.sendMessage(ColorUtils.color(
                    InventorySaver.getInstance().getConfig().getString("messages.no-permission"))
            );
            return true;
        }

        // Перезагрузка конфига
        InventorySaver.getInstance().reloadConfig();
        sender.sendMessage(ColorUtils.color(
                InventorySaver.getInstance().getConfig().getString("messages.reload-success"))
        );
        return true;
    }
}