package id.rnggagib.aethersuite.common.message;

import id.rnggagib.aethersuite.AetherSuite;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MessageProvider {
    private final AetherSuite plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, String> messageCache = new HashMap<>();
    private FileConfiguration messagesConfig;
    private LanguageManager languageManager;
    private final Pattern placeholderPattern = Pattern.compile("\\{([^}]+)\\}");
    
    public MessageProvider(AetherSuite plugin) {
        this.plugin = plugin;
        loadMessages();
        this.languageManager = new LanguageManager(plugin);
    }
    
    private void loadMessages() {
        // Create messages.yml if it doesn't exist
        if (!new java.io.File(plugin.getDataFolder(), "messages.yml").exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messagesConfig = plugin.getConfigManager().getConfig("messages");
        
        // Cache all messages
        messageCache.clear();
        for (String key : messagesConfig.getKeys(true)) {
            if (messagesConfig.isString(key)) {
                messageCache.put(key, messagesConfig.getString(key));
            }
        }
    }
    
    public void reloadMessages() {
        loadMessages();
        if (languageManager != null) {
            String currentLang = languageManager.getCurrentLanguage();
            languageManager = new LanguageManager(plugin);
            languageManager.setLanguage(currentLang);
        }
    }
    
    public String getRawMessage(String key) {
        if (languageManager != null) {
            String message = languageManager.getMessage(key);
            if (message != null && !message.startsWith("Missing message:")) {
                return message;
            }
        }
        return messageCache.getOrDefault(key, "Missing message: " + key);
    }
    
    public Component getMessage(String key) {
        return miniMessage.deserialize(getRawMessage(key));
    }
    
    public Component getMessage(String key, Map<String, String> placeholders) {
        String message = getRawMessage(key);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return miniMessage.deserialize(message);
    }
    
    public Component getPrefix() {
        return getMessage("prefix");
    }
    
    public Component getPrefixedMessage(String key) {
        return Component.empty().append(getPrefix()).append(getMessage(key));
    }
    
    public Component getPrefixedMessage(String key, Map<String, String> placeholders) {
        return Component.empty().append(getPrefix()).append(getMessage(key, placeholders));
    }
    
    public void sendMessage(CommandSender sender, String key) {
        sender.sendMessage(getMessage(key));
    }
    
    public void sendMessage(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(getMessage(key, placeholders));
    }
    
    public void sendPrefixedMessage(CommandSender sender, String key) {
        sender.sendMessage(getPrefixedMessage(key));
    }
    
    public void sendPrefixedMessage(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(getPrefixedMessage(key, placeholders));
    }
    
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    public void setPlayerLanguage(Player player, String language) {
        // This would be used in a future player preferences system
        // For now, it just sets the global language
        languageManager.setLanguage(language);
    }
}