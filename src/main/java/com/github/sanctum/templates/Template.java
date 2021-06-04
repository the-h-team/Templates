package com.github.sanctum.templates;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@DelegateDeserialization(SimpleTemplate.class)
public interface Template {
    @NotNull Optional<String> getName();
    @NotNull Optional<List<String>> getLore();
    @NotNull Optional<Integer> getCount();
    @NotNull Optional<Map<Enchantment, Integer>> getEnchantments();
    @NotNull Optional<List<ItemFlag>> getItemFlagsToAdd();
    @NotNull Optional<List<ItemFlag>> getItemFlagsToRemove();

    @NotNull
    default ItemStack produce(@NotNull Material material) {
        // new stack from material
        return produce(() -> new ItemStack(material));
    }

    @NotNull
    default ItemStack produce(@NotNull ItemStack original) {
        // clone
        return produce(() -> new ItemStack(original));
    }

    @NotNull
    default ItemStack produce(@NotNull Supplier<@NotNull ItemStack> supplier) {
        final ItemStack toStyle = supplier.get();
        getCount().ifPresent(toStyle::setAmount);
        final ItemMeta meta = toStyle.getItemMeta();
        if (meta == null) throw new IllegalStateException();
        getName().ifPresent(meta::setDisplayName);
        // TODO: add placeholder logic to allow preserving + referencing original lore
        getLore().ifPresent(meta::setLore);
        getEnchantments().ifPresent(map -> {
            map.forEach((enchantment, level) -> {
                meta.addEnchant(enchantment, level, true);
            });
        });
        getItemFlagsToAdd().ifPresent(list -> list.forEach(meta::addItemFlags));
        getItemFlagsToRemove().ifPresent(list -> list.forEach(meta::removeItemFlags));
        toStyle.setItemMeta(meta);
        return toStyle;
    }
}
