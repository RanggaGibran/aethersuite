package id.rnggagib.aethersuite.core.module;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.module.Module;

public abstract class AbstractModule implements Module {
    protected final AetherSuite plugin;
    private boolean enabled = false;
    
    protected AbstractModule(AetherSuite plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}