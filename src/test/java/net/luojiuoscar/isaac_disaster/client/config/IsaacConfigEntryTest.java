package net.luojiuoscar.isaac_disaster.client.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IsaacConfigEntryTest {
    @Test
    void rejectsNonFiniteAndOutOfRangeNumbersBeforeWriting() {
        IsaacConfigEntry<?> damage = entry("default_attack_damage");
        for (String text : new String[]{"NaN", "Infinity", "-Infinity", "1e309", "-0.01", "100000"}) {
            assertFalse(damage.isValidText(text), text);
        }
        assertTrue(damage.isValidText("0"));
        assertTrue(damage.isValidText("99999"));
        IsaacConfigEntry<?> weight = entry("coin_tier_1_weight");
        assertFalse(weight.isValidText("-1"));
        assertFalse(weight.isValidText("100000"));
        assertFalse(weight.isValidText("1.5"));
    }

    @Test
    void clientInputsUseTheirOwnSpecIncludingNegativeOffsets() {
        IsaacConfigEntry<?> margin = entry("attribute_indicator_left_margin");
        IsaacConfigEntry<?> offset = entry("attribute_indicator_vertical_offset");
        IsaacConfigEntry<?> scale = entry("attribute_indicator_scale");
        assertTrue(margin.isValidText("0"));
        assertTrue(margin.isValidText("4096"));
        assertFalse(margin.isValidText("-1"));
        assertFalse(margin.isValidText("4097"));
        assertTrue(offset.isValidText("-4096"));
        assertTrue(offset.isValidText("4096"));
        assertFalse(offset.isValidText("-4097"));
        assertFalse(offset.isValidText("4097"));
        assertTrue(scale.isValidText("0.5"));
        assertTrue(scale.isValidText("3.0"));
        for (String text : new String[]{"0.49", "3.01", "NaN", "Infinity", "-Infinity"}) {
            assertFalse(scale.isValidText(text), text);
        }
    }

    private IsaacConfigEntry<?> entry(String id) {
        return IsaacConfigCatalog.entries().stream().filter(entry -> entry.id().equals(id)).findFirst().orElseThrow();
    }
}
