package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class DescriptionHelper {
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
