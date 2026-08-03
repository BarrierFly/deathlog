package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.storage.DirectDeathLogStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.command.DefaultPermissions;
import java.util.Objects;

public class DeathListWidget extends AlwaysSelectedEntryListWidget<DeathListEntryContainer> {
    private final DirectDeathLogStorage storage;
    private String filter = "";
    public boolean restoreEnabled;
    private boolean draggingScrollbar;

    public DeathListWidget(MinecraftClient c, int w, int h, int top, int bot, int ih, DirectDeathLogStorage s) {
        super(c, w, bot - top, top, ih);
        this.storage = s;
        this.restoreEnabled = c.player != null && c.player.getPermissions().hasPermission(DefaultPermissions.OWNERS);
        this.refilter();
    }

    @Override
    public int getRowLeft() {
        return getX() + 4;
    }

    @Override
    protected void drawMenuListBackground(DrawContext context) {
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        int x = getScrollbarX();
        return mouseX >= x - 1 && mouseX <= x + 6 && mouseY >= getY() && mouseY <= getY() + getHeight();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (click.button() == 0 && isOverScrollbar(click.x(), click.y())) {
            draggingScrollbar = true;
            return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (draggingScrollbar) {
            setScrollY(Math.max(0, Math.min(getMaxScrollY(), getScrollY() + deltaY)));
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(click);
    }

    public boolean filter(String p) {
        p = p.toLowerCase();
        if (Objects.equals(filter, p)) return getEntryCount() != 0;
        this.filter = p; return refilter();
    }

    public boolean refilter() {
        this.clearEntries();
        if (filter.isBlank()) storage.getDeathInfoList().forEach(this::addDeathEntry);
        else storage.getDeathInfoList().forEach(i -> { if (i.createSearchString().contains(filter)) addDeathEntry(i); });
        return getEntryCount() != 0;
    }

    private void addDeathEntry(DeathInfo info) {
        var entry = new DeathListEntryContainer(this, info);
        addEntry(entry, entry.getEntryHeight());
    }
}
