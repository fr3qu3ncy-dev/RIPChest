package de.fr3qu3ncy.ripchest.config;

import de.fr3qu3ncy.ripchest.RIPChest;

public class Config {

    public static int CHEST_DESPAWN_TIME;
    public static int CHEST_BROADCAST_TIME;
    public static int CHEST_CHECK_INTERVAL;

    public static void loadConfig(RIPChest plugin) {
        CHEST_DESPAWN_TIME = plugin.getConfig().getInt("chest_despawn_time");
        CHEST_BROADCAST_TIME = plugin.getConfig().getInt("chest_broadcast_time");
        CHEST_CHECK_INTERVAL = plugin.getConfig().getInt("chest_check_interval");
    }

}
