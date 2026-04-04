package com.kartersanamo.rift.command;

import com.kartersanamo.rift.api.command.BaseCommand;
import com.kartersanamo.rift.api.command.CommandContext;
import com.kartersanamo.rift.api.command.annotations.PlayerOnly;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.entity.Player;

@PlayerOnly
public class WarpCommand extends BaseCommand {
    private final WarpManager warpManager;

    public WarpCommand(WarpManager warpManager) {
        super(
                "warp",
                "Teleports the player to a warp",
                "/warp <name>",
                "w"
        );
        this.warpManager = warpManager;
    }

    @Override
    protected boolean onExecute(CommandContext context) {
        Player player = context.getPlayer();

        if (!context.hasArgs()) {
            player.sendMessage("Provide a warp name!");
            return true;
        }

        String warpName = context.getArgs()[0];

        // TODO: Validate warp name

        // Teleport to warp

        Warp warp = warpManager.getWarp(warpName);

        if (warp == null) {
            player.sendMessage("Warp not found!");
            return true;
        }

        warp.teleport(player);

        return true;
    }
}
