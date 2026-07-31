package com.glisco.deathlog.death_info.properties;

import com.glisco.deathlog.death_info.DeathInfoPropertyType;
import com.glisco.deathlog.death_info.RestorableDeathInfoProperty;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public class InventoryProperty implements RestorableDeathInfoProperty {

    private static final StructEndec<InventoryProperty> ENDEC = StructEndecBuilder.of(
            defaulted(MinecraftEndecs.ITEM_STACK.listOf()).fieldOf("items", s -> s.playerItems),
            defaulted(MinecraftEndecs.ITEM_STACK.listOf()).fieldOf("armor", s -> s.playerArmor),
            (playerItems, playerArmor) -> new InventoryProperty(playerItems, playerArmor)
    );

    private final DefaultedList<ItemStack> playerItems;
    private final DefaultedList<ItemStack> playerArmor;

    public InventoryProperty(DefaultedList<ItemStack> playerItems, DefaultedList<ItemStack> playerArmor) {
        this.playerItems = playerItems;
        this.playerArmor = playerArmor;
    }

    public InventoryProperty(PlayerInventory playerInventory) {
        this.playerItems = DefaultedList.ofSize(37, ItemStack.EMPTY);
        this.playerArmor = DefaultedList.ofSize(4, ItemStack.EMPTY);

        for (int i = 0; i < 36; i++) {
            playerItems.set(i, playerInventory.getStack(i).copy());
        }
        playerArmor.set(0, playerInventory.player.getEquippedStack(EquipmentSlot.FEET).copy());
        playerArmor.set(1, playerInventory.player.getEquippedStack(EquipmentSlot.LEGS).copy());
        playerArmor.set(2, playerInventory.player.getEquippedStack(EquipmentSlot.CHEST).copy());
        playerArmor.set(3, playerInventory.player.getEquippedStack(EquipmentSlot.HEAD).copy());
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
    public String toSearchableString() {
        StringBuilder builder = new StringBuilder();

        playerItems.forEach(stack -> builder.append(stack.getName().getString()));
        playerArmor.forEach(stack -> builder.append(stack.getName().getString()));

        return builder.toString();
    }

    @Override
    public void restore(ServerPlayerEntity player) {
        var inventory = player.getInventory();
        inventory.clear();

        for (int i = 0; i < 36; i++) {
            inventory.setStack(i, playerItems.get(i));
        }
        player.equipStack(EquipmentSlot.OFFHAND, playerItems.get(36));
        player.equipStack(EquipmentSlot.FEET, playerArmor.get(0));
        player.equipStack(EquipmentSlot.LEGS, playerArmor.get(1));
        player.equipStack(EquipmentSlot.CHEST, playerArmor.get(2));
        player.equipStack(EquipmentSlot.HEAD, playerArmor.get(3));
    }

    public DefaultedList<ItemStack> getPlayerArmor() {
        return playerArmor;
    }

    public DefaultedList<ItemStack> getPlayerItems() {
        return playerItems;
    }

    private static <T> Endec<DefaultedList<T>> defaulted(Endec<List<T>> endec) {
        return endec.xmap(ts -> {
                    var defaulted = DefaultedList.<T>of();
                    defaulted.addAll(ts);
                    return defaulted;
                },
                defaulted -> defaulted
        );
    }

    public static class Type extends DeathInfoPropertyType<InventoryProperty> {

        public static final Type INSTANCE = new Type();

        private Type() {
            super("deathlog.deathinfoproperty.inventory", Identifier.of("deathlog", "inventory"));
        }

        @Override
        public boolean displayedInInfoView() {
            return false;
        }

        @Override
        public StructEndec<InventoryProperty> endec() {
            return InventoryProperty.ENDEC;
        }
    }
}
