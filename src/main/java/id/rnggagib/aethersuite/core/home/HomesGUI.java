package id.rnggagib.aethersuite.core.home;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.home.Home;
import id.rnggagib.aethersuite.core.gui.GUIUtils;
import id.rnggagib.aethersuite.core.gui.PaginatedGUI;
import id.rnggagib.aethersuite.core.gui.SimpleButton;
import id.rnggagib.aethersuite.core.gui.patterns.ConfirmationGUI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class HomesGUI extends PaginatedGUI {
    private final Player player;
    private final HomeManager homeManager;
    private List<Home> homes;
    
    public HomesGUI(AetherSuite plugin, Player player) {
        super(plugin, plugin.getMessageProvider().getRawMessage("modules.player.homes.gui-title"), 6);
        this.player = player;
        this.homeManager = plugin.getHomeManager();
        
        loadHomes();
        setupGui();
    }
    
    private void loadHomes() {
        this.homes = homeManager.getHomes(player).join();
    }
    
    private void setupGui() {
        // Add a create new home button
        setButton(4, new SimpleButton(
                GUIUtils.createGlowingItem(Material.EMERALD, 
                        plugin.getMessageProvider().getRawMessage("modules.player.homes.gui-set-home"),
                        plugin.getMessageProvider().getRawMessage("modules.player.homes.gui-set-home-desc")),
                (p, clickType) -> {
                    close(p);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    p.performCommand("sethome");
                }
        ));
        
        // Add home buttons
        for (Home home : homes) {
            addHomeButton(home);
        }
        
        // Add filler if there are no homes
        if (homes.isEmpty()) {
            setButton(22, new SimpleButton(
                    GUIUtils.createItem(Material.BARRIER, 
                            plugin.getMessageProvider().getRawMessage("modules.player.homes.gui-no-homes"),
                            plugin.getMessageProvider().getRawMessage("modules.player.homes.gui-no-homes-desc")),
                    null
            ));
        }
    }
    
    private void addHomeButton(Home home) {
        Location loc = home.getLocation();
        Material material = getBestMaterialForWorld(loc.getWorld().getName());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        ItemStack homeItem = GUIUtils.createItem(material,
                "§a" + home.getName(),
                "§7World: §f" + loc.getWorld().getName(),
                "§7Location: §f" + String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ()),
                "§7Created: §f" + sdf.format(Date.from(home.getCreatedAt())),
                "",
                "§eLeft-Click §7to teleport",
                "§eRight-Click §7to delete");
        
        addItem(new SimpleButton(homeItem, (p, clickType) -> {
            if (clickType == ClickType.LEFT) {
                // Teleport to home
                close(p);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f);
                homeManager.teleportToHome(p, home.getName());
            } else if (clickType == ClickType.RIGHT) {
                // Confirm deletion
                ConfirmationGUI.confirm(
                        plugin,
                        p,
                        plugin.getMessageProvider().getRawMessage("modules.player.homes.gui-confirm-delete-title"),
                        plugin.getMessageProvider().getRawMessage("modules.player.homes.gui-confirm-delete-desc")
                                .replace("{name}", home.getName()),
                        (player, click) -> {
                            homeManager.deleteHome(player, home.getName()).thenRun(() -> {
                                plugin.getScheduler().runTask(() -> {
                                    new HomesGUI(plugin, player).open(player);
                                });
                            });
                        },
                        (player, click) -> {
                            plugin.getScheduler().runTask(() -> {
                                new HomesGUI(plugin, player).open(player);
                            });
                        },
                        this
                );
            }
        }));
    }
    
    private Material getBestMaterialForWorld(String worldName) {
        return switch (worldName.toLowerCase()) {
            case "world_nether", "nether" -> Material.NETHERRACK;
            case "world_the_end", "the_end", "end" -> Material.END_STONE;
            default -> Material.GRASS_BLOCK;
        };
    }
}