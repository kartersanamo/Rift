package com.kartersanamo.rift.command;

import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.command.BaseCommand;
import com.kartersanamo.rift.api.command.CommandContext;
import com.kartersanamo.rift.api.command.annotations.CommandPermission;
import com.kartersanamo.rift.api.command.annotations.PlayerOnly;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.api.logging.AuditLogger;
import com.kartersanamo.rift.api.util.LocationUtil;
import com.kartersanamo.rift.api.util.PlaceholderUtil;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@PlayerOnly
@CommandPermission("rift.warp.set")
public class SetwarpCommand extends BaseCommand {
    private final WarpManager warpManager;

    public SetwarpCommand(WarpManager warpManager) {
        super(
                "setwarp",
                "Sets a new warp at your location",
                "/setwarp <name> [category]"
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
        String inputCategory = context.getArgs().length > 1 ? context.getArgs()[1] : null;
        String category = warpManager.ensureCategoryExists(inputCategory);

        // Validate warp name
        WarpManager.WarpNameValidationResult validationResult = warpManager.validateWarpName(warpName);
        if (validationResult != WarpManager.WarpNameValidationResult.VALID) {
            player.sendMessage(ChatFormat.error(
                    warpManager.getWarpNameValidationMessage(validationResult)
            ));
            return true;
        }

        // Warp already exists, so just update the location
        if (warpManager.exists(warpName)) {
            Warp existingWarp = warpManager.getWarp(warpName);
            Location newLocation = player.getLocation();

            existingWarp.setLocation(newLocation);
            if (inputCategory != null && !inputCategory.isBlank()) {
                existingWarp.setCategory(category);
            }
            warpManager.update(existingWarp);
            AuditLogger.action(player, "warp.update.location", "name=" + existingWarp.getName() + " category=" + existingWarp.getCategory());

            player.sendMessage(ChatFormat.info(
                    PlaceholderUtil.replace(
                        MessagesUtil.warpUpdatedLocation,
                            "%location%", LocationUtil.format(newLocation),
                                         "%name%", warpName
                    )
            ));
            return true;
        }

        // Create warp
        String id = "warp_" + player.getUniqueId() + "_" + UUID.randomUUID();
        Warp warp = new Warp(
                warpName,
                new ArrayList<>(),
                category,
                player.getLocation(),
                id,
                player,
                Material.ENDER_PEARL,
                System.currentTimeMillis(),
                0
        );
        warpManager.addWarp(warp);
        AuditLogger.action(player, "warp.create", "name=" + warpName + " category=" + category);

        player.sendMessage(ChatFormat.info(
                PlaceholderUtil.replace(
                        MessagesUtil.warpCreated,
                        "%name%", warpName,
                        "%location%", LocationUtil.format(player.getLocation())
                )
        ));

        return true;
    }

    @Override
    protected List<String> onTabComplete(CommandContext context) {
        String[] args = context.getArgs();
        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);
            List<String> completions = new ArrayList<>();
            for (Warp warp : warpManager.getWarps().values()) {
                String name = warp.getName();
                if (name.toLowerCase(Locale.ROOT).startsWith(partial)) {
                    completions.add(name);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            String partial = args[1].toLowerCase(Locale.ROOT);
            List<String> completions = new ArrayList<>();
            for (String category : warpManager.getCategories()) {
                if (category.toLowerCase(Locale.ROOT).startsWith(partial)) {
                    completions.add(category);
                }
            }
            return completions;
        }

        return new ArrayList<>();
    }
}
