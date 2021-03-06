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
 * @since 1.0.0
 * @author ms5984
 */
@SerializableAs("SanctumTemplate")
@DelegateDeserialization(SimpleTemplate.class)
public interface Template extends ConfigurationSerializable {
    /**
     * Builder for immutable Templates based on {@link SimpleTemplate}.
     *
     * @since 1.0.0
     * @author ms5984
     */
    class Builder {
        private String name;
        private String lore;
        private Integer count = null;
        private Map<String, Integer> enchantments;
        private List<ItemFlag> flags;
        private List<ItemFlag> removeFlags;
        private boolean skipColorName;
        private boolean skipColorLore;

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
         * <p>
         * You can reference the item input display name like so:
         * <pre>
         *     name: "Player info for %original_name%"
         * </pre>
         *
         * @param name the item display name
         * @return this builder
         */
        public Builder setName(@Nullable String name) {
            this.name = name;
            return this;
        }

        /**
         * Transform the current name string with the provided function.
         *
         * @param processing a transforming function
         * @return this builder
         * @since 1.1.0
         */
        public Builder transformName(Function<@Nullable String, @Nullable String> processing) {
            return setName(processing.apply(getName()));
        }

        /**
         * Manually set whether to skip ampersand ({@literal &})
         * color code processing for the display name.
         *
         * @param skip whether to skip color processing for the display name
         * @return this builder
         * @since 1.1.0
         */
        public Builder skipColorName(boolean skip) {
            skipColorName = skip;
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
         * <p>
         * You can reference item input lore anywhere like so:
         * <pre>
         *     lore: |
         *       This item is for sale:
         *       %original_lore%
         * </pre>
         * The placeholder <code>%original_lore%</code> will always occur as
         * its own line(s). It is thus easiest to put the placeholder on its
         * own line. Any text that occurs on the same line as a placeholder
         * will be moved onto an extra line (before or after).
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
         */
        public Builder transformLore(Function<@Nullable String, @Nullable String> processing) {
            return setLore(processing.apply(getLore()));
        }

        /**
         * Manually set whether to skip ampersand ({@literal &})
         * color code processing for the lore.
         *
         * @param skip whether to skip color processing for the lore
         * @return this builder
         * @since 1.1.0
         */
        public Builder skipColorLore(boolean skip) {
            skipColorLore = skip;
            return this;
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
            return new SimpleTemplate(name, lore, count, enchantments, flags, removeFlags, skipColorName, skipColorLore);
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
            copy.skipColorName = skipColorName;
            copy.skipColorLore = skipColorLore;
            return copy;
        }
    }
    /**
     * The placeholder for referencing an item's original display name.
     */
    String NAME_PLACEHOLDER = "%original_name%";
    /**
     * The placeholder for referencing an item's original lore.
     */
    String LORE_PLACEHOLDER = "%original_lore%";
    /**
     * An immutable, empty template.
     * <p>
     * No properties will be set.
     */
    Template EMPTY = new SimpleTemplate(null, null, null, null, null, null);

    /**
     * Indicates the display name transformation, if needed.
     *
     * @return an Optional describing the display name transform
     */
    @NotNull Optional<String> getName();

    /**
     * Get the original name transform provided to
     * this template before color processing.
     *
     * @return the defined name transform
     * @since 1.1.0
     */
    @Nullable String rawNameTransform();

    /**
     * Indicates that {@link #getName()} has been (or will be)
     * processed for ampersand color codes.
     *
     * @return true if {@link #getName()} is pre-processed
     * @since 1.1.0
     */
    default boolean colorName() {
        return true;
    }

    /**
     * Indicates the lore transformation, if needed.
     *
     * @return an Optional describing the lore transform
     */
    @NotNull Optional<List<String>> getLore();

    /**
     * Get the original lore transform provided to
     * this template before color processing.
     *
     * @return the defined name transform
     * @since 1.1.0
     */
    @Nullable String rawLoreTransform();

    /**
     * Indicates that {@link #getLore()} has been (or will be)
     * processed for ampersand color codes.
     *
     * @return true if {@link #getLore()} is pre-processed
     * @since 1.1.0
     */
    default boolean colorLore() {
        return true;
    }

    /**
     * Indicates the item amount transformation, if needed.
     *
     * @return an Optional describing the amount transform
     */
    @NotNull Optional<Integer> getCount();

