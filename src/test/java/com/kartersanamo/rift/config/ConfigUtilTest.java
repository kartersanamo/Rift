package com.kartersanamo.rift.config;

import com.kartersanamo.rift.api.config.ConfigUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigUtilTest {

    @Test
    void loadClampsAndParsesConfigValues() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("teleport-delay.enabled", true);
        cfg.set("teleport-delay.seconds", 500);
        cfg.set("teleport-delay.particle", "none");
        cfg.set("teleport-delay.sound", "none");
        cfg.set("teleport-complete.particle", "none");
        cfg.set("teleport-complete.sound", "none");
        cfg.set("warp-name.min-length", 0);
        cfg.set("warp-name.max-length", 999);
        cfg.set("warp-description.max-lines", 0);
        cfg.set("warp-description.max-length-per-line", 900);
        cfg.set("warps.gui.min-size", 11); // invalid because not divisible by 9
        cfg.set("warps.gui.max-rows", 10);
        cfg.set("warps.gui.category-filter-enabled", false);
        cfg.set("admin.backups.keep-last", 999);

        ConfigUtil.load(cfg);

        assertEquals(60, ConfigUtil.teleportDelaySeconds);
        assertEquals(1, ConfigUtil.warpNameMinLength);
        assertEquals(100, ConfigUtil.warpNameMaxLength);
        assertEquals(1, ConfigUtil.warpDescriptionMaxLines);
        assertEquals(500, ConfigUtil.warpDescriptionMaxLength);
        assertEquals(9, ConfigUtil.warpsGuiMinSize);
        assertEquals(6, ConfigUtil.warpsGuiMaxRows);
        assertFalse(ConfigUtil.warpsGuiCategoryFilterEnabled);
        assertEquals(200, ConfigUtil.adminBackupKeepLast);
    }
}

