package id.rnggagib.aethersuite.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;

public interface GUI {
    void open(Player player);
    
    void close(Player player);
    
    void update();
    
    String getTitle();
    
    int getSize();
    
    Inventory getInventory();
    
    Map<Integer, GUIButton> getButtons();
    
    GUIButton getButton(int slot);
    
    void setButton(int slot, GUIButton button);
    
    void removeButton(int slot);
    
    void handleClick(InventoryClickEvent event);
    
    boolean isAllowPlayerInventoryClick();
    
    void setPreviousGUI(GUI previousGUI);
    
    GUI getPreviousGUI();
}