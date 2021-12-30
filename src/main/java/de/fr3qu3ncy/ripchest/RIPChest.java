package de.fr3qu3ncy.ripchest;

import de.fr3qu3ncy.ripchest.chest.ChestManager;
import de.fr3qu3ncy.ripchest.chest.ChestObject;
import de.fr3qu3ncy.ripchest.lang.Lang;
import de.fr3qu3ncy.ripchest.config.Config;
import de.fr3qu3ncy.ripchest.item.DeathKeyItem;
import de.fr3qu3ncy.ripchest.listener.PlayerListener;
import de.fr3qu3ncy.bukkittools.datastorage.YAMLStorage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

public class RIPChest extends JavaPlugin {

    @Getter
    private YAMLStorage chestData;

    @Getter
    private ChestManager chestManager;

    @Override
    public void onEnable() {
        //Register serializers
        registerSerializers();

        this.saveDefaultConfig();

        //Create new YAMLStorage for chests
        chestData = new YAMLStorage(this, "plugins/RIPChest", "chestData.yml", false);
        YAMLStorage lang = new YAMLStorage(this, "plugins/RIPChest", "lang.yml", true);

        Config.loadConfig(this);
        Lang.loadLang(lang);

        //Load chests
        chestManager = new ChestManager(this, chestData);
        chestManager.loadChests();

        //Register listeners
        registerListeners();

        //Load recipes
        DeathKeyItem.createRecipe(this);
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    private void registerSerializers() {
        ConfigurationSerialization.registerClass(ChestObject.class);
    }
}
