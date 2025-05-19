package ru.devarplex.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import ru.devarplex.InventorySaver;

import java.util.List;

public class PlayerDeathListener implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Проверяем, находится ли игрок в мире, где записи отключены
        List<String> disabledWorlds = InventorySaver.getInstance().getConfig().getStringList("disabled-worlds");
        if (disabledWorlds.contains(player.getWorld().getName())) {
            return; // Не записываем смерть, если мир в списке исключений
        }

        // Get inventory and armor contents
        ItemStack[] inventory = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();

        // Save to database async
        InventorySaver.getInstance().getDatabase().saveDeathRecord(
                player.getUniqueId(),
                player.getName(),
                inventory,
                armor
        );
    }
}