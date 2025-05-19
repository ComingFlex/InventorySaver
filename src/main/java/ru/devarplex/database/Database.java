package ru.devarplex.database;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import ru.devarplex.InventorySaver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class Database {
    private Connection connection;

    public void initialize() {
        try {
            File dataFolder = InventorySaver.getInstance().getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            File databaseFile = new File(dataFolder, "death_records.db");
            String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();

            connection = DriverManager.getConnection(url);
            createTable();

            InventorySaver.getInstance().getLogger().info("Connected to SQLite database");
        } catch (SQLException e) {
            InventorySaver.getInstance().getLogger().log(Level.SEVERE, "Failed to connect to SQLite database", e);
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS death_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_uuid TEXT NOT NULL," +
                "player_name TEXT NOT NULL," +
                "inventory_data BLOB NOT NULL," +
                "armor_data BLOB NOT NULL," +
                "death_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        String indexSql = "CREATE INDEX IF NOT EXISTS idx_player_uuid ON death_records (player_uuid)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            stmt.execute(indexSql);
        }
    }

    public CompletableFuture<Void> saveDeathRecord(UUID playerUuid, String playerName, ItemStack[] inventory, ItemStack[] armor) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Serialize inventory
                byte[] invData = serializeItemStacks(inventory);
                // Serialize armor
                byte[] armorData = serializeItemStacks(armor);

                // Prepare SQL
                String sql = "INSERT INTO death_records (player_uuid, player_name, inventory_data, armor_data) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, playerUuid.toString());
                    stmt.setString(2, playerName);
                    stmt.setBytes(3, invData);
                    stmt.setBytes(4, armorData);
                    stmt.executeUpdate();
                }

                // Clean old records if needed
                cleanOldRecords(playerUuid);
            } catch (IOException | SQLException e) {
                InventorySaver.getInstance().getLogger().log(Level.SEVERE, "Failed to save death record", e);
            }
        });
    }

    private byte[] serializeItemStacks(ItemStack[] items) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOut = new BukkitObjectOutputStream(outStream);
        dataOut.writeObject(items);
        return outStream.toByteArray();
    }

    private void cleanOldRecords(UUID playerUuid) throws SQLException {
        int maxRecords = InventorySaver.getInstance().getConfig().getInt("max-records-per-player", 5);
        String deleteSql = "DELETE FROM death_records WHERE player_uuid = ? AND id NOT IN (" +
                "SELECT id FROM death_records WHERE player_uuid = ? ORDER BY death_date DESC LIMIT ?)";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, playerUuid.toString());
            stmt.setInt(3, maxRecords);
            stmt.executeUpdate();
        }
    }

    public CompletableFuture<List<DeathRecord>> getDeathRecords(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<DeathRecord> records = new ArrayList<>();
            String sql = "SELECT * FROM death_records WHERE player_uuid = ? ORDER BY death_date DESC";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    ItemStack[] inventory = deserializeItemStacks(rs.getBytes("inventory_data"));
                    ItemStack[] armor = deserializeItemStacks(rs.getBytes("armor_data"));

                    records.add(new DeathRecord(
                            rs.getInt("id"),
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("player_name"),
                            inventory,
                            armor,
                            rs.getTimestamp("death_date")
                    ));
                }
            } catch (SQLException e) {
                InventorySaver.getInstance().getLogger().log(Level.SEVERE, "Failed to load death records", e);
            }
            return records;
        });
    }

    private ItemStack[] deserializeItemStacks(byte[] data) {
        try {
            ByteArrayInputStream inStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataIn = new BukkitObjectInputStream(inStream);
            return (ItemStack[]) dataIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            InventorySaver.getInstance().getLogger().log(Level.SEVERE, "Failed to deserialize item stacks", e);
            return new ItemStack[0];
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            InventorySaver.getInstance().getLogger().log(Level.SEVERE, "Failed to close database connection", e);
        }
    }
}