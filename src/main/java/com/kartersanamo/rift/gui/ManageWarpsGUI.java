package com.kartersanamo.rift.gui;

import com.kartersanamo.rift.Rift;
import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.chat.ColorUtil;
import com.kartersanamo.rift.api.config.ConfigUtil;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.api.gui.GUI;
import com.kartersanamo.rift.api.item.ItemBuilder;
import com.kartersanamo.rift.api.logging.AuditLogger;
import com.kartersanamo.rift.api.util.LocationUtil;
import com.kartersanamo.rift.api.util.PlaceholderUtil;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ManageWarpsGUI extends GUI {

    private final WarpManager warpManager;
    private final Warp warp;

    public ManageWarpsGUI(WarpManager warpManager, Warp warp) {
        super("manage_warps_gui",
                "Warp Manager",
                27);
        this.warpManager = warpManager;
        this.warp = warp;
        build();
    }

    private void build() {
        // If the warp is null, show an error message
        if (warp == null) {
            ItemStack nullItem = new ItemBuilder(Material.BARRIER)
                    .name(ColorUtil.translate(MessagesUtil.manageWarpErrorName))
                    .lore(List.of(
                            ColorUtil.translate(MessagesUtil.manageWarpErrorLine1),
                            ColorUtil.translate(MessagesUtil.manageWarpErrorLine2),
                            ColorUtil.translate(MessagesUtil.manageWarpErrorLine3),
                            ColorUtil.translate(MessagesUtil.manageWarpErrorLine4)
                    ))
                    .build();
            setItem(13, nullItem);
            return;
        }

        // Change name item
        ItemStack changeNameItem = new ItemBuilder(Material.NAME_TAG)
                .name(ColorUtil.translate(MessagesUtil.manageWarpChangeNameTitle))
                .lore(List.of(
                        tr(MessagesUtil.manageWarpChangeNameLine1),
                        tr(MessagesUtil.manageWarpChangeNameLine2),
                        tr(MessagesUtil.blankLine),
                        tr(PlaceholderUtil.replace(
                                MessagesUtil.manageWarpCurrent,
                                "%value%", warp.getName()
                        ))
                ))
                .build();
        setItem(11, changeNameItem);
        setClickHandler(11, this::changeName);

        // Change material item
        ItemStack changeMaterialItem = new ItemBuilder(warp.getMaterial())
                .name(ColorUtil.translate(MessagesUtil.manageWarpChangeMaterialTitle))
                .lore(List.of(
                        tr(MessagesUtil.manageWarpChangeMaterialLine1),
                        tr(MessagesUtil.manageWarpChangeMaterialLine2),
                        tr(MessagesUtil.blankLine),
                        tr(PlaceholderUtil.replace(
                                MessagesUtil.manageWarpCurrent,
                                "%value%", warp.getMaterial().toString()
                        ))
                ))
                .build();
        setItem(12, changeMaterialItem);
        setClickHandler(12, this::changeMaterial);

        // Change description item
        List<String> description = new ArrayList<>();
        description.add(tr(MessagesUtil.manageWarpChangeDescriptionLine1));
        description.add(tr(MessagesUtil.manageWarpChangeDescriptionLine2));
        description.add(tr(MessagesUtil.blankLine));
        description.add(tr(MessagesUtil.manageWarpCurrentLabel));
        List<String> currentDescription = warp.getDescription();

        if (currentDescription == null || currentDescription.isEmpty()) {
            description.add(tr(MessagesUtil.manageWarpDescriptionNone));
        } else {
            for (String line : currentDescription) {
                description.add(tr(PlaceholderUtil.replace(MessagesUtil.manageWarpDescriptionEntry, "%line%", line)));
            }
        }

        ItemStack changeLoreItem = new ItemBuilder(Material.PAPER)
                .name(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionTitle))
                .lore(description)
                .build();

        setItem(13, changeLoreItem);
        setClickHandler(13, this::changeDescription);

        // Change location item
        ItemStack changeLocationItem = new ItemBuilder(Material.MAP)
                .name(ColorUtil.translate(MessagesUtil.manageWarpChangeLocationTitle))
                .lore(List.of(
                        tr(MessagesUtil.manageWarpChangeLocationLine1),
                        tr(MessagesUtil.manageWarpChangeLocationLine2),
                        tr(MessagesUtil.blankLine),
                        tr(PlaceholderUtil.replace(
                                MessagesUtil.manageWarpCurrent,
                                "%value%", LocationUtil.format(warp.getLocation())
                        ))
                ))
                .build();
        setItem(14, changeLocationItem);
        setClickHandler(14, this::changeLocation);

        // Change category item
        ItemStack changeCategoryItem = new ItemBuilder(Material.CHEST)
                .name(ColorUtil.translate(MessagesUtil.manageWarpChangeCategoryTitle))
                .lore(List.of(
                        tr(MessagesUtil.manageWarpChangeCategoryLine1),
                        tr(MessagesUtil.manageWarpChangeCategoryLine2),
                        tr(MessagesUtil.blankLine),
                        tr(PlaceholderUtil.replace(
                                MessagesUtil.manageWarpCurrent,
                                "%value%", warp.getCategory()
                        ))
                ))
                .build();
        setItem(16, changeCategoryItem);
        setClickHandler(16, this::changeCategory);

        // Delete warp item
        ItemStack deleteWarpItem = new ItemBuilder(Material.BARRIER)
                .name(ColorUtil.translate(MessagesUtil.managWarpDeleteTitle))
                .lore(List.of(
                        tr(MessagesUtil.manageWarpDeleteLine1),
                        tr(MessagesUtil.manageWarpDeleteLine2),
                        tr(MessagesUtil.manageWarpDeleteLine3),
                        tr(MessagesUtil.manageWarpDeleteLine4)
                ))
                .build();
        setItem(15, deleteWarpItem);
        setClickHandler(15, this::deleteWarp);

        // Back button item
        ItemStack backButtonItem = new ItemBuilder(Material.ARROW)
                .name(ColorUtil.translate(MessagesUtil.manageWarpBackTitle))
                .lore(List.of(
                        tr(MessagesUtil.manageWarpBackLine1),
                        tr(MessagesUtil.manageWarpBackLine2)
                ))
                .build();
        setItem(18, backButtonItem);
        setClickHandler(18, this::backButton);

        // Warp information
        List<String> lines = warpManager.getInformationLines(warp);
        ItemStack homeInformationItem = new ItemBuilder(Material.BOOK)
                .name(ColorUtil.translate(MessagesUtil.manageWwarpInfoTitle))
                .lore(lines)
                .build();
        setItem(22, homeInformationItem);
        setClickHandler(22, this::warpInformation);
    }

    // Called when someone clicks on the item to change the name of a warp
    private void changeName(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!ensureManagePermission(player)) {
            return;
        }
        player.closeInventory();

        Rift.getInstance().getChatInputManager().awaitInput(player,
                MessagesUtil.manageWarpChangeNamePrompt,
                input -> {
                    String newName = input.trim();

                    // Validate the new name
                    if (newName.isBlank()) {
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeNameInvalid));
                        return;
                    }
                    if (warp.getName().equalsIgnoreCase(newName)) {
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeNameSame));
                        return;
                    }

                    WarpManager.WarpNameValidationResult validationResult = warpManager.validateWarpName(newName);
                    if (validationResult != WarpManager.WarpNameValidationResult.VALID) {
                        player.sendMessage(ChatFormat.error(
                                warpManager.getWarpNameValidationMessage(validationResult)
                        ));
                        return;
                    }

                    // Check for duplicate names
                    if (warpManager.getWarp(newName) != null) {
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeNameDuplicate));
                        return;
                    }

                    warp.setName(newName);
                    warpManager.update(warp);
                    AuditLogger.action(player, "warp.manage.rename", "id=" + warp.getId() + " name=" + newName);
                    player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeNameSuccess));
                },
                () -> player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeNameCancelled))
        );
    }

    // Called when someone clicks on the item to change the material of a warp
    private void changeMaterial(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!ensureManagePermission(player)) {
            return;
        }
        player.closeInventory();

        Rift.getInstance().getChatInputManager().awaitInput(player,
                MessagesUtil.manageWarpChangeMaterialPrompt,
                input -> {
                    String newMaterialName = input.trim().replace(' ', '_').toUpperCase();

                    if (newMaterialName.isBlank()) {
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeMaterialInvalid));
                        return;
                    }

                    Material newMaterial = Material.matchMaterial(newMaterialName);
                    if (newMaterial == null || !newMaterial.isItem()) {
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeMaterialInvalid));
                        return;
                    }

                    if (warp.getMaterial() == newMaterial) {
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeMaterialSame));
                        return;
                    }

                    warp.setMaterial(newMaterial);
                    warpManager.update(warp);
                    AuditLogger.action(player, "warp.manage.material", "id=" + warp.getId() + " material=" + newMaterial.name());
                    player.sendMessage(tr(PlaceholderUtil.replace(
                            MessagesUtil.manageWarpChangeMaterialSuccess,
                            "%value%", newMaterial.name()
                    )));
                },
                () -> player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeMaterialCancelled))
        );
    }

    // Called when someone clicks on the item to change the description of a warp
    private void changeDescription(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!ensureManagePermission(player)) {
            return;
        }
        player.closeInventory();

        String prompt = PlaceholderUtil.replace(
                MessagesUtil.manageWarpChangeDescriptionPrompt,
                "%clear_keyword%", MessagesUtil.manageWarpChangeDescriptionClearKeyword
        );

        Rift.getInstance().getChatInputManager().awaitInput(player,
                prompt,
                input -> {
                    String rawInput = input.trim();
                    if (rawInput.isBlank()) {
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionInvalid));
                        return;
                    }

                    List<String> description = warp.getDescription();
                    if (description == null) {
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionInvalid));
                        return;
                    }

                    if (rawInput.equalsIgnoreCase(MessagesUtil.manageWarpChangeDescriptionClearKeyword)) {
                        if (description.isEmpty()) {
                            player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionAlreadyEmpty));
                            return;
                        }
                        description.clear();
                        warpManager.update(warp);
                        AuditLogger.action(player, "warp.manage.description.clear", "id=" + warp.getId());
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionCleared));
                        return;
                    }

                    List<String> newDescription = parseDescriptionLines(rawInput);
                    if (newDescription.isEmpty()) {
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionInvalid));
                        return;
                    }

                    // Validate line count
                    if (newDescription.size() > ConfigUtil.warpDescriptionMaxLines) {
                        player.sendMessage(tr(PlaceholderUtil.replace(
                                MessagesUtil.manageWarpChangeDescriptionLinesTooMany,
                                "%max%", String.valueOf(ConfigUtil.warpDescriptionMaxLines),
                                "%count%", String.valueOf(newDescription.size())
                        )));
                        return;
                    }

                    // Validate line length
                    for (int i = 0; i < newDescription.size(); i++) {
                        if (newDescription.get(i).length() > ConfigUtil.warpDescriptionMaxLength) {
                            player.sendMessage(tr(PlaceholderUtil.replace(
                                    MessagesUtil.manageWarpChangeDescriptionLineTooLong,
                                    "%max%", String.valueOf(ConfigUtil.warpDescriptionMaxLength),
                                    "%line%", String.valueOf(i + 1),
                                    "%length%", String.valueOf(newDescription.get(i).length())
                            )));
                            return;
                        }
                    }

                    if (description.equals(newDescription)) {
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionSame));
                        return;
                    }

                    description.clear();
                    description.addAll(newDescription);
                    warpManager.update(warp);
                    AuditLogger.action(player, "warp.manage.description.update", "id=" + warp.getId() + " lines=" + newDescription.size());
                    player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionSuccess));
                },
                () -> player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionCancelled))
        );
    }

    // Called when someone clicks on the item to change the location of a warp
    private void changeLocation(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!ensureManagePermission(player)) {
            return;
        }
        var newLocation = player.getLocation().clone();

        if (warp.getLocation() != null && warp.getLocation().equals(newLocation)) {
            player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeLocationSame));
            return;
        }

        warp.setLocation(newLocation);
        warpManager.update(warp);
        AuditLogger.action(player, "warp.manage.location", "id=" + warp.getId() + " location=" + LocationUtil.format(newLocation));
        player.sendMessage(ChatFormat.info(
                PlaceholderUtil.replace(
                        MessagesUtil.warpUpdatedLocation,
                        "%name%", warp.getName(),
                        "%location%", LocationUtil.format(newLocation)
                )
        ));
    }

    // Called when someone clicks on the item to change the category of a warp
    private void changeCategory(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!ensureManagePermission(player)) {
            return;
        }
        player.closeInventory();

        Rift.getInstance().getChatInputManager().awaitInput(player,
                MessagesUtil.manageWarpChangeCategoryPrompt,
                input -> {
                    String newCategory = input.trim();

                    if (newCategory.isBlank()) {
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeCategoryInvalid));
                        return;
                    }

                    String currentCategory = warp.getCategory();
                    if (currentCategory != null && currentCategory.equalsIgnoreCase(newCategory)) {
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeCategorySame));
                        return;
                    }

                    warp.setCategory(newCategory);
                    warpManager.update(warp);
                    AuditLogger.action(player, "warp.manage.category", "id=" + warp.getId() + " category=" + newCategory);
                    player.sendMessage(tr(PlaceholderUtil.replace(
                            MessagesUtil.manageWarpChangeCategorySuccess,
                            "%value%", newCategory
                    )));
                },
                () -> player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeCategoryCancelled))
        );
    }

    // Called when someone clicks on the item to delete a warp
    private void deleteWarp(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!ensureManagePermission(player)) {
            return;
        }
        player.closeInventory();

        String prompt = PlaceholderUtil.replace(
                MessagesUtil.manageWarpDeletePrompt,
                "%name%", warp.getName(),
                "%keyword%", MessagesUtil.manageWarpDeleteConfirmKeyword
        );

        Rift.getInstance().getChatInputManager().awaitInput(player,
                prompt,
                input -> {
                    if (!input.trim().equalsIgnoreCase(MessagesUtil.manageWarpDeleteConfirmKeyword)) {
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpDeleteConfirmInvalid));
                        return;
                    }

                    boolean deleted = warpManager.deleteWarp(warp.getName());
                    if (!deleted) {
                        player.sendMessage(ChatFormat.error(
                                PlaceholderUtil.replace(MessagesUtil.warpDeleteFailed, "%name%", warp.getName())
                        ));
                        return;
                    }

                    player.sendMessage(ChatFormat.success(
                            PlaceholderUtil.replace(MessagesUtil.warpDeleted, "%name%", warp.getName())
                    ));
                    AuditLogger.action(player, "warp.manage.delete", "id=" + warp.getId() + " name=" + warp.getName());
                    new WarpsGUI(warpManager).open(player);
                },
                () -> player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpDeleteCancelled))
        );
    }

    // Called when someone clicks on the item to go to the previous GUI
    private void backButton(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        new WarpsGUI(warpManager).open(player);
    }

    // Called when someone clicks on the item to display the warp information
    private void warpInformation(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        player.closeInventory();
        warpManager.sendInfo(warp, player);
    }

    private List<String> parseDescriptionLines(String rawInput) {
        List<String> lines = new ArrayList<>();
        for (String split : rawInput.split("\\|")) {
            String cleaned = split.trim();
            if (!cleaned.isEmpty()) {
                lines.add(cleaned);
            }
        }
        return lines;
    }

    private String tr(String message) {
        String translated = ColorUtil.translate(message);
        return translated != null ? translated : "";
    }

    private boolean ensureManagePermission(Player player) {
        if (player.hasPermission("rift.warp.manage")) {
            return true;
        }
        player.sendMessage(ChatFormat.error(MessagesUtil.commandNoPermission));
        player.closeInventory();
        return false;
    }
}
