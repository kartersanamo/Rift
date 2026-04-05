package com.kartersanamo.rift.command;

import com.kartersanamo.rift.api.command.BaseCommand;
import com.kartersanamo.rift.api.command.CommandContext;
import com.kartersanamo.rift.api.command.annotations.PlayerOnly;
import com.kartersanamo.rift.gui.WarpsGUI;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

@PlayerOnly
public class WarpsCommand extends BaseCommand {
    private final WarpManager warpManager;

    public WarpsCommand(WarpManager warpManager) {
        super(
                "warps",
                "Show all warps",
                "/warps"
        );
        this.warpManager = warpManager;
    }

    @Override
    protected boolean onExecute(CommandContext context) {
        Player player = context.getPlayer();
        WarpsGUI warpsGUI = new WarpsGUI(warpManager, player);
        warpsGUI.open(player);

        return true;
    }
}