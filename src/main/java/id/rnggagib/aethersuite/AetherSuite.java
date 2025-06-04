package id.rnggagib.aethersuite;

import id.rnggagib.aethersuite.api.command.BaseCommand;
import id.rnggagib.aethersuite.api.database.DatabaseManager;
import id.rnggagib.aethersuite.api.module.ModuleManager;
import id.rnggagib.aethersuite.api.scheduler.TaskScheduler;
import id.rnggagib.aethersuite.common.config.ConfigManager;
import id.rnggagib.aethersuite.common.message.MessageProvider;
import id.rnggagib.aethersuite.core.command.CommandManager;
import id.rnggagib.aethersuite.core.command.HelpCommand;
import id.rnggagib.aethersuite.core.command.player.LanguageCommand;
import id.rnggagib.aethersuite.core.command.player.SettingsCommand;
import id.rnggagib.aethersuite.core.command.teleport.*;
import id.rnggagib.aethersuite.core.database.SimpleDatabaseManager;
import id.rnggagib.aethersuite.core.gui.GUIManager;
import id.rnggagib.aethersuite.core.module.SimpleModuleManager;
import id.rnggagib.aethersuite.core.platform.PlatformType;
import id.rnggagib.aethersuite.core.platform.PlatformDetector;
import id.rnggagib.aethersuite.core.player.PlayerManager;
import id.rnggagib.aethersuite.core.scheduler.FoliaTaskScheduler;
import id.rnggagib.aethersuite.core.scheduler.PaperTaskScheduler;
import id.rnggagib.aethersuite.core.teleport.TeleportManager;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class AetherSuite extends JavaPlugin {
    private static AetherSuite instance;
    private PlatformType platformType;
    private TaskScheduler scheduler;
    private ConfigManager configManager;
    private MessageProvider messageProvider;
    private ModuleManager moduleManager;
    private DatabaseManager databaseManager;
    private CommandManager commandManager;
    private PlayerManager playerManager;
    private TeleportManager teleportManager;

    @Override
    public void onEnable() {
        instance = this;
        Logger logger = getLogger();
        
        logger.info("Initializing AetherSuite...");
        logger.info("Detecting platform...");
        
        this.platformType = PlatformDetector.detectPlatform();
        logger.info("Running on " + platformType.name() + " platform");
        
        initializeScheduler();
        
        this.configManager = new ConfigManager(this);
        
        this.messageProvider = new MessageProvider(this);
        
        this.databaseManager = new SimpleDatabaseManager(this);
        this.databaseManager.initialize();
        
        this.moduleManager = new SimpleModuleManager(this);
        
        this.playerManager = new PlayerManager(this);
        
        this.teleportManager = new TeleportManager(this);
        
        this.commandManager = new CommandManager(this);
        
        // Initialize GUI Manager
        GUIManager.initialize(this);
        
        registerCommands();
        
        registerModules();
        
        // Schedule periodic player data saving
        scheduler.runTaskTimer(() -> {
            playerManager.saveAllPlayerData();
        }, 20 * 60 * 5, 20 * 60 * 5); // Every 5 minutes
        
        logger.info("AetherSuite has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (playerManager != null) {
            playerManager.saveAllPlayerData();
        }
        
        if (moduleManager != null) {
            moduleManager.disableAllModules();
        }
        
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        
        getLogger().info("AetherSuite has been disabled");
        instance = null;
    }
    
    private void initializeScheduler() {
        this.scheduler = switch (platformType) {
            case FOLIA -> new FoliaTaskScheduler(this);
            case PAPER -> new PaperTaskScheduler(this);
        };
    }
    
    private void registerCommands() {
        // Register core commands
        registerCommand(new HelpCommand(this));
        registerCommand(new LanguageCommand(this));
        registerCommand(new SettingsCommand(this));
        
        // Register teleport commands
        registerCommand(new TpaCommand(this));
        registerCommand(new TpahereCommand(this));
        registerCommand(new TpacceptCommand(this));
        registerCommand(new TpdenyCommand(this));
        registerCommand(new BackCommand(this));
        
        // Additional commands will be registered here or by modules
    }
    
    public void registerCommand(BaseCommand command) {
        commandManager.registerCommand(command);
    }
    
    private void registerModules() {
        // Module registration will be implemented in next phase
    }
    
    public static AetherSuite getInstance() {
        return instance;
    }
    
    public TaskScheduler getScheduler() {
        return scheduler;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MessageProvider getMessageProvider() {
        return messageProvider;
    }
    
    public ModuleManager getModuleManager() {
        return moduleManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public CommandManager getCommandManager() {
        return commandManager;
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    
    public TeleportManager getTeleportManager() {
        return teleportManager;
    }
    
    public PlatformType getPlatformType() {
        return platformType;
    }
}