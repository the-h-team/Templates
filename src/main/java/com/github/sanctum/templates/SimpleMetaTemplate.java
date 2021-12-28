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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Create a simple MetaTemplate from an existing ItemStack.
     * <p>
     * Captures the item's Material and clones its ItemMeta.
     * The original item is not modified.
     *
     * @param original an existing ItemStack
     */
    public SimpleMetaTemplate(@NotNull ItemStack original) {
        this(original.getType(), Optional.ofNullable(original.getItemMeta()).map(ItemMeta::clone).orElseThrow(IllegalStateException::new));
    }

    /**
     * Create a simple meta template from a Material and an ItemMeta.
     * <p>
     * <b>Does not validate assignability of meta.</b>
     *
     * @param type a material
     * @param baseMeta an item meta
     */
    protected SimpleMetaTemplate(@NotNull Material type, @NotNull ItemMeta baseMeta) {
        this.type = type;
        this.baseMeta = baseMeta;
    }

    private SimpleMetaTemplate(@NotNull Map<String, Object> serialized) throws IllegalArgumentException {
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
        // validate assignability
        if (!Bukkit.getItemFactory().isApplicable(baseMeta, type)) {
            throw new IllegalArgumentException("This meta cannot be applied to the item type specified.");
        }
    }

    @Override
    public @NotNull Material getType() {
        return type;
    }

    @Override
    public @NotNull ItemMeta getBaseMeta() {
        return baseMeta;
    }

    // below for ConfigurationSerializable contract
    /**
     * Deserialize a saved meta template as a new SimpleMetaTemplate.
     *
     * @param map the saved meta template
     * @return a new SimpleMetaTemplate
     * @implNote Bukkit is unhappy if ConfigurationSerializable classes
     * throw exceptions (such as {@link IllegalArgumentException}); as
     * of Templates v1.1.0 this method will instead return null and log
     * the event to the server console.
     */
    public static @Nullable MetaTemplate deserialize(@NotNull Map<String, Object> map) {
        try {
            return new SimpleMetaTemplate(map);
        } catch (IllegalArgumentException e) {
            Bukkit.getServer().getLogger().warning("[Templates] MetaTemplate deserialization error: returning null");
            Bukkit.getServer().getLogger().warning(e.getMessage());
            return null;
        }
    }

    /**
     * Deserialize a saved meta template as a new SimpleMetaTemplate.
     *
     * @deprecated In favor of {@link #deserialize}. We will remove in v2.
     * @param map the saved meta template
     * @return a new SimpleMetaTemplate
     * @implNote As of v1.1.0, delegates to {@link #deserialize}. See notes.
     */
    @Deprecated // TODO: remove in 2.0.0
    public static @Nullable MetaTemplate valueOf(@NotNull Map<String, Object> map) {
        return deserialize(map);
    }
}
