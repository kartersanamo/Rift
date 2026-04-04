package com.kartersanamo.rift;

import com.kartersanamo.rift.api.command.CommandManager;
import com.kartersanamo.rift.api.config.ConfigUtil;
import com.kartersanamo.rift.api.gui.GUIManager;
import com.kartersanamo.rift.api.logging.CoreLogger;
import com.kartersanamo.rift.api.logging.LogLevel;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.command.DeletewarpCommand;
import com.kartersanamo.rift.command.SetwarpCommand;
import com.kartersanamo.rift.command.WarpCommand;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Rift extends JavaPlugin {

    private static Rift instance;
    private static CoreLogger logger;

    private CommandManager commandManager;
    private GUIManager guiManager;
    private WarpManager warpManager;

    private File messagesFile;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        instance = this;

        initLogger();
        registerManagers();
        registerCommands();
        registerListeners();
        saveDefaultConfig();
        ConfigUtil.load(getConfig());
        createConfigs();

        logger.info(getDescription().getName() + "v" + getDescription().getVersion() + " has been enabled!");
    }

    private void initLogger() {
        logger = new CoreLogger(this);
        logger.setLevel(LogLevel.INFO);
    }

    private void registerManagers() {
        commandManager = new CommandManager(this);
        warpManager = new WarpManager(this);
        guiManager = new GUIManager(this);
    }

    private void registerCommands() {
        commandManager.registerCommand(new SetwarpCommand(warpManager));
        commandManager.registerCommand(new WarpCommand(warpManager));
        commandManager.registerCommand(new DeletewarpCommand(warpManager));
    }

    private void registerListeners() {
        // Bukkit.getPluginManager().registerEvents(new TestListener(), this);
    }

    private void createConfigs() {
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        MessagesUtil.load(messagesConfig);
        logger.info("Messages cache loaded.");
    }

    @Override
    public void onDisable() {
        logger.close();
    }

    // Getters

    public static CoreLogger getLog() { return logger; }

    public static Rift getInstance() {
        return instance;
    }

    public GUIManager getGuiManager() { return guiManager; }
}
