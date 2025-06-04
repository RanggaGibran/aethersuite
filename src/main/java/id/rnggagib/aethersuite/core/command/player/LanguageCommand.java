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

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@CommandInfo(
    name = "language",
    aliases = {"lang"},
    description = "Change your language preferences",
    usage = "[language]",
    permission = "aethersuite.command.language",
    playerOnly = true
)
public class LanguageCommand extends AbstractCommand {
    private final PlayerManager playerManager;
    
    public LanguageCommand(AetherSuite plugin) {
        super(plugin);
        this.playerManager = plugin.getPlayerManager();
    }
    
    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showAvailableLanguages(player);
            return true;
        }
        
        String language = args[0].toLowerCase();
        File langFile = new File(plugin.getDataFolder() + "/languages", language + ".yml");
        
        if (!langFile.exists()) {
            plugin.getMessageProvider().sendPrefixedMessage(player, "general.invalid-language", 
                    Map.of("language", language));
            showAvailableLanguages(player);
            return true;
        }
        
        playerManager.setPlayerLanguage(player.getUniqueId(), language);
        return true;
    }
    
    private void showAvailableLanguages(Player player) {
        File langDir = new File(plugin.getDataFolder(), "languages");
        if (!langDir.exists() || !langDir.isDirectory()) {
            plugin.getMessageProvider().sendPrefixedMessage(player, "general.no-languages-found");
            return;
        }
        
        File[] langFiles = langDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (langFiles == null || langFiles.length == 0) {
            plugin.getMessageProvider().sendPrefixedMessage(player, "general.no-languages-found");
            return;
        }
        
        String currentLanguage = playerManager.getPlayerLanguage(player.getUniqueId());
        
        plugin.getMessageProvider().sendPrefixedMessage(player, "general.available-languages-header");
        
        for (File langFile : langFiles) {
            String langCode = langFile.getName().substring(0, langFile.getName().length() - 4);
            String langDisplayName = getLanguageDisplayName(langCode);
            
            Component langComponent;
            
            if (langCode.equalsIgnoreCase(currentLanguage)) {
                langComponent = plugin.getMessageProvider().getMessage("general.current-language-item", 
                        Map.of("code", langCode, "name", langDisplayName));
            } else {
                langComponent = plugin.getMessageProvider().getMessage("general.language-item", 
                        Map.of("code", langCode, "name", langDisplayName))
                        .clickEvent(ClickEvent.runCommand("/language " + langCode))
                        .hoverEvent(HoverEvent.showText(plugin.getMessageProvider().getMessage("general.click-to-select")));
            }
            
            player.sendMessage(langComponent);
        }
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            File langDir = new File(plugin.getDataFolder(), "languages");
            if (!langDir.exists() || !langDir.isDirectory()) {
                return Collections.emptyList();
            }
            
            File[] langFiles = langDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
            if (langFiles == null) {
                return Collections.emptyList();
            }
            
            List<String> languages = Arrays.stream(langFiles)
                    .map(file -> file.getName().substring(0, file.getName().length() - 4))
                    .filter(lang -> lang.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            
            return languages;
        }
        
        return Collections.emptyList();
    }
    
    private String getLanguageDisplayName(String localeCode) {
        Map<String, String> languageNames = new HashMap<>();
        languageNames.put("en_US", "English (US)");
        languageNames.put("id_ID", "Bahasa Indonesia");
        languageNames.put("ja_JP", "日本語");
        languageNames.put("zh_CN", "简体中文");
        languageNames.put("zh_TW", "繁體中文");
        languageNames.put("de_DE", "Deutsch");
        languageNames.put("es_ES", "Español");
        languageNames.put("fr_FR", "Français");
        languageNames.put("it_IT", "Italiano");
        languageNames.put("ko_KR", "한국어");
        languageNames.put("pt_BR", "Português (Brasil)");
        languageNames.put("pt_PT", "Português (Portugal)");
        languageNames.put("ru_RU", "Русский");
        
        return languageNames.getOrDefault(localeCode, localeCode);
    }
}