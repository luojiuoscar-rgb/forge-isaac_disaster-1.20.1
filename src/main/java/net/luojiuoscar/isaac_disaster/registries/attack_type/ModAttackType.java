package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModAttackType {
    public static final ResourceKey<Registry<AttackType>> ATTACK_TYPE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "attack_type"));

    public static final DeferredRegister<AttackType> ATTACK_TYPE_REGISTER =
            DeferredRegister.create(ATTACK_TYPE_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<AttackType> BULLET =
            ATTACK_TYPE_REGISTER.register("bullet", () -> new BulletAttack(AttackPrio.BULLET.getPriority()));

    public static final RegistryObject<AttackType> LASER =
            ATTACK_TYPE_REGISTER.register("laser", () -> new LaserAttack(AttackPrio.LASER.getPriority()));

    public static final RegistryObject<AttackType> BRIMSTONE =
            ATTACK_TYPE_REGISTER.register("brimstone", () -> new BrimstoneAttack(AttackPrio.BRIMSTONE.getPriority()));

    public static final RegistryObject<AttackType> C_SECTION =
            ATTACK_TYPE_REGISTER.register("c_section", () -> new CSectionAttack(AttackPrio.C_SECTION.getPriority()));

    public static final RegistryObject<AttackType> CURSED_EYE =
            ATTACK_TYPE_REGISTER.register("cursed_eye", () -> new CursedEyeAttack(AttackPrio.CURSED_EYE.getPriority()));

    public static final RegistryObject<AttackType> NEPTUNUS =
            ATTACK_TYPE_REGISTER.register("neptunus", () -> new NeptunusAttack(AttackPrio.NEPTUNUS.getPriority()));
}
