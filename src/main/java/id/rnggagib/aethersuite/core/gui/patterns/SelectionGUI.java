package id.rnggagib.aethersuite.core.gui.patterns;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.gui.GUI;
import id.rnggagib.aethersuite.api.gui.GUIAction;
import id.rnggagib.aethersuite.core.gui.GUIUtils;
import id.rnggagib.aethersuite.core.gui.PaginatedGUI;
import id.rnggagib.aethersuite.core.gui.SimpleButton;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class SelectionGUI<T> extends PaginatedGUI {
    private final List<T> options;
    private final BiConsumer<Player, T> selectionCallback;
    private final GUI returnGui;
    private final ItemProvider<T> itemProvider;
    
    public SelectionGUI(AetherSuite plugin, String title, List<T> options, 
                       ItemProvider<T> itemProvider, BiConsumer<Player, T> selectionCallback) {
        this(plugin, title, options, itemProvider, selectionCallback, null);
    }
    
    public SelectionGUI(AetherSuite plugin, String title, List<T> options, 
                       ItemProvider<T> itemProvider, BiConsumer<Player, T> selectionCallback,
                       GUI returnGui) {
        super(plugin, title, 6);
        this.options = options;
        this.selectionCallback = selectionCallback;
        this.returnGui = returnGui;
        this.itemProvider = itemProvider;
        
        setupGUI();
    }
    
    private void setupGUI() {
        // Add close button
        setButton(49, new SimpleButton(
                GUIUtils.createItem(Material.BARRIER, "§cClose", "§7Click to close this menu"),
                (player, clickType) -> {
                    close(player);
                    if (returnGui != null) {
                        plugin.getScheduler().runTaskLater(() -> returnGui.open(player), 1L);
                    }
                }
        ).setClickSound(Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF));
        
        // Fill with options
        for (int i = 0; i < options.size(); i++) {
            T option = options.get(i);
            ItemStack optionItem = itemProvider.getItemFor(option);
            
            addItem(new SimpleButton(optionItem, (player, clickType) -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                close(player);
                
                if (selectionCallback != null) {
                    selectionCallback.accept(player, option);
                }
            }));
        }
    }
    
    public static <T> void openSelection(AetherSuite plugin, Player player, String title, 
                                        List<T> options, ItemProvider<T> itemProvider, 
                                        BiConsumer<Player, T> selectionCallback) {
        openSelection(plugin, player, title, options, itemProvider, selectionCallback, null);
    }
    
    public static <T> void openSelection(AetherSuite plugin, Player player, String title, 
                                        List<T> options, ItemProvider<T> itemProvider, 
                                        BiConsumer<Player, T> selectionCallback, GUI returnGui) {
        SelectionGUI<T> gui = new SelectionGUI<>(plugin, title, options, itemProvider, selectionCallback, returnGui);
        gui.open(player);
    }
    
    public interface ItemProvider<T> {
        ItemStack getItemFor(T option);
    }
}