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
import net.minecraft.text.OrderedText;

import java.util.List;

public class DeathListEntryContainer extends AlwaysSelectedEntryListWidget.Entry<DeathListEntryContainer> {
    private static final int BASE_ENTRY_HEIGHT = 30;
    private static final int TEXT_LINE_HEIGHT = 9;
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
        int y = getY() + 18;
        var lines = getTitleLines();
        for (int i = 0; i < lines.size(); i++) {
            OrderedText line = lines.get(i);
            context.drawText(textRenderer, line, getX(), y + i * TEXT_LINE_HEIGHT, 0xFFFFFFFF, false);
        }
    }

    public int getEntryHeight() {
        return BASE_ENTRY_HEIGHT + (getTitleLines().size() - 1) * TEXT_LINE_HEIGHT;
    }

    private List<OrderedText> getTitleLines() {
        return textRenderer.wrapLines(info.getTitle(), parent.getWidth() - 8);
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
