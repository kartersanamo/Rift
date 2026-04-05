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

    private Map<String, Warp> warps;// ID -> Warp
    private final Set<String> categories;
    private final Plugin plugin;

    public WarpManager(Plugin plugin) {
        this.plugin = plugin;
        this.warps = new HashMap<>();
        this.categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    }

    private ConfigFile getWarpFile() {
        return new ConfigFile(plugin, "warps.yml");
    }

    public void loadWarps() {
        ConfigFile file = getWarpFile();
        ConfigurationSection section = file.getConfig().getConfigurationSection("warps");
        warps = new HashMap<>();
        categories.clear();

        List<String> persistedCategories = file.getConfig().getStringList("categories");
        for (String category : persistedCategories) {
            String normalized = normalizeCategory(category);
            if (normalized != null) {
                categories.add(normalized);
            }
        }
        categories.add("default");

        if (section == null) {
            Rift.getLog().info("Loaded 0 warps.");
            return;
        }

        int loadedCount = 0;
        for (String warpId : section.getKeys(false)) {
            ConfigurationSection warpSection = section.getConfigurationSection(warpId);
            if (warpSection == null) continue;

            String name = warpSection.getString("name", warpId);
            List<String> description = new ArrayList<>(warpSection.getStringList("description"));
            String category = ensureCategoryExists(warpSection.getString("category", "default"));

            Location location = warpSection.getLocation("location");
            if (location == null) {
                Rift.getLog().warning("Skipping warp '" + name + "' - location is invalid or world does not exist.");
                continue;
            }

            Material material = Material.matchMaterial(warpSection.getString("material", "CHEST"));
            if (material == null || !material.isItem()) {
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
        categories.clear();
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

        file.getConfig().set("categories", new ArrayList<>(categories));

        file.save();
    }

    public void addWarp(Warp warp) {
        ensureCategoryExists(warp.getCategory());
        warps.put(warp.getId(), warp);
        saveWarps();
    }


    public boolean exists(String name) {
        return getWarp(name) != null;
    }

    public Map<String, Warp> getWarps() {
        return warps;
    }

    public Warp getWarp(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (Warp warp : warps.values()) {
            if (warp.getName().equalsIgnoreCase(name)) {
                return warp;
            }
        }
        return null;
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
        for (Warp warp : warps.values()) {
            if (warp.getName().equalsIgnoreCase(warpName)) {
                return warp.getId();
            }
        }
        return null;
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
        ensureCategoryExists(warp.getCategory());
        warps.put(warp.getId(), warp);
        saveWarps();
    }

    public boolean warpNameHasColor(String warpName) {
        return !ChatColor.stripColor(
                ChatColor.translateAlternateColorCodes('&', warpName)
        ).equals(warpName);
    }

    public List<String> getCategories() {
        return new ArrayList<>(categories);
    }

    public boolean hasAnyNonDefaultCategory() {
        for (String category : categories) {
            if (!category.equalsIgnoreCase("default")) {
                return true;
            }
        }
        return false;
    }

    public boolean categoryExists(String category) {
        String normalized = normalizeCategory(category);
        return normalized != null && categories.contains(normalized);
    }

    public String ensureCategoryExists(String category) {
        String normalized = normalizeCategory(category);
        if (normalized == null) {
            normalized = "default";
        }
        categories.add(normalized);
        return normalized;
    }

    public boolean createCategory(String category) {
        String normalized = normalizeCategory(category);
        if (normalized == null) {
            return false;
        }
        boolean added = categories.add(normalized);
        if (added) {
            saveWarps();
        }
        return added;
    }

    public boolean deleteCategory(String category) {
        String normalized = normalizeCategory(category);
        if (normalized == null || normalized.equalsIgnoreCase("default")) {
            return false;
        }
        if (!categories.remove(normalized)) {
            return false;
        }

        for (Warp warp : warps.values()) {
            if (warp.getCategory() != null && warp.getCategory().equalsIgnoreCase(normalized)) {
                warp.setCategory("default");
            }
        }
        categories.add("default");
        saveWarps();
        return true;
    }

    public void assignWarpCategory(Warp warp, String category) {
        if (warp == null) {
            return;
        }
        warp.setCategory(ensureCategoryExists(category));
        update(warp);
    }

    public List<Warp> getWarpsInCategory(String category) {
        if (category == null || category.isBlank() || category.equalsIgnoreCase("all")) {
            return new ArrayList<>(warps.values());
        }
        List<Warp> filtered = new ArrayList<>();
        for (Warp warp : warps.values()) {
            if (warp.getCategory() != null && warp.getCategory().equalsIgnoreCase(category)) {
                filtered.add(warp);
            }
        }
        return filtered;
    }

    private String normalizeCategory(String category) {
        if (category == null) {
            return null;
        }
        String normalized = category.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public void sendInfo(Warp warp, Player player) {
        if (warp == null || player == null) {
            return;
        }

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
        if (warp == null) {
            return lines;
        }

        List<String> description = warp.getDescription();
        String creatorName = warp.getCreator() != null
                ? warp.getCreator().getName()
                : MessagesUtil.warpInfoCreatorUnknown;

        lines.add(ColorUtil.translate(PlaceholderUtil.replace(MessagesUtil.warpInfoName, "%name%", warp.getName())));

        if (description == null || description.isEmpty()) {
            lines.add(ColorUtil.translate(MessagesUtil.warpInfoDescriptionNone));
        } else {
            lines.add(ColorUtil.translate(MessagesUtil.warpInfoDescriptionLabel));
            for (String descriptionLine : description) {
                lines.add(ColorUtil.translate(PlaceholderUtil.replace(MessagesUtil.warpInfoDescriptionEntry, "%line%", descriptionLine)));
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
