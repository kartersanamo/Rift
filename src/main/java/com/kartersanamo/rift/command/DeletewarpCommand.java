package com.kartersanamo.rift.command;

import com.kartersanamo.rift.api.command.BaseCommand;
import com.kartersanamo.rift.api.command.CommandContext;
import com.kartersanamo.rift.api.command.annotations.PlayerOnly;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.entity.Player;

@PlayerOnly
public class DeletewarpCommand extends BaseCommand {
    private final WarpManager warpManager;

    public DeletewarpCommand(WarpManager warpManager) {
        super(
                "deletewarp",
                "Deletes a warp",
                "/deletewarp <name>",
                "dw"
        );
        this.warpManager = warpManager;
    }

    @Override
    protected boolean onExecute(CommandContext context) {
        Player player = context.getPlayer();

        // Ensured they entered a warp name
        if (!context.hasArgs()) {
            player.sendMessage("Provide a warp name!");
            return true;
        }

        String warpName = context.getArgs()[0];

        // TODO: Validate warp name

        Warp warp = warpManager.getWarp(warpName);

        if (warp == null) {
            player.sendMessage("Warp not found!");
            return true;
        }

        warpManager.deleteWarp(warpName);

        player.sendMessage("Warp " + warpName + "deleted!");

        return true;
    }
}
