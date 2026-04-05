package com.kartersanamo.rift.api.command;

import com.kartersanamo.rift.Rift;
import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.command.annotations.CommandPermission;
import com.kartersanamo.rift.api.command.annotations.PlayerOnly;
import com.kartersanamo.rift.api.config.MessagesUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class BaseCommand {

    private final String name;
    private final String description;
    private final String usage;
    private final List<String> aliases;
    private String permission;
    private boolean playerOnly;

    public BaseCommand(String name, String description, String usage, String... aliases) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.aliases = new ArrayList<>();
        for (String alias : aliases) {
            this.aliases.add(alias.toLowerCase());
        }

        // Check for annotations
        CommandPermission permAnnotation = getClass().getAnnotation(CommandPermission.class);
        if (permAnnotation != null) {
            this.permission = permAnnotation.value();
        }

        PlayerOnly playerOnlyAnnotation = getClass().getAnnotation(PlayerOnly.class);
        if (playerOnlyAnnotation != null) {
            this.playerOnly = true;
        }
    }

    public boolean execute(CommandContext context) {
        CommandSender sender = context.getSender();

        // Check if player-only
        if (playerOnly && !(sender instanceof Player)) {
            sender.sendMessage(ChatFormat.error(MessagesUtil.commandPlayerOnly));
            return true;
        }

        // Check permission
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(ChatFormat.error(MessagesUtil.commandNoPermission));
            return true;
        }

        // Execute the command
        try {
            return onExecute(context);
        } catch (Exception e) {
            sender.sendMessage(ChatFormat.error(MessagesUtil.commandError));
            Rift.getLog().severe("An error occurred while executing the command " + getName());
            e.printStackTrace();
            return true;
        }
    }

    public List<String> tabComplete(CommandContext context) {
        return onTabComplete(context);
    }

    protected abstract boolean onExecute(CommandContext context);

    protected List<String> onTabComplete(CommandContext context) {
        return new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public List<String> getAliases() {
        return new ArrayList<>(aliases);
    }

    public boolean matches(String command) {
        if (command == null) {
            return false;
        }
        String lowerCommand = command.toLowerCase(Locale.ROOT);
        return name.equalsIgnoreCase(lowerCommand) || aliases.contains(lowerCommand);
    }
}
