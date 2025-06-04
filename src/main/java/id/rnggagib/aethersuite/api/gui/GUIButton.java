package id.rnggagib.aethersuite.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public interface GUIButton {
    ItemStack getItem();
    
    void setItem(ItemStack item);
    
    boolean isClickable();
    
    void setClickable(boolean clickable);
    
    void onClick(Player player, ClickType clickType);
    
    void update();
}