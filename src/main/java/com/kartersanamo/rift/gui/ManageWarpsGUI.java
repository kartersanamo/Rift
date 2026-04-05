package com.kartersanamo.rift.gui;

import com.kartersanamo.rift.Rift;
import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.chat.ColorUtil;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.api.gui.GUI;
import com.kartersanamo.rift.api.item.ItemBuilder;
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
import java.util.Objects;

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
        }
        assert warp != null;

        // Change name item
        ItemStack changeNameItem = new ItemBuilder(Material.NAME_TAG)
                .name(ColorUtil.translate(MessagesUtil.manageWarpChangeNameTitle))
                .lore(List.of(
                        ColorUtil.translate(MessagesUtil.manageWarpChangeNameLine1),
                        ColorUtil.translate(MessagesUtil.manageWarpChangeNameLine2),
                        ColorUtil.translate(MessagesUtil.blankLine),
                        Objects.requireNonNull(ColorUtil.translate(
                                PlaceholderUtil.replace(
                                        MessagesUtil.manageWarpCurrent,
                                        "%value%", warp.getName()
                                )
                        ))
                ))
                .build();
        setItem(11, changeNameItem);
        setClickHandler(11, this::changeName);

        // Change material item
        ItemStack changeMaterialItem = new ItemBuilder(warp.getMaterial())
                .name(ColorUtil.translate(MessagesUtil.manageWarpChangeMaterialTitle))
                .lore(List.of(
                        ColorUtil.translate(MessagesUtil.manageWarpChangeMaterialLine1),
                        ColorUtil.translate(MessagesUtil.manageWarpChangeMaterialLine2),
                        ColorUtil.translate(MessagesUtil.blankLine),
                        Objects.requireNonNull(ColorUtil.translate(
                                PlaceholderUtil.replace(
                                        MessagesUtil.manageWarpCurrent,
                                        "%value%", warp.getMaterial().toString()
                                )
                        ))
                ))
                .build();
        setItem(12, changeMaterialItem);
        setClickHandler(12, this::changeMaterial);

        // Change description item
        List<String> description = new ArrayList<>();
        description.add(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionLine1));
        description.add(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionLine2));
        description.add(ColorUtil.translate(MessagesUtil.blankLine));
        description.add(ColorUtil.translate(MessagesUtil.manageWarpCurrentLabel));
        List<String> currentDescription = warp.getDescription();

        if (currentDescription == null || currentDescription.isEmpty()) {
            description.add(ColorUtil.translate(MessagesUtil.manageWarpDescriptionNone));
        } else {
            for (String line : currentDescription) {
                description.add(ColorUtil.translate(
                        PlaceholderUtil.replace(MessagesUtil.manageWarpDescriptionEntry, "%line%", line)
                ));
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
                        ColorUtil.translate(MessagesUtil.manageWarpChangeLocationLine1),
                        ColorUtil.translate(MessagesUtil.manageWarpChangeLocationLine2),
                        ColorUtil.translate(MessagesUtil.blankLine),
                        Objects.requireNonNull(ColorUtil.translate(
                                PlaceholderUtil.replace(
                                        MessagesUtil.manageWarpCurrent,
                                        "%value%", LocationUtil.format(warp.getLocation())
                                )
                        ))
                ))
                .build();
        setItem(14, changeLocationItem);
        setClickHandler(14, this::changeLocation);

        // Delete warp item
        ItemStack deleteWarpItem = new ItemBuilder(Material.BARRIER)
                .name(ColorUtil.translate(MessagesUtil.managWarpDeleteTitle))
                .lore(List.of(
                        ColorUtil.translate(MessagesUtil.manageWarpDeleteLine1),
                        ColorUtil.translate(MessagesUtil.manageWarpDeleteLine2),
                        ColorUtil.translate(MessagesUtil.manageWarpDeleteLine3),
                        ColorUtil.translate(MessagesUtil.manageWarpDeleteLine4)
                ))
                .build();
        setItem(15, deleteWarpItem);
        setClickHandler(15, this::deleteWarp);

        // Back button item
        ItemStack backButtonItem = new ItemBuilder(Material.ARROW)
                .name(ColorUtil.translate(MessagesUtil.manageWarpBackTitle))
                .lore(List.of(
                        ColorUtil.translate(MessagesUtil.manageWarpBackLine1),
                        ColorUtil.translate(MessagesUtil.manageWarpBackLine2)
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
                    if (warp.getName().equals(newName)) {
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

                    warp.setName(newName);
                    warpManager.update(warp);
                    player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeNameSuccess));
                },
                () -> player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeNameCancelled))
        );
    }

    // Called when someone clicks on the item to change the material of a warp
    private void changeMaterial(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
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
                    player.sendMessage(Objects.requireNonNull(ColorUtil.translate(
                            PlaceholderUtil.replace(
                                    MessagesUtil.manageWarpChangeMaterialSuccess,
                                    "%value%", newMaterial.name()
                            )
                    )));
                },
                () -> player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeMaterialCancelled))
        );
    }

    // Called when someone clicks on the item to change the description of a warp
    private void changeDescription(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
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
                            player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionSame));
                            return;
                        }
                        description.clear();
                        warpManager.update(warp);
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionCleared));
                        return;
                    }

                    List<String> newDescription = parseDescriptionLines(rawInput);
                    if (newDescription.isEmpty()) {
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionInvalid));
                        return;
                    }

                    if (description.equals(newDescription)) {
                        player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionSame));
                        return;
                    }

                    description.clear();
                    description.addAll(newDescription);
                    warpManager.update(warp);
                    player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionSuccess));
                },
                () -> player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeDescriptionCancelled))
        );
    }

    // Called when someone clicks on the item to change the location of a warp
    private void changeLocation(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        var newLocation = player.getLocation().clone();

        if (warp.getLocation() != null && warp.getLocation().equals(newLocation)) {
            player.sendMessage(ColorUtil.translate(MessagesUtil.manageWarpChangeLocationSame));
            return;
        }

        warp.setLocation(newLocation);
        warpManager.update(warp);
        player.sendMessage(ChatFormat.info(
                PlaceholderUtil.replace(
                        MessagesUtil.warpUpdatedLocation,
                        "%name%", warp.getName(),
                        "%location%", LocationUtil.format(newLocation)
                )
        ));
    }

    // Called when someone clicks on the item to delete a warp
    private void deleteWarp(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
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
}
