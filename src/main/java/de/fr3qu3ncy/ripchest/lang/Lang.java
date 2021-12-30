package de.fr3qu3ncy.ripchest.lang;

import de.fr3qu3ncy.bukkittools.datastorage.YAMLStorage;
import de.fr3qu3ncy.ripchest.chest.ChestObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class Lang {

    private static String CHEST_OWNER_MESSAGE;
    private static String CHEST_BROADCAST_MESSAGE;

    public static void loadLang(YAMLStorage storage) {
        CHEST_OWNER_MESSAGE = storage.getData().getString("chest_owner_message");
        CHEST_BROADCAST_MESSAGE = storage.getData().getString("chest_broadcast_message");
    }

    public static String getChestOwnerMessage(ChestObject chest) {
        return ChatColor.translateAlternateColorCodes('&', CHEST_OWNER_MESSAGE)
                .replace("%deathLocation", locationToString(chest.getChestLocation()));
    }

    public static String getChestBroadcastMessage(ChestObject chest) {
        return ChatColor.translateAlternateColorCodes('&', CHEST_BROADCAST_MESSAGE)
                .replace("%deathLocation", locationToString(chest.getChestLocation()))
                .replace("%player", Bukkit.getPlayer(chest.getOwner()).getName());
    }

    private static String locationToString(Location loc) {
        return "X: " + loc.getBlockX() +
                " Y: " + loc.getBlockY() +
                " Z: " + loc.getBlockZ();
    }
}
