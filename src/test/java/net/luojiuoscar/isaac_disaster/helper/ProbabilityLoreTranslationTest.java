package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProbabilityLoreTranslationTest {
    @Test
    void probabilityLoreRendersTheDynamicValueAndLiteralPercent() throws Exception {
        Map<String, String> translations = new HashMap<>();
        try (var input = getClass().getResourceAsStream("/assets/isaac_disaster/lang/en_us.json")) {
            assertNotNull(input);
            Language.loadFromJson(input, translations::put);
        }
        Language previous = Language.getInstance();
        Language.inject(new Language() {
            @Override
            public String getOrDefault(String key, String fallback) { return translations.getOrDefault(key, fallback); }

            @Override
            public boolean has(String key) { return translations.containsKey(key); }

            @Override
            public boolean isDefaultRightToLeft() { return false; }

            @Override
            public FormattedCharSequence getVisualOrder(FormattedText text) { return previous.getVisualOrder(text); }
        });
        try {
            Map<String, String> expected = Map.of(
                    "moms_contacts.lore.1", "37.50%概率发射石化泪弹",
                    "celtic_cross.lore.1", "受伤时37.50%概率获得无敌",
                    "old_bandage.lore.1", "受伤时37.50%概率掉红心",
                    "dark_matter.lore.1", "37.50%概率恐惧弹",
                    "head_of_the_keeper.lore.2", "命中有37.50%概率掉普通硬币",
                    "cone_head.lore.1", "37.50%概率忽略伤害",
                    "marbles.lore.1", "受伤时37.50%概率吞下饰品"
            );
            for (var entry : expected.entrySet()) {
                String key = "item.isaac_disaster." + entry.getKey();
                assertEquals(entry.getValue(), Component.translatable(key,
                        DescriptionHelper.dynamicNumber(20, 37.5)).getString(), key);
            }
        } finally {
            Language.inject(previous);
        }
    }
}
