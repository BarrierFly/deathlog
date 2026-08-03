package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.client.DeathLogClient;
import com.glisco.deathlog.network.RemoteDeathLogStorage;
import com.glisco.deathlog.storage.DirectDeathLogStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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
        this.deathList.setLeftPos(10);
        if (!this.canRestore) this.deathList.restoreEnabled = false;
        this.addDrawableChild(deathList);

        final var originX = 230 + 30;
        this.addDrawableChild(new ButtonWidget(originX - 120, this.height - 28, 100, 20,
                Text.translatable("gui.done"), button -> this.close()));

        this.restoreButton = new ButtonWidget(originX, this.height - 28, 90, 20,
                Text.translatable("text.deathlog.action.restore"), button -> restoreSelected());
        this.deleteButton = new ButtonWidget(originX + 96, this.height - 28, 90, 20,
                Text.translatable("text.deathlog.action.delete"), button -> deleteSelected());
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
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.renderBackground(matrices, 0);

        fill(matrices, 10, 32, 230, this.height - 68, LIST_BACKGROUND_COLOR);

        final var hasSelection = deathList.getSelectedOrNull() != null;
        restoreButton.visible = hasSelection && canRestore;
        deleteButton.visible = hasSelection;
        super.render(matrices, mouseX, mouseY, delta);

        final var originX = 230 + 30;

        if (deathList.getSelectedOrNull() != null) {
            DeathInfo info = deathList.getSelectedOrNull().getInfo();

            if (info.isPartial() && this.storage instanceof RemoteDeathLogStorage remote) {
                remote.fetchCompleteInfo(info);
                textRenderer.draw(matrices, Text.translatable("text.deathlog.death_info_loading"), originX, 16, 0xFFFFFF);
            } else {
                drawTitleText(matrices, info.getTitle(), originX, 16);

                final var leftColumnText = info.getLeftColumnText();
                for (int i = 0; i < leftColumnText.size(); i++) {
                    textRenderer.draw(matrices, leftColumnText.get(i), originX, 30 + 14 * i, 0xFFFFFF);
                }

                final var rightColumnText = info.getRightColumnText();
                for (int i = 0; i < rightColumnText.size(); i++) {
                    drawRightColumnText(matrices, rightColumnText.get(i), originX + 100, 30 + 14 * i);
                }

                final var originY = Math.min(this.height - 40, 121 + 14 * Math.max(leftColumnText.size(), rightColumnText.size()));
                RenderSystem.setShaderTexture(0, INVENTORY_TEXTURE);
                drawTexture(matrices, originX - 8, originY - 83, 0, 0, 210, 107);

                hoveredStack = null;

                for (int i = 0; i < info.getPlayerItems().size() - 1; i++) {
                    final ItemStack stack = info.getPlayerItems().get(i);
                    if (stack.isEmpty()) continue;
                    final var slotX = originX + 18 * (i % 9);
                    final var slotY = originY + (i < 9 ? 0 : -58 + 18 * (i / 9 - 1));
                    renderSlotWithPossibleTooltip(matrices, stack, slotX, slotY, mouseX, mouseY);
                }

                if (!info.getPlayerItems().get(36).isEmpty()) {
                    renderSlotWithPossibleTooltip(matrices, info.getPlayerItems().get(36), originX + 178, originY - 75, mouseX, mouseY);
                }

                for (int i = 0; i < info.getPlayerArmor().size(); i++) {
                    final ItemStack stack = info.getPlayerArmor().get(i);
                    if (stack.isEmpty()) continue;
                    renderSlotWithPossibleTooltip(matrices, stack, originX + 178, originY - 18 * i, mouseX, mouseY);
                }

                if (hoveredStack != null) {
                    var tooltip = new ArrayList<>(this.getTooltipFromItem(hoveredStack));
                    tooltip.add(Text.translatable(this.client.player.isCreative() ? "text.deathlog.action.give_item.spawn" : "text.deathlog.action.give_item.copy_give").formatted(Formatting.GRAY));
                    renderTooltip(matrices, tooltip, mouseX, mouseY);
                }
            }
        }

        textRenderer.draw(matrices, Text.translatable("text.deathlog.death_list_title", storage.getDeathInfoList().size()),
                16, this.height - 80, 0xFFFFFF);
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredStack != null && button == 2) {
            if (client.player.isCreative()) {
                client.interactionManager.dropCreativeStack(hoveredStack);
            } else {
                var command = new StringBuilder("/give ");
                command.append(client.player.getName().getString());
                command.append(" ");
                command.append(Registry.ITEM.getId(hoveredStack.getItem()));
                command.append(hoveredStack.getOrCreateNbt().toString());
                client.keyboard.setClipboard(command.toString());
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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

    private void drawTitleText(MatrixStack matrices, Text text, int x, int y) {
        int maxWidth = Math.max(50, this.width - x - 10);
        if (textRenderer.getWidth(text) <= maxWidth) {
            textRenderer.draw(matrices, text, x, y, 0xFFFFFF);
        } else {
            drawScrollingText(matrices, text, x, y, maxWidth, 0xFFFFFF);
        }
    }

    private void drawRightColumnText(MatrixStack matrices, Text text, int x, int y) {
        int maxWidth = Math.max(50, this.width - x - 10);
        if (textRenderer.getWidth(text) <= maxWidth) {
            textRenderer.draw(matrices, text, x, y, 0xFFFFFF);
            return;
        }

        var lines = textRenderer.wrapLines(text, maxWidth * 2);
        if (lines.size() <= 2) {
            for (int i = 0; i < lines.size(); i++) {
                drawSmallText(matrices, lines.get(i), x, y + i * 4, 0xFFFFFF);
            }
        } else {
            drawScrollingText(matrices, text, x, y, maxWidth, 0xFFFFFF);
        }
    }

    private void drawScrollingText(MatrixStack matrices, Text text, int x, int y, int maxWidth, int color) {
        int textWidth = textRenderer.getWidth(text);
        int speed = (int) (textRenderer.getWidth(" ") * SCROLL_CHARS_PER_SECOND);
        int period = Math.max(1, textWidth + maxWidth);
        int offset = (int) ((System.currentTimeMillis() / 1000.0 * speed) % period);
        RenderSystem.enableScissor(x, y, maxWidth, SCROLL_TEXT_HEIGHT);
        textRenderer.draw(matrices, text, x - offset, y, color);
        textRenderer.draw(matrices, text, x - offset + period, y, color);
        RenderSystem.disableScissor();
    }

    private void drawSmallText(MatrixStack matrices, OrderedText text, int x, int y, int color) {
        matrices.push();
        matrices.scale(SMALL_TEXT_SCALE, SMALL_TEXT_SCALE, 1.0F);
        textRenderer.draw(matrices, text, x / SMALL_TEXT_SCALE, y / SMALL_TEXT_SCALE, color);
        matrices.pop();
    }

    private void renderSlotWithPossibleTooltip(MatrixStack matrices, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        if (mouseX > x && mouseX < x + 16 && mouseY > y && mouseY < y + 16) {
            fill(matrices, x, y, x + 16, y + 16, 0xFFBBBBBB);
            this.hoveredStack = stack.copy();
        }
        itemRenderer.renderGuiItemIcon(stack, x, y);
        itemRenderer.renderGuiItemOverlay(textRenderer, stack, x, y);
    }
}
