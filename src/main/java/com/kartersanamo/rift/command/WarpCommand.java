package com.kartersanamo.rift.command;

import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.command.BaseCommand;
import com.kartersanamo.rift.api.command.CommandContext;
import com.kartersanamo.rift.api.command.annotations.PlayerOnly;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.api.util.PlaceholderUtil;
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

        // Ensure they entered a warp name
        if (!context.hasArgs()) {
            player.sendMessage(ChatFormat.info(
                    PlaceholderUtil.replace(
                            MessagesUtil.commandUsage,
                            "%usage%", getUsage()
                    )
            ));
            return true;
        }

        String warpName = context.getArgs()[0];

        // Validate warp exists
        Warp warp = warpManager.getWarp(warpName);
        if (warp == null) {
            player.sendMessage(ChatFormat.error(
                    PlaceholderUtil.replace(
                            MessagesUtil.warpNotFound,
                            "%name%", warpName
                    )
            ));
            return true;
        }

        // Teleport player to warp
        warp.queueTeleport(player);

        return true;
    }
}
