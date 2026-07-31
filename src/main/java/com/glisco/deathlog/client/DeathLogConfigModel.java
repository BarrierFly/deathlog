package com.glisco.deathlog.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DeathLogConfigModel {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("deathlog.json");

    public boolean screenshotsEnabled = false;
    public boolean useLegacyDeathDetection = false;

    public static DeathLogConfigModel load() {
        if (Files.exists(PATH)) {
            try { return GSON.fromJson(Files.readString(PATH), DeathLogConfigModel.class); }
            catch (IOException e) { return new DeathLogConfigModel(); }
        }
        return new DeathLogConfigModel();
    }

    public void save() {
        try { Files.writeString(PATH, GSON.toJson(this)); }
        catch (IOException ignored) {}
    }
}
