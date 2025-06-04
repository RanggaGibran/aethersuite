package id.rnggagib.aethersuite.core.gui;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.gui.GUIButton;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PaginatedGUI extends AbstractGUI {
    private final List<GUIButton> pageItems = new ArrayList<>();
    private final int pageSize;
    private final int[] contentSlots;
    private int currentPage = 0;
    private final int navigationRow;
    private boolean showPageNumbers = true;
    
    public PaginatedGUI(AetherSuite plugin, String title, int rows, int[] contentSlots) {
        super(plugin, title, rows * 9);
        this.contentSlots = contentSlots;
        this.pageSize = contentSlots.length;
        this.navigationRow = rows - 1;
        setupNavigationBar();
    }
    
    public PaginatedGUI(AetherSuite plugin, String title, int rows) {
        super(plugin, title, rows * 9);
        
        // Default content slots (all except bottom row)
        List<Integer> slots = new ArrayList<>();
        for (int row = 0; row < rows - 1; row++) {
            for (int col = 0; col < 9; col++) {
                slots.add(row * 9 + col);
            }
        }
        
        this.contentSlots = slots.stream().mapToInt(Integer::intValue).toArray();
        this.pageSize = contentSlots.length;
        this.navigationRow = rows - 1;
        setupNavigationBar();
    }
    
    private void setupNavigationBar() {
        int navBarStart = navigationRow * 9;
        
        // Previous page button (slot 0 of navigation bar)
        setButton(navBarStart, new SimpleButton(
                GUIUtils.createItem(Material.ARROW, "§a← Previous Page", "§7Click to go to the previous page"),
                (player, clickType) -> {
                    if (currentPage > 0) {
                        currentPage--;
                        update();
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                    }
                }
        ));
        
        // Next page button (slot 8 of navigation bar)
        setButton(navBarStart + 8, new SimpleButton(
                GUIUtils.createItem(Material.ARROW, "§aNext Page →", "§7Click to go to the next page"),
                (player, clickType) -> {
                    if ((currentPage + 1) * pageSize < pageItems.size()) {
                        currentPage++;
                        update();
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                    }
                }
        ));
        
        // Page indicator (center of navigation bar)
        setButton(navBarStart + 4, new SimpleButton(
                GUIUtils.createItem(Material.BOOK, "§ePage Information", "§7Current Page: §e1"),
                null
        ));
        
        // Fill navigation bar with glass panes
        for (int i = 1; i < 4; i++) {
            setButton(navBarStart + i, new SimpleButton(
                    GUIUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " "),
                    null
            ));
            setButton(navBarStart + i + 4, new SimpleButton(
                    GUIUtils.createItem(Material.BLACK_STAINED_GLASS_PANE, " "),
                    null
            ));
        }
    }
    
    public void addItem(GUIButton button) {
        pageItems.add(button);
    }
    
    public void addItems(List<GUIButton> buttons) {
        pageItems.addAll(buttons);
    }
    
    public void clearItems() {
        pageItems.clear();
        currentPage = 0;
    }
    
    public int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) pageItems.size() / pageSize));
    }
    
    public int getCurrentPage() {
        return currentPage + 1; // 1-based for display
    }
    
    public void setCurrentPage(int page) {
        // Convert from 1-based to 0-based
        int zeroBasedPage = page - 1;
        
        if (zeroBasedPage >= 0 && zeroBasedPage < getTotalPages()) {
            this.currentPage = zeroBasedPage;
        }
    }
    
    public void setShowPageNumbers(boolean showPageNumbers) {
        this.showPageNumbers = showPageNumbers;
    }
    
    @Override
    public void update() {
        // Clear content slots
        for (int slot : contentSlots) {
            inventory.setItem(slot, null);
        }
        
        // Update navigation buttons
        int navBarStart = navigationRow * 9;
        
        // Previous page button visibility
        GUIButton prevButton = getButton(navBarStart);
        if (prevButton instanceof SimpleButton simpleButton) {
            if (currentPage > 0) {
                simpleButton.setItem(GUIUtils.createItem(Material.ARROW, "§a← Previous Page", "§7Click to go to the previous page"));
                simpleButton.setClickable(true);
            } else {
                simpleButton.setItem(GUIUtils.createItem(Material.BARRIER, "§8← Previous Page", "§7You are on the first page"));
                simpleButton.setClickable(false);
            }
        }
        
        // Next page button visibility
        GUIButton nextButton = getButton(navBarStart + 8);
        if (nextButton instanceof SimpleButton simpleButton) {
            if ((currentPage + 1) * pageSize < pageItems.size()) {
                simpleButton.setItem(GUIUtils.createItem(Material.ARROW, "§aNext Page →", "§7Click to go to the next page"));
                simpleButton.setClickable(true);
            } else {
                simpleButton.setItem(GUIUtils.createItem(Material.BARRIER, "§8Next Page →", "§7You are on the last page"));
                simpleButton.setClickable(false);
            }
        }
        
        // Page indicator
        if (showPageNumbers) {
            GUIButton pageIndicator = getButton(navBarStart + 4);
            if (pageIndicator instanceof SimpleButton simpleButton) {
                simpleButton.setItem(GUIUtils.createItem(Material.BOOK, "§ePage Information", 
                        "§7Current Page: §e" + getCurrentPage() + "§7/§e" + getTotalPages(),
                        "§7Total Items: §e" + pageItems.size()));
            }
        }
        
        // Add content items for current page
        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, pageItems.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            if (slotIndex < contentSlots.length) {
                setButton(contentSlots[slotIndex], pageItems.get(i));
            }
        }
        
        super.update();
    }
    
    @Override
    public void open(Player player) {
        update();
        super.open(player);
    }
}