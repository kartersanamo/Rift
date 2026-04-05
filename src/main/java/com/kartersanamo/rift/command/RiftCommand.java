package com.kartersanamo.rift.command;

import com.kartersanamo.rift.Rift;
import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.command.BaseCommand;
import com.kartersanamo.rift.api.command.CommandContext;
import com.kartersanamo.rift.api.command.annotations.CommandPermission;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.api.util.PlaceholderUtil;
import com.kartersanamo.rift.warp.Warp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@CommandPermission("rift.command")
public class RiftCommand extends BaseCommand {

    public RiftCommand() {
        super("rift",
                "Main plugin entry point",
                "/rift <reload|admin|category>"
        );
    }

    @Override
    protected boolean onExecute(CommandContext context) {
        context.getSender().sendMessage(ChatFormat.info(
                PlaceholderUtil.replace(
                        MessagesUtil.commandUsage,
                        "%usage%", getUsage())
        ));
        return true;
    }

    @Override
    protected List<String> onTabComplete(CommandContext context) {
        String[] args = context.getArgs();
        if (args.length < 2 || !args[0].equalsIgnoreCase("category")) {
            return new ArrayList<>();
        }

        var warpManager = Rift.getInstance().getWarpManager();
        String action = args[1].toLowerCase(Locale.ROOT);

        if (args.length == 2) {
            return complete(args[1], List.of("create", "delete", "list", "assign"));
        }

        if (args.length == 3) {
            if (action.equals("delete")) {
                return complete(args[2], warpManager.getCategories());
            }
            if (action.equals("assign")) {
                List<String> names = new ArrayList<>();
                for (Warp warp : warpManager.getWarps().values()) {
                    names.add(warp.getName());
                }
                return complete(args[2], names);
            }
            return new ArrayList<>();
        }

        if (args.length == 4 && action.equals("assign")) {
            return complete(args[3], warpManager.getCategories());
        }

        return new ArrayList<>();
    }

    private List<String> complete(String partial, List<String> source) {
        String normalized = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String entry : source) {
            if (entry != null && entry.toLowerCase(Locale.ROOT).startsWith(normalized)) {
                result.add(entry);
            }
        }
        return result;
    }
}
