package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathInfo;
import com.glisco.deathlog.network.RemoteDeathLogStorage;
import com.glisco.deathlog.storage.DirectDeathLogStorage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DeathLogScreen extends Screen {
    private static final Identifier INVENTORY_TEXTURE = Identifier.of("deathlog", "textures/gui/inventory_overlay.png");
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        final var originX = 230 + 30;
        final var originY = Math.min(this.height - 40, 300);

        if (deathList.getSelectedOrNull() != null) {
            DeathInfo info = deathList.getSelectedOrNull().getInfo();
            if (info.isPartial() && this.storage instanceof RemoteDeathLogStorage remote) {
                remote.fetchCompleteInfo(info);
                context.drawText(textRenderer, Text.translatable("text.deathlog.death_info_loading"), originX, 16, 0xFFFFFF, false);
            } else {
                context.drawText(textRenderer, info.getTitle(), originX, 16, 0xFFFFFF, false);
                final var left = info.getLeftColumnText();
                for (int i = 0; i < left.size(); i++)
                    context.drawText(textRenderer, left.get(i), originX, 30 + 14 * i, 0xFFFFFF, false);
                final var right = info.getRightColumnText();
                for (int i = 0; i < right.size(); i++)
                    context.drawText(textRenderer, right.get(i), originX + 100, 30 + 14 * i, 0xFFFFFF, false);

                context.drawTexture(RenderPipelines.GUI_TEXTURED, INVENTORY_TEXTURE, originX - 8, originY - 83, 0, 0, 210, 107, 210, 107);
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
                    context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
                }
            }
        }
        super.render(context, mouseX, mouseY, delta);
        context.drawText(textRenderer, Text.translatable("text.deathlog.death_list_title", storage.getDeathInfoList().size()), 16, this.height - 80, 0xFFFFFF, false);
    }

    private void renderSlot(DrawContext context, ItemStack stack, int x, int y, int mx, int my) {
        if (mx > x && mx < x + 16 && my > y && my < y + 16) {
            context.fill(x, y, x + 16, y + 16, 0xFFBBBBBB);
            this.hoveredStack = stack.copy();
        }
        context.drawItem(stack, x, y);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (hoveredStack != null && click.button() == 2) {
            if (client.player.isCreative()) {
                client.interactionManager.dropCreativeStack(hoveredStack);
            } else {
                var cmd = new StringBuilder("/give ").append(client.player.getName().getString()).append(" ");
                cmd.append(Registries.ITEM.getId(hoveredStack.getItem()));
                if (client.world != null) {
                    var ops = client.world.getRegistryManager().getOps(NbtOps.INSTANCE);
                    var encodedNbt = ItemStack.CODEC.encodeStart(ops, hoveredStack).result().orElseGet(NbtCompound::new);
                    cmd.append(encodedNbt);
                }
                client.keyboard.setClipboard(cmd.toString());
            }
            return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public void close() { client.setScreen(parent); }
}
