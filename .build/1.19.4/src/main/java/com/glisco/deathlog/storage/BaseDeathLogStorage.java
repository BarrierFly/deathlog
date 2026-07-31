package com.glisco.deathlog.storage;

import com.glisco.deathlog.client.DeathInfo;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class BaseDeathLogStorage implements DeathLogStorage {

    private static final int FORMAT_REVISION = 2;
    public static final Logger LOGGER = LogManager.getLogger();

    private boolean errored = false;
    private String errorCondition = "";

    protected CompletableFuture<List<DeathInfo>> load(File file) {
        final var future = new CompletableFuture<List<DeathInfo>>();
        Util.getIoWorkerExecutor().submit(() -> {
            if (errored) {
                LOGGER.warn("Attempted to load DeathLog database even though disk operations are disabled");
                future.complete(new ArrayList<>());
                return;
            }

            NbtCompound deathNbt;

            if (file.exists()) {
                try {
                    deathNbt = NbtIo.read(file);

                    if (deathNbt.getInt("FormatRevision") != FORMAT_REVISION) {
                        LOGGER.warn("DeathLog database format revision is {} but this version expects {}. Data will be loaded but may be incomplete.",
                                deathNbt.getInt("FormatRevision"), FORMAT_REVISION);
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to read DeathLog database file, attempting recovery", e);

                    // Back up the corrupted file so the user doesn't lose all data
                    try {
                        Path backupPath = file.toPath().resolveSibling(file.getName() + ".corrupted_backup");
                        Files.move(file.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);
                        LOGGER.warn("Corrupted DeathLog database backed up to {}", backupPath);
                    } catch (IOException backupException) {
                        LOGGER.error("Failed to back up corrupted database file", backupException);
                    }

                    // Return empty list instead of failing completely — the mod will
                    // start fresh and save a new file on the next death event.
                    future.complete(new ArrayList<>());
                    return;
                } catch (Exception e) {
                    LOGGER.error("Unexpected error while reading DeathLog database, starting fresh", e);
                    future.complete(new ArrayList<>());
                    return;
                }
            } else {
                deathNbt = new NbtCompound();
            }

            final var list = new ArrayList<DeathInfo>();
            final NbtList infoList = deathNbt.getList("Deaths", NbtElement.LIST_TYPE);
            for (int i = 0; i < infoList.size(); i++) {
                try {
                    list.add(DeathInfo.readFromNbt(infoList.getList(i)));
                } catch (Exception e) {
                    LOGGER.error("Failed to decode death info entry #{}, skipping", i, e);
                }
            }

            future.complete(list);
        });

        return future;
    }

    protected void save(File file, List<DeathInfo> listIn) {
        final var list = ImmutableList.copyOf(listIn);
        Util.getIoWorkerExecutor().submit(() -> {
            if (errored) {
                LOGGER.warn("Attempted to save DeathLog database even though disk operations are disabled");
                return;
            }

            final NbtCompound deathNbt = new NbtCompound();
            final NbtList infoList = new NbtList();

            list.forEach(deathInfo -> infoList.add(deathInfo.writeNbt()));

            deathNbt.put("Deaths", infoList);
            deathNbt.putInt("FormatRevision", FORMAT_REVISION);

            // Atomic save: write to a temporary file first, then rename.
            // This prevents file corruption if the game crashes during the write.
            File tempFile = new File(file.getParentFile(), file.getName() + ".tmp");
            try {
                NbtIo.write(deathNbt, tempFile);
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                LOGGER.error("Failed to save DeathLog database", e);
                // Clean up temp file if it was left behind
                try {
                    Files.deleteIfExists(tempFile.toPath());
                } catch (IOException ignored) {
                }
            }
        });
    }

    @Override
    public boolean isErrored() {
        return errored;
    }

    @Override
    public String getErrorCondition() {
        return errorCondition;
    }

    protected void raiseError(String error) {
        this.errored = true;
        this.errorCondition = error;
    }

}
