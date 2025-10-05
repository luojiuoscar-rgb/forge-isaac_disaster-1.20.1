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

    public static final RegistryObject<MobEffect> DIZZINESS = MOB_EFFECTS.register(EffectNameManager.DIZZINESS,
            () -> new DizzinessEffect(MobEffectCategory.HARMFUL, 0xFFFFFF)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, UUID.randomUUID().toString(), -1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .addAttributeModifier(Attributes.JUMP_STRENGTH, UUID.randomUUID().toString(), -1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> TRANSCENDENCE = MOB_EFFECTS.register(EffectNameManager.TRANSCENDENCE,
            () -> new TranscendenceEffect(MobEffectCategory.BENEFICIAL, 0xFFFFFF));

    public static final RegistryObject<MobEffect> INVINCIBLE = MOB_EFFECTS.register(EffectNameManager.INVINCIBLE,
            () -> new InvincibleEffect(MobEffectCategory.BENEFICIAL, 0xFFD700));

    public static final RegistryObject<MobEffect> FRAILTY = MOB_EFFECTS.register(EffectNameManager.FRAILTY,
            () -> new FrailtyEffect(MobEffectCategory.BENEFICIAL, 0xFFD700));

    public static final RegistryObject<MobEffect> NECRONMICON_SHIELD = MOB_EFFECTS.register(EffectNameManager.NECRONMICON_SHIELD,
            () -> new NecronmiconShieldEffect(MobEffectCategory.BENEFICIAL, 0x000000));

    public static final RegistryObject<MobEffect> HOLY_SHIELD = MOB_EFFECTS.register(EffectNameManager.HOLY_SHIELD,
            () -> new HolyShieldEffect(MobEffectCategory.BENEFICIAL, 0x87CEEB));
}
