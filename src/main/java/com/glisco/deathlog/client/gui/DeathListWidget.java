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

    public DeathListWidget(MinecraftClient c, int w, int h, int top, int bot, int ih, DirectDeathLogStorage s) {
        super(c, w, h, top, bot, ih);
        this.setRenderBackground(false);
        this.scrollTop = top;
        this.scrollBottom = bot;
        this.storage = s;
        this.restoreEnabled = c.player != null && c.player.hasPermissionLevel(4);
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
}
