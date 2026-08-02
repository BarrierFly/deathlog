package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.storage.DirectDeathLogStorage;
import net.minecraft.client.MinecraftClient;

import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import java.util.Objects;

public class DeathListWidget extends AlwaysSelectedEntryListWidget<DeathListEntryContainer> {

    private final DirectDeathLogStorage storage;
    private final int scrollTop;
    private final int scrollBottom;
    private String filter = "";
    public boolean restoreEnabled;
    private boolean draggingScrollbar;

    public DeathListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight,
                           DirectDeathLogStorage storage) {
        super(client, width, height, top, bottom, itemHeight);
        this.setRenderBackground(false);
        this.setRenderHorizontalShadows(false);
        this.scrollTop = top;
        this.scrollBottom = bottom;
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

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        int x = getScrollbarPositionX();
        return mouseX >= x - 1 && mouseX <= x + 6 && mouseY >= scrollTop && mouseY <= scrollBottom;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isOverScrollbar(mouseX, mouseY)) {
            draggingScrollbar = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingScrollbar) {
            setScrollAmount(Math.max(0, Math.min(getMaxScroll(), getScrollAmount() + deltaY)));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
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
