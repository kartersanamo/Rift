package com.kartersanamo.rift.warp;

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

    public static final int MAX_NUM_WARPS = 9;
    private Map<String, Warp> warps;// ID -> Home
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

        if (section == null) {
            warps = new HashMap<>();
            return;
        }

        for (String warpId : section.getKeys(false)) {
            ConfigurationSection warpSection = section.getConfigurationSection(warpId);
            if (warpSection == null) continue;

            String name = warpSection.getString("name", warpId);
            List<String> description = warpSection.getStringList("description");
            String category = warpSection.getString("category", "default");

            Location location = warpSection.getLocation("location");
            if (location == null) continue;

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

            Player creator = plugin.getServer().getPlayer(
                    UUID.fromString(
                            warpSection.getString("creator")
                    )
            );

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
        }
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
        // TODO: Validate warp name
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

    public void deleteWarp(String homeName) {
        String id = getIdByName(homeName);
        if (id == null) {
            return; // Home not found
        }
        warps.remove(id);
        saveWarps();
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
        player.sendMessage(warp.getName() + " " + warp.getDescription());
//        player.sendMessage(ColorUtil.translate(MessagesUtil.warpInfoDivider));
//        player.sendMessage(ColorUtil.translate(MessagesUtil.warpInfoTitle));
//        player.sendMessage(ColorUtil.translate(MessagesUtil.warpInfoSpacer));
//        List<String> lines = getInformationLines(warp);
//        for (String line : lines) {
//            player.sendMessage(ColorUtil.translate(line));
//        }
//        player.sendMessage(ColorUtil.translate(MessagesUtil.warpInfoDivider));
    }

//    public List<String> getInformationLines(@org.jetbrains.annotations.UnknownNullability Warp home) {
//        List<String> lines = new ArrayList<>();
//
//        lines.add(ColorUtil.translate(
//                PlaceholderUtil.replace(MessagesUtil.homeInfoName, "%name%", home.getDisplayName())
//        ));
//        lines.add(ColorUtil.translate(
//                PlaceholderUtil.replace(MessagesUtil.homeInfoCreated, "%time%", TimeUtil.formatUnix(home.getCreatedAt()))
//        ));
//        lines.add(ColorUtil.translate(
//                PlaceholderUtil.replace(MessagesUtil.homeInfoLastUsed, "%time%", TimeUtil.formatUnix(home.getLastUsedAt()))
//        ));
//        lines.add(ColorUtil.translate(
//                PlaceholderUtil.replace(MessagesUtil.homeInfoMaterial, "%material%", home.getMaterial().name())
//        ));
//        lines.add(ColorUtil.translate(
//                PlaceholderUtil.replace(MessagesUtil.homeInfoLocation, "%location%", LocationUtil.format(home.getLocation()))
//        ));
//        if (home.getDescription().isEmpty()) {
//            lines.add(ColorUtil.translate(MessagesUtil.homeInfoLoreNone));
//        } else {
//            lines.add(ColorUtil.translate(MessagesUtil.homeInfoLoreLabel));
//            for (String loreLine : home.getLore()) {
//                lines.add(ColorUtil.translate(
//                        PlaceholderUtil.replace(MessagesUtil.homeInfoLoreEntry, "%line%", loreLine)
//                ));
//            }
//        }
//
//        return lines;
//    }
}
