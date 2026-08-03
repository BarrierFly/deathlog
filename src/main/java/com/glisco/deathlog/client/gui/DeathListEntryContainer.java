package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class DeathListEntryContainer extends AlwaysSelectedEntryListWidget.Entry<DeathListEntryContainer> {
    private static final int TEXT_LINE_HEIGHT = 9;
    private static final float SCROLL_CHARS_PER_SECOND = 10.0F;
    private final TextRenderer textRenderer;
    private final DeathListWidget parent;
    private DeathInfo info;

    public DeathListEntryContainer(DeathListWidget parent, DeathInfo info) {
        this.info = info;
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float delta) {
        context.drawText(textRenderer, info.getListName(), getX(), getY() + 4, 0xFFFFFFFF, false);
        Text title = info.getTitle();
        int maxWidth = Math.max(1, getWidth() - 8);
        int titleY = getY() + 18;
        int textWidth = textRenderer.getWidth(title);
        if (textWidth <= maxWidth) {
            context.drawText(textRenderer, title, getX(), titleY, 0xFFFFFFFF, false);
        } else {
            int speed = (int) (textRenderer.getWidth(" ") * SCROLL_CHARS_PER_SECOND);
            int period = Math.max(1, textWidth + maxWidth);
            int offset = (int) ((System.currentTimeMillis() / 1000.0 * speed) % period);
            context.enableScissor(getX(), titleY, getX() + maxWidth, titleY + TEXT_LINE_HEIGHT);
            context.drawText(textRenderer, title, getX() - offset, titleY, 0xFFFFFFFF, false);
            context.drawText(textRenderer, title, getX() - offset + period, titleY, 0xFFFFFFFF, false);
            context.disableScissor();
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        parent.setSelected(this);
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        return true;
    }

    public DeathInfo getInfo() { return info; }

    public void updateInfo(DeathInfo info) { this.info = info; }

    @Override
    public Text getNarration() { return Text.of(info.getTitle().getString()); }
}
