package com.kartersanamo.rift.api.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandContext {

    private final CommandSender sender;
    private final String label;
    private final String[] args;
    private final List<String> arguments;

    public CommandContext(CommandSender sender, String label, String[] args) {
        this.sender = sender;
        this.label = label;
        this.args = args != null ? args : new String[0];
        this.arguments = new ArrayList<>(Arrays.asList(this.args));
    }

    public CommandSender getSender() {
        return sender;
    }

    public Player getPlayer() {
        return sender instanceof Player ? (Player) sender : null;
    }

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public String getLabel() {
        return label;
    }

    public String[] getArgs() {
        return args.clone();
    }

    public List<String> getArguments() {
        return new ArrayList<>(arguments);
    }

    public int getArgCount() {
        return args.length;
    }

    public String getArg(int index) {
        if (index >= 0 && index < args.length) {
            return args[index];
        }
        return null;
    }

    public String getArg(int index, String defaultValue) {
        String arg = getArg(index);
        return arg != null ? arg : defaultValue;
    }

    public String joinArgs(int startIndex) {
        return joinArgs(startIndex, args.length);
    }

    public String joinArgs(int startIndex, int endIndex) {
        if (startIndex < 0 || startIndex >= args.length) {
            return "";
        }
        endIndex = Math.min(endIndex, args.length);
        StringBuilder result = new StringBuilder();
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                result.append(" ");
            }
            result.append(args[i]);
        }
        return result.toString();
    }

    public boolean hasArgs() {
        return args.length > 0;
    }

    public boolean hasArgs(int count) {
        return args.length >= count;
    }
}
