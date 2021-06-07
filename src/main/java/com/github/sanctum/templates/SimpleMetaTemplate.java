package com.github.sanctum.templates;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

@SerializableAs("SanctumMetaTemplate")
public class SimpleMetaTemplate implements MetaTemplate {
    private final Material type;
    private final ItemMeta baseMeta;

    public SimpleMetaTemplate(@NotNull ItemStack original) {
        this(original.getType(), Optional.ofNullable(original.getItemMeta()).map(ItemMeta::clone).orElseThrow(IllegalStateException::new));
    }
    // doesn't validate assignability of meta
    protected SimpleMetaTemplate(@NotNull Material type, @NotNull ItemMeta baseMeta) {
        this.type = type;
        this.baseMeta = baseMeta;
    }
    private SimpleMetaTemplate(@NotNull Map<String, Object> serialized) {
        this(
                Optional.ofNullable(serialized.get("material"))
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(Material::valueOf)
                        .orElseThrow(IllegalArgumentException::new),
                Optional.ofNullable(serialized.get("meta"))
                        .filter(ItemMeta.class::isInstance)
                        .map(ItemMeta.class::cast)
                        .orElseThrow(IllegalStateException::new)
        );
    }

    @Override
    public @NotNull Material getType() {
        return type;
    }

    @Override
    public @NotNull ItemMeta getBaseMeta() {
        return baseMeta;
    }

    public static MetaTemplate deserialize(@NotNull Map<String, Object> map) throws IllegalArgumentException {
        return new SimpleMetaTemplate(map);
    }

    public static MetaTemplate valueOf(@NotNull Map<String, Object> map) throws IllegalArgumentException {
        return new SimpleMetaTemplate(map);
    }
}
