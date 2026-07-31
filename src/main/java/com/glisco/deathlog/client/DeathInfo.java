package com.glisco.deathlog.client;

import com.glisco.deathlog.death_info.DeathInfoProperty;
import com.glisco.deathlog.death_info.DeathInfoPropertySerializer;
import com.glisco.deathlog.death_info.RestorableDeathInfoProperty;
import com.glisco.deathlog.death_info.properties.InventoryProperty;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;
import java.util.function.Consumer;

public class DeathInfo {

    public static final String COORDINATES_KEY = "coordinates";
    public static final String DIMENSION_KEY = "dimension";
    public static final String LOCATION_KEY = "location";
    public static final String SCORE_KEY = "score";
    public static final String DEATH_MESSAGE_KEY = "death_message";
    public static final String TIME_OF_DEATH_KEY = "time_of_death";
    public static final String INVENTORY_KEY = "inventory";

    private final Map<String, DeathInfoProperty> properties;

    public DeathInfo() {
        this.properties = new LinkedHashMap<>();
    }

    public static DeathInfo readFromNbt(NbtList nbt) {
        return readFromNbt(nbt, null);
    }

    public static DeathInfo readFromNbt(NbtList nbt, RegistryWrapper.WrapperLookup registries) {
        final DeathInfo deathInfo = new DeathInfo();
        nbt.forEach(element -> {
            final var parsed = DeathInfoPropertySerializer.load((NbtCompound) element, registries);
            deathInfo.setProperty(parsed.getRight(), parsed.getLeft());
        });
        return deathInfo;
    }

    public NbtList writeNbt() {
        return writeNbt(null);
    }

    public NbtList writeNbt(RegistryWrapper.WrapperLookup registries) {
        final NbtList nbt = new NbtList();
        properties.forEach((s, property) -> nbt.add(DeathInfoPropertySerializer.save(property, s, registries)));
        return nbt;
    }

    public static DeathInfo read(PacketByteBuf buffer) {
        return read(buffer, null);
    }

    public static DeathInfo read(PacketByteBuf buffer, RegistryWrapper.WrapperLookup registries) {
        var nbt = buffer.readNbt();
        if (nbt == null) return new DeathInfo();
        return readFromNbt(nbt.getList("DeathInfo", NbtElement.COMPOUND_TYPE), registries);
    }

    public void write(PacketByteBuf buffer) {
        write(buffer, null);
    }

    public void write(PacketByteBuf buffer, RegistryWrapper.WrapperLookup registries) {
        final var nbt = new NbtCompound();
        nbt.put("DeathInfo", writeNbt(registries));
        buffer.writeNbt(nbt);
    }

    public void writePartial(PacketByteBuf buffer) {
        writePartial(buffer, null);
    }

    public void writePartial(PacketByteBuf buffer, RegistryWrapper.WrapperLookup registries) {
        final var list = new NbtList();
        properties.forEach((s, property) -> {
            if (property instanceof InventoryProperty ) return;
            list.add(DeathInfoPropertySerializer.save(property, s, registries));
        });

        var nbt = new NbtCompound();
        nbt.put("DeathInfo", list);
        buffer.writeNbt(nbt);
    }

    public void restore(ServerPlayerEntity player) {
        properties.values().stream().filter(property -> property instanceof RestorableDeathInfoProperty).forEach(property -> ((RestorableDeathInfoProperty) property).restore(player));
    }

    public void setProperty(String property, DeathInfoProperty value) {
        this.properties.put(property, value);
    }

    public Optional<DeathInfoProperty> getProperty(String property) {
        return Optional.ofNullable(properties.get(property));
    }

    public boolean isPartial() {
        return getProperty(INVENTORY_KEY).isEmpty();
    }

    public Text getListName() {
        DeathInfoProperty property = getProperty(TIME_OF_DEATH_KEY).orElse(null);
        return property == null ? Text.translatable("text.deathlog.info.time_missing") : property.formatted();
    }

    public Text getTitle() {
        DeathInfoProperty property = getProperty(DEATH_MESSAGE_KEY).orElse(null);
        return property == null ? Text.translatable("text.deathlog.info.death_message_missing") : property.formatted();
    }

    public List<Text> getLeftColumnText() {
        final var texts = new ArrayList<Text>();
        iterateDisplayProperties(property -> texts.add(property.getName()));
        return texts;
    }

    public List<Text> getRightColumnText() {
        final var texts = new ArrayList<Text>();
        iterateDisplayProperties(property -> texts.add(property.formatted()));
        return texts;
    }

    public String createSearchString() {
        final StringBuilder builder = new StringBuilder();
        properties.forEach((s, property) -> builder.append(property.toSearchableString()));
        return builder.toString().toLowerCase();
    }

    private void iterateDisplayProperties(Consumer<DeathInfoProperty> callback) {
        properties.forEach((s, property) -> {
            if (!property.getType().displayedInInfoView()) return;

            callback.accept(property);
        });
    }

    public DefaultedList<ItemStack> getPlayerArmor() {
        var propertyOptional = getProperty(INVENTORY_KEY);
        if (propertyOptional.isEmpty()) return DefaultedList.of();
        return ((InventoryProperty) propertyOptional.get()).getPlayerArmor();
    }

    public DefaultedList<ItemStack> getPlayerItems() {
        var propertyOptional = getProperty(INVENTORY_KEY);
        if (propertyOptional.isEmpty()) return DefaultedList.of();
        return ((InventoryProperty) propertyOptional.get()).getPlayerItems();
    }
}
