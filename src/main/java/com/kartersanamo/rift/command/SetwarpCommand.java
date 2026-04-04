package com.kartersanamo.rift.command;

import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.command.BaseCommand;
import com.kartersanamo.rift.api.command.CommandContext;
import com.kartersanamo.rift.api.command.annotations.PlayerOnly;
import com.kartersanamo.rift.api.config.ConfigUtil;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.api.util.LocationUtil;
import com.kartersanamo.rift.api.util.PlaceholderUtil;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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

        // Validate warp name
        if (warpManager.isHomeNameCorrectSize(warpName)) {
            player.sendMessage(ChatFormat.error(
                    PlaceholderUtil.replace(
                            MessagesUtil.warpNameSize,
                            "%min%", String.valueOf(ConfigUtil.warpNameMinLength),
                                         "%max%", String.valueOf(ConfigUtil.warpNameMaxLength)
            )));
            return true;
        }
        if (warpManager.warpNameHasColor(warpName)) {
            player.sendMessage(ChatFormat.error(
                    MessagesUtil.warpNameNoColor
            ));
            return true;
        }

        // Warp already exists, so just update the location
        if (warpManager.exists(warpName)) {
            Warp existingWarp = warpManager.getWarp(warpName);
            Location newLocation = player.getLocation();

            existingWarp.setLocation(newLocation);
            warpManager.update(existingWarp);

            player.sendMessage(ChatFormat.info(
                    PlaceholderUtil.replace(
                        MessagesUtil.warpUpdatedLocation,
                            "%location%", LocationUtil.format(newLocation)
                    )
            ));
            return true;
        }

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

        player.sendMessage(ChatFormat.info(
                PlaceholderUtil.replace(
                        MessagesUtil.warpCreated,
                        "%name%", warpName,
                        "%location%", LocationUtil.format(player.getLocation())
                )
        ));

        return true;
    }
}
