package com.glisco.deathlog.client;

import com.glisco.deathlog.client.gui.DeathLogConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DeathLogModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<DeathLogConfigScreen> getModConfigScreenFactory() {
        return DeathLogConfigScreen::new;
    }
}
