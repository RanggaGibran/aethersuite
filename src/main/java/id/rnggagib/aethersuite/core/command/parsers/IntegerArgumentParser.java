package id.rnggagib.aethersuite.core.command.parsers;

import id.rnggagib.aethersuite.api.command.ArgumentParser;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class IntegerArgumentParser implements ArgumentParser<Integer> {
    private final int min;
    private final int max;
    
    public IntegerArgumentParser() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    
    public IntegerArgumentParser(int min, int max) {
        this.min = min;
        this.max = max;
    }
    
    @Override
    public Optional<Integer> parse(CommandSender sender, String arg) {
        try {
            int value = Integer.parseInt(arg);
            if (value < min || value > max) {
                return Optional.empty();
            }
            return Optional.of(value);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String arg) {
        // Usually doesn't make sense to provide tab completions for integers
        return Collections.emptyList();
    }
}