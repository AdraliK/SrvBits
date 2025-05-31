package adralik.srvBits;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    public static PluginManager pluginManager;
    public static JavaPlugin javaPlugin;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        pluginManager = Bukkit.getPluginManager();
        javaPlugin = this;

        getConfig().options().copyDefaults();
        saveDefaultConfig();
        config = javaPlugin.getConfig();

        Features.init();
    }

    @Override
    public void onDisable() {

    }
}
