package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.storage.DirectDeathLogStorage;
import net.minecraft.client.MinecraftClient;

import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import java.util.Objects;

public class DeathListWidget extends AlwaysSelectedEntryListWidget<DeathListEntryContainer> {

    private final DirectDeathLogStorage storage;
    private String filter = "";
    public boolean restoreEnabled;

    public DeathListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight,
                           DirectDeathLogStorage storage) {
        super(client, width, height, top, bottom, itemHeight);
        this.setRenderBackground(false);
        this.setRenderHorizontalShadows(false);
        this.storage = storage;
        this.restoreEnabled = client.player != null && client.player.hasPermissionLevel(4);
        this.refilter();
    }

    public void deleteInfoFromStorage(DeathInfo info) {
        storage.delete(info);
    }

    public void restoreInfo(int index) {
        storage.restore(index);
    }

    public boolean filter(String pattern) {
        pattern = pattern.toLowerCase();
        if (Objects.equals(filter, pattern)) return getEntryCount() != 0;
        this.filter = pattern;
        return refilter();
    }

    public boolean refilter() {
        this.clearEntries();
        if (filter.isBlank()) {
            storage.getDeathInfoList().forEach(info -> addEntry(new DeathListEntryContainer(this, info)));
        } else {
            storage.getDeathInfoList().forEach(info -> {
                if (info.createSearchString().contains(filter))
                    addEntry(new DeathListEntryContainer(this, info));
            });
        }
        return getEntryCount() != 0;
    }
}
