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

    private Map<String, Warp> warps; // ID -> Warp
    private final Map<String, Category> categories;
    private final Plugin plugin;

    public WarpManager(Plugin plugin) {
        this.plugin = plugin;
        this.warps = new HashMap<>();
        this.categories = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    private ConfigFile getWarpFile() {
        return new ConfigFile(plugin, "warps.yml");
    }

    public void loadWarps() {
        ConfigFile file = getWarpFile();
        ConfigurationSection section = file.getConfig().getConfigurationSection("warps");
        warps = new HashMap<>();
        categories.clear();

        ConfigurationSection categoriesSection = file.getConfig().getConfigurationSection("categories");
        if (categoriesSection != null) {
            for (String key : categoriesSection.getKeys(false)) {
                ConfigurationSection categorySection = categoriesSection.getConfigurationSection(key);
                if (categorySection == null) {
                    continue;
                }

                String name = categorySection.getString("name", key);
                List<String> description = categorySection.getStringList("description");

                Material material = Material.matchMaterial(categorySection.getString("material", "BOOKSHELF"));
                if (material == null || !material.isItem()) {
                    material = Material.BOOKSHELF;
                }

                Integer menuSlot = categorySection.contains("slot") ? categorySection.getInt("slot") : null;

                Map<String, Integer> warpSlots = new HashMap<>();
                ConfigurationSection warpSlotsSection = categorySection.getConfigurationSection("warp-slots");
                if (warpSlotsSection != null) {
                    for (String warpId : warpSlotsSection.getKeys(false)) {
                        warpSlots.put(warpId, warpSlotsSection.getInt(warpId));
                    }
                }

                categories.put(normalizeCategoryKey(name), new Category(name, description, material, menuSlot, warpSlots));
            }
        }

        ensureCategoryExists("default");

        if (section == null) {
            Rift.getLog().info("Loaded 0 warps.");
            return;
        }

        int loadedCount = 0;
        for (String warpId : section.getKeys(false)) {
            ConfigurationSection warpSection = section.getConfigurationSection(warpId);
            if (warpSection == null) {
                continue;
            }

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
            if (creatorRaw != null && plugin != null) {
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
        if (plugin == null) {
            return;
        }

        ConfigFile file = getWarpFile();
        file.getConfig().set("warps", null);
        file.getConfig().set("categories", null);

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

        for (Category category : categories.values()) {
            String key = normalizeCategoryKey(category.getName());
            String path = "categories." + key;
            file.getConfig().set(path + ".name", category.getName());
            file.getConfig().set(path + ".description", category.getDescription());
            file.getConfig().set(path + ".material", category.getMaterial().name());
            file.getConfig().set(path + ".slot", category.getMenuSlot());
            file.getConfig().set(path + ".warp-slots", null);
            for (Map.Entry<String, Integer> slotEntry : category.getWarpSlots().entrySet()) {
                file.getConfig().set(path + ".warp-slots." + slotEntry.getKey(), slotEntry.getValue());
            }
        }

        file.save();
    }

    public void addWarp(Warp warp) {
        warp.setCategory(ensureCategoryExists(warp.getCategory()));
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
        return newName.length() < ConfigUtil.warpNameMinLength || newName.length() > ConfigUtil.warpNameMaxLength;
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
        for (Category category : categories.values()) {
            category.getWarpSlots().remove(id);
        }
        saveWarps();
        return true;
    }

    public int getWarpCount() {
        return warps != null ? warps.size() : 0;
    }

    public void update(Warp warp) {
        warp.setCategory(ensureCategoryExists(warp.getCategory()));
        warps.put(warp.getId(), warp);
        saveWarps();
    }

    public boolean warpNameHasColor(String warpName) {
        return !ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', warpName)).equals(warpName);
    }

    public List<String> getCategories() {
        List<String> names = new ArrayList<>();
        for (Category category : getCategoriesSorted()) {
            names.add(category.getName());
        }
        return names;
    }

    public List<Category> getCategoriesSorted() {
        List<Category> list = new ArrayList<>(categories.values());
        list.sort((a, b) -> {
            Integer slotA = a.getMenuSlot();
            Integer slotB = b.getMenuSlot();
            if (slotA == null && slotB == null) {
                return a.getName().compareToIgnoreCase(b.getName());
            }
            if (slotA == null) {
                return 1;
            }
            if (slotB == null) {
                return -1;
            }
            int compare = Integer.compare(slotA, slotB);
            return compare != 0 ? compare : a.getName().compareToIgnoreCase(b.getName());
        });
        return list;
    }

    public List<Category> getCategoriesForWarpsMenu() {
        List<Category> visible = new ArrayList<>();
        for (Category category : getCategoriesSorted()) {
            if (category.getName().equalsIgnoreCase("default")
                    && getWarpsInCategory("default").isEmpty()) {
                continue;
            }
            visible.add(category);
        }
        return visible;
    }

    public boolean hasAnyNonDefaultCategory() {
        for (Category category : categories.values()) {
            if (!category.getName().equalsIgnoreCase("default")) {
                return true;
            }
        }
        return false;
    }

    public boolean categoryExists(String category) {
        String key = normalizeCategoryKey(category);
        return key != null && categories.containsKey(key);
    }

    public String ensureCategoryExists(String category) {
        String name = normalizeCategoryName(category);
        if (name == null) {
            name = "default";
        }
        String key = normalizeCategoryKey(name);
        categories.putIfAbsent(key, new Category(name, new ArrayList<>(), Material.BOOKSHELF, null, new HashMap<>()));
        return categories.get(key).getName();
    }

    public boolean createCategory(String category) {
        String name = normalizeCategoryName(category);
        if (name == null) {
            return false;
        }
        String key = normalizeCategoryKey(name);
        if (categories.containsKey(key)) {
            return false;
        }
        categories.put(key, new Category(name, new ArrayList<>(), Material.BOOKSHELF, null, new HashMap<>()));
        saveWarps();
        return true;
    }

    public boolean deleteCategory(String category) {
        String key = normalizeCategoryKey(category);
        if (key == null || key.equalsIgnoreCase("default")) {
            return false;
        }
        Category removed = categories.remove(key);
        if (removed == null) {
            return false;
        }

        for (Warp warp : warps.values()) {
            if (warp.getCategory() != null && warp.getCategory().equalsIgnoreCase(removed.getName())) {
                warp.setCategory("default");
            }
        }
        ensureCategoryExists("default");
        saveWarps();
        return true;
    }

    public boolean renameCategory(String oldName, String newName) {
        String oldKey = normalizeCategoryKey(oldName);
        String newDisplay = normalizeCategoryName(newName);
        if (oldKey == null || newDisplay == null || oldKey.equalsIgnoreCase("default")) {
            return false;
        }

        Category category = categories.get(oldKey);
        if (category == null) {
            return false;
        }

        String newKey = normalizeCategoryKey(newDisplay);
        if (!oldKey.equalsIgnoreCase(newKey) && categories.containsKey(newKey)) {
            return false;
        }

        categories.remove(oldKey);
        String oldDisplay = category.getName();
        category.setName(newDisplay);
        categories.put(newKey, category);

        for (Warp warp : warps.values()) {
            if (warp.getCategory() != null && warp.getCategory().equalsIgnoreCase(oldDisplay)) {
                warp.setCategory(newDisplay);
            }
        }

        saveWarps();
        return true;
    }

    public Category getCategory(String categoryName) {
        String key = normalizeCategoryKey(categoryName);
        return key == null ? null : categories.get(key);
    }

    public void updateCategory(Category category) {
        if (category == null) {
            return;
        }
        categories.put(normalizeCategoryKey(category.getName()), category);
        saveWarps();
    }

    public void assignWarpCategory(Warp warp, String category) {
        if (warp == null) {
            return;
        }
        Category previous = getCategory(warp.getCategory());
        if (previous != null) {
            previous.getWarpSlots().remove(warp.getId());
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

    public Integer getCategorySlot(String categoryName) {
        Category category = getCategory(categoryName);
        return category != null ? category.getMenuSlot() : null;
    }

    public void setCategorySlot(String categoryName, Integer slot) {
        Category category = getCategory(categoryName);
        if (category == null) {
            return;
        }
        category.setMenuSlot(slot);
        updateCategory(category);
    }

    public Integer getWarpSlotInCategory(String categoryName, String warpId) {
        Category category = getCategory(categoryName);
        if (category == null) {
            return null;
        }
        return category.getWarpSlots().get(warpId);
    }

    public void setWarpSlotInCategory(String categoryName, String warpId, Integer slot) {
        Category category = getCategory(categoryName);
        if (category == null || warpId == null) {
            return;
        }
        if (slot == null) {
            category.getWarpSlots().remove(warpId);
        } else {
            category.getWarpSlots().put(warpId, slot);
        }
        updateCategory(category);
    }

    private String normalizeCategoryName(String category) {
        if (category == null) {
            return null;
        }
        String normalized = category.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeCategoryKey(String category) {
        String normalized = normalizeCategoryName(category);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
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
