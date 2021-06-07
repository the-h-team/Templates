package com.github.sanctum.templates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@SerializableAs("SanctumTemplate")
@DelegateDeserialization(SimpleTemplate.class)
public interface Template extends ConfigurationSerializable {
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

    @SuppressWarnings("CodeBlock2Expr")
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

    @SuppressWarnings("CodeBlock2Expr")
    @Override
    default @NotNull Map<String, Object> serialize() {
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        getName().ifPresent(name -> builder.put("name", name));
        getLore().map(list -> String.join("\n", list))
                .ifPresent(lore -> builder.put("lore", lore));
        getEnchantments().filter(map -> !map.isEmpty()).ifPresent(enchants -> {
            enchants.forEach((enchantment, value) -> {
                final NamespacedKey key = enchantment.getKey();
                builder.put(key.getNamespace().equals("minecraft") ? key.getKey() : key.toString(), value);
            });
        });
        getItemFlagsToAdd().filter(list -> !list.isEmpty()).ifPresent(flags -> {
            final ImmutableList.Builder<String> flagList = ImmutableList.builder();
            flags.forEach(itemFlag -> flagList.add(itemFlag.name()));
            builder.put("flags", flagList);
        });
        getItemFlagsToRemove().filter(list -> !list.isEmpty()).ifPresent(removeFlags -> {
            final ImmutableList.Builder<String> removeFlagList = ImmutableList.builder();
            removeFlags.forEach(itemFlag -> removeFlagList.add(itemFlag.name()));
            builder.put("remove-flags", removeFlagList);
        });
        return builder.build();
    }
}
