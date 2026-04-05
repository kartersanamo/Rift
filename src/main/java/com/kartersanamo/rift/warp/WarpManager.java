package com.kartersanamo.rift.warp;

import com.kartersanamo.rift.Rift;
import com.kartersanamo.rift.api.chat.ColorUtil;
import com.kartersanamo.rift.api.config.ConfigFile;
import com.kartersanamo.rift.api.config.ConfigUtil;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.api.util.LocationUtil;
import com.kartersanamo.rift.api.util.PlaceholderUtil;
import com.kartersanamo.rift.api.util.TimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class WarpManager {

    public enum WarpNameValidationResult {
        VALID,
        INVALID_SIZE,
        HAS_COLOR
    }

    public static final int MAX_NUM_WARPS = 9;
    private Map<String, Warp> warps;// ID -> Warp
    private final Plugin plugin;

    public WarpManager(Plugin plugin) {
        this.plugin = plugin;
        this.warps = new HashMap<>();
    }

    private ConfigFile getWarpFile() {
        return new ConfigFile(plugin, "warps.yml");
    }

    public void loadWarps() {
        ConfigFile file = getWarpFile();
        ConfigurationSection section = file.getConfig().getConfigurationSection("warps");
        warps = new HashMap<>();

        if (section == null) {
            Rift.getLog().info("Loaded 0 warps.");
            return;
        }

        int loadedCount = 0;
        for (String warpId : section.getKeys(false)) {
            ConfigurationSection warpSection = section.getConfigurationSection(warpId);
            if (warpSection == null) continue;

            String name = warpSection.getString("name", warpId);
            List<String> description = warpSection.getStringList("description");
            String category = warpSection.getString("category", "default");

            Location location = warpSection.getLocation("location");
            if (location == null) {
                Rift.getLog().warning("Skipping warp '" + name + "' - location is invalid or world does not exist.");
                continue;
            }

            Material material;
            try {
                material = Material.valueOf(
                        warpSection.getString("material", "CHEST").toUpperCase()
                );
            } catch (IllegalArgumentException e) {
                material = Material.CHEST;
            }

            long createdAt = warpSection.getLong("created-at");
            int uses = warpSection.getInt("uses", 0);

            Player creator = null;
            String creatorRaw = warpSection.getString("creator");
            if (creatorRaw != null) {
                try {
                    creator = plugin.getServer().getPlayer(UUID.fromString(creatorRaw));
                } catch (IllegalArgumentException ignored) {
                    // Ignore malformed UUIDs and keep creator unset.
                }
            }

            Warp warp = new Warp(
                    name,
                    description,
                    category,
                    location,
                    warpId,
                    creator,
                    material,
                    createdAt,
                    uses
            );

            warps.put(warpId, warp);
            loadedCount++;
        }

        Rift.getLog().info("Loaded " + loadedCount + " warps.");
    }

    public void unloadWarps() {
        warps = new HashMap<>();
    }

    private void saveWarps() {
        ConfigFile file = getWarpFile();
        file.getConfig().set("warps", null);

        for (Warp warp : warps.values()) {
            String path = "warps." + warp.getId();

            file.getConfig().set(path + ".name", warp.getName());
            file.getConfig().set(path + ".description", warp.getDescription());
            file.getConfig().set(path + ".category", warp.getCategory());
            file.getConfig().set(path + ".location", warp.getLocation());
            file.getConfig().set(path + ".material", warp.getMaterial().name());
            file.getConfig().set(path + ".created-at", warp.getCreatedAt());
            file.getConfig().set(path + ".uses", warp.getUses());
            file.getConfig().set(path + ".creator", warp.getCreator() != null ? warp.getCreator().getUniqueId().toString() : null);

        }

        file.save();
    }

    public void addWarp(Warp warp) {
        warps.put(warp.getId(), warp);
        saveWarps();
    }

    private ConfigurationSection getSection() {
        ConfigFile cf = getWarpFile();
        return cf.getConfig().getConfigurationSection("warps");
    }

    public Map<String, String> getWarpNameToId() {
        Map<String, String> warpNameToId = new HashMap<>();
        ConfigurationSection section = getSection();
        if (section == null) {
            return null;
        }

        for (String id : section.getKeys(false)) {
            String name = (String) section.get(id + ".name");
            if (name != null) {
                warpNameToId.put(name, id);
            }
        }
        return warpNameToId;
    }

    public boolean exists(String name) {
        return getWarp(name) != null;
    }

    public Map<String, Warp> getWarps() {
        return warps;
    }

    public Warp getWarp(String name) {
        Map<String, String> nameToId = getWarpNameToId();
        if (nameToId == null) {
            return null;
        }
        String id = nameToId.get(name);
        return warps.get(id);
    }

    private Map<String, String> getNameToId(UUID uuid) {
        return getWarpNameToId();
    }

    public boolean isHomeNameCorrectSize(String newName) {
        return (newName.length() < ConfigUtil.warpNameMinLength
                || newName.length() > ConfigUtil.warpNameMaxLength);
    }

    public WarpNameValidationResult validateWarpName(String warpName) {
        if (warpName == null || isHomeNameCorrectSize(warpName)) {
            return WarpNameValidationResult.INVALID_SIZE;
        }
        if (warpNameHasColor(warpName)) {
            return WarpNameValidationResult.HAS_COLOR;
        }
        return WarpNameValidationResult.VALID;
    }

    public String getWarpNameValidationMessage(WarpNameValidationResult validationResult) {
        return switch (validationResult) {
            case INVALID_SIZE -> PlaceholderUtil.replace(
                    MessagesUtil.warpNameSize,
                    "%min%", String.valueOf(ConfigUtil.warpNameMinLength),
                    "%max%", String.valueOf(ConfigUtil.warpNameMaxLength)
            );
            case HAS_COLOR -> MessagesUtil.warpNameNoColor;
            case VALID -> "";
        };
    }

    private String getIdByName(String warpName) {
        ConfigurationSection section = getSection();
        if (section == null) return null;

        for (String id : section.getKeys(false)) {
            String name = section.getString(id + ".name");
            if (name != null && name.equalsIgnoreCase(warpName)) {
                return id;
            }
        }

        return null; // Not found
    }

    public boolean deleteWarp(String homeName) {
        String id = getIdByName(homeName);
        if (id == null) {
            return false;
        }
        warps.remove(id);
        saveWarps();
        return true;
    }

    public int getWarpCount() {
        return warps != null ? warps.size() : 0;
    }

    public void update(Warp warp) {
        String normalizedId = warp.getId().toLowerCase().trim();
        warps.put(normalizedId, warp);
        saveWarps();
    }

    public boolean warpNameHasColor(String warpName) {
        return !ChatColor.stripColor(
                ChatColor.translateAlternateColorCodes('&', warpName)
        ).equals(warpName);
    }

    public void sendInfo(Warp warp, Player player) {
        player.sendMessage(ColorUtil.translate(MessagesUtil.warpInfoDivider));
        player.sendMessage(ColorUtil.translate(MessagesUtil.warpInfoTitle));
        player.sendMessage(ColorUtil.translate(MessagesUtil.warpInfoSpacer));
        List<String> lines = getInformationLines(warp);
        for (String line : lines) {
            player.sendMessage(ColorUtil.translate(line));
        }
        player.sendMessage(ColorUtil.translate(MessagesUtil.warpInfoDivider));
    }

    public List<String> getInformationLines(Warp warp) {
        List<String> lines = new ArrayList<>();
        List<String> description = warp.getDescription();
        String creatorName = warp.getCreator() != null
                ? warp.getCreator().getName()
                : MessagesUtil.warpInfoCreatorUnknown;

        lines.add(ColorUtil.translate( PlaceholderUtil.replace(MessagesUtil.warpInfoName, "%name%", warp.getName())));

        if (description == null || description.isEmpty()) { lines.add(ColorUtil.translate(MessagesUtil.warpInfoDescriptionNone));
        } else {
            lines.add(ColorUtil.translate(MessagesUtil.warpInfoDescriptionLabel));
            for (String descriptionLines : description) {
                lines.add(ColorUtil.translate( PlaceholderUtil.replace(MessagesUtil.warpInfoDescriptionEntry, "%line%", descriptionLines)));
            }
        }

        lines.add(ColorUtil.translate(PlaceholderUtil.replace(MessagesUtil.warpInfoCategory, "%category%", warp.getCategory())));
        lines.add(ColorUtil.translate(PlaceholderUtil.replace(MessagesUtil.warpInfoLocation, "%location%", LocationUtil.format(warp.getLocation()))));
        lines.add(ColorUtil.translate(PlaceholderUtil.replace(MessagesUtil.warpInfoCreator, "%creator_name%", creatorName)));
        lines.add(ColorUtil.translate(PlaceholderUtil.replace(MessagesUtil.warpInfoMaterial, "%material%", warp.getMaterial().name())));
        lines.add(ColorUtil.translate(PlaceholderUtil.replace(MessagesUtil.warpInfoCreated, "%time%", TimeUtil.formatUnix(warp.getCreatedAt()))));
        lines.add(ColorUtil.translate(PlaceholderUtil.replace(MessagesUtil.warpInfoUses, "%uses%", String.valueOf(warp.getUses()))));

        return lines;
    }
}
