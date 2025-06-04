package id.rnggagib.aethersuite.core.command;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.command.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final AetherSuite plugin;
    private final Map<String, BaseCommand> commands = new HashMap<>();
    
    public CommandManager(AetherSuite plugin) {
        this.plugin = plugin;
    }
    
    public void registerCommand(BaseCommand command) {
        String name = command.getName().toLowerCase();
        commands.put(name, command);
        
        PluginCommand pluginCommand = plugin.getCommand(name);
        if (pluginCommand == null) {
            registerDynamicCommand(command);
            return;
        }
        
        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);
        
        if (command.getAliases().length > 0) {
            pluginCommand.setAliases(Arrays.asList(command.getAliases()));
        }
        
        if (!command.getDescription().isEmpty()) {
            pluginCommand.setDescription(command.getDescription());
        }
        
        if (!command.getUsage().isEmpty()) {
            pluginCommand.setUsage(command.getUsage());
        }
        
        if (!command.getPermission().isEmpty()) {
            pluginCommand.setPermission(command.getPermission());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void registerDynamicCommand(BaseCommand command) {
        try {
            final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            
            PluginCommand pluginCommand = constructPluginCommand(command.getName());
            if (pluginCommand == null) {
                plugin.getLogger().warning("Failed to dynamically register command: " + command.getName());
                return;
            }
            
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
            
            if (command.getAliases().length > 0) {
                pluginCommand.setAliases(Arrays.asList(command.getAliases()));
            }
            
            if (!command.getDescription().isEmpty()) {
                pluginCommand.setDescription(command.getDescription());
            }
            
            if (!command.getUsage().isEmpty()) {
                pluginCommand.setUsage(command.getUsage());
            }
            
            if (!command.getPermission().isEmpty()) {
                pluginCommand.setPermission(command.getPermission());
            }
            
            commandMap.register(plugin.getDescription().getName(), pluginCommand);
            
            // Also register aliases to commandMap if they're not already registered
            for (String alias : command.getAliases()) {
                if (commandMap.getCommand(alias) == null) {
                    PluginCommand aliasCommand = constructPluginCommand(alias);
                    if (aliasCommand != null) {
                        aliasCommand.setExecutor(this);
                        aliasCommand.setTabCompleter(this);
                        aliasCommand.setDescription(command.getDescription());
                        aliasCommand.setUsage(command.getUsage());
                        aliasCommand.setPermission(command.getPermission());
                        commandMap.register(plugin.getDescription().getName(), aliasCommand);
                    }
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register command dynamically: " + command.getName());
            e.printStackTrace();
        }
    }
    
    private PluginCommand constructPluginCommand(String name) {
        try {
            PluginCommand command = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class)
                    .newInstance(name, plugin);
            return command;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to construct plugin command: " + name);
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        BaseCommand baseCommand = commands.get(command.getName().toLowerCase());
        if (baseCommand == null) {
            for (BaseCommand cmd : commands.values()) {
                for (String alias : cmd.getAliases()) {
                    if (alias.equalsIgnoreCase(command.getName())) {
                        baseCommand = cmd;
                        break;
                    }
                }
                if (baseCommand != null) break;
            }
        }
        
        if (baseCommand != null) {
            return baseCommand.execute(sender, label, args);
        }
        
        return false;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        BaseCommand baseCommand = commands.get(command.getName().toLowerCase());
        if (baseCommand == null) {
            for (BaseCommand cmd : commands.values()) {
                for (String cmdAlias : cmd.getAliases()) {
                    if (cmdAlias.equalsIgnoreCase(command.getName())) {
                        baseCommand = cmd;
                        break;
                    }
                }
                if (baseCommand != null) break;
            }
        }
        
        if (baseCommand != null) {
            return baseCommand.tabComplete(sender, alias, args);
        }
        
        return Collections.emptyList();
    }
    
    public Collection<BaseCommand> getCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }
    
    public BaseCommand getCommand(String name) {
        return commands.get(name.toLowerCase());
    }
    
    public List<BaseCommand> getCommandsByPermission(CommandSender sender) {
        return commands.values().stream()
                .filter(cmd -> !cmd.isPlayerOnly() || sender instanceof Player)
                .filter(cmd -> cmd.getPermission().isEmpty() || sender.hasPermission(cmd.getPermission()))
                .collect(Collectors.toList());
    }
}