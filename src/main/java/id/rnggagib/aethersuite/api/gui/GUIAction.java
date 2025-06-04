package id.rnggagib.aethersuite.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

@FunctionalInterface
public interface GUIAction {
    void execute(Player player, ClickType clickType);
}