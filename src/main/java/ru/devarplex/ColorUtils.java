package ru.devarplex.utils;

import org.bukkit.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");

    public static String color(String text) {
        if (text == null) return "";

        // Сначала обрабатываем hex-цвета
        text = translateHexColors(text);

        // Затем стандартные цвета
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private static String translateHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer,
                    ChatColor.COLOR_CHAR + "x" +
                            ChatColor.COLOR_CHAR + hex.charAt(0) +
                            ChatColor.COLOR_CHAR + hex.charAt(1) +
                            ChatColor.COLOR_CHAR + hex.charAt(2) +
                            ChatColor.COLOR_CHAR + hex.charAt(3) +
                            ChatColor.COLOR_CHAR + hex.charAt(4) +
                            ChatColor.COLOR_CHAR + hex.charAt(5));
        }

        return matcher.appendTail(buffer).toString();
    }
}