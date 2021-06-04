package com.github.sanctum.templates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SerializableAs("SanctumTemplate")
public class SimpleTemplate implements Template, ConfigurationSerializable {
    private final String name;
    private final List<String> lore;
    private final Integer count;
    private final Map<Enchantment, Integer> enchants;
    private final List<ItemFlag> flags;
    private final List<ItemFlag> removeFlags;

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

    private SimpleTemplate(@Nullable String name,
                           @Nullable String lore,
                           @Nullable Integer count,
                           @Nullable Map<String, Integer> enchants,
                           @Nullable List<ItemFlag> flags,
                           @Nullable List<ItemFlag> removeFlags) {
        this.name = name;
        this.lore = Optional.ofNullable(lore)
                .map(encoded -> lore.split("\\n"))
                .map(Arrays::asList)
                .orElse(null);
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
    public @NotNull Optional<List<String>> getLore() {
        return Optional.ofNullable(lore);
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

    @Override
    public @NotNull Map<String, Object> serialize() {
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        if (name != null) {
            builder.put("name", name);
        }
        getLore().map(list -> String.join("\n", list))
                .ifPresent(lore -> builder.put("lore", lore));
        if (enchants != null && !enchants.isEmpty()) {
            enchants.forEach((enchantment, value) -> {
                final NamespacedKey key = enchantment.getKey();
                builder.put(key.getNamespace().equals("minecraft") ? key.getKey() : key.toString(), value);
            });
        }
        if (flags != null && !flags.isEmpty()) {
            final ImmutableList.Builder<String> flagList = ImmutableList.builder();
            flags.forEach(itemFlag -> flagList.add(itemFlag.name()));
            builder.put("flags", flagList);
        }
        if (removeFlags != null && !removeFlags.isEmpty()) {
            final ImmutableList.Builder<String> removeFlagList = ImmutableList.builder();
            removeFlags.forEach(itemFlag -> removeFlagList.add(itemFlag.name()));
            builder.put("remove-flags", removeFlagList);
        }
        return builder.build();
    }

    public static Template deserialize(@NotNull Map<String, Object> map) throws IllegalArgumentException {
        return new SimpleTemplate(map);
    }

    public static Template valueOf(@NotNull Map<String, Object> map) throws IllegalArgumentException {
        return new SimpleTemplate(map);
    }
}
