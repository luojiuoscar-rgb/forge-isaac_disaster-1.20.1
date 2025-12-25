package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.BrimstoneAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.BulletAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.CSectionAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.LaserAttack;
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
            ATTACK_TYPE_REGISTER.register("bullet", () -> new BulletAttack(0));

    public static final RegistryObject<AttackType> LASER =
            ATTACK_TYPE_REGISTER.register("laser", () -> new LaserAttack(100));

    public static final RegistryObject<AttackType> BRIMSTONE =
            ATTACK_TYPE_REGISTER.register("brimstone", () -> new BrimstoneAttack(210));

    public static final RegistryObject<AttackType> C_SECTION =
            ATTACK_TYPE_REGISTER.register("c_section", () -> new CSectionAttack(200));
}
