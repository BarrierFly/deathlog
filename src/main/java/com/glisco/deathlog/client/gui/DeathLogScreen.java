package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.network.RemoteDeathLogStorage;
import com.glisco.deathlog.storage.DirectDeathLogStorage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class DeathLogScreen extends Screen {
    private static final Identifier INVENTORY_TEXTURE = Identifier.of("deathlog", "textures/gui/inventory_overlay.png");
    private static final int LIST_BACKGROUND_COLOR = 0xFF101010;
    private static final int TEXT_LINE_HEIGHT = 9;
    private static final float MIN_TEXT_SCALE = 0.6F;
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
        this.deathList.setX(10);
        this.deathList.refilter();
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
        final String defaultFilter = this.storage.getDefaultFilter();
        if (deathList.filter(defaultFilter)) {
            searchField.setText(defaultFilter);
        } else {
            deathList.filter("");
            searchField.setText("");
        }
        this.addDrawableChild(searchField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
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
                final var titleLines = drawTitleText(context, info.getTitle(), originX, 16);
                final var titleOffset = (titleLines - 1) * TEXT_LINE_HEIGHT;
                final var detailTop = 30 + titleOffset;
                final var left = info.getLeftColumnText();
                for (int i = 0; i < left.size(); i++)
                    context.drawText(textRenderer, left.get(i), originX, detailTop + 14 * i, 0xFFFFFF, false);
                final var right = info.getRightColumnText();
                for (int i = 0; i < right.size(); i++)
                    drawRightColumnText(context, right.get(i), originX + 100, detailTop + 14 * i);

                final var originY = Math.min(this.height - 40, 121 + titleOffset + 14 * Math.max(left.size(), right.size()));
                context.drawTexture(INVENTORY_TEXTURE, originX - 8, originY - 83, 0, 0, 210, 107, 256, 256);
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
        context.drawItem(stack, x, y);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (hoveredStack != null && button == 2) {
            performItemAction();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (hoveredStack != null && client.options.dropKey.matchesKey(keyCode, scanCode)) {
            performItemAction();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void performItemAction() {
        if (hoveredStack == null) return;
        if (client.player.isCreative()) {
            client.interactionManager.dropCreativeStack(hoveredStack);
        } else {
            var cmd = new StringBuilder("/give ").append(client.player.getName().getString()).append(" ");
            cmd.append(Registries.ITEM.getId(hoveredStack.getItem()));
            var nbt = new NbtCompound();
            hoveredStack.encode(client.world.getRegistryManager(), nbt);
            cmd.append(nbt);
            client.keyboard.setClipboard(cmd.toString());
        }
    }

    private int drawTitleText(DrawContext context, Text text, int x, int y) {
        int maxWidth = Math.max(50, this.width - x - 10);
        var lines = textRenderer.wrapLines(text, maxWidth);
        for (int i = 0; i < lines.size(); i++) {
            context.drawText(textRenderer, lines.get(i), x, y + i * TEXT_LINE_HEIGHT, 0xFFFFFFFF, false);
        }
        return Math.max(1, lines.size());
    }

    private void drawRightColumnText(DrawContext context, Text text, int x, int y) {
        int maxWidth = Math.max(50, this.width - x - 10);
        int textWidth = textRenderer.getWidth(text);
        if (textWidth <= maxWidth) {
            context.drawText(textRenderer, text, x, y, 0xFFFFFFFF, false);
            return;
        }

        float fitScale = (float) maxWidth / textWidth;
        if (fitScale >= MIN_TEXT_SCALE) {
            drawScaledText(context, text.asOrderedText(), x, y, fitScale, 0xFFFFFFFF);
            return;
        }

        int wrapWidth = Math.max(1, (int) Math.ceil(maxWidth / MIN_TEXT_SCALE));
        var lines = textRenderer.wrapLines(text, wrapWidth);
        int lineSpacing = Math.max(1, Math.min(scaledLineHeight(MIN_TEXT_SCALE), 13 / Math.max(1, lines.size() - 1)));
        for (int i = 0; i < lines.size(); i++) {
            drawScaledText(context, lines.get(i), x, y + i * lineSpacing, MIN_TEXT_SCALE, 0xFFFFFFFF);
        }
    }

    private void drawScaledText(DrawContext context, OrderedText text, int x, int y, float scale, int color) {
        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, 1.0F);
        context.drawText(textRenderer, text, (int) (x / scale), (int) (y / scale), color, false);
        context.getMatrices().pop();
    }

    private int scaledLineHeight(float scale) {
        return Math.max(1, Math.round(TEXT_LINE_HEIGHT * scale));
    }

    @Override
    public void close() { client.setScreen(parent); }
}
