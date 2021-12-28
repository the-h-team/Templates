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
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common text transformations.
 *
 * @since 1.1.0
 * @author ms5984
 */
public class TextProcessing {
    private static final AtomicReference<InternalColorProcessor> INTERNAL_COLOR_PROCESSOR = new AtomicReference<>();
    /**
     * Translate ampersand-coded ({@literal &}) text to Minecraft's
     * color character format.
     *
     * @param text original text
     * @return translated text (unless {@code text} was null)
     */
    @Contract("null -> null")
    public static String translateColor(String text) {
        if (text == null) return null;
        return INTERNAL_COLOR_PROCESSOR.updateAndGet(i -> {
            if (i != null) return i;
            return new InternalColorProcessor();
        }).process(text);
    }

    /**
     * Split text on newline characters and newline escapes.
     *
     * @param text text to split
     * @return a String array of each line; length 0 if {@code text} is null
     */
    public static String[] splitAtNewline(String text) {
        if (text == null) return new String[0];
        return text.split("\\n|\\\\n");
    }

    /**
     * Provide lazy-init of color processor through object-orientation.
     * <p>
     * Performs environment-based setup upon initialization.
     */
    static class InternalColorProcessor {
        static final Pattern HEX_PATTERN = Pattern.compile("&(#(\\d|[A-F]|[a-f]){6})");
        static final Pattern VERSION_TEST = Pattern.compile("MC: (?:1\\.(?:1[6-9]\\d*|2\\d+)|[2-9]\\.\\d+)\\.?");
        final boolean doesHex;
        final boolean isSpigot;

        InternalColorProcessor() {
            doesHex = VERSION_TEST.matcher(Bukkit.getVersion()).find();
            isSpigot = checkIsSpigot();
        }

        @NotNull String process(@NotNull String text) {
            if (isSpigot && doesHex) {
                // use bungee lib translation
                if (text.contains("&#")) {
                    // try to process hex
                    final Matcher matcher = HEX_PATTERN.matcher(text);
                    if (matcher.find()) {
                        text = processHex(matcher, this::processHexBungeeLib);
                    }
                }
            } else if (doesHex) {
                // custom impl
                if (text.contains("&#")) {
                    // try to process hex
                    final Matcher matcher = HEX_PATTERN.matcher(text);
                    if (matcher.find()) {
                        text = processHex(matcher, this::processHexCustom);
                    }
                }
            }
            // fallback to legacy
            return ChatColor.translateAlternateColorCodes('&', text);
        }

        private static String processHex(Matcher matcher, UnaryOperator<String> colorFunction) {
            final StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, colorFunction.apply(matcher.group(1)));
            }
            matcher.appendTail(sb);
            return sb.toString();
        }

        private String processHexBungeeLib(String hexCode) {
            return net.md_5.bungee.api.ChatColor.of(hexCode).toString();
        }

        private String processHexCustom(String hexCode) {
            final StringBuilder sb = new StringBuilder(ChatColor.COLOR_CHAR + "x");
            for (int i = 1; i < hexCode.length(); i++) {
                sb.append(ChatColor.COLOR_CHAR).append(hexCode.charAt(i));
            }
            return sb.toString();
        }

        private static boolean checkIsSpigot() {
            for (Class<?> declaredClass : Bukkit.getServer().getClass().getDeclaredClasses()) {
                if (declaredClass.getSimpleName().equals("Spigot")) {
                    return true;
                }
            }
            return false;
        }
    }
}
