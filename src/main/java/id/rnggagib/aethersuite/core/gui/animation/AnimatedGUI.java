package id.rnggagib.aethersuite.core.gui.animation;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.gui.GUIButton;
import id.rnggagib.aethersuite.core.gui.AbstractGUI;
import id.rnggagib.aethersuite.core.gui.SimpleButton;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class AnimatedGUI extends AbstractGUI {
    private final Set<Integer> animatedSlots = new HashSet<>();
    private int animationTaskId = -1;
    private final Map<Integer, Integer> slotStates = new HashMap<>();
    private final Map<Integer, AnimationPattern> slotPatterns = new HashMap<>();
    
    public AnimatedGUI(AetherSuite plugin, String title, int size) {
        super(plugin, title, size);
    }
    
    public void addAnimatedButton(int slot, AnimatedButton button) {
        setButton(slot, button);
        animatedSlots.add(slot);
    }
    
    public void addAnimatedSlot(int slot, AnimationPattern pattern) {
        animatedSlots.add(slot);
        slotStates.put(slot, 0);
        slotPatterns.put(slot, pattern);
    }
    
    public void startAnimations() {
        if (animationTaskId != -1) return;
        
        animationTaskId = plugin.getScheduler().runTaskTimer(() -> {
            for (int slot : animatedSlots) {
                GUIButton button = getButton(slot);
                
                if (button instanceof AnimatedButton animatedButton) {
                    if (!animatedButton.isPlaying()) {
                        animatedButton.startAnimation();
                    }
                } else if (slotPatterns.containsKey(slot)) {
                    AnimationPattern pattern = slotPatterns.get(slot);
                    int state = slotStates.getOrDefault(slot, 0);
                    
                    // Update state
                    state = (state + 1) % pattern.getFrameCount();
                    slotStates.put(slot, state);
                    
                    // Update button
                    setButton(slot, new SimpleButton(pattern.getFrame(state)));
                }
            }
            
            update();
        }, 10L, 10L);
    }
    
    public void stopAnimations() {
        if (animationTaskId != -1) {
            plugin.getScheduler().cancelTask(animationTaskId);
            animationTaskId = -1;
        }
        
        for (int slot : animatedSlots) {
            GUIButton button = getButton(slot);
            if (button instanceof AnimatedButton animatedButton) {
                animatedButton.stopAnimation();
            }
        }
    }
    
    @Override
    public void open(Player player) {
        startAnimations();
        super.open(player);
        
        // Play sound effect
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1.0f);
    }
    
    @Override
    public void close(Player player) {
        stopAnimations();
        super.close(player);
    }
    
    // Animation pattern interface for customizable animations
    public interface AnimationPattern {
        ItemStack getFrame(int state);
        int getFrameCount();
    }
    
    // Border animation pattern
    public static class BorderAnimation implements AnimationPattern {
        private final Material[] materials;
        
        public BorderAnimation(Material... materials) {
            this.materials = materials;
        }
        
        @Override
        public ItemStack getFrame(int state) {
            return new ItemStack(materials[state % materials.length]);
        }
        
        @Override
        public int getFrameCount() {
            return materials.length;
        }
    }
    
    // Color cycle animation pattern
    public static class ColorCycle implements AnimationPattern {
        private static final Material[] GLASS_COLORS = {
                Material.WHITE_STAINED_GLASS_PANE,
                Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                Material.BLUE_STAINED_GLASS_PANE,
                Material.PURPLE_STAINED_GLASS_PANE,
                Material.PINK_STAINED_GLASS_PANE,
                Material.RED_STAINED_GLASS_PANE,
                Material.ORANGE_STAINED_GLASS_PANE,
                Material.YELLOW_STAINED_GLASS_PANE,
                Material.LIME_STAINED_GLASS_PANE,
                Material.GREEN_STAINED_GLASS_PANE,
                Material.CYAN_STAINED_GLASS_PANE
        };
        
        private final Function<Material, ItemStack> itemFactory;
        
        public ColorCycle() {
            this(material -> new ItemStack(material));
        }
        
        public ColorCycle(Function<Material, ItemStack> itemFactory) {
            this.itemFactory = itemFactory;
        }
        
        @Override
        public ItemStack getFrame(int state) {
            return itemFactory.apply(GLASS_COLORS[state % GLASS_COLORS.length]);
        }
        
        @Override
        public int getFrameCount() {
            return GLASS_COLORS.length;
        }
    }
}