package net.luojiuoscar.isaac_disaster.commands.gamerule;

import net.minecraft.world.level.GameRules;

public class ModGameRules {
    public static GameRules.Key<GameRules.BooleanValue> PEDESTAL_TRIGGERABLE;

    public static void register() {
        PEDESTAL_TRIGGERABLE = GameRules.register(
                "pedestalTriggerable",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(true)
        );
    }
}
