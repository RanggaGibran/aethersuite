package id.rnggagib.aethersuite.common.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {
    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadDefaultConfig();
    }
    
    private void loadDefaultConfig() {
        plugin.saveDefaultConfig();
        configs.put("config", plugin.getConfig());
    }
    
    public FileConfiguration getConfig(String name) {
        if (configs.containsKey(name)) {
            return configs.get(name);
        }
        
        return loadConfig(name);
    }
    
    public FileConfiguration loadConfig(String name) {
        File configFile = new File(plugin.getDataFolder(), name + ".yml");
        
        if (!configFile.exists()) {
            try {
                plugin.saveResource(name + ".yml", false);
            } catch (IllegalArgumentException e) {
                try {
                    if (configFile.createNewFile()) {
                        plugin.getLogger().info("Created new config file: " + name + ".yml");
                    }
                } catch (IOException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to create config file: " + name + ".yml", ex);
                    return new YamlConfiguration();
                }
            }
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        configs.put(name, config);
        
        return config;
    }
    
    public void saveConfig(String name) {
        if (!configs.containsKey(name)) {
            return;
        }
        
        File configFile = new File(plugin.getDataFolder(), name + ".yml");
        try {
            configs.get(name).save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save config file: " + name + ".yml", e);
        }
    }
    
    public void reloadConfig(String name) {
        if (name.equals("config")) {
            plugin.reloadConfig();
            configs.put("config", plugin.getConfig());
            return;
        }
        
        File configFile = new File(plugin.getDataFolder(), name + ".yml");
        if (configFile.exists()) {
            configs.put(name, YamlConfiguration.loadConfiguration(configFile));
        }
    }
    
    public void reloadAllConfigs() {
        for (String name : configs.keySet()) {
            reloadConfig(name);
        }
    }
}