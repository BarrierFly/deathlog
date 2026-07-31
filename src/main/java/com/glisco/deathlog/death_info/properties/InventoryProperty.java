package com.glisco.deathlog.death_info.properties;

import com.glisco.deathlog.death_info.DeathInfoPropertyType;
import com.glisco.deathlog.death_info.RestorableDeathInfoProperty;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

public class InventoryProperty implements RestorableDeathInfoProperty {

    private final DefaultedList<ItemStack> playerItems;
    private final DefaultedList<ItemStack> playerArmor;

    public InventoryProperty(DefaultedList<ItemStack> playerItems, DefaultedList<ItemStack> playerArmor) {
        this.playerItems = playerItems;
        this.playerArmor = playerArmor;
    }

    public InventoryProperty(PlayerInventory playerInventory) {
        this.playerItems = DefaultedList.ofSize(37, ItemStack.EMPTY);
        this.playerArmor = DefaultedList.ofSize(4, ItemStack.EMPTY);

        for (int i = 0; i < 36; i++) playerItems.set(i, playerInventory.getStack(i).copy());
        for (int i = 0; i < 4; i++) playerArmor.set(i, playerInventory.getStack(36 + i).copy());
        playerItems.set(36, playerInventory.getStack(PlayerInventory.OFF_HAND_SLOT).copy());
    }

    @Override
    public DeathInfoPropertyType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public Text formatted() {
        return null;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        writeNbt(nbt, null);
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        final NbtList armorNbt = new NbtList();
        playerArmor.forEach(stack -> armorNbt.add(encodeStack(stack, registries)));
        nbt.put("Armor", armorNbt);

        final NbtList inventoryNbt = new NbtList();
        playerItems.forEach(stack -> inventoryNbt.add(encodeStack(stack, registries)));
        nbt.put("Items", inventoryNbt);
    }

    @Override
    public String toSearchableString() {
        StringBuilder builder = new StringBuilder();

        playerItems.forEach(stack -> builder.append(stack.getName().getString()));
        playerArmor.forEach(stack -> builder.append(stack.getName().getString()));

        return builder.toString();
    }

    @Override
    public void restore(ServerPlayerEntity player) {
        final var inventory = player.getInventory();
        inventory.clear();

        for (int i = 0; i < 4; i++) inventory.setStack(36 + i, playerArmor.get(i).copy());
        for (int i = 0; i < 36; i++) inventory.setStack(i, playerItems.get(i).copy());
        inventory.setStack(PlayerInventory.OFF_HAND_SLOT, playerItems.get(36).copy());
    }

    public DefaultedList<ItemStack> getPlayerArmor() {
        return playerArmor;
    }

    public DefaultedList<ItemStack> getPlayerItems() {
        return playerItems;
    }

    private static NbtElement encodeStack(ItemStack stack, RegistryWrapper.WrapperLookup registries) {
        if (stack.isEmpty()) return new NbtCompound();
        return ItemStack.CODEC.encodeStart(getRegistryOps(registries), stack).result().orElseGet(NbtCompound::new);
    }

    private static ItemStack decodeStack(NbtElement element, RegistryWrapper.WrapperLookup registries) {
        return ItemStack.CODEC.parse(getRegistryOps(registries), element).result().orElse(ItemStack.EMPTY);
    }

    private static RegistryOps<NbtElement> getRegistryOps(RegistryWrapper.WrapperLookup registries) {
        if (registries != null) return registries.getOps(NbtOps.INSTANCE);
        return DynamicRegistryManager.EMPTY.getOps(NbtOps.INSTANCE);
    }

    public static class Type extends DeathInfoPropertyType<InventoryProperty> {

        public static final Type INSTANCE = new Type();

        private Type() {
            super("deathlog.deathinfoproperty.inventory", "inventory");
        }

        @Override
        public boolean displayedInInfoView() {
            return false;
        }

        @Override
        public InventoryProperty readFromNbt(NbtCompound nbt) {
            return readFromNbt(nbt, null);
        }

        @Override
        public InventoryProperty readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {

            final NbtList armorNbt = nbt.getListOrEmpty("Armor");
            final var armorList = DefaultedList.ofSize(4, ItemStack.EMPTY);
            for (int i = 0; i < armorNbt.size(); i++) {
                armorList.set(i, decodeStack(armorNbt.getCompoundOrEmpty(i), registries));
            }

            final NbtList itemNbt = nbt.getListOrEmpty("Items");
            final var itemList = DefaultedList.ofSize(37, ItemStack.EMPTY);
            for (int i = 0; i < itemNbt.size(); i++) {
                itemList.set(i, decodeStack(itemNbt.getCompoundOrEmpty(i), registries));
            }

            return new InventoryProperty(itemList, armorList);
        }
    }
}
