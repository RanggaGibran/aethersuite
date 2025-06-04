package id.rnggagib.aethersuite.core.command.teleport;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.command.CommandInfo;
import id.rnggagib.aethersuite.core.command.AbstractCommand;
import id.rnggagib.aethersuite.core.teleport.TeleportManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

@CommandInfo(
    name = "back",
    description = "Teleport to your previous location",
    permission = "aethersuite.teleport.back",
    playerOnly = true
)
public class BackCommand extends AbstractCommand {
    private final TeleportManager teleportManager;
    
    public BackCommand(AetherSuite plugin) {
        super(plugin);
        this.teleportManager = plugin.getTeleportManager();
    }
    
    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        teleportManager.teleportBack(player);
        return true;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        return Collections.emptyList();
    }
}