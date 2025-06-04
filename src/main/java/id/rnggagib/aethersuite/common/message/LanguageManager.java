package id.rnggagib.aethersuite.common.message;

import id.rnggagib.aethersuite.AetherSuite;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LanguageManager {
    private final AetherSuite plugin;
    private final Map<String, FileConfiguration> languageFiles = new HashMap<>();
    private String currentLanguage;
    
    public LanguageManager(AetherSuite plugin) {
        this.plugin = plugin;
        this.currentLanguage = plugin.getConfigManager().getConfig("config").getString("general.language", "en_US");
        
        createLanguageDirectory();
        loadDefaultLanguage();
        loadLanguage(currentLanguage);
    }
    
    private void createLanguageDirectory() {
        File langDir = new File(plugin.getDataFolder(), "languages");
        if (!langDir.exists() && langDir.mkdirs()) {
            plugin.getLogger().info("Created languages directory");
        }
    }
    
    private void loadDefaultLanguage() {
        // Save default language files if they don't exist
        saveDefaultLanguageFile("en_US");
    }
    
    private void saveDefaultLanguageFile(String language) {
        File langFile = new File(plugin.getDataFolder() + "/languages", language + ".yml");
        
        if (!langFile.exists()) {
            try (InputStream in = plugin.getResource("languages/" + language + ".yml")) {
                if (in != null) {
                    Files.copy(in, langFile.toPath());
                    plugin.getLogger().info("Created default language file: " + language + ".yml");
                } else {
                    // Create empty language file based on messages.yml
                    FileConfiguration messagesConfig = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(plugin.getResource("messages.yml"), StandardCharsets.UTF_8));
                    
                    YamlConfiguration langConfig = new YamlConfiguration();
                    for (String key : messagesConfig.getKeys(true)) {
                        if (messagesConfig.isString(key)) {
                            langConfig.set(key, messagesConfig.getString(key));
                        }
                    }
                    
                    langConfig.save(langFile);
                    plugin.getLogger().info("Created empty language file: " + language + ".yml");
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create language file: " + language + ".yml", e);
            }
        }
    }
    
    public void loadLanguage(String language) {
        if (languageFiles.containsKey(language)) {
            return;
        }
        
        File langFile = new File(plugin.getDataFolder() + "/languages", language + ".yml");
        
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file not found: " + language + ".yml, creating it");
            saveDefaultLanguageFile(language);
        }
        
        FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
        languageFiles.put(language, langConfig);
        
        plugin.getLogger().info("Loaded language: " + language);
    }
    
    public void setLanguage(String language) {
        if (!languageFiles.containsKey(language)) {
            loadLanguage(language);
        }
        
        this.currentLanguage = language;
        
        // Update the config
        FileConfiguration config = plugin.getConfigManager().getConfig("config");
        config.set("general.language", language);
        plugin.getConfigManager().saveConfig("config");
        
        plugin.getLogger().info("Changed language to: " + language);
    }
    
    public String getCurrentLanguage() {
        return currentLanguage;
    }
    
    public FileConfiguration getLanguageConfig() {
        return languageFiles.getOrDefault(currentLanguage, languageFiles.get("en_US"));
    }
    
    public FileConfiguration getLanguageConfig(String language) {
        if (!languageFiles.containsKey(language)) {
            loadLanguage(language);
        }
        
        return languageFiles.get(language);
    }
    
    public String getMessage(String key) {
        FileConfiguration langConfig = getLanguageConfig();
        
        if (langConfig.contains(key)) {
            return langConfig.getString(key);
        }
        
        // Fallback to en_US
        if (!currentLanguage.equals("en_US")) {
            FileConfiguration fallbackConfig = getLanguageConfig("en_US");
            if (fallbackConfig.contains(key)) {
                return fallbackConfig.getString(key);
            }
        }
        
        // Final fallback to messages.yml
        FileConfiguration messagesConfig = plugin.getConfigManager().getConfig("messages");
        return messagesConfig.getString(key, "Missing message: " + key);
    }
}