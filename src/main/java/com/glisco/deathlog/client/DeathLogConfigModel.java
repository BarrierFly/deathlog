package com.glisco.deathlog.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DeathLogConfigModel {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("deathlog.json");
    private static final Logger LOGGER = LoggerFactory.getLogger(DeathLogConfigModel.class);

    public boolean screenshotsEnabled = false;
    public boolean useLegacyDeathDetection = false;

    public static DeathLogConfigModel load() {
        if (!Files.exists(PATH)) return new DeathLogConfigModel();

        try {
            final var loaded = GSON.fromJson(Files.readString(PATH), DeathLogConfigModel.class);
            return loaded != null ? loaded : new DeathLogConfigModel();
        } catch (IOException | JsonParseException e) {
            LOGGER.warn("Failed to read deathlog.json, falling back to default config", e);
            return new DeathLogConfigModel();
        }
    }

    public void save() {
        try {
            Files.writeString(PATH, GSON.toJson(this));
        } catch (IOException e) {
            LOGGER.warn("Failed to save deathlog.json", e);
        }
    }
}
