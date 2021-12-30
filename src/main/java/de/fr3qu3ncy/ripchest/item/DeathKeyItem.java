package de.fr3qu3ncy.ripchest.item;

import de.fr3qu3ncy.ripchest.RIPChest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class DeathKeyItem {

    public static ItemStack DEATH_KEY;

    static {
        ItemStack item = new ItemStack(Material.STICK);
        item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Death Key");
        meta.setLore(Arrays.asList("Use this key to open", "anybody's RIPChest!"));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        DEATH_KEY = item;
    }

    public static void createRecipe(RIPChest plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "death_key");

        ShapelessRecipe recipe = new ShapelessRecipe(key, DEATH_KEY);
        recipe.addIngredient(Material.TOTEM_OF_UNDYING);
        recipe.addIngredient(Material.EMERALD);

        Bukkit.addRecipe(recipe);
    }

}
