package id.rnggagib.aethersuite.core.gui;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.gui.GUI;
import id.rnggagib.aethersuite.api.gui.GUIButton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractGUI implements GUI {
    protected final AetherSuite plugin;
    protected final String title;
    protected final int size;
    protected final Map<Integer, GUIButton> buttons = new HashMap<>();
    protected Inventory inventory;
    protected GUI previousGUI;
    protected boolean allowPlayerInventoryClick = false;
    protected final Map<UUID, Inventory> openInventories = new HashMap<>();
    
    public AbstractGUI(AetherSuite plugin, String title, int size) {
        this.plugin = plugin;
        this.title = title;
        this.size = size;
        this.inventory = Bukkit.createInventory(null, size, title);
    }
    
    @Override
    public void open(Player player) {
        update();
        player.openInventory(inventory);
        openInventories.put(player.getUniqueId(), inventory);
        GUIManager.registerActiveGUI(player, this);
    }
    
    @Override
    public void close(Player player) {
        if (player.getOpenInventory().getTopInventory().equals(inventory)) {
            player.closeInventory();
        }
        openInventories.remove(player.getUniqueId());
        GUIManager.unregisterActiveGUI(player);
    }
    
    @Override
    public void update() {
        inventory.clear();
        
        for (Map.Entry<Integer, GUIButton> entry : buttons.entrySet()) {
            int slot = entry.getKey();
            GUIButton button = entry.getValue();
            
            if (button != null) {
                button.update();
                inventory.setItem(slot, button.getItem());
            }
        }
    }
    
    @Override
    public String getTitle() {
        return title;
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
    @Override
    public Map<Integer, GUIButton> getButtons() {
        return buttons;
    }
    
    @Override
    public GUIButton getButton(int slot) {
        return buttons.get(slot);
    }
    
    @Override
    public void setButton(int slot, GUIButton button) {
        if (slot >= 0 && slot < size) {
            buttons.put(slot, button);
            if (inventory != null && button != null) {
                button.update();
                inventory.setItem(slot, button.getItem());
            }
        }
    }
    
    @Override
    public void removeButton(int slot) {
        buttons.remove(slot);
        if (inventory != null) {
            inventory.setItem(slot, null);
        }
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        
        if (slot >= 0 && slot < size) {
            GUIButton button = getButton(slot);
            
            if (button != null) {
                if (button.isClickable()) {
                    button.onClick((Player) event.getWhoClicked(), event.getClick());
                }
                event.setCancelled(true);
            } else {
                event.setCancelled(true);
            }
        } else if (slot >= size && !allowPlayerInventoryClick) {
            event.setCancelled(true);
        }
    }
    
    @Override
    public boolean isAllowPlayerInventoryClick() {
        return allowPlayerInventoryClick;
    }
    
    @Override
    public void setPreviousGUI(GUI previousGUI) {
        this.previousGUI = previousGUI;
    }
    
    @Override
    public GUI getPreviousGUI() {
        return previousGUI;
    }
}