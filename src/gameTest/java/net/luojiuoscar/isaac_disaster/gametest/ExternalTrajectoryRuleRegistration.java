package net.luojiuoscar.isaac_disaster.gametest;

import java.util.Set;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.trajectory.rule.ModTrajectoryRules;
import net.luojiuoscar.isaac_disaster.registries.trajectory.rule.TrajectoryRule;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

/** Test-only addon-style registration, deliberately outside the built-in DeferredRegister. */
@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ExternalTrajectoryRuleRegistration {
    public static final ResourceLocation A = ResourceLocation.parse("trajectory_test:a");
    public static final ResourceLocation B = ResourceLocation.parse("trajectory_test:b");
    public static final ResourceLocation E = ResourceLocation.parse("trajectory_test:e");

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(
            ModTrajectoryRules.TRAJECTORY_RULE_KEY,
            helper ->
                helper.register(
                    ResourceLocation.parse("trajectory_test:external_rule"),
                    TrajectoryRule.replace(Set.of(A, B), E, null, 0)));
    }
}
