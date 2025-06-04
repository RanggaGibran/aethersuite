package id.rnggagib.aethersuite.core.command;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.command.BaseCommand;
import id.rnggagib.aethersuite.api.command.CommandInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

@CommandInfo(
    name = "aetherhelp",
    aliases = {"ahelp"},
    description = "Shows help information for AetherSuite commands",
    permission = "aethersuite.command.help"
)
public class HelpCommand extends AbstractCommand {
    private static final int COMMANDS_PER_PAGE = 7;
    
    public HelpCommand(AetherSuite plugin) {
        super(plugin);
    }
    
    @Override
    protected boolean onCommand(CommandSender sender, String label, String[] args) {
        // Get all commands that the sender can use
        List<BaseCommand> availableCommands = ((CommandManager) plugin.getCommandManager())
                .getCommandsByPermission(sender);
        
        // Sort commands alphabetically
        availableCommands.sort(Comparator.comparing(BaseCommand::getName));
        
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                // If the argument is not a number, try to find a command with that name
                String commandName = args[0].toLowerCase();
                BaseCommand command = availableCommands.stream()
                        .filter(cmd -> cmd.getName().equalsIgnoreCase(commandName) || 
                                Arrays.asList(cmd.getAliases()).contains(commandName))
                        .findFirst()
                        .orElse(null);
                
                if (command != null) {
                    showCommandHelp(sender, command);
                    return true;
                } else {
                    sender.sendMessage(plugin.getMessageProvider().getPrefixedMessage("help.command-not-found",
                            Map.of("command", args[0])));
                    return true;
                }
            }
        }
        
        int totalPages = (int) Math.ceil((double) availableCommands.size() / COMMANDS_PER_PAGE);
        
        if (page < 1) {
            page = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }
        
        showHelpPage(sender, availableCommands, page, totalPages, label);
        return true;
    }
    
    private void showHelpPage(CommandSender sender, List<BaseCommand> commands, int page, int totalPages, String label) {
        // Calculate index range for this page
        int startIndex = (page - 1) * COMMANDS_PER_PAGE;
        int endIndex = Math.min(startIndex + COMMANDS_PER_PAGE, commands.size());
        
        // Send header
        sender.sendMessage(plugin.getMessageProvider().getMessage("help.header", 
                Map.of("page", String.valueOf(page), 
                      "total", String.valueOf(totalPages))));
        
        // Send command list
        for (int i = startIndex; i < endIndex; i++) {
            BaseCommand command = commands.get(i);
            
            Component helpLine = plugin.getMessageProvider().getMessage("help.command-line", 
                    Map.of("command", command.getName(),
                          "description", command.getDescription()));
            
            // Add click and hover events
            helpLine = helpLine.clickEvent(ClickEvent.suggestCommand("/" + command.getName()))
                    .hoverEvent(HoverEvent.showText(plugin.getMessageProvider().getMessage("help.click-to-suggest")));
            
            sender.sendMessage(helpLine);
        }
        
        // Send footer with pagination buttons
        Component footer = plugin.getMessageProvider().getMessage("help.footer");
        
        // Add previous page button if not on first page
        if (page > 1) {
            Component prevButton = plugin.getMessageProvider().getMessage("help.prev-button")
                    .clickEvent(ClickEvent.runCommand("/" + label + " " + (page - 1)))
                    .hoverEvent(HoverEvent.showText(plugin.getMessageProvider().getMessage("help.prev-page")));
            footer = footer.append(prevButton);
        } else {
            footer = footer.append(plugin.getMessageProvider().getMessage("help.prev-button-disabled"));
        }
        
        // Add page indicator
        footer = footer.append(plugin.getMessageProvider().getMessage("help.page-indicator", 
                Map.of("page", String.valueOf(page), 
                      "total", String.valueOf(totalPages))));
        
        // Add next page button if not on last page
        if (page < totalPages) {
            Component nextButton = plugin.getMessageProvider().getMessage("help.next-button")
                    .clickEvent(ClickEvent.runCommand("/" + label + " " + (page + 1)))
                    .hoverEvent(HoverEvent.showText(plugin.getMessageProvider().getMessage("help.next-page")));
            footer = footer.append(nextButton);
        } else {
            footer = footer.append(plugin.getMessageProvider().getMessage("help.next-button-disabled"));
        }
        
        sender.sendMessage(footer);
    }
    
    private void showCommandHelp(CommandSender sender, BaseCommand command) {
        // Send detailed help for a specific command
        sender.sendMessage(plugin.getMessageProvider().getMessage("help.command-header", 
                Map.of("command", command.getName())));
        
        // Description
        sender.sendMessage(plugin.getMessageProvider().getMessage("help.command-description", 
                Map.of("description", command.getDescription())));
        
        // Usage
        sender.sendMessage(plugin.getMessageProvider().getMessage("help.command-usage", 
                Map.of("usage", command.getUsage())));
        
        // Aliases
        if (command.getAliases().length > 0) {
            String aliases = String.join(", ", command.getAliases());
            sender.sendMessage(plugin.getMessageProvider().getMessage("help.command-aliases", 
                    Map.of("aliases", aliases)));
        }
        
        // Permission
        if (!command.getPermission().isEmpty()) {
            sender.sendMessage(plugin.getMessageProvider().getMessage("help.command-permission", 
                    Map.of("permission", command.getPermission())));
        }
        
        // Subcommands
        if (!command.getSubcommands().isEmpty()) {
            sender.sendMessage(plugin.getMessageProvider().getMessage("help.subcommands-header"));
            
            List<BaseCommand> subcommands = command.getSubcommands().values().stream()
                    .distinct()
                    .sorted(Comparator.comparing(BaseCommand::getName))
                    .collect(Collectors.toList());
            
            for (BaseCommand subcommand : subcommands) {
                if (subcommand.getPermission().isEmpty() || sender.hasPermission(subcommand.getPermission())) {
                    Component subHelpLine = plugin.getMessageProvider().getMessage("help.subcommand-line", 
                            Map.of("command", subcommand.getName(),
                                  "description", subcommand.getDescription()));
                    
                    // Add click and hover events
                    subHelpLine = subHelpLine.clickEvent(ClickEvent.suggestCommand("/" + command.getName() + " " + subcommand.getName()))
                            .hoverEvent(HoverEvent.showText(plugin.getMessageProvider().getMessage("help.click-to-suggest")));
                    
                    sender.sendMessage(subHelpLine);
                }
            }
        }
        
        // Footer with click to run
        Component runButton = plugin.getMessageProvider().getMessage("help.run-command-button")
                .clickEvent(ClickEvent.suggestCommand("/" + command.getName()))
                .hoverEvent(HoverEvent.showText(plugin.getMessageProvider().getMessage("help.click-to-run")));
        
        sender.sendMessage(runButton);
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            
            // Add page numbers (1 to total pages)
            List<BaseCommand> availableCommands = ((CommandManager) plugin.getCommandManager())
                    .getCommandsByPermission(sender);
            int totalPages = (int) Math.ceil((double) availableCommands.size() / COMMANDS_PER_PAGE);
            
            for (int i = 1; i <= totalPages; i++) {
                completions.add(String.valueOf(i));
            }
            
            // Add command names
            availableCommands.forEach(cmd -> completions.add(cmd.getName()));
            
            // Filter by current argument
            String current = args[0].toLowerCase();
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(current))
                    .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }
}