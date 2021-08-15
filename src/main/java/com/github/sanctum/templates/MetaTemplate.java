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

import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Describes a Material-ItemMeta pair to which you may apply Templates.
 *
 * @since 1.0.0
 * @author ms5984
 */
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
        builder.put("material", getType().name());
        builder.put("meta", getBaseMeta());
        return builder.build();
    }
}
