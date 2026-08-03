package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class DeathListEntryContainer extends AlwaysSelectedEntryListWidget.Entry<DeathListEntryContainer> {
    private static final int TEXT_LINE_HEIGHT = 9;
    private static final float SCROLL_CHARS_PER_SECOND = 3.0F;
    private final TextRenderer textRenderer;
    private final DeathListWidget parent;
    private DeathInfo info;

    public DeathListEntryContainer(DeathListWidget parent, DeathInfo info) {
        this.info = info;
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        this.parent = parent;
    }

    @Override
    public void render(MatrixStack m, int idx, int y, int x, int ew, int eh, int mx, int my, boolean hov, float dt) {
        textRenderer.draw(m, info.getListName(), x, y + 4, 0xFFFFFF);
        Text title = info.getTitle();
        int maxWidth = Math.max(1, ew - 8);
        int titleY = y + 18;
        int textWidth = textRenderer.getWidth(title);
        if (textWidth <= maxWidth) {
            textRenderer.draw(m, title, x, titleY, 0xFFFFFF);
        } else {
            int speed = (int) (textRenderer.getWidth(" ") * SCROLL_CHARS_PER_SECOND);
            int period = Math.max(1, textWidth + maxWidth);
            int offset = (int) ((System.currentTimeMillis() / 1000.0 * speed) % period);
            DrawableHelper.enableScissor(x, titleY, x + maxWidth, titleY + TEXT_LINE_HEIGHT);
            textRenderer.draw(m, title, x - offset, titleY, 0xFFFFFF);
            textRenderer.draw(m, title, x - offset + period, titleY, 0xFFFFFF);
            DrawableHelper.disableScissor();
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
