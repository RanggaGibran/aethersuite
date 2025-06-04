package id.rnggagib.aethersuite.core.gui;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.gui.GUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GUIManager implements Listener {
    private static final Map<UUID, GUI> activeGUIs = new ConcurrentHashMap<>();
    private static AetherSuite plugin;
    
    public static void initialize(AetherSuite plugin) {
        GUIManager.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new GUIManager(), plugin);
    }
    
    public static void registerActiveGUI(Player player, GUI gui) {
        activeGUIs.put(player.getUniqueId(), gui);
    }
    
    public static void unregisterActiveGUI(Player player) {
        activeGUIs.remove(player.getUniqueId());
    }
    
    public static GUI getActiveGUI(Player player) {
        return activeGUIs.get(player.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        GUI gui = activeGUIs.get(player.getUniqueId());
        if (gui != null && event.getView().getTopInventory().equals(gui.getInventory())) {
            gui.handleClick(event);
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        GUI gui = activeGUIs.get(player.getUniqueId());
        if (gui != null && event.getView().getTopInventory().equals(gui.getInventory())) {
            // Cancel all drag events in the GUI
            for (int slot : event.getRawSlots()) {
                if (slot < gui.getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        
        GUI gui = activeGUIs.get(player.getUniqueId());
        if (gui != null && event.getInventory().equals(gui.getInventory())) {
            // Small delay to check if a new inventory was opened
            plugin.getScheduler().runTaskLater(() -> {
                if (!activeGUIs.containsKey(player.getUniqueId()) || 
                    !player.getOpenInventory().getTopInventory().equals(gui.getInventory())) {
                    unregisterActiveGUI(player);
                }
            }, 1L);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        unregisterActiveGUI(event.getPlayer());
    }
}