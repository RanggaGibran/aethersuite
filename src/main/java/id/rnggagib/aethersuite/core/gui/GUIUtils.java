package id.rnggagib.aethersuite.core.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GUIUtils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    
    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
            
            if (lore.length > 0) {
                List<Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(Component.text(line).decoration(TextDecoration.ITALIC, false));
                }
                meta.lore(loreComponents);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public static ItemStack createItem(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.displayName(name.decoration(TextDecoration.ITALIC, false));
            
            if (lore != null && !lore.isEmpty()) {
                List<Component> loreWithoutItalic = new ArrayList<>();
                for (Component line : lore) {
                    loreWithoutItalic.add(line.decoration(TextDecoration.ITALIC, false));
                }
                meta.lore(loreWithoutItalic);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public static ItemStack createItemFromMiniMessage(Material material, String name, String... lore) {
        Component nameComponent = miniMessage.deserialize(name);
        
        List<Component> loreComponents = new ArrayList<>();
        for (String line : lore) {
            loreComponents.add(miniMessage.deserialize(line));
        }
        
        return createItem(material, nameComponent, loreComponents);
    }
    
    public static ItemStack createGlowingItem(Material material, String name, String... lore) {
        ItemStack item = createItem(material, name, lore);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public static ItemStack setTag(ItemStack item, String key, String value) {
        if (item == null) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(new NamespacedKey("aethersuite", key), PersistentDataType.STRING, value);
        
        item.setItemMeta(meta);
        return item;
    }
    
    public static String getTag(ItemStack item, String key) {
        if (item == null) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(new NamespacedKey("aethersuite", key), PersistentDataType.STRING);
    }
    
    public static boolean hasTag(ItemStack item, String key) {
        if (item == null) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(new NamespacedKey("aethersuite", key), PersistentDataType.STRING);
    }
    
    public static int[] getOuterSlots(int rows) {
        List<Integer> slots = new ArrayList<>();
        
        // Top row
        for (int i = 0; i < 9; i++) {
            slots.add(i);
        }
        
        // Side columns (excluding corners)
        for (int row = 1; row < rows - 1; row++) {
            slots.add(row * 9);
            slots.add(row * 9 + 8);
        }
        
        // Bottom row
        for (int i = (rows - 1) * 9; i < rows * 9; i++) {
            slots.add(i);
        }
        
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }
    
    public static int[] getContentSlots(int rows) {
        List<Integer> slots = new ArrayList<>();
        
        for (int row = 1; row < rows - 1; row++) {
            for (int col = 1; col < 8; col++) {
                slots.add(row * 9 + col);
            }
        }
        
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }
    
    public static void fillBorder(AbstractGUI gui, Material material) {
        int[] borderSlots = getOuterSlots(gui.getSize() / 9);
        
        for (int slot : borderSlots) {
            gui.setButton(slot, new SimpleButton(
                    createItem(material, " "),
                    null
            ));
        }
    }
}