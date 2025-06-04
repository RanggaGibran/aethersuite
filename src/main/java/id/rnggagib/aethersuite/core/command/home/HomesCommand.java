package id.rnggagib.aethersuite.core.command.home;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.command.CommandInfo;
import id.rnggagib.aethersuite.api.home.Home;
import id.rnggagib.aethersuite.core.command.AbstractCommand;
import id.rnggagib.aethersuite.core.home.HomeManager;
import id.rnggagib.aethersuite.core.home.HomesGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@CommandInfo(
    name = "homes",
    aliases = {"listhomes"},
    description = "List all of your homes",
    permission = "aethersuite.command.homes",
    playerOnly = true
)
public class HomesCommand extends AbstractCommand {
    private final HomeManager homeManager;
    
    public HomesCommand(AetherSuite plugin) {
        super(plugin);
        this.homeManager = plugin.getHomeManager();
    }
    
    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        
        // Open GUI by default
        if (args.length == 0) {
            plugin.getScheduler().runTask(() -> {
                new HomesGUI(plugin, player).open(player);
            });
            return true;
        }
        
        // Show text list if "list" argument is provided
        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            homeManager.getHomes(player).thenAccept(homes -> {
                if (homes.isEmpty()) {
                    plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.homes.none");
                    return;
                }
                
                plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.homes.list-header", 
                        Map.of("count", String.valueOf(homes.size()),
                              "limit", String.valueOf(homeManager.getHomeLimit(player))));
                
                for (Home home : homes) {
                    Location loc = home.getLocation();
                    
                    Component homeComponent = plugin.getMessageProvider().getMessage("modules.player.homes.list-item", 
                            Map.of("name", home.getName(),
                                  "world", loc.getWorld().getName(),
                                  "x", String.format("%.1f", loc.getX()),
                                  "y", String.format("%.1f", loc.getY()),
                                  "z", String.format("%.1f", loc.getZ())))
                            .clickEvent(ClickEvent.runCommand("/home " + home.getName()))
                            .hoverEvent(HoverEvent.showText(plugin.getMessageProvider().getMessage("modules.player.homes.click-to-teleport")));
                    
                    player.sendMessage(homeComponent);
                }
            });
            
            return true;
        }
        
        return true;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("list");
        }
        
        return Collections.emptyList();
    }
}