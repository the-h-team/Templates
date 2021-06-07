package com.github.sanctum.templates;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SerializableAs("SanctumMetaTemplate")
@DelegateDeserialization(SimpleMetaTemplate.class)
public interface MetaTemplate extends ConfigurationSerializable {
    @NotNull Material getType();
    @NotNull ItemMeta getBaseMeta();

    default @NotNull ItemStack compose(@NotNull Template template) {
        final ItemStack item = new ItemStack(getType());
        if (!item.setItemMeta(getBaseMeta())) throw new IllegalStateException("Unable to map BaseMeta!");
        return template.produce(() -> item);
    }

    @Override
    default @NotNull Map<String, Object> serialize() {
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put("material", getType());
        builder.put("meta", getBaseMeta());
        return builder.build();
    }
}
