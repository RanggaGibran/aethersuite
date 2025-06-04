package id.rnggagib.aethersuite.core.command.home;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.command.CommandInfo;
import id.rnggagib.aethersuite.api.home.Home;
import id.rnggagib.aethersuite.core.command.AbstractCommand;
import id.rnggagib.aethersuite.core.home.HomesGUI;
import id.rnggagib.aethersuite.core.home.HomeManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommandInfo(
    name = "home",
    aliases = {"h"},
    description = "Teleport to your home",
    usage = "[name]",
    permission = "aethersuite.command.home",
    playerOnly = true
)
public class HomeCommand extends AbstractCommand {
    private final HomeManager homeManager;
    
    public HomeCommand(AetherSuite plugin) {
        super(plugin);
        this.homeManager = plugin.getHomeManager();
    }
    
    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Open GUI if no home name is provided
            plugin.getHomeManager().getHomes(player).thenAccept(homes -> {
                if (homes.isEmpty()) {
                    plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.homes.none");
                    return;
                }
                
                if (homes.size() == 1) {
                    // If only one home, teleport to it directly
                    Home home = homes.get(0);
                    homeManager.teleportToHome(player, home.getName());
                } else {
                    // Open homes GUI
                    plugin.getScheduler().runTask(() -> {
                        new HomesGUI(plugin, player).open(player);
                    });
                }
            });
            return true;
        }
        
        String homeName = args[0];
        homeManager.teleportToHome(player, homeName);
        return true;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            String partialName = args[0].toLowerCase();
            
            return homeManager.getHomes(player)
                    .thenApply(homes -> homes.stream()
                            .map(Home::getName)
                            .filter(name -> name.toLowerCase().startsWith(partialName))
                            .collect(Collectors.toList()))
                    .join();
        }
        
        return Collections.emptyList();
    }
}