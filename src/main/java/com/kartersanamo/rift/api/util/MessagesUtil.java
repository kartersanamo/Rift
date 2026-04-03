package com.kartersanamo.rift.api.util;

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
    }
}
