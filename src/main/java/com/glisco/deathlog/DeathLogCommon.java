package com.glisco.deathlog;

import com.glisco.deathlog.network.DeathLogPackets;
import com.glisco.deathlog.storage.DeathLogStorage;
import net.fabricmc.api.ModInitializer;

public class DeathLogCommon implements ModInitializer {

    private static DeathLogStorage currentStorage = null;

    @Override
    public void onInitialize() {
        DeathLogPackets.Server.registerCommonListeners();
    }

    public static void setStorage(DeathLogStorage storage) {
        DeathLogCommon.currentStorage = storage;
    }

    public static DeathLogStorage getStorage() {
        return currentStorage;
    }
}
