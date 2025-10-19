package net.luojiuoscar.isaac_disaster.effect;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.EffectNameManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS
            = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus){
        MOB_EFFECTS.register(eventBus);
    }

    public static final RegistryObject<MobEffect> ISAAC_POISON = MOB_EFFECTS.register(EffectNameManager.POISON,
            () -> new PoisonEffect(MobEffectCategory.HARMFUL, 8889187));

    public static final RegistryObject<MobEffect> POWER_OF_BELIAL = MOB_EFFECTS.register(EffectNameManager.POWER_OF_BELIAL,
            () -> new PowerOfBelialEffect(MobEffectCategory.BENEFICIAL, 0xee1c24));

    public static final RegistryObject<MobEffect> DIZZINESS = MOB_EFFECTS.register(EffectNameManager.DIZZINESS,
            () -> new DizzinessEffect(MobEffectCategory.HARMFUL, 0xFFFFFF));

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

    public static final RegistryObject<MobEffect> LACRIMAL_HYPOSECRETION = MOB_EFFECTS.register(EffectNameManager.LACRIMAL_HYPOSECRETION,
            () -> new LacrimalHyposecretionEffect(MobEffectCategory.HARMFUL, 0xFFFFFF));

    public static final RegistryObject<MobEffect> X_RAY_VISION = MOB_EFFECTS.register(EffectNameManager.X_RAY_VISION,
            () -> new XRayVision(MobEffectCategory.BENEFICIAL, 0xC2FF66));

    public static final RegistryObject<MobEffect> CHARM = MOB_EFFECTS.register(EffectNameManager.CHARM,
            () -> new CharmEffect(MobEffectCategory.NEUTRAL, 0x9C27B0));

    public static final RegistryObject<MobEffect> VULNERABLE = MOB_EFFECTS.register(EffectNameManager.VULNERABLE,
            () -> new VulnerableEffect(MobEffectCategory.BENEFICIAL, 0x808080));

    public static final RegistryObject<MobEffect> PANIC = MOB_EFFECTS.register(EffectNameManager.PANIC,
            () -> new PanicEffect(MobEffectCategory.NEUTRAL, 0xFF0000));

    public static final RegistryObject<MobEffect> PAC_MAN = MOB_EFFECTS.register(EffectNameManager.PAC_MAN,
            () -> new PacManEffect(MobEffectCategory.BENEFICIAL, 0xFFFF00));

    public static final RegistryObject<MobEffect> RAMPAGE = MOB_EFFECTS.register(EffectNameManager.RAMPAGE,
            () -> new RampageEffect(MobEffectCategory.BENEFICIAL, 0xFFFFFF));

    public static final RegistryObject<MobEffect> FRAGILE_HEART = MOB_EFFECTS.register(EffectNameManager.FRAGILE_HEART,
            () -> new FragileHeartEffect(MobEffectCategory.BENEFICIAL, 0x7f7d70));

    public static final RegistryObject<MobEffect> TELEPATHY = MOB_EFFECTS.register(EffectNameManager.TELEPATHY,
            () -> new TelepathyEffect(MobEffectCategory.BENEFICIAL, ColorManager.SPOON_BENDER_FILTER));

    public static final RegistryObject<MobEffect> BABYLON = MOB_EFFECTS.register(EffectNameManager.BABYLON,
            () -> new BabylonEffect(MobEffectCategory.BENEFICIAL, 0x990000));
}
