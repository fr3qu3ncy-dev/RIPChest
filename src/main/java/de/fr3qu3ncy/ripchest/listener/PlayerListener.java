package de.fr3qu3ncy.ripchest.listener;

import de.fr3qu3ncy.ripchest.RIPChest;
import de.fr3qu3ncy.ripchest.chest.ChestManager;
import de.fr3qu3ncy.ripchest.chest.ChestObject;
import de.fr3qu3ncy.ripchest.item.DeathKeyItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final RIPChest plugin;

    public PlayerListener(RIPChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        //Get the dead player
        Player player = event.getEntity();

        //Check if the player's inventory is empty
        if (player.getInventory().isEmpty()) return;

        //Get death location
        Location deathLocation = player.getLocation().clone().add(0, 1, 0);

        //Check for null world before continuing
        if (deathLocation.getWorld() == null) return;

        event.getDrops().clear();

        //Create new ChestObject
        plugin.getChestManager().createChest(deathLocation, player);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        //Stop when action is not right click block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();

        //Get the player
        Player player = event.getPlayer();

        //Stop when clicked block is no Chest
        if (clicked == null || !(clicked.getState() instanceof Chest)) return;

        //Get ChestObject by location
        ChestObject chest = plugin.getChestManager().getChest(clicked.getLocation());

        //Stop when no chest is found
        if (chest == null) return;

        //Check if player is owner of chest
        if (!chest.getOwner().equals(player.getUniqueId())) {
            //Player is not the owner, check if he is using the Death Key
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (!itemInHand.isSimilar(DeathKeyItem.DEATH_KEY)) {
                //Don't allow the player to open the chest
                event.setCancelled(true);
            } else {
                //Player used Death Key
                player.getInventory().remove(DeathKeyItem.DEATH_KEY);
                player.updateInventory();
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.CHEST
                && event.getBlock().getType() != Material.OAK_WALL_SIGN) return;
        Location blockLocation = event.getBlock().getLocation();

        //Get Chest by Location
        ChestObject chest = plugin.getChestManager().getChest(blockLocation);
        if (chest != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        //Stop if inventory is not a chest
        if (event.getInventory().getType() != InventoryType.CHEST) return;

        plugin.getChestManager().closeChest(event.getInventory().getLocation());
    }
}
