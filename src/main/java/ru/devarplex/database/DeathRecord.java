package ru.devarplex.database;

import org.bukkit.inventory.ItemStack;
import java.sql.Timestamp;
import java.util.UUID;

public class DeathRecord {
    private final int id;
    private final UUID playerUuid;
    private final String playerName;
    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final Timestamp deathDate;

    public DeathRecord(int id, UUID playerUuid, String playerName, ItemStack[] inventory, ItemStack[] armor, Timestamp deathDate) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.inventory = inventory;
        this.armor = armor;
        this.deathDate = deathDate;
    }

    // Getters
    public int getId() { return id; }
    public UUID getPlayerUuid() { return playerUuid; }
    public String getPlayerName() { return playerName; }
    public ItemStack[] getInventory() { return inventory; }
    public ItemStack[] getArmor() { return armor; }
    public Timestamp getDeathDate() { return deathDate; }
}