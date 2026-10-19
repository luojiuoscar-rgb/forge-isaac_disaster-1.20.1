package net.luojiuoscar.isaac_disaster.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.minecraft.network.chat.Component;

public class DescriptionHelper {
    private DescriptionHelper() {
    }

    /**
     * Creates a yellow dynamic description value rounded to two decimal places.
     *
     * @param currentValue the current calculated value to display
     * @return the formatted yellow value component
     */
    public static Component dynamicNumber(double currentValue) {
        BigDecimal roundedCurrent = roundToTwoDecimals(currentValue);
        return Component.literal(roundedCurrent.toPlainString())
                .withStyle(style -> style.withColor(ColorManager.SYNERGY));
    }

    private static BigDecimal roundToTwoDecimals(double value) {
        if (!Double.isFinite(value)) value = 0;
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    public static Component getSynergyDesc(Component sourceItem, Component effect){
        return Component.empty()
                .append("[")
                .append(sourceItem)
                .append("]")
                .append(effect.copy().withStyle(style -> style.withColor(ColorManager.SYNERGY)))
                .withStyle(style -> style.withColor(ColorManager.SYNERGY));
    }

    public static List<Component> getSynergyDesc(Component sourceItem, List<Component> effects) {
        List<Component> result = new ArrayList<>();

        result.add(Component.empty()
                .append("[")
                .append(sourceItem)
                .append("]")
                .append(effects.get(0).copy().withStyle(style -> style.withColor(ColorManager.SYNERGY)))
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));

        for (int i = 1; i < effects.size(); i++){
            result.add(Component.empty()
                    .append("- ")
                    .append(effects.get(i).copy()
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY))));
        }

        return result;
    }

}