    /**
     * Indicates the enchantment transformations, if needed.
     *
     * @return an Optional describing the enchantment transforms
     */
    @NotNull Optional<Map<Enchantment, Integer>> getEnchantments();

    /**
     * Indicates the ItemFlag additive transformation, if needed.
     *
     * @return an Optional describing the ItemFlag additive transform
     */
    @NotNull Optional<List<ItemFlag>> getItemFlagsToAdd();

    /**
     * Indicates the ItemFlag subtractive transformation, if needed.
     *
     * @return an Optional describing the ItemFlag subtractive transform
     */
    @NotNull Optional<List<ItemFlag>> getItemFlagsToRemove();

    /**
     * Produce an ItemStack with this Template using a new ItemStack
     * of the material provided.
     *
     * @param material material for new ItemStack
     * @return a new, styled ItemStack
     */
    default @NotNull ItemStack produce(@NotNull Material material) {
        // new stack from material
        return produce(() -> new ItemStack(material));
    }

    /**
     * Produce an ItemStack with this Template using an existing ItemStack.
     * <p>
     * The provided ItemStack will be cloned as per the clone constructor
     * of {@link ItemStack}.
     *
     * @param original an ItemStack to clone
     * @return a new, styled ItemStack
     */
    default @NotNull ItemStack produce(@NotNull ItemStack original) {
        // clone
        return produce(() -> new ItemStack(original));
    }

    /**
     * Produce an ItemStack with this Template using a Supplier
     * to source the base item.
     *
     * @param supplier an ItemStack supplier
     * @return a styled ItemStack
     */
    default @NotNull ItemStack produce(@NotNull Supplier<@NotNull ItemStack> supplier) {
        final ItemStack toStyle = supplier.get();
        getCount().ifPresent(toStyle::setAmount);
        final ItemMeta meta = toStyle.getItemMeta();
        if (meta == null) throw new IllegalStateException();
        getName().ifPresent(newName -> {
            // check for placeholder
            if (!newName.contains(NAME_PLACEHOLDER)) {
                meta.setDisplayName(newName);
                return;
            }
            meta.setDisplayName(newName.replace(
                    NAME_PLACEHOLDER,
                    Optional.of(meta)
                            .filter(ItemMeta::hasDisplayName)
                            .map(ItemMeta::getDisplayName).orElse(""))
            );
        });
        getLore().ifPresent(newLore -> {
            // placeholder logic to allow preserving + referencing original lore
            if (newLore.stream().noneMatch(str -> str.contains(LORE_PLACEHOLDER))) {
                meta.setLore(newLore);
                return;
            }
            final ImmutableList.Builder<String> finalLore = ImmutableList.builder();
            for (String line : newLore) {
                if (!line.contains(LORE_PLACEHOLDER)) {
                    finalLore.add(line);
                    continue;
                }
                final List<String> originalLore = meta.getLore();
                if (line.equals(LORE_PLACEHOLDER) && originalLore != null) {
                    originalLore.forEach(finalLore::add);
                    continue;
                }
                final String[] split = line.split(LORE_PLACEHOLDER, 0);
                for (int i = 0; i < split.length; i++) {
                    if (i > 0 && originalLore != null) finalLore.addAll(originalLore);
                    finalLore.add(split[i]);
                }
            }
            meta.setLore(finalLore.build());
        });
        getEnchantments().ifPresent(map ->
            map.forEach((enchantment, level) ->
                meta.addEnchant(enchantment, level, true)
            )
        );
        getItemFlagsToAdd().ifPresent(list -> list.forEach(meta::addItemFlags));
        getItemFlagsToRemove().ifPresent(list -> list.forEach(meta::removeItemFlags));
        toStyle.setItemMeta(meta);
        return toStyle;
    }

    @Override
    default @NotNull Map<String, Object> serialize() {
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        getName().ifPresent(name -> builder.put("name", name));
        getLore().map(list -> String.join("\n", list))
                .ifPresent(lore -> builder.put("lore", lore));
        getEnchantments().filter(map -> !map.isEmpty()).ifPresent(enchants ->
            enchants.forEach((enchantment, value) -> {
                final NamespacedKey key = enchantment.getKey();
                builder.put(key.getNamespace().equals("minecraft") ? key.getKey() : key.toString(), value);
            })
        );
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

    /**
     * Get a new Template builder.
     *
     * @return a new Builder
     * @since 1.1.0
     */
    static Builder builder() {
        return new Builder();
    }
}
