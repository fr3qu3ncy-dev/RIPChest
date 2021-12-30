package de.fr3qu3ncy.ripchest.chest;

import de.fr3qu3ncy.ripchest.lang.Lang;
import de.fr3qu3ncy.ripchest.particle.DoubleHalfCirclePlayer;
import de.fr3qu3ncy.ripchest.particle.ParticlePlayer;
import de.fr3qu3ncy.ripchest.utils.ChestUtils;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ChestObject implements ConfigurationSerializable {

    @Getter
    private final Location chestLocation;
    private Chest chestBlock;

    @Getter
    private final UUID owner;

    @Getter
    private final long timeCreated;

    @Getter
    private boolean isDoubleChest;

    @Getter
    private final List<Material> replacedBlocks;

    @Getter
    private boolean ownerMessaged;

    @Getter
    private boolean messageBroadcast;

    private ParticlePlayer particlePlayer;

    /**
     * This constructor is called when a ChestObject is loaded from storage
     */
    public ChestObject(Location chestLocation, UUID owner, boolean isDoubleChest,
                       long timeCreated, List<Material> replacedBlocks,
                       boolean ownerMessaged, boolean messageBroadcast) {
        this.chestLocation = chestLocation;
        this.isDoubleChest = isDoubleChest;
        this.owner = owner;
        this.timeCreated = timeCreated;
        this.replacedBlocks = replacedBlocks;
        this.ownerMessaged = ownerMessaged;
        this.messageBroadcast = messageBroadcast;

        if (isValid()) this.chestBlock = (Chest) chestLocation.getBlock().getState();
    }

    /**
     * This constructor is called when a ChestObject is being created
     */
    public ChestObject(Location chestLocation, UUID owner) {
        this(chestLocation, owner, false, System.currentTimeMillis(), new ArrayList<>(),
                false, false);
        this.chestBlock = createChestBlock(chestLocation);
    }

    /**
     * Checks if the given location is a valid chest block
     * @return - true if valid, else false
     */
    public boolean isValid() {
        if (!isDoubleChest) {
            return chestLocation.getBlock().getState() instanceof Chest;
        } else {
            return chestLocation.getBlock().getState() instanceof Chest
                    && chestLocation.clone().add(0, 0, -1).getBlock().getState() instanceof Chest;
        }
    }

    /**
     * @return - The location of the sign attached to the chest
     */
    public Location getSignLocation() {
        return getChestLocation().clone().add(1, 0, 0);
    }

    /**
     * @return - The location of the second chest that forms a Double Chest, only valid call
     * if this.isDoubleChest is true
     */
    public Location getSecondChestLocation() {
        return chestBlock.getLocation().clone().add(0, 0, -1);
    }

    /**
     * Starts playing particles on this ChestObject
     */
    public void playParticles(JavaPlugin plugin) {
        //Calculate the center position of the chest
        Location centerLocation = ChestUtils.getBlockLocation(this.chestLocation).add(0.5, 0.5, 0.5);
        if (this.isDoubleChest) {
            centerLocation = centerLocation.add(0, 0, -0.5);
        }

        if (this.particlePlayer == null) {
            //Create a new ParticlePlayer
            this.particlePlayer = new DoubleHalfCirclePlayer(plugin, centerLocation, 1.2F, 1F,
                    true,
                    Arrays.asList(Color.AQUA, Color.BLACK, Color.RED));
        }
        this.particlePlayer.playParticles();
    }

    /**
     * This is called whenever a chest is being removed
     */
    public void removeChest() {
        this.particlePlayer.stop();
    }

    /**
     * @return - All locations associated with this ChestObject
     */
    public List<Location> getChestLocations() {
        List<Location> locations = new ArrayList<>();
        locations.add(chestBlock.getLocation());
        locations.add(getSignLocation());

        if (chestBlock.getInventory().getHolder() instanceof DoubleChest) {
            locations.add(getSecondChestLocation());
        }

        return locations;
    }

    /**
     * This method sets the position to a minecraft chest block, along with its player sign
     * @param chestLocation - The location to place the chest at
     * @return - The created chest
     */
    private Chest createChestBlock(Location chestLocation) {
        //Create Chest at position
        replacedBlocks.add(chestLocation.getBlock().getType());
        chestLocation.getWorld().setType(chestLocation, Material.CHEST);

        Chest chestBlock = (Chest) chestLocation.getBlock().getState();
        ChestUtils.setFacing(chestBlock, BlockFace.EAST);

        replacedBlocks.add(ChestUtils.setChestSign(this));

        return chestBlock;
    }

    /**
     * Fills this chest's inventory with the player's items
     * @param player
     */
    public void fillContents(Player player) {
        //Check if the entire player inventory fits into the chest
        ItemStack[] leftOvers = ChestUtils.tryInsert(chestBlock.getInventory(), player.getInventory().getContents());

        if (leftOvers.length > 0) {
            //Chest was too small, so we need to place another one next to it
            Location secondChestLocation = chestBlock.getLocation().clone().add(0, 0, -1);

            //Set if the chest is a Double Chest
            this.isDoubleChest = true;

            //Save the second replaced block
            replacedBlocks.add(secondChestLocation.getBlock().getType());

            //World cannot be null anymore
            secondChestLocation.getWorld().setType(secondChestLocation, Material.CHEST);
            Chest secondChest = (Chest) secondChestLocation.getBlock().getState();

            //Set block data so the chests appear as a double chest
            DoubleChest doubleChest = ChestUtils.setDoubleChest(chestBlock, secondChest);

            //The player cannot carry more than fits into a double chest, so we can ignore the result now
            ChestUtils.tryInsert(doubleChest.getInventory(), player.getInventory().getContents());
        }
    }

    /**
     * This is called when the player closes this chest
     */
    public void chestClosed() {
        //Drop all items that haven't been removed
        for (ItemStack item : chestBlock.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                chestBlock.getWorld().dropItemNaturally(chestBlock.getLocation(), item);
            }
        }
        //Reset replaced blocks
        rollBackReplacedBlocks();
    }

    private void rollBackReplacedBlocks() {
        Material mainBlock = replacedBlocks.get(0);
        Material signBlock = replacedBlocks.size() > 1 ? replacedBlocks.get(1) : null;
        Material secondBlock = replacedBlocks.size() > 2 ? replacedBlocks.get(2) : null;

        if (signBlock != null) chestBlock.getWorld().setType(getSignLocation(), signBlock);
        chestBlock.getWorld().setType(chestBlock.getLocation(), mainBlock);
        if (secondBlock != null) chestBlock.getWorld().setType(getSecondChestLocation(), secondBlock);
    }

    /**
     * Message the owner about the position of his chest
     */
    public void messageOwner() {
        Player player = Bukkit.getPlayer(this.owner);
        if (player == null) return;

        player.sendMessage(Lang.getChestOwnerMessage(this));

        this.ownerMessaged = true;
    }

    /**
     * Broadcast the position of this chest
     */
    public void broadcast() {
        Bukkit.broadcastMessage(Lang.getChestBroadcastMessage(this));

        this.messageBroadcast = true;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("owner", this.owner.toString());
        map.put("isDoubleChest", this.isDoubleChest);
        map.put("timeCreated", this.timeCreated);
        map.put("location", this.chestBlock.getLocation());
        map.put("ownerMessaged", this.ownerMessaged);
        map.put("messageBroadcast", this.messageBroadcast);

        List<String> materials = new ArrayList<>();
        this.replacedBlocks.forEach(mat -> materials.add(mat.name()));
        map.put("replacedBlocks", materials.toArray(new String[0]));

        return map;
    }

    public static ChestObject deserialize(Map<String, Object> map) {
        UUID owner = UUID.fromString((String) map.get("owner"));
        boolean isDoubleChest = (boolean) map.get("isDoubleChest");
        long timeCreated = (long) map.get("timeCreated");
        Location chestLocation = (Location) map.get("location");
        boolean ownerMessaged = (boolean) map.get("ownerMessaged");
        boolean messageBroadcast = (boolean) map.get("messageBroadcast");

        List<Material> replacedBlocks = new ArrayList<>();
        List<String> materialOrdinal = (List<String>) map.get("replacedBlocks");
        materialOrdinal.forEach(name -> replacedBlocks.add(Material.getMaterial(name)));

        return new ChestObject(chestLocation, owner, isDoubleChest,
                timeCreated, replacedBlocks, ownerMessaged, messageBroadcast);
    }
}
