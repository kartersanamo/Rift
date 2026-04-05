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

import java.util.ArrayList;
import java.util.List;

@PlayerOnly
public class DeletewarpCommand extends BaseCommand {
    private final WarpManager warpManager;

    public DeletewarpCommand(WarpManager warpManager) {
        super(
                "deletewarp",
                "Deletes a warp",
                "/deletewarp <name>",
                "delwarp"
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
            return true;
        }

        player.sendMessage(ChatFormat.info(
                PlaceholderUtil.replace(
                        MessagesUtil.warpDeleted,
                        "%name%", warpName
                )
        ));

        return true;
    }

     @Override
     protected List<String> onTabComplete(CommandContext context) {
         if (context.getArgs().length != 1) {
             return new ArrayList<>();
         }

         List<String> completions = new ArrayList<>();
         String partial = context.getArgs()[0].toLowerCase();

         for (Warp warp : warpManager.getWarps().values()) {
             if (warp.getName().toLowerCase().startsWith(partial)) {
                 completions.add(warp.getName());
             }
         }

         return completions;
     }
}
