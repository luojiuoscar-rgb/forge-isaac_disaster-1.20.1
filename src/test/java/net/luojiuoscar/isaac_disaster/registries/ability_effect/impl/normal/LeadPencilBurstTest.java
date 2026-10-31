package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment.LeadPencilAttachment;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.*;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryBuilder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeadPencilBurstTest {

    @Test
    void redRollsAreIndependentAndDoNotRecolorFetusesOrLasers() throws Exception {
        var effect = new LeadPencilAttachment();
        ensureColors();
        TestPlayer player = player();
        ExecutableEffectContext context = new ExecutableEffectContext(player);
        int red = 0;
        for (int i = 0; i < 1000; i++) {
            AttackContext tear = tear(player, TestAttackTypes.BULLET, ModBulletColors.BASE.getId());
            context.set(ContextKeys.ATTACK_CONTEXT, tear);
            context.set(ContextKeys.EFFECT_ATTACHMENT_NODE, tear);
            effect.apply(context);
            if (tear.getColorId().equals(ModBulletColors.BLOOD_TEAR.getId())) red++;
        }
        assertTrue(red > 400 && red < 600, "One independent 50% roll per tear");
        for (var type : List.of(TestAttackTypes.C_SECTION, TestAttackTypes.LASER, TestAttackTypes.BRIMSTONE)) {
            for (int i = 0; i < 30; i++) {
                AttackContext projectile = tear(player, type, ModBulletColors.BASE.getId());
                context.set(ContextKeys.ATTACK_CONTEXT, projectile);
                context.set(ContextKeys.EFFECT_ATTACHMENT_NODE, projectile);
            effect.apply(context);
                assertEquals(ModBulletColors.BASE.getId(), projectile.getColorId());
            }
        }
        for (int i = 0; i < 30; i++) {
            AttackContext poison = tear(player, TestAttackTypes.BULLET, ModBulletColors.POISON.getId());
            context.set(ContextKeys.ATTACK_CONTEXT, poison);
            context.set(ContextKeys.EFFECT_ATTACHMENT_NODE, poison);
            effect.apply(context);
            assertEquals(ModBulletColors.POISON.getId(), poison.getColorId());
        }
    }

    private static AttackContext tear(TestPlayer player, AttackType type, ResourceLocation color) {
        return AttackContext.builder(player, player).attackType(type).damage(7.0).bulletScaleModifier(0.0).bulletKnockback(0.2)
                .range(24).speed(1.5).position(Vec3.ZERO).mainAxis(new Vec3(0, 0, 1)).color(color).build();
    }

    private static TestPlayer player() throws Exception {
        Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
        Field field = unsafeType.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        TestPlayer player = (TestPlayer) unsafeType.getMethod("allocateInstance", Class.class)
                .invoke(field.get(null), TestPlayer.class);
        player.randomSource = RandomSource.create(531);
        return player;
    }

    @SuppressWarnings("unchecked")
    private static void ensureColors() throws Exception {
        if (net.minecraftforge.registries.RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY) != null) return;
        var builder = new RegistryBuilder<BulletColor>().setName(ModBulletColors.BULLET_COLOR_KEY.location())
                .disableSaving().disableSync();
        var create = RegistryBuilder.class.getDeclaredMethod("create");
        create.setAccessible(true);
        var registry = (net.minecraftforge.registries.IForgeRegistry<BulletColor>) create.invoke(builder);
        registry.register(ModBulletColors.BASE.getId(), BulletColor.builder().color(0xE7C5E0).alpha(1).priority(BulletColorPrio.BASE).build());
        registry.register(ModBulletColors.BLOOD_TEAR.getId(), BulletColor.builder().color(0xCC171F).alpha(1).priority(BulletColorPrio.BLOOD_TEAR).build());
        registry.register(ModBulletColors.POISON.getId(), BulletColor.builder().color(0x5CA45C).alpha(1).priority(BulletColorPrio.POISON).build());
    }

    private static final class TestPlayer extends ServerPlayer {
        private RandomSource randomSource;
        private TestPlayer() { super(null, null, null); }
        @Override public RandomSource getRandom() { return randomSource; }
    }
}
