package com.kartersanamo.rift.warp;

import com.kartersanamo.rift.api.config.ConfigUtil;
import org.bukkit.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WarpManagerTest {

    private WarpManager warpManager;

    @BeforeEach
    void setUp() {
        warpManager = new WarpManager(null);
        ConfigUtil.warpNameMinLength = 3;
        ConfigUtil.warpNameMaxLength = 16;
    }

    @Test
    void getWarpIsCaseInsensitiveAndExistsWorksCaseInsensitive() {
        Warp warp = new Warp(
                "Home",
                new ArrayList<>(),
                "default",
                null,
                "warp_" + UUID.randomUUID(),
                null,
                Material.ENDER_PEARL,
                System.currentTimeMillis(),
                0
        );
        warpManager.getWarps().put(warp.getId(), warp);

        assertNotNull(warpManager.getWarp("home"));
        assertNotNull(warpManager.getWarp("HOME"));
        assertTrue(warpManager.exists("HoMe"));
    }

    @Test
    void validateWarpNameRejectsInvalidNames() {
        assertEquals(WarpManager.WarpNameValidationResult.INVALID_SIZE, warpManager.validateWarpName("aa"));
        assertEquals(WarpManager.WarpNameValidationResult.HAS_COLOR, warpManager.validateWarpName("&aSpawn"));
        assertEquals(WarpManager.WarpNameValidationResult.VALID, warpManager.validateWarpName("spawn"));
    }
}

