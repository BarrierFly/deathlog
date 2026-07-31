package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.storage.DirectDeathLogStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.command.DefaultPermissions;
import java.util.Objects;

public class DeathListWidget extends AlwaysSelectedEntryListWidget<DeathListEntryContainer> {
    private final DirectDeathLogStorage storage;
    private String filter = "";
    public boolean restoreEnabled;

    public DeathListWidget(MinecraftClient c, int w, int h, int top, int bot, int ih, DirectDeathLogStorage s) {
        super(c, w, h, top, bot);
        this.storage = s;
        this.restoreEnabled = c.player != null && c.player.getPermissions().hasPermission(DefaultPermissions.OWNERS);
        this.refilter();
    }

    public boolean filter(String p) {
        p = p.toLowerCase();
        if (Objects.equals(filter, p)) return getEntryCount() != 0;
        this.filter = p; return refilter();
    }

    public boolean refilter() {
        this.clearEntries();
        if (filter.isBlank()) storage.getDeathInfoList().forEach(i -> addEntry(new DeathListEntryContainer(this, i)));
        else storage.getDeathInfoList().forEach(i -> { if (i.createSearchString().contains(filter)) addEntry(new DeathListEntryContainer(this, i)); });
        return getEntryCount() != 0;
    }
}
