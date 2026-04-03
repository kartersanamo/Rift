package com.kartersanamo.rift.command;

import java.util.ArrayList;
import java.util.List;

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
        for (String alias: aliases) {
            this.aliases.add(alias.toLowerCase());
        }

        // TODO: Check for annotations, and update permission and playerOnly accordingly
        this.permission = "rift.command." + name.toLowerCase();
        this.playerOnly = true;
    }

    public boolean execute(CommandContext context) {
        // Check if player-only
        // Check permission
        // Execute the command
        return true;
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

    public String getUsage() {
        return usage;
    }

    public List<String> getAliases() {
        return new ArrayList<>(aliases);
    }
}