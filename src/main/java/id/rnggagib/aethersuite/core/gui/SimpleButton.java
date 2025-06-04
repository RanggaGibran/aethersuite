package id.rnggagib.aethersuite.core.gui;

import id.rnggagib.aethersuite.api.gui.GUIAction;
import id.rnggagib.aethersuite.api.gui.GUIButton;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SimpleButton implements GUIButton {
    private ItemStack item;
    private boolean clickable = true;
    private final Map<ClickType, GUIAction> actions = new HashMap<>();
    private GUIAction defaultAction;
    private Sound clickSound;
    
    public SimpleButton(ItemStack item) {
        this.item = item;
    }
    
    public SimpleButton(ItemStack item, GUIAction action) {
        this.item = item;
        this.defaultAction = action;
    }
    
    @Override
    public ItemStack getItem() {
        return item;
    }
    
    @Override
    public void setItem(ItemStack item) {
        this.item = item;
    }
    
    @Override
    public boolean isClickable() {
        return clickable;
    }
    
    @Override
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }
    
    @Override
    public void onClick(Player player, ClickType clickType) {
        if (!clickable) return;
        
        if (clickSound != null) {
            player.playSound(player.getLocation(), clickSound, 0.5f, 1.0f);
        }
        
        GUIAction action = actions.getOrDefault(clickType, defaultAction);
        if (action != null) {
            action.execute(player, clickType);
        }
    }
    
    @Override
    public void update() {
        // Simple button doesn't need updates
    }
    
    public SimpleButton addAction(ClickType clickType, GUIAction action) {
        actions.put(clickType, action);
        return this;
    }
    
    public SimpleButton setDefaultAction(GUIAction action) {
        this.defaultAction = action;
        return this;
    }
    
    public SimpleButton setClickSound(Sound sound) {
        this.clickSound = sound;
        return this;
    }
}