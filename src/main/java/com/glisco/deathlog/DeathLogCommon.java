package com.glisco.deathlog;

import com.glisco.deathlog.death_info.DeathInfoPropertySerializer;
import com.glisco.deathlog.death_info.SpecialPropertyProvider;
import com.glisco.deathlog.network.DeathLogPackets;
import com.glisco.deathlog.storage.DeathLogStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class DeathLogCommon implements ModInitializer {

    private static DeathLogStorage currentStorage = null;
    private static boolean usePermissions;

    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            // Trinkets disabled for this build
        }

        usePermissions = false; // Permissions API disabled

        DeathLogPackets.Server.registerCommonListeners();
    }

    public static boolean usePermissions() {
        return usePermissions;
    }

    public static void setStorage(DeathLogStorage storage) {
        if (DeathLogCommon.currentStorage != null) throw new IllegalStateException("Storage has already been set!");
        if (storage == null) throw new IllegalArgumentException("Storage cannot be null!");
        DeathLogCommon.currentStorage = storage;
    }

    public static DeathLogStorage getStorage() {
        return currentStorage;
    }
}
