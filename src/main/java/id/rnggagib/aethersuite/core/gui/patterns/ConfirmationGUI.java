package id.rnggagib.aethersuite.core.gui.patterns;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.gui.GUI;
import id.rnggagib.aethersuite.api.gui.GUIAction;
import id.rnggagib.aethersuite.core.gui.AbstractGUI;
import id.rnggagib.aethersuite.core.gui.GUIUtils;
import id.rnggagib.aethersuite.core.gui.SimpleButton;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ConfirmationGUI extends AbstractGUI {
    public enum ConfirmationType {
        YES_NO,
        OK_CANCEL,
        ACCEPT_DECLINE
    }
    
    private final GUIAction onConfirm;
    private final GUIAction onCancel;
    private final String confirmMessage;
    private final ConfirmationType type;
    
    public ConfirmationGUI(AetherSuite plugin, String title, String confirmMessage, 
                           GUIAction onConfirm, GUIAction onCancel) {
        this(plugin, title, confirmMessage, onConfirm, onCancel, ConfirmationType.YES_NO);
    }
    
    public ConfirmationGUI(AetherSuite plugin, String title, String confirmMessage,
                           GUIAction onConfirm, GUIAction onCancel, ConfirmationType type) {
        super(plugin, title, 3 * 9);
        this.confirmMessage = confirmMessage;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        this.type = type;
        
        setupGUI();
    }
    
    private void setupGUI() {
        // Fill with glass panes
        for (int i = 0; i < getSize(); i++) {
            setButton(i, new SimpleButton(
                    GUIUtils.createItem(Material.GRAY_STAINED_GLASS_PANE, " "),
                    null
            ));
        }
        
        // Add message item in the center
        setButton(4, new SimpleButton(
                GUIUtils.createItem(Material.PAPER, "§eConfirmation", confirmMessage),
                null
        ));
        
        // Add confirm button (green wool/terracotta)
        Material confirmMaterial = Material.LIME_WOOL;
        String confirmText;
        String confirmDesc;
        
        switch (type) {
            case YES_NO:
                confirmText = "§aYes";
                confirmDesc = "§7Click to confirm";
                break;
            case OK_CANCEL:
                confirmText = "§aOK";
                confirmDesc = "§7Click to proceed";
                break;
            case ACCEPT_DECLINE:
                confirmText = "§aAccept";
                confirmDesc = "§7Click to accept";
                break;
            default:
                confirmText = "§aConfirm";
                confirmDesc = "§7Click to confirm";
        }
        
        setButton(11, new SimpleButton(
                GUIUtils.createItem(confirmMaterial, confirmText, confirmDesc),
                (player, clickType) -> {
                    close(player);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    if (onConfirm != null) {
                        onConfirm.execute(player, clickType);
                    }
                }
        ));
        
        // Add cancel button (red wool/terracotta)
        Material cancelMaterial = Material.RED_WOOL;
        String cancelText;
        String cancelDesc;
        
        switch (type) {
            case YES_NO:
                cancelText = "§cNo";
                cancelDesc = "§7Click to cancel";
                break;
            case OK_CANCEL:
                cancelText = "§cCancel";
                cancelDesc = "§7Click to cancel";
                break;
            case ACCEPT_DECLINE:
                cancelText = "§cDecline";
                cancelDesc = "§7Click to decline";
                break;
            default:
                cancelText = "§cCancel";
                cancelDesc = "§7Click to cancel";
        }
        
        setButton(15, new SimpleButton(
                GUIUtils.createItem(cancelMaterial, cancelText, cancelDesc),
                (player, clickType) -> {
                    close(player);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                    if (onCancel != null) {
                        onCancel.execute(player, clickType);
                    }
                }
        ));
    }
    
    @Override
    public void close(Player player) {
        super.close(player);
        
        // Return to previous GUI if available
        if (getPreviousGUI() != null) {
            plugin.getScheduler().runTaskLater(() -> {
                getPreviousGUI().open(player);
            }, 1L);
        }
    }
    
    public static void confirm(AetherSuite plugin, Player player, String title, String message, 
                              GUIAction onConfirm) {
        confirm(plugin, player, title, message, onConfirm, null, null);
    }
    
    public static void confirm(AetherSuite plugin, Player player, String title, String message, 
                              GUIAction onConfirm, GUIAction onCancel) {
        confirm(plugin, player, title, message, onConfirm, onCancel, null);
    }
    
    public static void confirm(AetherSuite plugin, Player player, String title, String message, 
                              GUIAction onConfirm, GUIAction onCancel, GUI previousGUI) {
        ConfirmationGUI gui = new ConfirmationGUI(plugin, title, message, onConfirm, onCancel);
        
        if (previousGUI != null) {
            gui.setPreviousGUI(previousGUI);
        }
        
        gui.open(player);
    }
}