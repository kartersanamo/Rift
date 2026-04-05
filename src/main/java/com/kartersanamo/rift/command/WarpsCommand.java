package com.kartersanamo.rift.command;

import com.kartersanamo.rift.api.command.BaseCommand;
import com.kartersanamo.rift.api.command.CommandContext;
import com.kartersanamo.rift.api.command.annotations.CommandPermission;
import com.kartersanamo.rift.api.command.annotations.PlayerOnly;
import com.kartersanamo.rift.gui.WarpsGUI;
import com.kartersanamo.rift.warp.Category;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


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
        WarpsGUI warpsGUI = new WarpsGUI(warpManager, categoryFilter, player);
        warpsGUI.open(player);
        return true;
    }

    @Override
    protected List<String> onTabComplete(CommandContext context) {
        if (context.getArgs().length != 1) {
            return new ArrayList<>();
        }

        String partial = context.getArgs()[0].toLowerCase(Locale.ROOT);
        List<String> completions = new ArrayList<>();

        for (Category category : warpManager.getCategoriesForWarpsMenu()) {
            String name = category.getName();
            if (name.toLowerCase(Locale.ROOT).startsWith(partial)) {
                completions.add(name);
            }
        }

        if ("all".startsWith(partial)) {
            completions.add("all");
        }

        return completions;
    }
}