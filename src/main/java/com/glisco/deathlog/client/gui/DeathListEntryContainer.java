package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
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
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float tickDelta) {
        textRenderer.draw(matrices, info.getListName(), x, y + 4, 0xFFFFFF);
        int titleY = y + 18;
        var lines = textRenderer.wrapLines(info.getTitle(), entryWidth - 8);
        for (int i = 0; i < Math.min(2, lines.size()); i++) {
            OrderedText line = lines.get(i);
            textRenderer.draw(matrices, line, x, titleY + i * 9, 0xFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        parent.setSelected(this);
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        return true;
    }

    public DeathInfo getInfo() {
        return info;
    }

    public void updateInfo(DeathInfo info) {
        this.info = info;
    }

    @Override
    public Text getNarration() {
        return Text.of(info.getTitle().getString() + " - " + info.getListName().getString());
    }
}
