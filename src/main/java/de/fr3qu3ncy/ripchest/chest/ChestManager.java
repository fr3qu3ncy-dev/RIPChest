package de.fr3qu3ncy.ripchest.chest;

import de.fr3qu3ncy.bukkittools.datastorage.YAMLStorage;
import de.fr3qu3ncy.ripchest.RIPChest;
import de.fr3qu3ncy.ripchest.config.Config;
import de.fr3qu3ncy.ripchest.utils.ChestUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all active ChestObjects
 */

public class ChestManager {

    private List<ChestObject> CHESTS = new ArrayList<>();

    private final RIPChest plugin;
    private final YAMLStorage storage;

    public ChestManager(RIPChest plugin, YAMLStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    /**
     * Load all available ChestObjects from Storage
     */
    public void loadChests() {
        Object obj = storage.getData().get("chests");
        if (obj == null) return;

        CHESTS = (List<ChestObject>) obj;

        CHESTS.forEach(chest -> chest.playParticles(plugin));

        new ChestChecker().runTaskTimer(plugin, 1L, 20L * Config.CHEST_CHECK_INTERVAL);
    }

    /**
     * Save all current ChestObjects to Storage
     */
    public void saveChests() {
        storage.getData().set("chests", CHESTS.toArray(new ChestObject[0]));
        storage.saveDataFile();
    }

    /**
     * Create a new ChestObject instance
     * @param deathLocation - The location to create the chest at
     * @param player - The owner of the chest
     */
    public void createChest(Location deathLocation, Player player) {
        //Check if there is already a ChestObject
        deathLocation = checkChestLocation(deathLocation.clone());

        ChestObject chest = new ChestObject(deathLocation, player.getUniqueId());
        chest.fillContents(player);
        chest.playParticles(plugin);

        CHESTS.add(chest);
        saveChests();
    }

    /**
     * Looks for the next possible location for a ChestObject
     * @param location - The start location to search from
     * @return - The next possible location to create to ChestObject on
     */
    private Location checkChestLocation(Location location) {
        while (getChest(location) != null) {
            location = location.add(0, 0, 1);
        }
        return location;
    }

    /**
     * Removes a ChestObject from memory and storage
     * @param chest - The chest to be removed
     */
    public void removeChest(ChestObject chest) {
        chest.removeChest();
        CHESTS.remove(chest);

        saveChests();
    }

    /**
     * This is called whenever a player closes his chest
     * @param chestLocation - The location of the closed chest
     */
    public void closeChest(Location chestLocation) {
        //Get ChestObject by location
        ChestObject chest = getChest(chestLocation);
        if (chest == null) return;
        chest.chestClosed();

        removeChest(chest);
    }

    /**
     * Searches for a ChestObject by location of the chest, the second chest or the player sign
     * @param location - The location to search on
     * @return - A ChestObject is one was found, or else null
     */
    public ChestObject getChest(Location location) {
        //Search a chest by location
        return CHESTS.stream().filter(chest -> ChestUtils.containsLocation(chest.getChestLocations(), location))
                .findAny()
                .orElse(null);
    }

    /**
     * This class is used for checking running ChestObjects
     */
    private class ChestChecker extends BukkitRunnable {

        @Override
        public void run() {
            //These chests will be removed
            List<ChestObject> removedChests = new ArrayList<>();

            //Loop through all available chests
            for (ChestObject chest : CHESTS) {
                //Define the time that passed since the chest was created
                long secondsPassed = (System.currentTimeMillis() - chest.getTimeCreated()) / 1000;

                //Check if chest should despawn
                if (secondsPassed > Config.CHEST_DESPAWN_TIME * 60L) {
                    removedChests.add(chest);
                } else {
                    //Check if owner has been messaged
                    if (secondsPassed > 60 && !chest.isOwnerMessaged()) {
                        chest.messageOwner();
                    }

                    //Check
                    if (secondsPassed > Config.CHEST_BROADCAST_TIME * 60L) {
                        if (!chest.isMessageBroadcast()) {
                            chest.broadcast();
                        }
                    }
                }
            }

            //Remove expired chests
            for (ChestObject chest : removedChests) {
                removeChest(chest);
            }
        }
    }
}
