package com.glisco.deathlog.client.gui;

import com.glisco.deathlog.mixin.SystemToastAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class DeathLogToast extends SystemToast {

    public DeathLogToast(Type type, Text title, @Nullable Text description) {
        super(type, title, description);
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        super.draw(context, textRenderer, startTime);
    }

    @Override
    public Visibility getVisibility() {
        return System.currentTimeMillis() - ((SystemToastAccessor) this).deathlog_getStartTime() < 10000L ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }
}
