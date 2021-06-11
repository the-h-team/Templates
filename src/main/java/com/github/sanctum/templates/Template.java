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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Describe various abstract properties to set of any item processed
 * by this class:
 *
 * <ul>
 *     <li>Display name</li>
 *     <li>Lore</li>
 *     <li>Item count</li>
 *     <li>Enchantments and their levels</li>
 *     <li>ItemFlags to add</li>
 *     <li>ItemFlags to remove</li>
 * </ul>
 *
 * @author ms5984
 */
@SerializableAs("SanctumTemplate")
@DelegateDeserialization(SimpleTemplate.class)
public interface Template extends ConfigurationSerializable {
    /**
     * Builder for immutable Templates based on {@link SimpleTemplate}.
     *
     * @author ms5984
     */
    class Builder {
        private String name;
        private String lore;
        private Integer count = null;
        private Map<String, Integer> enchantments;
        private List<ItemFlag> flags;
        private List<ItemFlag> removeFlags;

        /**
         * Get the display name of the item.
         *
         * @return display name of the item
         */
        public @Nullable String getName() {
            return name;
        }

        /**
         * Set the display name of the item.
         * <p>
         * Null = default.
         *
         * @param name the item display name
         * @return this builder
         */
        public Builder setName(@Nullable String name) {
            this.name = name;
            return this;
        }

        /**
         * Get the lore for the item.
         *
         * @return the lore for the item
         */
        public @Nullable String getLore() {
            return lore;
        }

        /**
         * Set the lore for the item.
         * <p>
         * Multi-line support is available with processing of both true
         * newlines and the \n escape sequence. This allows us not only to use
         * \n-formatted strings but also to use the following YAML syntax for
         * a given template:
         * <pre>
         *     lore: |
         *       What is love
         *       Baby don't hurt me
         *       Don't hurt me
         *       No more
         * </pre>
         *
         * @param lore a lore string
         * @return this builder
         */
        public Builder setLore(@Nullable String lore) {
            this.lore = lore;
            return this;
        }

        /**
         * Transform the current lore string with the provided function.
         *
         * @param processing a transforming function
         * @return this builder
         * @throws IllegalStateException if current lore provided by
         * {@link #getLore()} is null
         */
        public Builder transformLore(Function<String, String> processing) throws IllegalStateException {
            return setLore(processing.apply(getLore()));
        }

        /**
         * Get item count, if specified.
         * <p>
         * Null = default.
         *
         * @return specified item count or null
         */
        public @Nullable Integer getCount() {
            return count;
        }

        /**
         * Set item count or leave at its default.
         * <p>
         * Null = default.
         *
         * @param count a nullable Integer representing the quantity
         *              the produced ItemStack should have
         * @return this builder
         */
        public Builder setCount(@Nullable Integer count) {
            this.count = count;
            return this;
        }

        /**
         * Set enchantments on the item.
         * <p>
         * The map provided MUST follow this format for all entries:
         * <dl>
         *     <dt>key</dt>
         *     <dd>Either fully-qualified toString value of the desired
         *     enchantment--such as <code>minecraft:silk_touch</code>--or
         *     simply said key less its "<code>minecraft:</code>" namespace
         *     (it is assumed). Custom enchantments have not been tested
         *     but are hopefully supported this way.</dd>
         *     <dt>value</dt>
         *     <dd>The level of the enchantment. Do not use negatives, final
         *     build WILL fail.</dd>
         * </dl>
         *
         * @param enchantments a Map of Strings to Integers containing
         *                    enchantment keys and levels, respectively
         * @return this builder
         */
        public Builder setEnchantments(@Nullable Map<String, Integer> enchantments) {
            this.enchantments = enchantments == null ? null : ImmutableMap.copyOf(enchantments);
            return this;
        }

        /**
         * Set ItemFlags to add to the item.
         *
         * @param flagsToAdd ItemFlags to add to the item
         * @return this builder
         */
        public Builder setItemFlagsToAdd(@Nullable List<ItemFlag> flagsToAdd) {
            this.flags = flagsToAdd == null ? null : ImmutableList.copyOf(flagsToAdd);
            return this;
        }

        /**
         * Set ItemFlags to remove from the item.
         * <p>
         * Silently fails if flags are not present.
         *
         * @param flagsToRemove ItemFlags to remove from the item
         * @return this builder
         */
        public Builder setItemFlagsToRemove(@Nullable List<ItemFlag> flagsToRemove) {
            this.removeFlags = flagsToRemove == null ? null : ImmutableList.copyOf(flagsToRemove);
            return this;
        }

        /**
         * Produce an immutable Template from this builder.
         *
         * @return an immutable Template
         * @throws IllegalArgumentException if any enchantment level negative
         */
        public Template build() throws IllegalArgumentException {
            return new SimpleTemplate(name, lore, count, enchantments, flags, removeFlags);
        }

        /**
         * Copy the values of this builder to a new instance.
         *
         * @return new builder instance
         */
        public Builder copy() {
            final Builder copy = new Builder();
            copy.name = name;
            copy.lore = lore;
            copy.count = count;
            copy.enchantments = enchantments;
            copy.flags = flags;
            copy.removeFlags = removeFlags;
            return copy;
        }
    }

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
