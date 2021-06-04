package com.github.sanctum.templates;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

@DelegateDeserialization(SimpleMetaTemplate.class)
public interface MetaTemplate {
    @NotNull Material getType();
    @NotNull ItemMeta getBaseMeta();

    default @NotNull ItemStack compose(@NotNull Template template) {
        final ItemStack item = new ItemStack(getType());
        if (!item.setItemMeta(getBaseMeta())) throw new IllegalStateException("Unable to map BaseMeta!");
        return template.produce(() -> item);
    }
}
