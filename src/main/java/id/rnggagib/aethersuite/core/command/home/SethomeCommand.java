package id.rnggagib.aethersuite.core.command.home;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.command.CommandInfo;
import id.rnggagib.aethersuite.core.command.AbstractCommand;
import id.rnggagib.aethersuite.core.home.HomeManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

@CommandInfo(
    name = "sethome",
    aliases = {"createhome", "addhome"},
    description = "Set a home at your current location",
    usage = "<name>",
    permission = "aethersuite.command.sethome",
    playerOnly = true
)
public class SethomeCommand extends AbstractCommand {
    private final HomeManager homeManager;
    
    public SethomeCommand(AetherSuite plugin) {
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
        homeManager.setHome(player, homeName);
        return true;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        return Collections.emptyList();
    }
}