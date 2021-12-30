package de.fr3qu3ncy.ripchest.utils;

import de.fr3qu3ncy.ripchest.chest.ChestObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Wall;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ChestUtils {

    public static ItemStack[] tryInsert(Inventory chestInventory, ItemStack[] items) {
        List<ItemStack> list = new ArrayList<>();
        Arrays.stream(items).forEach(item -> {
            if (item != null) {
                list.add(item);
            }
        });

        //Try to fill the chest inventory, everything that didn't fit will land in the map
        Map<Integer, ItemStack> exceed = chestInventory.addItem(list.toArray(new ItemStack[0]));

        return exceed.values().toArray(new ItemStack[0]);
    }

    public static void setFacing(Chest chest, BlockFace direction) {
        org.bukkit.block.data.type.Chest chestData = ((org.bukkit.block.data.type.Chest) chest.getBlockData());
        chestData.setFacing(direction);
        chest.setBlockData(chestData);
        chest.getBlock().setBlockData(chestData);
        chest.update();
        chest.getWorld().setBlockData(chest.getLocation(), chestData);
    }

    public static DoubleChest setDoubleChest(Chest chest1, Chest chest2) {
        org.bukkit.block.data.type.Chest chestData1 = ((org.bukkit.block.data.type.Chest) chest1.getBlockData());
        org.bukkit.block.data.type.Chest chestData2 = ((org.bukkit.block.data.type.Chest) chest2.getBlockData());

        chestData1.setType(org.bukkit.block.data.type.Chest.Type.RIGHT);
        chestData1.setFacing(BlockFace.EAST);

        chestData2.setType(org.bukkit.block.data.type.Chest.Type.LEFT);
        chestData2.setFacing(BlockFace.EAST);

        chest1.setBlockData(chestData1);
        chest2.setBlockData(chestData2);

        chest1.getBlock().setBlockData(chestData1);
        chest2.getBlock().setBlockData(chestData2);

        chest1.update();
        chest2.update();

        chest1.getWorld().setBlockData(chest1.getLocation(), chestData1);
        chest2.getWorld().setBlockData(chest2.getLocation(), chestData2);

        return (DoubleChest) chest1.getInventory().getHolder();
    }

    public static Material setChestSign(ChestObject chest) {
        Location signLocation = chest.getSignLocation();
        Material oldMaterial = signLocation.getBlock().getType();

        signLocation.getWorld().setType(signLocation, Material.OAK_WALL_SIGN);

        Sign sign = (Sign) signLocation.getBlock().getState();
        sign.setLine(1, "RIPChest of");
        String name = Bukkit.getOfflinePlayer(chest.getOwner()).getName();
        if (name != null) {
            sign.setLine(2, name);
        } else {
            sign.setLine(2, "n/A");
        }

        WallSign data = (WallSign) sign.getBlockData();
        data.setFacing(BlockFace.EAST);

        sign.setBlockData(data);

        sign.update();

        return oldMaterial;
    }

    public static void removeChestSign(ChestObject chest, Material type) {
        Location signLocation = chest.getSignLocation();
        signLocation.getWorld().setType(signLocation, type);
    }

    public static Location getBlockLocation(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static boolean containsLocation(List<Location> list, Location loc) {
        return list.stream().anyMatch(listLocation -> compareLocations(listLocation, loc));
    }

    public static boolean compareLocations(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }
}
