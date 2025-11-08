package net.luojiuoscar.isaac_disaster.commands.gamerule;

import net.minecraft.world.level.GameRules;

public class ModGameRules {
    public static GameRules.Key<GameRules.BooleanValue> DISABLE_PLACEHOLDER;

    public static void register() {
        DISABLE_PLACEHOLDER = GameRules.register(
                "disablePlaceholder",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(false)
        );
    }
}
