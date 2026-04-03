package com.kartersanamo.rift.api.command;

import java.util.ArrayList;
import java.util.List;

public class SubCommand {

    private final String name;
    private final String description;
    private final String usage;
    private final String permission;
    private final SubCommandExecutor executor;
    private final List<String> aliases;

    public SubCommand(String name, String description, String usage, String permission,
                      SubCommandExecutor executor, String... aliases) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.permission = permission;
        this.executor = executor;
        this.aliases = new ArrayList<>();
        for (String alias : aliases) {
            this.aliases.add(alias.toLowerCase());
        }
    }

    public boolean execute(CommandContext context) {
        if (executor != null) {
            return executor.execute(context);
        }
        return false;
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

    public String getPermission() {
        return permission;
    }

    public List<String> getAliases() {
        return new ArrayList<>(aliases);
    }

    public boolean matches(String command) {
        if (command == null) {
            return false;
        }
        String lowerCommand = command.toLowerCase();
        return name.equalsIgnoreCase(lowerCommand) || aliases.contains(lowerCommand);
    }

    @FunctionalInterface
    public interface SubCommandExecutor {
        boolean execute(CommandContext context);
    }
}