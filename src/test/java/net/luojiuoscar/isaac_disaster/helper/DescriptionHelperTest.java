package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DescriptionHelperTest {
    @Test
    void keepsTheDefaultStyleWhenTheDisplayedValueMatchesTheBaseValue() {
        Component value = DescriptionHelper.dynamicNumber(15.0, 15.0);

        assertEquals("15.00", value.getString());
        assertNull(value.getStyle().getColor());
    }

    @Test
    void highlightsAChangedDisplayedValue() {
        Component value = DescriptionHelper.dynamicNumber(15.0, 15.1515);

        assertEquals("15.15", value.getString());
        assertEquals(ColorManager.SYNERGY, value.getStyle().getColor().getValue());
    }

    @Test
    void ignoresDifferencesThatDisappearAtTwoDecimalPlaces() {
        Component value = DescriptionHelper.dynamicNumber(15.0, 15.004);

        assertEquals("15.00", value.getString());
        assertNull(value.getStyle().getColor());
    }
}
