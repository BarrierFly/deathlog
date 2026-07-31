package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.network.RemoteDeathLogStorage;
import com.glisco.deathlog.storage.DirectDeathLogStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public class DeathLogScreen extends Screen {
    private static final Identifier INVENTORY_TEXTURE = new Identifier("deathlog", "textures/gui/inventory_overlay.png");
    private final Screen parent;
    private final DirectDeathLogStorage storage;
    private DeathListWidget deathList;
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
        this.storage.getDeathInfoList().set(index, info);
    }

    @Override
    protected void init() {
        this.deathList = new DeathListWidget(client, 220, this.height, 32, this.height - 68, 30, storage);
        this.deathList.setLeftPos(10);
        if (!this.canRestore) this.deathList.restoreEnabled = false;
        this.addDrawableChild(deathList);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
                .dimensions(this.width / 2 - 50, this.height - 28, 100, 20).build());

        final TextFieldWidget searchField = new TextFieldWidget(textRenderer, 10, this.height - 63, 220, 20, Text.of(""));
        searchField.setChangedListener(s -> searchField.setEditableColor(deathList.filter(s) ? 0xFFFFFF : 0xFF2222));
        searchField.setText(this.storage.getDefaultFilter());
        this.addDrawableChild(searchField);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        final var originX = 230 + 30;
        final var originY = Math.min(this.height - 40, 300);

        if (deathList.getSelectedOrNull() != null) {
            DeathInfo info = deathList.getSelectedOrNull().getInfo();
            if (info.isPartial() && this.storage instanceof RemoteDeathLogStorage remote) {
                remote.fetchCompleteInfo(info);
                textRenderer.draw(matrices, Text.translatable("text.deathlog.death_info_loading"), originX, 16, 0xFFFFFF);
            } else {
                textRenderer.draw(matrices, info.getTitle(), originX, 16, 0xFFFFFF);
                final var left = info.getLeftColumnText();
                for (int i = 0; i < left.size(); i++)
                    textRenderer.draw(matrices, left.get(i), originX, 30 + 14 * i, 0xFFFFFF);
                final var right = info.getRightColumnText();
                for (int i = 0; i < right.size(); i++)
                    textRenderer.draw(matrices, right.get(i), originX + 100, 30 + 14 * i, 0xFFFFFF);

                RenderSystem.setShaderTexture(0, INVENTORY_TEXTURE);
                drawTexture(matrices, originX - 8, originY - 83, 0, 0, 210, 107);
                hoveredStack = null;
                for (int i = 0; i < info.getPlayerItems().size() - 1; i++) {
                    final ItemStack stack = info.getPlayerItems().get(i);
                    if (stack.isEmpty()) continue;
                    final var sx = originX + 18 * (i % 9);
                    final var sy = originY + (i < 9 ? 0 : -58 + 18 * (i / 9 - 1));
                    renderSlot(matrices, stack, sx, sy, mouseX, mouseY);
                }
                if (!info.getPlayerItems().get(36).isEmpty())
                    renderSlot(matrices, info.getPlayerItems().get(36), originX + 178, originY - 75, mouseX, mouseY);
                for (int i = 0; i < info.getPlayerArmor().size(); i++) {
                    final ItemStack stack = info.getPlayerArmor().get(i);
                    if (stack.isEmpty()) continue;
                    renderSlot(matrices, stack, originX + 178, originY - 18 * i, mouseX, mouseY);
                }
                if (hoveredStack != null)
                    renderTooltip(matrices, hoveredStack, mouseX, mouseY);
            }
        }
        super.render(matrices, mouseX, mouseY, delta);
        textRenderer.draw(matrices, Text.translatable("text.deathlog.death_list_title", storage.getDeathInfoList().size()), 16, this.height - 80, 0xFFFFFF);
    }

    private void renderSlot(MatrixStack m, ItemStack stack, int x, int y, int mx, int my) {
        if (mx > x && mx < x + 16 && my > y && my < y + 16) {
            fill(m, x, y, x + 16, y + 16, 0xFFBBBBBB);
            this.hoveredStack = stack.copy();
        }
        itemRenderer.renderGuiItemIcon(stack, x, y);
        itemRenderer.renderGuiItemOverlay(textRenderer, stack, x, y);
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
    public void close() { client.setScreen(parent); }
}
