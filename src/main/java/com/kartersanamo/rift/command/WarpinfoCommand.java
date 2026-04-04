package com.kartersanamo.rift.command;

import com.kartersanamo.rift.api.command.BaseCommand;
import com.kartersanamo.rift.api.command.CommandContext;
import com.kartersanamo.rift.api.command.annotations.PlayerOnly;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.entity.Player;

@PlayerOnly
public class WarpinfoCommand extends BaseCommand {
    private final WarpManager warpManager;

    public WarpinfoCommand(WarpManager warpManager) {
        super(
                "warpinfo",
                "Shows info about a warp",
                "/warpinfo <name>",
                "wi"
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

        Warp warp = warpManager.getWarp(warpName);

        if (warp == null) {
            player.sendMessage("Warp not found!");
            return true;
        }

        warpManager.sendInfo(warp, player);

        return true;
    }
}
