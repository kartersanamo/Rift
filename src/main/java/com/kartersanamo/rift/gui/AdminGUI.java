package com.kartersanamo.rift.gui;

import com.kartersanamo.rift.Rift;
import com.kartersanamo.rift.api.chat.ChatFormat;
import com.kartersanamo.rift.api.chat.ColorUtil;
import com.kartersanamo.rift.api.config.MessagesUtil;
import com.kartersanamo.rift.api.gui.GUI;
import com.kartersanamo.rift.api.item.ItemBuilder;
import com.kartersanamo.rift.warp.Warp;
import com.kartersanamo.rift.warp.WarpManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdminGUI extends GUI {

    private final WarpManager warpManager;

    public AdminGUI(WarpManager warpManager) {
        super("admin_gui",
                "Admin Panel",
                27);
        this.warpManager = warpManager;
        build();
    }

     private void build() {
         // Title item - statistics
         int totalWarps = warpManager.getWarps().size();
         int totalUses = warpManager.getWarps().values().stream()
                 .mapToInt(Warp::getUses)
                 .sum();

        ItemStack statsItem = new ItemBuilder(Material.BOOK)
                .name(ColorUtil.translate("&b&lRift Statistics"))
                .lore(List.of(
                        ColorUtil.translate("&7Total Warps: &b" + totalWarps),
                        ColorUtil.translate("&7Total Uses: &b" + totalUses),
                        ColorUtil.translate("&7Average Uses: &b" + (totalWarps > 0 ? totalUses / totalWarps : 0))
                ))
                .build();
        setItem(4, statsItem);

        // Manage all warps button
        ItemStack manageAllItem = new ItemBuilder(Material.COMMAND_BLOCK)
                .name(ColorUtil.translate("&b&lManage All Warps"))
                .lore(List.of(
                        ColorUtil.translate("&7Click to view all warp settings."),
                        ColorUtil.translate("&7You can modify any warp here.")
                ))
                .build();
         setItem(11, manageAllItem);
         setClickHandler(11, event -> {
             // Open a new WarpsGUI which shows all warps that can be managed
             Player player = (Player) event.getWhoClicked();
             new WarpsGUI(warpManager).open(player);
         });

         // Plugin info item
         ItemStack infoItem = new ItemBuilder(Material.PAPER)
                 .name(ColorUtil.translate("&b&lPlugin Information"))
                 .lore(List.of(
                         ColorUtil.translate("&7Plugin: &bRift"),
                         ColorUtil.translate("&7Version: &b" + Rift.getInstance().getDescription().getVersion()),
                         ColorUtil.translate("&7Status: &aRunning")
                 ))
                 .build();
         setItem(13, infoItem);

         // Reload config button
         ItemStack reloadItem = new ItemBuilder(Material.REDSTONE_BLOCK)
                 .name(ColorUtil.translate("&c&lReload Configs"))
                 .lore(List.of(
                         ColorUtil.translate("&7Click to reload all configs."),
                         ColorUtil.translate("&cWarning: &7This may cause brief lag.")
                 ))
                 .build();
         setItem(15, reloadItem);
         setClickHandler(15, event -> {
             Player player = (Player) event.getWhoClicked();
             Rift.getInstance().reloadAll();
             player.sendMessage(ChatFormat.success(ColorUtil.translate(MessagesUtil.configsReloaded)));
         });

         // Backup warps button
         ItemStack backupItem = new ItemBuilder(Material.BARREL)
                 .name(ColorUtil.translate("&b&lBackup Warps"))
                 .lore(List.of(
                         ColorUtil.translate("&7Click to create a backup"),
                         ColorUtil.translate("&7of all warps.")
                 ))
                 .build();
         setItem(22, backupItem);
         setClickHandler(22, event -> {
             Player player = (Player) event.getWhoClicked();
             backupWarps(player);
         });
     }

     private void backupWarps(Player player) {
         try {
             File dataFolder = Rift.getInstance().getDataFolder();
             File warpsFile = new File(dataFolder, "warps.yml");

             if (!warpsFile.exists()) {
                 player.sendMessage(ChatFormat.warning(ColorUtil.translate("&7There is nothing to backup.")));
                 return;
             }

             File backupFolder = new File(dataFolder, "backups");
             if (!backupFolder.exists() && !backupFolder.mkdirs()) {
                 player.sendMessage(ChatFormat.error(ColorUtil.translate("&cFailed to create backup folder.")));
                 return;
             }

             String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
             File backupFile = new File(backupFolder, "warps_backup_" + timestamp + ".yml");

             Files.copy(warpsFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

             player.sendMessage(ChatFormat.success(ColorUtil.translate("&7Backup created: &b" + backupFile.getName())));
          } catch (IOException e) {
              player.sendMessage(ChatFormat.error(ColorUtil.translate("&cFailed to create backup.")));
              Rift.getLog().warning("Failed to backup warps: " + e.getMessage());
          }
      }
}

