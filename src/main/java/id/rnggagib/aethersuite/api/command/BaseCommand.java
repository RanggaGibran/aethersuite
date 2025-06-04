package id.rnggagib.api.command;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public interface BaseCommand {
    String getName();
    
    String[] getAliases();
    
    String getDescription();
    
    String getUsage();
    
    String getPermission();
    
    boolean isPlayerOnly();
    
    boolean execute(CommandSender sender, String label, String[] args);
    
    List<String> tabComplete(CommandSender sender, String alias, String[] args);
    
    Map<String, BaseCommand> getSubcommands();
    
    void registerSubcommand(BaseCommand subcommand);
}