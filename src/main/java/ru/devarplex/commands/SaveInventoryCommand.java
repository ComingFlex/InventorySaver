package ru.devarplex.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.devarplex.InventorySaver;
import ru.devarplex.database.DeathRecord;
import ru.devarplex.utils.ColorUtils;
import ru.devarplex.utils.InventoryUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SaveInventoryCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.color(
                    InventorySaver.getInstance().getConfig().getString("messages.only-players"))
            );
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ColorUtils.color(
                    InventorySaver.getInstance().getConfig().getString("messages.usage"))
            );
            return true;
        }

        String targetName = args[0];
        String historyIndexStr = args[1];

        try {
            int historyIndex = Integer.parseInt(historyIndexStr);
            if (historyIndex < 1) {
                player.sendMessage(ColorUtils.color(
                        InventorySaver.getInstance().getConfig().getString("messages.invalid-history-index"))
                );
                return true;
            }

            CompletableFuture<UUID> uuidFuture = CompletableFuture.supplyAsync(() ->
                    Bukkit.getOfflinePlayer(targetName).getUniqueId());

            uuidFuture.thenCompose(uuid ->
                            InventorySaver.getInstance().getDatabase().getDeathRecords(uuid))
                    .thenAccept(records -> {
                        if (records.isEmpty()) {
                            player.sendMessage(ColorUtils.color(
                                    InventorySaver.getInstance().getConfig().getString("messages.no-records"))
                            );
                            return;
                        }

                        if (historyIndex > records.size()) {
                            player.sendMessage(ColorUtils.color(
                                    InventorySaver.getInstance().getConfig().getString("messages.history-index-too-high"))
                            );
                            return;
                        }

                        DeathRecord record = records.get(historyIndex - 1);
                        Inventory inventory = InventoryUtils.createDeathInventory(record);
                        Bukkit.getScheduler().runTask(InventorySaver.getInstance(), () ->
                                player.openInventory(inventory));
                    }).exceptionally(e -> {
                        player.sendMessage(ColorUtils.color(
                                InventorySaver.getInstance().getConfig().getString("messages.error-loading"))
                        );
                        InventorySaver.getInstance().getLogger().severe("Ошибка при загрузке инвентаря: " + e.getMessage());
                        return null;
                    });

        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtils.color(
                    InventorySaver.getInstance().getConfig().getString("messages.invalid-number"))
            );
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return List.of("1", "2", "3", "4", "5");
        }
        return List.of();
    }
}