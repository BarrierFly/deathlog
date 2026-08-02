package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;

public class DeathListEntryContainer extends AlwaysSelectedEntryListWidget.Entry<DeathListEntryContainer> {
    private final TextRenderer textRenderer;
    private final DeathListWidget parent;
    private DeathInfo info;

    public DeathListEntryContainer(DeathListWidget parent, DeathInfo info) {
        this.info = info;
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int idx, int y, int x, int ew, int eh, int mx, int my, boolean hov, float dt) {
        context.drawText(textRenderer, info.getListName(), x, y + 4, 0xFFFFFF, false);
        var lines = textRenderer.wrapLines(info.getTitle(), ew - 8);
        for (int i = 0; i < Math.min(2, lines.size()); i++) {
            OrderedText line = lines.get(i);
            context.drawText(textRenderer, line, x, y + 18 + i * 9, 0xFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        parent.setSelected(this);
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        return true;
    }

    public DeathInfo getInfo() { return info; }

    public void updateInfo(DeathInfo info) { this.info = info; }

    @Override
    public Text getNarration() { return Text.of(info.getTitle().getString()); }
}
