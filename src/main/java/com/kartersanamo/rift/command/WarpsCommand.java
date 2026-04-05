package com.kartersanamo.rift.command;

import com.kartersanamo.rift.api.command.BaseCommand;
import com.kartersanamo.rift.api.command.CommandContext;
import com.kartersanamo.rift.api.command.annotations.CommandPermission;
import com.kartersanamo.rift.api.command.annotations.PlayerOnly;
import com.kartersanamo.rift.gui.WarpsGUI;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.entity.Player;


@PlayerOnly
@CommandPermission("rift.warp.list")
public class WarpsCommand extends BaseCommand {
    private final WarpManager warpManager;

    public WarpsCommand(WarpManager warpManager) {
        super(
                "warps",
                "Show all warps",
                "/warps [category]"
        );
        this.warpManager = warpManager;
    }

    @Override
    protected boolean onExecute(CommandContext context) {
        Player player = context.getPlayer();
        String categoryFilter = context.hasArgs() ? context.getArgs()[0] : null;
        WarpsGUI warpsGUI = new WarpsGUI(warpManager, categoryFilter);
        warpsGUI.open(player);

        return true;
    }
}