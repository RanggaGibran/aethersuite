package id.rnggagib.aethersuite.api.module;

public interface Module {
    String getName();
    
    void onEnable();
    
    void onDisable();
    
    boolean isEnabled();
    
    void setEnabled(boolean enabled);
    
    default String getConfigSection() {
        return "modules." + getName().toLowerCase();
    }
}