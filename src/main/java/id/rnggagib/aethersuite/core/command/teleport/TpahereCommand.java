package id.rnggagib.aethersuite.core.command.teleport;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.command.CommandInfo;
import id.rnggagib.aethersuite.core.command.AbstractCommand;
import id.rnggagib.aethersuite.core.command.parsers.PlayerArgumentParser;
import id.rnggagib.aethersuite.core.teleport.TeleportManager;
import id.rnggagib.aethersuite.core.teleport.TeleportRequest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CommandInfo(
    name = "tpahere",
    aliases = {"tphere"},
    description = "Request a player to teleport to you",
    usage = "<player>",
    permission = "aethersuite.teleport.tpahere",
    playerOnly = true
)
public class TpahereCommand extends AbstractCommand {
    private final TeleportManager teleportManager;
    private final PlayerArgumentParser playerParser = new PlayerArgumentParser();
    
    public TpahereCommand(AetherSuite plugin) {
        super(plugin);
        this.teleportManager = plugin.getTeleportManager();
    }
    
    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        
        if (args.length < 1) {
            sendUsage(sender, label);
            return true;
        }
        
        String targetName = args[0];
        
        // Don't allow teleporting to yourself
        if (targetName.equalsIgnoreCase(player.getName())) {
            plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport.cannot-teleport-self");
            return true;
        }
        
        Optional<Player> targetOptional = playerParser.parse(sender, targetName);
        
        if (targetOptional.isEmpty()) {
            plugin.getMessageProvider().sendPrefixedMessage(player, "general.unknown-player", 
                    Map.of("player", targetName));
            return true;
        }
        
        Player target = targetOptional.get();
        
        // Send the request
        teleportManager.sendTeleportRequest(player, target, TeleportRequest.Type.TPAHERE);
        return true;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return playerParser.tabComplete(sender, args[0]);
        }
        
        return Collections.emptyList();
    }
}