package com.kartersanamo.rift.command;

import org.bukkit.command.CommandExecutor;

public interface BaseCommand extends CommandExecutor {
        String getName();
        String getDescription();
        String getUsage();
        String[] getAliases();
}
