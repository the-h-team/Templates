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
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Immutable implementation of {@link Template}.
 * <p>
 * Provides Template's default Bukkit deserialization.
 * @see org.bukkit.configuration.serialization.DelegateDeserialization
 *
 * @since 1.0.0
 * @author ms5984
 */
@SerializableAs("SanctumTemplate")
public class SimpleTemplate implements Template {
    private final String rawName;
    private final String name;
    private final String rawLore;
    private final List<String> lore;
    private final Integer count;
    private final Map<Enchantment, Integer> enchants;
    private final List<ItemFlag> flags;
    private final List<ItemFlag> removeFlags;
    private final boolean skippedColorName;
    private final boolean skippedColorLore;

    private SimpleTemplate(@NotNull Map<String, Object> serialized) {
        this(
                Optional.ofNullable(serialized.get("name"))
                        .filter(String.class::isInstance)
                        .map(String.class::cast).orElse(null),
                Optional.ofNullable(serialized.get("lore"))
                        .filter(String.class::isInstance)
                        .map(String.class::cast).orElse(null),
                Optional.ofNullable(serialized.get("count"))
                        .filter(Integer.class::isInstance)
                        .map(Integer.class::cast).orElse(null),
                Optional.ofNullable(serialized.get("enchantments"))
                        .filter(Map.class::isInstance)
                        .map(map -> {
                            final ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
                            final Map<String, Object> enchantsSection;
                            try {
                                //noinspection unchecked
                                enchantsSection = (Map<String, Object>) map;
                            } catch (ClassCastException e) {
                                throw new IllegalArgumentException("Invalid enchants section!", e);
                            }
                            enchantsSection.forEach((enchantName, level) -> {
                                // process level
                                final int levelInt;
                                try {
                                    levelInt = (int) level;
                                } catch (ClassCastException e) {
                                    return;
                                }
                                builder.put(enchantName, levelInt);
                            });
                            return builder.build();
                        }).orElse(null),
                Optional.ofNullable(serialized.get("flags"))
                        .filter(List.class::isInstance)
                        .map(list -> {
                            final ImmutableList.Builder<ItemFlag> builder = ImmutableList.builder();
                            final List<String> flagsList;
                            try {
                                //noinspection unchecked
                                flagsList = (List<String>) list;
                            } catch (ClassCastException e) {
                                throw new IllegalArgumentException("Invalid flags section!", e);
                            }
                            flagsList.forEach(flagName -> {
                                // get flag by name
                                final ItemFlag flag;
                                try {
                                    flag = ItemFlag.valueOf(flagName);
                                } catch (IllegalArgumentException ignored) {
                                    // skip to next member
                                    return;
                                }
                                builder.add(flag);
                            });
                            return builder.build();
                        }).orElse(null),
                Optional.ofNullable(serialized.get("remove-flags"))
                        .filter(List.class::isInstance)
                        .map(list -> {
                            final ImmutableList.Builder<ItemFlag> builder = ImmutableList.builder();
                            final List<String> removeFlagsSection;
                            try {
                                //noinspection unchecked
                                removeFlagsSection = (List<String>) list;
                            } catch (ClassCastException e) {
                                throw new IllegalArgumentException("Invalid remove-flags section!", e);
                            }
                            removeFlagsSection.forEach(flagName -> {
                                // get flag by name
                                final ItemFlag flag;
                                try {
                                    flag = ItemFlag.valueOf(flagName);
                                } catch (IllegalArgumentException ignored) {
                                    // skip to next member
                                    return;
                                }
                                builder.add(flag);
                            });
                            return builder.build();
                        }).orElse(null)
                );
    }

    SimpleTemplate(@Nullable String name, @Nullable String lore, @Nullable Integer count,
                   @Nullable Map<String, Integer> enchants,
                   @Nullable List<ItemFlag> flags,
                   @Nullable List<ItemFlag> removeFlags) {
        this(name, lore, count, enchants, flags, removeFlags,
                name == null || !name.contains("&"),
                lore == null || !lore.contains("&"));
    }

