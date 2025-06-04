package id.rnggagib.aethersuite.core.command.teleport;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.command.CommandInfo;
import id.rnggagib.aethersuite.core.command.AbstractCommand;
import id.rnggagib.aethersuite.core.command.parsers.PlayerArgumentParser;
import id.rnggagib.aethersuite.core.teleport.TeleportManager;
import id.rnggagib.aethersuite.core.teleport.TeleportRequest;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CommandInfo(
    name = "tpdeny",
    aliases = {"tpno"},
    description = "Deny a teleport request",
    usage = "[player]",
    permission = "aethersuite.teleport.tpdeny",
    playerOnly = true
)
public class TpdenyCommand extends AbstractCommand {
    private final TeleportManager teleportManager;
    private final PlayerArgumentParser playerParser = new PlayerArgumentParser();
    
    public TpdenyCommand(AetherSuite plugin) {
        super(plugin);
        this.teleportManager = plugin.getTeleportManager();
    }
    
    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        
        // Get pending requests
        List<TeleportRequest> requests = teleportManager.getPendingRequests(player);
        
        if (requests.isEmpty()) {
            plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport.no-pending-requests");
            return true;
        }
        
        if (args.length > 0) {
            // Deny a specific request
            String requesterName = args[0];
            Optional<Player> requesterOptional = playerParser.parse(sender, requesterName);
            
            if (requesterOptional.isEmpty()) {
                plugin.getMessageProvider().sendPrefixedMessage(player, "general.unknown-player", 
                        Map.of("player", requesterName));
                return true;
            }
            
            Player requester = requesterOptional.get();
            teleportManager.denyTeleportRequest(player, requester);
            
        } else if (requests.size() == 1) {
            // Only one request, deny it
            TeleportRequest request = requests.get(0);
            Player requester = Bukkit.getPlayer(request.getSenderUuid());
            
            if (requester != null && requester.isOnline()) {
                teleportManager.denyTeleportRequest(player, requester);
            } else {
                plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport.requester-offline");
            }
            
        } else {
            // Multiple requests, show list
            plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport.multiple-requests");
            
            for (TeleportRequest request : requests) {
                Player requester = Bukkit.getPlayer(request.getSenderUuid());
                if (requester != null && requester.isOnline()) {
                    String type = request.getType() == TeleportRequest.Type.TPA ? 
                            "modules.player.teleport.list-to-you" : "modules.player.teleport.list-you-to";
                    
                    plugin.getMessageProvider().sendMessage(player, type, 
                            Map.of("player", requester.getName()));
                }
            }
            
            plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport.specify-player");
        }
        
        return true;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            List<TeleportRequest> requests = teleportManager.getPendingRequests(player);
            
            if (requests.isEmpty()) {
                return Collections.emptyList();
            }
            
            List<String> requesters = new ArrayList<>();
            for (TeleportRequest request : requests) {
                Player requester = Bukkit.getPlayer(request.getSenderUuid());
                if (requester != null && requester.isOnline()) {
                    requesters.add(requester.getName());
                }
            }
            
            String current = args[0].toLowerCase();
            return requesters.stream()
                    .filter(name -> name.toLowerCase().startsWith(current))
                    .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }
}