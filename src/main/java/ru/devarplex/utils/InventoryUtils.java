package ru.devarplex.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.devarplex.InventorySaver;
import ru.devarplex.database.DeathRecord;

import java.text.SimpleDateFormat;
import java.util.Date;

public class InventoryUtils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM HH:mm");

    public static Inventory createDeathInventory(DeathRecord record) {
        String title = InventorySaver.getInstance().getConfig().getString("inventory-title")
                .replace("%player%", record.getPlayerName())
                .replace("%history%", String.valueOf(record.getId()))
                .replace("%date%", DATE_FORMAT.format(new Date(record.getDeathDate().getTime())));

        // Создаем инвентарь с поддержкой цветов
        Inventory inventory = Bukkit.createInventory(
                null,
                54,
                ru.devarplex.utils.ColorUtils.color(title)
        );

        // Устанавливаем броню и предметы
        ItemStack[] armor = record.getArmor();
        if (armor.length >= 4) {
            inventory.setItem(45, armor[3]); // Ботинки
            inventory.setItem(46, armor[2]); // Поножи
            inventory.setItem(47, armor[1]); // Нагрудник
            inventory.setItem(48, armor[0]); // Шлем
        }

        ItemStack[] invContents = record.getInventory();
        for (int i = 0; i < Math.min(36, invContents.length); i++) {
            inventory.setItem(i, invContents[i]);
        }

        return inventory;
    }
}