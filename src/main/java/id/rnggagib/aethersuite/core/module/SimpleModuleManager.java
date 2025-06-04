package id.rnggagib.aethersuite.core.module;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.module.Module;
import id.rnggagib.aethersuite.api.module.ModuleManager;

import java.util.*;
import java.util.logging.Level;

public class SimpleModuleManager implements ModuleManager {
    private final AetherSuite plugin;
    private final Map<String, Module> modules = new HashMap<>();
    
    public SimpleModuleManager(AetherSuite plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void registerModule(Module module) {
        modules.put(module.getName().toLowerCase(), module);
    }
    
    @Override
    public void enableModule(String moduleName) {
        Optional<Module> moduleOpt = getModule(moduleName);
        
        if (moduleOpt.isPresent()) {
            Module module = moduleOpt.get();
            
            if (!module.isEnabled()) {
                try {
                    module.setEnabled(true);
                    plugin.getLogger().info("Enabled module: " + module.getName());
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to enable module: " + module.getName(), e);
                }
            }
        }
    }
    
    @Override
    public void disableModule(String moduleName) {
        Optional<Module> moduleOpt = getModule(moduleName);
        
        if (moduleOpt.isPresent()) {
            Module module = moduleOpt.get();
            
            if (module.isEnabled()) {
                try {
                    module.setEnabled(false);
                    plugin.getLogger().info("Disabled module: " + module.getName());
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to disable module: " + module.getName(), e);
                }
            }
        }
    }
    
    @Override
    public void enableAllModules() {
        for (Module module : modules.values()) {
            if (!module.isEnabled()) {
                enableModule(module.getName());
            }
        }
    }
    
    @Override
    public void disableAllModules() {
        for (Module module : modules.values()) {
            if (module.isEnabled()) {
                disableModule(module.getName());
            }
        }
    }
    
    @Override
    public Optional<Module> getModule(String moduleName) {
        return Optional.ofNullable(modules.get(moduleName.toLowerCase()));
    }
    
    @Override
    public Collection<Module> getModules() {
        return Collections.unmodifiableCollection(modules.values());
    }
    
    @Override
    public boolean isModuleEnabled(String moduleName) {
        return getModule(moduleName).map(Module::isEnabled).orElse(false);
    }
}