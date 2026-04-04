package com.kartersanamo.rift.api.config;

import org.bukkit.configuration.file.FileConfiguration;

public class MessagesUtil {
    public static String commandUsage;
    public static String commandPlayerOnly;
    public static String commandNoPermission;
    public static String commandError;
    public static String subCommandNoPermission;
    public static String messagesReloaded;
    public static String blankLine;
    public static String chatPrefixDefault;
    public static String chatPrefixSuccess;
    public static String chatPrefixWarning;

    public static String teleportCountdown;
    public static String teleportSuccessLocation;
    public static String teleportSuccessPlayer;
    public static String teleportCancelledMoved;

    public static String warpNameSize;
    public static String warpNameNoColor;
    public static String warpUpdatedLocation;
    public static String warpCreated;

    public static void load(FileConfiguration cfg) {
        commandUsage = cfg.getString("command-usage", "Usage: %usage%");
        commandPlayerOnly = cfg.getString("command-player-only", "This command can only be executed by players.");
        commandNoPermission = cfg.getString("command-no-permission", "You do not have permission to use this command.");
        commandError = cfg.getString("command-error", "An error occurred while executing this command.");
        subCommandNoPermission = cfg.getString("subcommand-no-permission", "You do not have permission to use this sub-command.");
        messagesReloaded = cfg.getString("messages-reloaded", "Config and messages reloaded.");
        blankLine = cfg.getString("blank-line", " ");
        chatPrefixDefault = cfg.getString("chat-prefix-default", "&8&l[&b&lH0M3&8&l]");
        chatPrefixSuccess = cfg.getString("chat-prefix-success", "&8&l[&a&lH0M3&8&l]");
        chatPrefixWarning = cfg.getString("chat-prefix-warning", "&8&l[&e&lH0M3&8&l]");

        teleportCountdown = cfg.getString("teleport-delay.countdown", "&7Teleporting in &b%seconds% &7seconds");
        teleportSuccessLocation = cfg.getString("teleport-complete.success", "&7Teleported to &b%location%");
        teleportSuccessPlayer = cfg.getString("teleport-complete.success-player", "&7Teleported to &b%player%");
        teleportCancelledMoved = cfg.getString("teleport-cancelled.moved", "&7You moved, teleport cancelled.");

        warpNameSize = cfg.getString("warp.name.size", "&7Warp name must be between &b%min% &7and &b%max% &7characters.");
        warpNameNoColor = cfg.getString("warp.name.no-color", "&7Warp name cannot contain color codes.");
        warpUpdatedLocation = cfg.getString("warp.updated-location", "&7Warp &b%name% &7location updated to &b%location%&7.");
        warpCreated = cfg.getString("warp.created", "&7Warp &b%name% &7created at &b%location%&7.");
    }
}
