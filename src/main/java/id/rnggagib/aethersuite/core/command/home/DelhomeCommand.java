package id.rnggagib.aethersuite.core.command.home;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.command.CommandInfo;
import id.rnggagib.aethersuite.api.home.Home;
import id.rnggagib.aethersuite.core.command.AbstractCommand;
import id.rnggagib.aethersuite.core.home.HomeManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CommandInfo(
    name = "delhome",
    aliases = {"removehome", "deletehome"},
    description = "Delete one of your homes",
    usage = "<name>",
    permission = "aethersuite.command.delhome",
    playerOnly = true
)
public class DelHomeCommand extends AbstractCommand {
    private final HomeManager homeManager;
    
    public DelHomeCommand(AetherSuite plugin) {
        super(plugin);
        this.homeManager = plugin.getHomeManager();
    }
    
    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        
        if (args.length < 1) {
            sendUsage(sender, label);
            return true;
        }
        
        String homeName = args[0];
        homeManager.deleteHome(player, homeName);
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