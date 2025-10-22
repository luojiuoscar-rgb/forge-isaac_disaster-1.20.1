package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class TextHelper {
    public static Component formatAttribute(String key) {
        return Component.translatable(key);
    }

    public static Component formatAttribute(String key, double value, Style style) {
        // 四舍五入到两位小数
        double rounded = Math.round(value * 100.0) / 100.0;

        // 判断是否为整数/一位/两位小数
        String formatted;
        if (Math.abs(rounded - Math.floor(rounded)) < 0.0001) {
            formatted = String.format("%.0f", rounded);
        } else if (Math.abs(rounded * 10 - Math.floor(rounded * 10)) < 0.0001) {
            formatted = String.format("%.1f", rounded);
        } else {
            formatted = String.format("%.2f", rounded);
        }

        return Component.translatable(key, formatted).withStyle(style);
    }

    public static Component formatAttribute(String key, double value) {
        return formatAttribute(key, value, Style.EMPTY.withColor(ChatFormatting.WHITE));
    }


}
