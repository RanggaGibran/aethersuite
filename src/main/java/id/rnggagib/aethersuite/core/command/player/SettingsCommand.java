package id.rnggagib.aethersuite.core.command.player;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.command.CommandInfo;
import id.rnggagib.aethersuite.core.command.AbstractCommand;
import id.rnggagib.aethersuite.core.player.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommandInfo(
    name = "settings",
    aliases = {"options", "prefs"},
    description = "Manage your personal settings",
    usage = "[setting] [value]",
    permission = "aethersuite.command.settings",
    playerOnly = true
)
public class SettingsCommand extends AbstractCommand {
    private final PlayerManager playerManager;
    private final List<String> validSettings = Arrays.asList(
            "teleport_requests", 
            "pm_notifications", 
            "social_spy", 
            "join_messages", 
            "death_messages",
            "chat_format"
    );
    
    private final List<String> booleanValues = Arrays.asList("true", "false", "on", "off", "yes", "no", "1", "0");
    
    public SettingsCommand(AetherSuite plugin) {
        super(plugin);
        this.playerManager = plugin.getPlayerManager();
    }
    
    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showSettingsMenu(player);
            return true;
        }
        
        String settingName = args[0].toLowerCase();
        
        if (!validSettings.contains(settingName)) {
            plugin.getMessageProvider().sendPrefixedMessage(player, "general.invalid-setting", 
                    Map.of("setting", settingName));
            return true;
        }
        
        if (args.length == 1) {
            String currentValue = playerManager.getPlayerSetting(player.getUniqueId(), settingName, "default");
            plugin.getMessageProvider().sendPrefixedMessage(player, "general.setting-current-value", 
                    Map.of("setting", settingName, "value", currentValue));
            return true;
        }
        
        String newValue = args[1].toLowerCase();
        playerManager.updatePlayerSetting(player.getUniqueId(), settingName, newValue);
        
        plugin.getMessageProvider().sendPrefixedMessage(player, "general.setting-updated", 
                Map.of("setting", settingName, "value", newValue));
        
        // Apply immediate effects for some settings
        applySettingEffect(player, settingName, newValue);
        
        return true;
    }
    
    private void showSettingsMenu(Player player) {
        plugin.getMessageProvider().sendPrefixedMessage(player, "general.settings-header");
        
        for (String setting : validSettings) {
            String currentValue = playerManager.getPlayerSetting(player.getUniqueId(), setting, "default");
            
            Component settingComponent = plugin.getMessageProvider().getMessage("general.setting-item", 
                    Map.of("setting", setting, "value", currentValue))
                    .clickEvent(ClickEvent.suggestCommand("/settings " + setting + " "))
                    .hoverEvent(HoverEvent.showText(plugin.getMessageProvider().getMessage("general.click-to-change")));
            
            player.sendMessage(settingComponent);
        }
        
        plugin.getMessageProvider().sendPrefixedMessage(player, "general.settings-footer");
    }
    
    private void applySettingEffect(Player player, String setting, String value) {
        boolean boolValue = parseBoolean(value);
        
        switch (setting) {
            case "teleport_requests" -> playerManager.toggleTeleportAccept(player.getUniqueId(), boolValue);
            case "social_spy" -> {
                if (player.hasPermission("aethersuite.socialspy")) {
                    // Social spy functionality will be implemented in the chat module
                }
            }
        }
    }
    
    private boolean parseBoolean(String value) {
        return value.equalsIgnoreCase("true") || 
               value.equalsIgnoreCase("yes") || 
               value.equalsIgnoreCase("on") || 
               value.equals("1");
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            String partialSetting = args[0].toLowerCase();
            return validSettings.stream()
                    .filter(setting -> setting.startsWith(partialSetting))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String setting = args[0].toLowerCase();
            String partialValue = args[1].toLowerCase();
            
            if (validSettings.contains(setting)) {
                return booleanValues.stream()
                        .filter(value -> value.startsWith(partialValue))
                        .collect(Collectors.toList());
            }
        }
        
        return Collections.emptyList();
    }
}