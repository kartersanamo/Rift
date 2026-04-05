package com.kartersanamo.rift.api.config;

import org.bukkit.configuration.file.FileConfiguration;

public class MessagesUtil {
    public static String commandUsage;
    public static String commandPlayerOnly;
    public static String commandNoPermission;
    public static String commandError;
    public static String subCommandNoPermission;
    public static String configsReloaded;
    public static String blankLine;
    public static String chatPrefixDefault;
    public static String chatPrefixSuccess;
    public static String chatPrefixWarning;

    public static String teleportCountdown;
    public static String teleportSuccessLocation;
    public static String teleportSuccessPlayer;
    public static String teleportCancelledMoved;

    public static String warpNameSize;
    public static String warpNameNoColor;
    public static String warpUpdatedLocation;
    public static String warpCreated;
    public static String warpNotFound;
    public static String warpDeleted;
    public static String warpDeleteFailed;

    public static String warpInfoDivider;
    public static String warpInfoTitle;
    public static String warpInfoSpacer;
    public static String warpInfoName;
    public static String warpInfoDescriptionNone;
    public static String warpInfoDescriptionLabel;
    public static String warpInfoDescriptionEntry;
    public static String warpInfoCategory;
    public static String warpInfoLocation;
    public static String warpInfoCreator;
    public static String warpInfoCreatorUnknown;
    public static String warpInfoMaterial;
    public static String warpInfoCreated;
    public static String warpInfoUses;

    public static String warpsGuiTitle;
    public static String warpsGuiEmptyName;
    public static String warpsGuiEmptyLore;
    public static String warpsGuiInstructionTeleport;
    public static String warpsGuiInstructionManage;
    public static String warpsGuiHomeName;

    public static String manageWarpErrorName;
    public static String manageWarpErrorLine1;
    public static String manageWarpErrorLine2;
    public static String manageWarpErrorLine3;
    public static String manageWarpErrorLine4;
    public static String manageWarpChangeNameTitle;
    public static String manageWarpChangeNameLine1;
    public static String manageWarpChangeNameLine2;
    public static String manageWarpCurrent;
    public static String manageWarpChangeMaterialTitle;
    public static String manageWarpChangeMaterialLine1;
    public static String manageWarpChangeMaterialLine2;
    public static String manageWarpChangeDescriptionTitle;
    public static String manageWarpChangeDescriptionLine1;
    public static String manageWarpChangeDescriptionLine2;
    public static String manageWarpCurrentLabel;
    public static String manageWarpDescriptionNone;
    public static String manageWarpDescriptionEntry;
    public static String manageWarpChangeLocationTitle;
    public static String manageWarpChangeLocationLine1;
    public static String manageWarpChangeLocationLine2;
    public static String managWarpDeleteTitle;
    public static String manageWarpDeleteLine1;
    public static String manageWarpDeleteLine2;
    public static String manageWarpDeleteLine3;
    public static String manageWarpDeleteLine4;
    public static String manageWarpBackTitle;
    public static String manageWarpBackLine1;
    public static String manageWarpBackLine2;
    public static String manageWwarpInfoTitle;