    /**
     * Create a simple template from components.
     *
     * @param name display name to apply
     * @param lore lore to apply
     * @param count amount to apply
     * @param enchants enchantments to apply
     * @param flags ItemFlags to add
     * @param removeFlags ItemFlags to remove
     * @param skipColorName whether to skip color processing of {@code name}
     * @param skipColorLore whether to skip color processing of {@code lore}
     * @throws IllegalArgumentException if {@code enchants} has negative values
     * @since 1.1.0
     */
    protected SimpleTemplate(@Nullable String name,
                             @Nullable String lore,
                             @Nullable Integer count,
                             @Nullable Map<String, Integer> enchants,
                             @Nullable List<ItemFlag> flags,
                             @Nullable List<ItemFlag> removeFlags,
                             boolean skipColorName,
                             boolean skipColorLore) throws IllegalArgumentException {
        rawName = name;
        rawLore = lore;
        this.name = skipColorName ? name : TextProcessing.translateColor(name);
        skippedColorName = skipColorName;
        this.lore = processLore(rawLore, skipColorLore);
        skippedColorLore = skipColorLore;
        this.count = count;
        this.enchants = Optional.ofNullable(enchants)
                .map(map -> {
                    final ImmutableMap.Builder<Enchantment, Integer> builder = ImmutableMap.builder();
                    map.forEach((key, value) -> {
                        final NamespacedKey enchantmentKey;
                        if (key.contains(":")) {
                            final String[] split = key.split(":");
                            if (split.length != 2) return; // Invalid format
                            //noinspection deprecation
                            enchantmentKey = new NamespacedKey(split[0], split[1]);
                        } else {
                            enchantmentKey = NamespacedKey.minecraft(key);
                        }
                        Arrays.stream(Enchantment.values())
                                .filter(enchantment -> enchantment.getKey().equals(enchantmentKey))
                                .findAny()
                                .ifPresent(validEnchant -> {
                                    final int level = value;
                                    if (level < 1) throw new IllegalArgumentException("Level cannot be less than 1!");
                                    builder.put(validEnchant, level);
                                });
                    });
                    return builder.build();
                }).orElse(null);
        this.flags = flags;
        this.removeFlags = removeFlags;
    }

    @Override
    public @NotNull Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @Override
    public @Nullable String rawNameTransform() {
        return rawName;
    }

    @Override
    public boolean colorName() {
        return !skippedColorName;
    }

    @Override
    public @NotNull Optional<List<String>> getLore() {
        return Optional.ofNullable(lore);
    }

    @Override
    public @Nullable String rawLoreTransform() {
        return rawLore;
    }

    @Override
    public boolean colorLore() {
        return !skippedColorLore;
    }

    @Override
    public @NotNull Optional<Integer> getCount() {
        return Optional.ofNullable(count);
    }

    @Override
    public @NotNull Optional<Map<Enchantment, Integer>> getEnchantments() {
        return Optional.ofNullable(enchants);
    }

    @Override
    public @NotNull Optional<List<ItemFlag>> getItemFlagsToAdd() {
        return Optional.ofNullable(flags);
    }

    @Override
    public @NotNull Optional<List<ItemFlag>> getItemFlagsToRemove() {
        return Optional.ofNullable(removeFlags);
    }

    static @Nullable List<String> processLore(String rawLore, boolean skipColor) {
        if (rawLore == null) return null;
        if (!skipColor) rawLore = TextProcessing.translateColor(rawLore);
        return ImmutableList.copyOf(TextProcessing.splitAtNewline(rawLore));
    }

    // below for ConfigurationSerializable contract
    /**
     * Deserialize a saved template as a new SimpleTemplate.
     *
     * @param map the saved template
     * @return a new SimpleTemplate
     * @implNote Bukkit is unhappy if ConfigurationSerializable classes
     * throw exceptions (such as {@link IllegalArgumentException}); as
     * of Templates v1.1.0 this method will instead return null and log
     * the event to the server console.
     */
    public static @Nullable Template deserialize(@NotNull Map<String, Object> map) {
        try {
            return new SimpleTemplate(map);
        } catch (IllegalArgumentException e) {
            Bukkit.getServer().getLogger().warning("[Templates] Template deserialization error: returning null");
            Bukkit.getServer().getLogger().warning(e.getMessage());
            return null;
        }
    }

    /**
     * Deserialize a saved template as a new SimpleTemplate.
     *
     * @deprecated In favor of {@link #deserialize}. We will remove in v2.
     * @param map the saved template
     * @return a new SimpleTemplate
     * @implNote As of v1.1.0, delegates to {@link #deserialize}. See notes.
     */
    @Deprecated // TODO: remove in 2.0.0
    public static @Nullable Template valueOf(@NotNull Map<String, Object> map) {
        return deserialize(map);
    }
}
