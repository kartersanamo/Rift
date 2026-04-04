package com.kartersanamo.rift.command;

import com.kartersanamo.rift.api.command.BaseCommand;
import com.kartersanamo.rift.api.command.CommandContext;
import com.kartersanamo.rift.api.command.annotations.PlayerOnly;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        if (!context.hasArgs()) {
            player.sendMessage("Provide a name for the warp!");
            return true;
        }

        String warpName = context.getArgs()[0];

        // TODO: Validate warp name

        // Create warp
        String id = "warp_" + player.getUniqueId() + "_" + UUID.randomUUID();
        Warp warp = new Warp(
                warpName,
                new ArrayList<>(),
                "default",
                player.getLocation(),
                id,
                player,
                Material.ENDER_PEARL,
                System.currentTimeMillis(),
                0
        );
        warpManager.addWarp(warp);

        player.sendMessage("Warp '" + warpName + "' set successfully!");

        return true;
    }
}
