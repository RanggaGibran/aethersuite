package id.rnggagib.aethersuite.core.command;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.command.BaseCommand;
import id.rnggagib.aethersuite.api.command.CommandInfo;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractCommand implements BaseCommand {
    protected final AetherSuite plugin;
    private final String name;
    private final String[] aliases;
    private final String description;
    private final String usage;
    private final String permission;
    private final boolean playerOnly;
    private final Map<String, BaseCommand> subcommands = new HashMap<>();
    
    public AbstractCommand(AetherSuite plugin) {
        this.plugin = plugin;
        
        CommandInfo info = getClass().getAnnotation(CommandInfo.class);
        if (info == null) {
            throw new IllegalStateException("Command class must be annotated with @CommandInfo");
        }
        
        this.name = info.name();
        this.aliases = info.aliases();
        this.description = info.description();
        this.usage = info.usage().isEmpty() ? "/" + name : info.usage();
        this.permission = info.permission();
        this.playerOnly = info.playerOnly();
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String[] getAliases() {
        return aliases;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public String getUsage() {
        return usage;
    }
    
    @Override
    public String getPermission() {
        return permission;
    }
    
    @Override
    public boolean isPlayerOnly() {
        return playerOnly;
    }
    
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        // Check if player-only command is used by non-player
        if (isPlayerOnly() && !(sender instanceof Player)) {
            plugin.getMessageProvider().sendPrefixedMessage(sender, "general.player-only");
            return true;
        }
        
        // Check permissions
        if (!hasPermission(sender)) {
            plugin.getMessageProvider().sendPrefixedMessage(sender, "general.no-permission");
            return true;
        }
        
        // Handle subcommands
        if (args.length > 0 && !subcommands.isEmpty()) {
            String subcommandName = args[0].toLowerCase();
            BaseCommand subcommand = subcommands.get(subcommandName);
            
            if (subcommand != null) {
                return subcommand.execute(sender, label + " " + subcommandName, 
                        Arrays.copyOfRange(args, 1, args.length));
            }
        }
        
        // Execute the command
        return onCommand(sender, label, args);
    }
    
    protected abstract boolean onCommand(CommandSender sender, String label, String[] args);
    
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        // Check permissions
        if (!hasPermission(sender)) {
            return Collections.emptyList();
        }
        
        // Handle subcommand tab completion
        if (args.length == 1 && !subcommands.isEmpty()) {
            String partial = args[0].toLowerCase();
            return subcommands.keySet().stream()
                    .filter(cmd -> cmd.startsWith(partial))
                    .filter(cmd -> {
                        BaseCommand subcommand = subcommands.get(cmd);
                        return !subcommand.isPlayerOnly() || sender instanceof Player;
                    })
                    .filter(cmd -> {
                        BaseCommand subcommand = subcommands.get(cmd);
                        return subcommand.getPermission().isEmpty() || 
                                sender.hasPermission(subcommand.getPermission());
                    })
                    .collect(Collectors.toList());
        }
        
        // Handle subcommand arguments
        if (args.length > 1 && !subcommands.isEmpty()) {
            String subcommandName = args[0].toLowerCase();
            BaseCommand subcommand = subcommands.get(subcommandName);
            
            if (subcommand != null) {
                return subcommand.tabComplete(sender, alias + " " + subcommandName, 
                        Arrays.copyOfRange(args, 1, args.length));
            }
        }
        
        // Custom tab completion
        return onTabComplete(sender, alias, args);
    }
    
    protected List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        return Collections.emptyList();
    }
    
    @Override
    public Map<String, BaseCommand> getSubcommands() {
        return Collections.unmodifiableMap(subcommands);
    }
    
    @Override
    public void registerSubcommand(BaseCommand subcommand) {
        subcommands.put(subcommand.getName().toLowerCase(), subcommand);
        for (String alias : subcommand.getAliases()) {
            subcommands.put(alias.toLowerCase(), subcommand);
        }
    }
    
    protected boolean hasPermission(CommandSender sender) {
        return permission.isEmpty() || sender.hasPermission(permission);
    }
    
    protected void sendUsage(CommandSender sender, String label) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("usage", label + " " + getUsage());
        
        plugin.getMessageProvider().sendPrefixedMessage(sender, "general.invalid-syntax", placeholders);
    }
    
    protected Component getHelpMessage() {
        return plugin.getMessageProvider().getMessage("help.command", Map.of(
                "command", getName(),
                "description", getDescription(),
                "usage", getUsage(),
                "permission", getPermission().isEmpty() ? "None" : getPermission()
        ));
    }
}