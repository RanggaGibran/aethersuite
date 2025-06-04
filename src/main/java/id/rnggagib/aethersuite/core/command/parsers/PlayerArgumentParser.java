package id.rnggagib.aethersuite.core.command.parsers;

import id.rnggagib.aethersuite.api.command.ArgumentParser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayerArgumentParser implements ArgumentParser<Player> {
    
    @Override
    public Optional<Player> parse(CommandSender sender, String arg) {
        return Optional.ofNullable(Bukkit.getPlayer(arg));
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String arg) {
        String lowerArg = arg.toLowerCase();
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> sender instanceof Player && ((Player) sender).canSee(player))
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(lowerArg))
                .collect(Collectors.toList());
    }
}