    public static void load(FileConfiguration cfg) {
        commandUsage = cfg.getString("command-usage", "Usage: %usage%");
        commandPlayerOnly = cfg.getString("command-player-only", "This command can only be executed by players.");
        commandNoPermission = cfg.getString("command-no-permission", "You do not have permission to use this command.");
        commandError = cfg.getString("command-error", "An error occurred while executing this command.");
        subCommandNoPermission = cfg.getString("subcommand-no-permission", "You do not have permission to use this sub-command.");
        configsReloaded = cfg.getString("config-reloaded", "&7Config and messages reloaded.");
        blankLine = cfg.getString("blank-line", " ");
        chatPrefixDefault = cfg.getString("chat-prefix-default", "&8&l[&b&lH0M3&8&l]");
        chatPrefixSuccess = cfg.getString("chat-prefix-success", "&8&l[&a&lH0M3&8&l]");
        chatPrefixWarning = cfg.getString("chat-prefix-warning", "&8&l[&e&lH0M3&8&l]");

        teleportCountdown = cfg.getString("teleport-delay.countdown", "&7Teleporting in &b%seconds% &7seconds...");
        teleportSuccessLocation = cfg.getString("teleport-complete.success", "&7Teleported to &b%location%");
        teleportSuccessPlayer = cfg.getString("teleport-complete.success-player", "&7Teleported to &b%player%");
        teleportCancelledMoved = cfg.getString("teleport-cancelled.moved", "&7You moved, teleport cancelled.");

        warpNameSize = cfg.getString("warp.name.size", "&7Warp name must be between &b%min% &7and &b%max% &7characters.");
        warpNameNoColor = cfg.getString("warp.name.no-color", "&7Warp name cannot contain color codes.");
        warpUpdatedLocation = cfg.getString("warp.updated-location", "&7Warp &b%name% &7location updated to &b%location%&7.");
        warpCreated = cfg.getString("warp.created", "&7Warp &b%name% &7created at &b%location%&7.");
        warpNotFound = cfg.getString("warp.not-found", "&7Warp &b%name% &7not found.");
        warpDeleted = cfg.getString("warp.deleted", "&7Warp &b%name% &7deleted.");
        warpDeleteFailed = cfg.getString("warp.delete-failed", "&7Failed to delete warp &b%name%&7.");

        warpInfoDivider = cfg.getString("warp.info.divider", "&m----------------------------------------");
        warpInfoTitle = cfg.getString("warp.info.title", "&7Warp Information");
        warpInfoSpacer = cfg.getString("warp.info.spacer", "&7");
        warpInfoName = cfg.getString("warp.info.name", "&7Name: &b%name%");
        warpInfoDescriptionNone = cfg.getString("warp.info.description-none", "&7Description: &8(none)");
        warpInfoDescriptionLabel = cfg.getString("warp.info.description-label", "&7Description:");
        warpInfoDescriptionEntry = cfg.getString("warp.info.description-entry", "&8- &7%line%");
        warpInfoCategory = cfg.getString("warp.info.category", "&7Category: &b%category%");
        warpInfoLocation = cfg.getString("warp.info.location", "&7Location: &b%location%");
        warpInfoCreator = cfg.getString("warp.info.creator", "&7Creator: &b%creator_name%");
        warpInfoCreatorUnknown = cfg.getString("warp.info.creator-unknown", "Unknown");
        warpInfoMaterial = cfg.getString("warp.info.material", "&7Material: &b%material%");
        warpInfoCreated = cfg.getString("warp.info.created", "&7Created: &b%time%");
        warpInfoUses = cfg.getString("warp.info.uses", "&7Uses: &b%uses%");

        warpsGuiTitle = cfg.getString("warps.gui.title", "Warps (&b%count%&r)");
        warpsGuiEmptyName = cfg.getString("warps.gui.empty.name", "&cNo warps available");
        warpsGuiEmptyLore = cfg.getString("warps.gui.empty.lore", "&7There are no warps to display.");
        warpsGuiInstructionTeleport = cfg.getString("warps.gui.instruction.teleport", "&7Left-click to teleport.");
        warpsGuiInstructionManage = cfg.getString("warps.gui.instruction.manage", "&7Right-click to manage.");
        warpsGuiHomeName = cfg.getString("warps.gui.home.name", "&b%name%");

        manageWarpErrorName = cfg.getString("warp.manage-gui.error.name", "&cUnable to load warp");
        manageWarpErrorLine1 = cfg.getString("warp.manage-gui.error.line1", "&7This warp is not available.");
        manageWarpErrorLine2 = cfg.getString("warp.manage-gui.error.line2", "&7It may have been deleted");
        manageWarpErrorLine3 = cfg.getString("warp.manage-gui.error.line3", "&7or failed to load.");
        manageWarpErrorLine4 = cfg.getString("warp.manage-gui.error.line4", "&7Re-open the menu and try again.");
        manageWarpChangeNameTitle = cfg.getString("warp.manage-gui.change-name.title", "&bChange Name");
        manageWarpChangeNameLine1 = cfg.getString("warp.manage-gui.change-name.line1", "&7Click to rename this warp.");
        manageWarpChangeNameLine2 = cfg.getString("warp.manage-gui.change-name.line2", "&7The new name must be unique.");
        manageWarpCurrent = cfg.getString("warp.manage-gui.current", "&7Current: &b%value%");
        manageWarpChangeMaterialTitle = cfg.getString("warp.manage-gui.change-material.title", "&bChange Material");
        manageWarpChangeMaterialLine1 = cfg.getString("warp.manage-gui.change-material.line1", "&7Click to change this warp icon.");
        manageWarpChangeMaterialLine2 = cfg.getString("warp.manage-gui.change-material.line2", "&7Use any valid material.");
        manageWarpChangeDescriptionTitle = cfg.getString("warp.manage-gui.change-description.title", "&bChange Description");
        manageWarpChangeDescriptionLine1 = cfg.getString("warp.manage-gui.change-description.line1", "&7Click to edit the description.");
        manageWarpChangeDescriptionLine2 = cfg.getString("warp.manage-gui.change-description.line2", "&7Use multiple lines if needed.");
        manageWarpCurrentLabel = cfg.getString("warp.manage-gui.current-label", "&7Current:");
        manageWarpDescriptionNone = cfg.getString("warp.manage-gui.description-none", "&8- &7(none)");
        manageWarpDescriptionEntry = cfg.getString("warp.manage-gui.description-entry", "&8- &7%line%");
        manageWarpChangeLocationTitle = cfg.getString("warp.manage-gui.change-location.title", "&bChange Location");
        manageWarpChangeLocationLine1 = cfg.getString("warp.manage-gui.change-location.line1", "&7Click to set this warp location");
        manageWarpChangeLocationLine2 = cfg.getString("warp.manage-gui.change-location.line2", "&7to your current position.");
        managWarpDeleteTitle = cfg.getString("warp.manage-gui.delete.title", "&cDelete Warp");
        manageWarpDeleteLine1 = cfg.getString("warp.manage-gui.delete.line1", "&7Click to delete this warp.");
        manageWarpDeleteLine2 = cfg.getString("warp.manage-gui.delete.line2", "&cThis action cannot be undone.");
        manageWarpDeleteLine3 = cfg.getString("warp.manage-gui.delete.line3", "&7You can recreate it later");
        manageWarpDeleteLine4 = cfg.getString("warp.manage-gui.delete.line4", "&7with /setwarp.");
        manageWarpBackTitle = cfg.getString("warp.manage-gui.back.title", "&eBack");
        manageWarpBackLine1 = cfg.getString("warp.manage-gui.back.line1", "&7Return to the warp list.");
        manageWarpBackLine2 = cfg.getString("warp.manage-gui.back.line2", "&7Your changes are saved instantly.");
        manageWwarpInfoTitle = cfg.getString("warp.manage-gui.info.title", "&bWarp Information");
    }
}
