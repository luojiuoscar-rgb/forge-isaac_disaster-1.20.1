package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class DescriptionHelper {
    /**
     * Creates a two-decimal description value that is highlighted when its displayed value differs from its base value.
     *
     * @param baseValue the unmodified value shown without highlighting
     * @param currentValue the current calculated value to display
     * @return a formatted value component, highlighted when its rounded value changed
     */
    public static Component dynamicNumber(double baseValue, double currentValue) {
        BigDecimal roundedBase = roundToTwoDecimals(baseValue);
        BigDecimal roundedCurrent = roundToTwoDecimals(currentValue);
        MutableComponent result = Component.literal(roundedCurrent.toPlainString());
        return roundedBase.compareTo(roundedCurrent) == 0
                ? result
                : result.withStyle(style -> style.withColor(ColorManager.SYNERGY));
    }

    private static BigDecimal roundToTwoDecimals(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Dynamic description values must be finite");
        }
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
