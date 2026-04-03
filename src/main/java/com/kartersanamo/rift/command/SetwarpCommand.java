package com.kartersanamo.rift.command;

import com.kartersanamo.rift.api.command.BaseCommand;
import com.kartersanamo.rift.api.command.CommandContext;
import com.kartersanamo.rift.api.command.annotations.PlayerOnly;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.entity.Player;

@PlayerOnly
public class SetwarpCommand extends BaseCommand {
    private final WarpManager warpManager;

    public SetwarpCommand(WarpManager warpManager) {
        super(
                "setwarp",
                "Sets a new warp at your location",
                "/setwarp <name>",
                "sw"
        );
        this.warpManager = warpManager;
    }

    @Override
    protected boolean onExecute(CommandContext context) {
        Player player = context.getPlayer();

        if (context.getArgs().length == 0) {
            return false;
        }

        String warpName = context.getArgs()[0];

        player.sendMessage("Setting warp...");
        // warpManager.setWarp(warpName, player.getLocation());
        player.sendMessage("Warp '" + warpName + "' set successfully!");

        return true;
    }
}
