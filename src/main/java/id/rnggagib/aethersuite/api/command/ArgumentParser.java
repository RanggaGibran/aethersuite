package id.rnggagib.aethersuite.api.command;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;

public interface ArgumentParser<T> {
    /**
     * Parse a string argument into the target type
     *
     * @param sender the command sender
     * @param arg    the string argument
     * @return an Optional containing the parsed value, or empty if parsing failed
     */
    Optional<T> parse(CommandSender sender, String arg);
    
    /**
     * Generate tab completions for this argument type
     *
     * @param sender the command sender
     * @param arg    the current partial argument
     * @return a list of possible completions
     */
    List<String> tabComplete(CommandSender sender, String arg);
}