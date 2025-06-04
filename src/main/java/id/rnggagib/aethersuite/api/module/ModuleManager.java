package id.rnggagib.aethersuite.api.module;

import java.util.Collection;
import java.util.Optional;

public interface ModuleManager {
    void registerModule(Module module);
    
    void enableModule(String moduleName);
    
    void disableModule(String moduleName);
    
    void enableAllModules();
    
    void disableAllModules();
    
    Optional<Module> getModule(String moduleName);
    
    Collection<Module> getModules();
    
    boolean isModuleEnabled(String moduleName);
}