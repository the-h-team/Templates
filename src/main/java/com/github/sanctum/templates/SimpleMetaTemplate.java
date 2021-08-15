/*
 *   Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.github.sanctum.templates;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

/**
 * Immutable (shallow!) implementation of {@link MetaTemplate}
 * <p>
 * Provides MetaTemplate's default Bukkit deserialization.
 * @see org.bukkit.configuration.serialization.DelegateDeserialization
 *
 * @since 1.0.0
 * @author ms5984
 */
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
