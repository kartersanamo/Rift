package com.kartersanamo.rift;

import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.chat.ChatInputManager;
import com.kartersanamo.rift.api.command.CommandManager;
import com.kartersanamo.rift.api.command.SubCommand;
import com.kartersanamo.rift.api.config.ConfigUtil;
import com.kartersanamo.rift.api.gui.GUIManager;
import com.kartersanamo.rift.api.logging.AuditLogger;
import com.kartersanamo.rift.api.logging.CoreLogger;
import com.kartersanamo.rift.api.logging.LogLevel;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.command.*;
import com.kartersanamo.rift.listeners.TeleportMoveListener;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Rift extends JavaPlugin {

    private static Rift instance;
    private static CoreLogger logger;

    private CommandManager commandManager;
    private ChatInputManager chatInputManager;
    private GUIManager guiManager;
    private WarpManager warpManager;

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

        warpManager.loadWarps();
    }

    private void initLogger() {
        logger = new CoreLogger(this);
        logger.setLevel(LogLevel.INFO);
    }

    private void registerManagers() {
        commandManager = new CommandManager(this);
        chatInputManager = new ChatInputManager(this);
        warpManager = new WarpManager(this);
        guiManager = new GUIManager(this);
    }

    private void registerCommands() {
        commandManager.registerCommand(new SetwarpCommand(warpManager));
        commandManager.registerCommand(new WarpCommand(warpManager));
        commandManager.registerCommand(new DeletewarpCommand(warpManager));
        commandManager.registerCommand(new WarpinfoCommand(warpManager));
        commandManager.registerCommand(new WarpsCommand(warpManager));
        commandManager.registerCommand(new RiftCommand());
        commandManager.registerSubCommand("rift", new SubCommand(
                "reload",
                "Reloads the plugin configs",
                "/rift reload",
                "rift.reload",
                ReloadCommand::execute
        ));
        commandManager.registerSubCommand("rift", new SubCommand(
                "admin",
                "Open the Rift admin panel",
                "/rift admin",
                "rift.admin",
                context -> {
                    if (!(context.getSender() instanceof org.bukkit.entity.Player player)) {
                        context.getSender().sendMessage(ChatFormat.error(MessagesUtil.commandPlayerOnly));
                        return true;
                    }
                    AuditLogger.action(player, "admin.gui.open", "source=rift-command");
                    new com.kartersanamo.rift.gui.AdminGUI(warpManager).open(player);
                    return true;
                }
        ));
        commandManager.registerSubCommand("rift", new SubCommand(
                "category",
                "Manage warp categories",
                "/rift category <create|delete|list|assign>",
                "rift.category.manage",
                context -> {
                    String[] args = context.getArgs();
                    if (args.length == 0) {
                        context.getSender().sendMessage(ChatFormat.info("Usage: /rift category <create|delete|list|assign>"));
                        return true;
                    }

                    String action = args[0].toLowerCase();
                    switch (action) {
                        case "create" -> {
                            if (args.length < 2) {
                                context.getSender().sendMessage(ChatFormat.info("Usage: /rift category create <name>"));
                                return true;
                            }
                            String category = warpManager.ensureCategoryExists(args[1]);
                            AuditLogger.action(context.getSender(), "category.create", "name=" + category);
                            context.getSender().sendMessage(ChatFormat.success("Category ready: " + category));
                            return true;
                        }
                        case "delete" -> {
                            if (args.length < 2) {
                                context.getSender().sendMessage(ChatFormat.info("Usage: /rift category delete <name>"));
                                return true;
                            }
                            boolean removed = warpManager.deleteCategory(args[1]);
                            if (!removed) {
                                context.getSender().sendMessage(ChatFormat.error("Could not delete that category (it may not exist or is protected)."));
                                return true;
                            }
                            AuditLogger.action(context.getSender(), "category.delete", "name=" + args[1]);
                            context.getSender().sendMessage(ChatFormat.success("Category deleted: " + args[1]));
                            return true;
                        }
                        case "assign" -> {
                            if (args.length < 3) {
                                context.getSender().sendMessage(ChatFormat.info("Usage: /rift category assign <warp> <category>"));
                                return true;
                            }
                            String warpName = args[1];
                            String category = args[2];
                            var warp = warpManager.getWarp(warpName);
                            if (warp == null) {
                                context.getSender().sendMessage(ChatFormat.error("Warp not found: " + warpName));
                                return true;
                            }
                            warpManager.assignWarpCategory(warp, category);
                            AuditLogger.action(context.getSender(), "category.assign", "warp=" + warpName + " category=" + category);
                            context.getSender().sendMessage(ChatFormat.success("Warp " + warpName + " assigned to category " + warp.getCategory()));
                            return true;
                        }
                        case "list" -> {
                            context.getSender().sendMessage(ChatFormat.info("Categories: " + String.join(", ", warpManager.getCategories())));
                            return true;
                        }
                        default -> {
                            context.getSender().sendMessage(ChatFormat.info("Usage: /rift category <create|delete|list|assign>"));
                            return true;
                        }
                    }
                }
        ));
    }

     public void reloadAll() {
         reloadConfig();
         ConfigUtil.load(getConfig());
         createConfigs();         // reloads messages.yml + MessagesUtil cache
         warpManager.loadWarps(); // reloads warps.yml into memory
     }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new TeleportMoveListener(), this);
    }

    private void createConfigs() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            File parent = messagesFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                logger.warning("Could not create plugin data folder: " + parent.getAbsolutePath());
            }
            saveResource("messages.yml", false);
        }

        MessagesUtil.load(YamlConfiguration.loadConfiguration(messagesFile));
        logger.info("Messages cache loaded.");
    }

    @Override
    public void onDisable() {
        if (warpManager != null) {
            warpManager.unloadWarps();
        }
        logger.close();
    }

    // Getters

    public static CoreLogger getLog() { return logger; }

    public static Rift getInstance() {
        return instance;
    }

    public GUIManager getGuiManager() { return guiManager; }

    public ChatInputManager getChatInputManager() { return chatInputManager; }

    public WarpManager getWarpManager() { return warpManager; }
}
