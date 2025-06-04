package id.rnggagib.aethersuite.core.gui.animation;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.gui.GUIAction;
import id.rnggagib.aethersuite.core.gui.SimpleButton;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AnimatedButton extends SimpleButton {
    private final List<ItemStack> frames = new ArrayList<>();
    private int currentFrame = 0;
    private int animationTaskId = -1;
    private long frameDelay;
    private boolean repeating = true;
    private boolean playing = false;
    private final AetherSuite plugin;
    
    public AnimatedButton(AetherSuite plugin, ItemStack initialItem, GUIAction action, long frameDelayTicks) {
        super(initialItem, action);
        this.plugin = plugin;
        this.frameDelay = frameDelayTicks;
        frames.add(initialItem);
    }
    
    public AnimatedButton addFrame(ItemStack frame) {
        frames.add(frame);
        return this;
    }
    
    public AnimatedButton addFrames(List<ItemStack> frames) {
        this.frames.addAll(frames);
        return this;
    }
    
    public AnimatedButton setRepeating(boolean repeating) {
        this.repeating = repeating;
        return this;
    }
    
    public AnimatedButton setFrameDelay(long frameDelayTicks) {
        this.frameDelay = frameDelayTicks;
        return this;
    }
    
    public void startAnimation() {
        if (playing || frames.size() <= 1) return;
        
        playing = true;
        currentFrame = 0;
        
        animationTaskId = plugin.getScheduler().runTaskTimer(() -> {
            if (!playing) return;
            
            currentFrame = (currentFrame + 1) % frames.size();
            setItem(frames.get(currentFrame));
            
            if (!repeating && currentFrame == frames.size() - 1) {
                stopAnimation();
            }
        }, frameDelay, frameDelay);
    }
    
    public void stopAnimation() {
        if (!playing) return;
        
        playing = false;
        if (animationTaskId != -1) {
            plugin.getScheduler().cancelTask(animationTaskId);
            animationTaskId = -1;
        }
        
        // Reset to first frame
        currentFrame = 0;
        setItem(frames.get(0));
    }
    
    public boolean isPlaying() {
        return playing;
    }
    
    @Override
    public void onClick(Player player, ClickType clickType) {
        if (!isClickable()) return;
        
        if (isPlaying()) {
            stopAnimation();
        } else {
            startAnimation();
        }
        
        if (clickSound != null) {
            player.playSound(player.getLocation(), clickSound, 0.5f, 1.0f);
        }
        
        // Delegate action execution to the superclass
        super.onClick(player, clickType);
    }
    
    private Sound clickSound;
    
    @Override
    public AnimatedButton setClickSound(Sound sound) {
        this.clickSound = sound;
        return this;
    }
}