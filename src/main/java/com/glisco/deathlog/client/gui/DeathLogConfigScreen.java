package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.client.DeathLogClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.BooleanSupplier;

public class DeathLogConfigScreen extends Screen {

    private static final int OPTION_WIDTH = 310;
    private static final int OPTION_HEIGHT = 20;

    private final Screen parent;

    public DeathLogConfigScreen(Screen parent) {
        super(Text.translatable("text.config.deathlog.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = Math.max(40, this.height / 4 - 20);

        this.addDrawableChild(toggleButton(Text.translatable("text.config.deathlog.option.screenshotsEnabled"), centerX, y, () -> DeathLogClient.CONFIG.screenshotsEnabled, value -> DeathLogClient.CONFIG.screenshotsEnabled = value, null));
        y += OPTION_HEIGHT + 6;

        this.addDrawableChild(toggleButton(Text.translatable("text.config.deathlog.option.useLegacyDeathDetection"), centerX, y, () -> DeathLogClient.CONFIG.useLegacyDeathDetection, value -> DeathLogClient.CONFIG.useLegacyDeathDetection = value, Tooltip.of(Text.translatable("text.config.deathlog.option.useLegacyDeathDetection.tooltip"))));

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
                .dimensions(centerX - 100, this.height - 28, 200, 20)
                .build());
    }

    private ButtonWidget toggleButton(Text label, int centerX, int y, BooleanSupplier getter, Consumer<Boolean> setter, Tooltip tooltip) {
        final var button = ButtonWidget.builder(message(label, getter.getAsBoolean()), widget -> {
            setter.accept(!getter.getAsBoolean());
            widget.setMessage(message(label, getter.getAsBoolean()));
        }).dimensions(centerX - OPTION_WIDTH / 2, y, OPTION_WIDTH, OPTION_HEIGHT).build();
        if (tooltip != null) button.setTooltip(tooltip);
        return button;
    }

    private static Text message(Text label, boolean value) {
        return label.copy().append(": ").append(Text.translatable(value ? "options.on" : "options.off"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Screen.renderWithTooltip already renders the background (and blur) once per frame;
        // calling renderBackground here again trips the "Can only blur once per frame" check.
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        DeathLogClient.CONFIG.save();
        this.client.setScreen(this.parent);
    }
}
