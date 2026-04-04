package com.kartersanamo.rift.command;

import com.kartersanamo.rift.api.command.BaseCommand;
import com.kartersanamo.rift.api.command.CommandContext;
import com.kartersanamo.rift.api.command.annotations.PlayerOnly;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;

import java.util.List;
import java.util.Map;

@PlayerOnly
public class WarpsCommand extends BaseCommand {
    private final WarpManager warpManager;

    public WarpsCommand(WarpManager warpManager) {
        super(
                "warps",
                "Show all warps",
                "/warps",
                "warpslist"
        );
        this.warpManager = warpManager;
    }

    @Override
    protected boolean onExecute(CommandContext context) {
        context.getSender().sendMessage("Warps:");

        Map<String, Warp> warps = warpManager.getWarps();
        if (warps.isEmpty()) {
            context.getSender().sendMessage("No warps found!");
        }

        for (Warp warp : warps.values()) {
            context.getSender().sendMessage("- " + warp.getName());
        }

        return true;
    }
}