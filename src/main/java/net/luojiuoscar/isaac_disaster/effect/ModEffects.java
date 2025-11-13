package net.luojiuoscar.isaac_disaster.effect;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.effect.curse.CurseOfTheBlind;
import net.luojiuoscar.isaac_disaster.effect.curse.CurseOfTheMaze;
import net.luojiuoscar.isaac_disaster.effect.custom.*;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.id.BulletColorId;
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

    public static final RegistryObject<MobEffect> ISAAC_POISON = MOB_EFFECTS.register(EffectManager.POISON.getName(),
            () -> new PoisonEffect(MobEffectCategory.HARMFUL, 8889187));

    public static final RegistryObject<MobEffect> POWER_OF_BELIAL = MOB_EFFECTS.register(EffectManager.POWER_OF_BELIAL.getName(),
            () -> new PowerOfBelialEffect(MobEffectCategory.BENEFICIAL, 0xee1c24));

    public static final RegistryObject<MobEffect> DIZZINESS = MOB_EFFECTS.register(EffectManager.DIZZINESS.getName(),
            () -> new DizzinessEffect(MobEffectCategory.HARMFUL, 0xFFFFFF));

    public static final RegistryObject<MobEffect> TRANSCENDENCE = MOB_EFFECTS.register(EffectManager.TRANSCENDENCE.getName(),
            () -> new TranscendenceEffect(MobEffectCategory.BENEFICIAL, 0xFFFFFF));

    public static final RegistryObject<MobEffect> INVINCIBLE = MOB_EFFECTS.register(EffectManager.INVINCIBLE.getName(),
            () -> new InvincibleEffect(MobEffectCategory.BENEFICIAL, 0xFFD700));

    public static final RegistryObject<MobEffect> FRAILTY = MOB_EFFECTS.register(EffectManager.FRAILTY.getName(),
            () -> new FrailtyEffect(MobEffectCategory.HARMFUL, 0xFFD700));

    public static final RegistryObject<MobEffect> NECRONMICON_SHIELD = MOB_EFFECTS.register(EffectManager.NECRONMICON_SHIELD.getName(),
            () -> new NecronmiconShieldEffect(MobEffectCategory.BENEFICIAL, 0x000000));

    public static final RegistryObject<MobEffect> HOLY_SHIELD = MOB_EFFECTS.register(EffectManager.HOLY_SHIELD.getName(),
            () -> new HolyShieldEffect(MobEffectCategory.BENEFICIAL, 0x87CEEB));

    public static final RegistryObject<MobEffect> LACRIMAL_HYPOSECRETION = MOB_EFFECTS.register(EffectManager.LACRIMAL_HYPOSECRETION.getName(),
            () -> new LacrimalHyposecretionEffect(MobEffectCategory.HARMFUL, 0xFFFFFF));

    public static final RegistryObject<MobEffect> X_RAY_VISION = MOB_EFFECTS.register(EffectManager.X_RAY_VISION.getName(),
            () -> new XRayVision(MobEffectCategory.BENEFICIAL, 0xC2FF66));

    public static final RegistryObject<MobEffect> CHARM = MOB_EFFECTS.register(EffectManager.CHARM.getName(),
            () -> new CharmEffect(MobEffectCategory.NEUTRAL, 0x9C27B0));

    public static final RegistryObject<MobEffect> VULNERABLE = MOB_EFFECTS.register(EffectManager.VULNERABLE.getName(),
            () -> new VulnerableEffect(MobEffectCategory.BENEFICIAL, 0x808080));

    public static final RegistryObject<MobEffect> PANIC = MOB_EFFECTS.register(EffectManager.PANIC.getName(),
            () -> new PanicEffect(MobEffectCategory.NEUTRAL, 0xFF0000));

    public static final RegistryObject<MobEffect> PAC_MAN = MOB_EFFECTS.register(EffectManager.PAC_MAN.getName(),
            () -> new PacManEffect(MobEffectCategory.BENEFICIAL, 0xFFFF00));

    public static final RegistryObject<MobEffect> RAMPAGE = MOB_EFFECTS.register(EffectManager.RAMPAGE.getName(),
            () -> new RampageEffect(MobEffectCategory.BENEFICIAL, 0xFFFFFF));

    public static final RegistryObject<MobEffect> FRAGILE_HEART = MOB_EFFECTS.register(EffectManager.FRAGILE_HEART.getName(),
            () -> new FragileHeartEffect(MobEffectCategory.BENEFICIAL, 0x7f7d70));

    public static final RegistryObject<MobEffect> TELEPATHY = MOB_EFFECTS.register(EffectManager.TELEPATHY.getName(),
            () -> new TelepathyEffect(MobEffectCategory.BENEFICIAL, BulletColorId.SPOON_BENDER.getColor()));

    public static final RegistryObject<MobEffect> BABYLON = MOB_EFFECTS.register(EffectManager.BABYLON.getName(),
            () -> new BabylonEffect(MobEffectCategory.BENEFICIAL, 0x990000));

    public static final RegistryObject<MobEffect> SOUL_STATE = MOB_EFFECTS.register(EffectManager.SOUL_STATE.getName(),
            () -> new SoulStateEffect(MobEffectCategory.BENEFICIAL, 0x3b6bcb));

    public static final RegistryObject<MobEffect> THE_WORLD = MOB_EFFECTS.register(EffectManager.THE_WORLD.getName(),
            () -> new TheWorldEffect(MobEffectCategory.BENEFICIAL, 0xFDFF00));

    public static final RegistryObject<MobEffect> ETERNAL_HEART = MOB_EFFECTS.register(EffectManager.ETERNAL_HEART.getName(),
            () -> new EternalHeartEffect(MobEffectCategory.BENEFICIAL, 0xFFFFFF));

    public static final RegistryObject<MobEffect> GILDING = MOB_EFFECTS.register(EffectManager.GILDING.getName(),
            () -> new GildingEffect(MobEffectCategory.BENEFICIAL, 0xefe300));

    // curses
    public static final RegistryObject<MobEffect> CURSE_OF_THE_BLIND = MOB_EFFECTS.register(EffectManager.CURSE_OF_THE_BLIND.getName(),
            () -> new CurseOfTheBlind(MobEffectCategory.HARMFUL, 0x512799));
    public static final RegistryObject<MobEffect> CURSE_OF_THE_MAZE = MOB_EFFECTS.register(EffectManager.CURSE_OF_THE_MAZE.getName(),
            () -> new CurseOfTheMaze(MobEffectCategory.HARMFUL, 0x512799));
}
