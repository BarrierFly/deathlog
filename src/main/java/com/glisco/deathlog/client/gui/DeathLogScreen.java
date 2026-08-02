package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.network.RemoteDeathLogStorage;
import com.glisco.deathlog.storage.DirectDeathLogStorage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class DeathLogScreen extends Screen {
    private static final Identifier INVENTORY_TEXTURE = new Identifier("deathlog", "textures/gui/inventory_overlay.png");
    private static final int LIST_BACKGROUND_COLOR = 0xFF101010;
    private static final int SCROLL_TEXT_HEIGHT = 9;
    private static final float SMALL_TEXT_SCALE = 0.5F;
    private static final float SCROLL_CHARS_PER_SECOND = 3.0F;
    private final Screen parent;
    private final DirectDeathLogStorage storage;
    private DeathListWidget deathList;
    private ButtonWidget restoreButton;
    private ButtonWidget deleteButton;
    private ItemStack hoveredStack = null;
    private boolean canRestore = true;

    public DeathLogScreen(Screen parent, DirectDeathLogStorage storage) {
        super(Text.of("Death Log"));
        this.parent = parent;
        this.storage = storage;
    }

    public void disableRestoring() {
        this.canRestore = false;
        if (this.deathList != null) this.deathList.restoreEnabled = false;
    }

    public void updateInfo(DeathInfo info, int index) {
        var selected = this.deathList.getSelectedOrNull();
        final int selectedIndex = selected != null ? this.storage.getDeathInfoList().indexOf(selected.getInfo()) : -1;
        this.storage.getDeathInfoList().set(index, info);
        if (selected != null && selectedIndex == index) selected.updateInfo(info);
    }

    @Override
    protected void init() {
        this.deathList = new DeathListWidget(client, 220, this.height, 32, this.height - 68, 30, storage);
        if (!this.canRestore) this.deathList.restoreEnabled = false;
        this.addDrawableChild(deathList);

        final var originX = 230 + 30;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
                .dimensions(originX - 120, this.height - 28, 100, 20).build());

        this.restoreButton = ButtonWidget.builder(Text.translatable("text.deathlog.action.restore"), button -> restoreSelected())
                .dimensions(originX, this.height - 28, 90, 20).build();
        this.deleteButton = ButtonWidget.builder(Text.translatable("text.deathlog.action.delete"), button -> deleteSelected())
                .dimensions(originX + 96, this.height - 28, 90, 20).build();
        this.addDrawableChild(restoreButton);
        this.addDrawableChild(deleteButton);

        final TextFieldWidget searchField = new TextFieldWidget(textRenderer, 10, this.height - 63, 220, 20, Text.of(""));
        searchField.setChangedListener(s -> searchField.setEditableColor(deathList.filter(s) ? 0xFFFFFF : 0xFF2222));
        searchField.setText(this.storage.getDefaultFilter());
        this.addDrawableChild(searchField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.fill(10, 32, 230, this.height - 68, LIST_BACKGROUND_COLOR);

        final var hasSelection = deathList.getSelectedOrNull() != null;
        restoreButton.visible = hasSelection && canRestore;
        deleteButton.visible = hasSelection;
        super.render(context, mouseX, mouseY, delta);

        final var originX = 230 + 30;

        if (deathList.getSelectedOrNull() != null) {
            DeathInfo info = deathList.getSelectedOrNull().getInfo();
            if (info.isPartial() && this.storage instanceof RemoteDeathLogStorage remote) {
                remote.fetchCompleteInfo(info);
                context.drawText(textRenderer, Text.translatable("text.deathlog.death_info_loading"), originX, 16, 0xFFFFFF, false);
            } else {
                drawTitleText(context, info.getTitle(), originX, 16);
                final var left = info.getLeftColumnText();
                for (int i = 0; i < left.size(); i++)
                    context.drawText(textRenderer, left.get(i), originX, 30 + 14 * i, 0xFFFFFF, false);
                final var right = info.getRightColumnText();
                for (int i = 0; i < right.size(); i++)
                    drawRightColumnText(context, right.get(i), originX + 100, 30 + 14 * i);

                final var originY = Math.min(this.height - 40, 121 + 14 * Math.max(left.size(), right.size()));
                context.drawTexture(INVENTORY_TEXTURE, originX - 8, originY - 83, 0, 0, 210, 107, 210, 107);
                hoveredStack = null;
                for (int i = 0; i < info.getPlayerItems().size() - 1; i++) {
                    final ItemStack stack = info.getPlayerItems().get(i);
                    if (stack.isEmpty()) continue;
                    final var sx = originX + 18 * (i % 9);
                    final var sy = originY + (i < 9 ? 0 : -58 + 18 * (i / 9 - 1));
                    renderSlot(context, stack, sx, sy, mouseX, mouseY);
                }
                if (!info.getPlayerItems().get(36).isEmpty())
                    renderSlot(context, info.getPlayerItems().get(36), originX + 178, originY - 75, mouseX, mouseY);
                for (int i = 0; i < info.getPlayerArmor().size(); i++) {
                    final ItemStack stack = info.getPlayerArmor().get(i);
                    if (stack.isEmpty()) continue;
                    renderSlot(context, stack, originX + 178, originY - 18 * i, mouseX, mouseY);
                }
                if (hoveredStack != null) {
                    var tooltip = Screen.getTooltipFromItem(this.client, hoveredStack);
                    var actionTooltip = new ArrayList<>(tooltip);
                    actionTooltip.add(Text.translatable(this.client.player.isCreative() ? "text.deathlog.action.give_item.spawn" : "text.deathlog.action.give_item.copy_give").formatted(net.minecraft.util.Formatting.GRAY));
                    context.drawTooltip(textRenderer, actionTooltip, mouseX, mouseY);
                }
            }
        }
        context.drawText(textRenderer, Text.translatable("text.deathlog.death_list_title", storage.getDeathInfoList().size()), 16, this.height - 80, 0xFFFFFF, false);
    }

    private void restoreSelected() {
        var selected = deathList.getSelectedOrNull();
        if (selected == null) return;
        final int index = storage.getDeathInfoList().indexOf(selected.getInfo());
        if (index >= 0) storage.restore(index);
    }

    private void deleteSelected() {
        var selected = deathList.getSelectedOrNull();
        if (selected == null) return;
        storage.delete(selected.getInfo());
        deathList.refilter();
    }

    private void renderSlot(DrawContext context, ItemStack stack, int x, int y, int mx, int my) {
        if (mx > x && mx < x + 16 && my > y && my < y + 16) {
            context.fill(x, y, x + 16, y + 16, 0xFFBBBBBB);
            this.hoveredStack = stack.copy();
        }
        context.drawItemInSlot(textRenderer, stack, x, y);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (hoveredStack != null && button == 2) {
            if (client.player.isCreative()) {
                client.interactionManager.dropCreativeStack(hoveredStack);
            } else {
                var cmd = new StringBuilder("/give ").append(client.player.getName().getString()).append(" ");
                cmd.append(Registries.ITEM.getId(hoveredStack.getItem()));
                cmd.append(hoveredStack.getOrCreateNbt().toString());
                client.keyboard.setClipboard(cmd.toString());
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (hoveredStack != null && client.options.dropKey.matchesKey(keyCode, scanCode)) {
            dropHoveredItem();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void dropHoveredItem() {
        if (hoveredStack == null) return;
        client.player.dropItem(hoveredStack.copy(), false);
    }

    private void drawTitleText(DrawContext context, Text text, int x, int y) {
        int maxWidth = Math.max(50, this.width - x - 10);
        if (textRenderer.getWidth(text) <= maxWidth) {
            context.drawText(textRenderer, text, x, y, 0xFFFFFFFF, false);
        } else {
            drawScrollingText(context, text, x, y, maxWidth, 0xFFFFFFFF);
        }
    }

    private void drawRightColumnText(DrawContext context, Text text, int x, int y) {
        int maxWidth = Math.max(50, this.width - x - 10);
        if (textRenderer.getWidth(text) <= maxWidth) {
            context.drawText(textRenderer, text, x, y, 0xFFFFFFFF, false);
            return;
        }

        var lines = textRenderer.wrapLines(text, maxWidth * 2);
        if (lines.size() <= 2) {
            for (int i = 0; i < lines.size(); i++) {
                drawSmallText(context, lines.get(i), x, y + i * 4, 0xFFFFFFFF);
            }
        } else {
            drawScrollingText(context, text, x, y, maxWidth, 0xFFFFFFFF);
        }
    }

    private void drawScrollingText(DrawContext context, Text text, int x, int y, int maxWidth, int color) {
        int textWidth = textRenderer.getWidth(text);
        int speed = (int) (textRenderer.getWidth(" ") * SCROLL_CHARS_PER_SECOND);
        int period = Math.max(1, textWidth + maxWidth);
        int offset = (int) ((System.currentTimeMillis() / 1000.0 * speed) % period);
        context.enableScissor(x, y, maxWidth, SCROLL_TEXT_HEIGHT);
        context.drawText(textRenderer, text, x - offset, y, color, false);
        context.drawText(textRenderer, text, x - offset + period, y, color, false);
        context.disableScissor();
    }

    private void drawSmallText(DrawContext context, OrderedText text, int x, int y, int color) {
        context.getMatrices().push();
        context.getMatrices().scale(SMALL_TEXT_SCALE, SMALL_TEXT_SCALE, 1.0F);
        context.drawText(textRenderer, text, (int) (x / SMALL_TEXT_SCALE), (int) (y / SMALL_TEXT_SCALE), color, false);
        context.getMatrices().pop();
    }

    @Override
    public void close() { client.setScreen(parent); }
}
