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

        // Delete the warp
        boolean wasDeleted = warpManager.deleteWarp(warpName);

        // This technically shouldn't happen, since we are validating first, but just in case
        if (!wasDeleted) {
            player.sendMessage(ChatFormat.error(
                    PlaceholderUtil.replace(
                            MessagesUtil.warpDeleteFailed,
                            "%name%", warpName
                    )
            ));
        }

        player.sendMessage(ChatFormat.success(
                PlaceholderUtil.replace(
                        MessagesUtil.warpDeleted,
                        "%name%", warpName
                )
        ));

        return true;
    }
}
