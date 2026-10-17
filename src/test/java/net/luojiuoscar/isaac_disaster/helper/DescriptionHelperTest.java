package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DescriptionHelperTest {
    @Test
    void dynamicNumbersAreAlwaysYellowIncludingUnchangedAndZeroValues() {
        for (double current : new double[]{0, 20, 37.5, -0.25}) {
            Component number = DescriptionHelper.dynamicNumber(current);
            assertEquals(TextColor.fromRgb(ColorManager.SYNERGY), number.getStyle().getColor());
        }
    }

}
