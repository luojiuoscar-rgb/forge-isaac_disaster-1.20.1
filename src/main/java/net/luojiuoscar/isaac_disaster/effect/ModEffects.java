package net.luojiuoscar.isaac_disaster.effect;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.EffectNameManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.UUID;


public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS
            = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus){
        MOB_EFFECTS.register(eventBus);
    }

    public static final RegistryObject<MobEffect> ISAAC_POISON = MOB_EFFECTS.register(EffectNameManager.ISAAC_POISON,
            () -> new IsaacPoisonEffect(MobEffectCategory.HARMFUL, 8889187));

    public static final RegistryObject<MobEffect> POWER_OF_BELIAL = MOB_EFFECTS.register(EffectNameManager.POWER_OF_BELIAL,
            () -> new PowerOfBelialEffect(MobEffectCategory.BENEFICIAL, 0xee1c24)
            .addAttributeModifier(Attributes.ATTACK_DAMAGE, UUID.randomUUID().toString(), 0.0D, AttributeModifier.Operation.ADDITION));
}
