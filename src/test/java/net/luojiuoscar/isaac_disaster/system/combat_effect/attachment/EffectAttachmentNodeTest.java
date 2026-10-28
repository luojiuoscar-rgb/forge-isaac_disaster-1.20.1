package net.luojiuoscar.isaac_disaster.system.combat_effect.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class EffectAttachmentNodeTest {
    private AttackContext attack() throws Exception {
        Class<?> unsafe = Class.forName("sun.misc.Unsafe");
        var field = unsafe.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        var owner = (ServerPlayer) unsafe.getMethod("allocateInstance", Class.class).invoke(field.get(null), ServerPlayer.class);
        return AttackContext.builder(owner, owner).damage(4.0).bulletScaleModifier(0.0)
                .position(Vec3.ZERO).mainAxis(new Vec3(1, 0, 0)).build();
    }

    @Test
    void statusPhasesAndCopiesPreserveAttachmentsWithoutPreparingAgain() throws Exception {
        var attack = attack();
        var before = new AtomicInteger();
        var after = new AtomicInteger();
        attack.addStatusEffect(EffectAttachmentNode.HitPhase.BEFORE_DAMAGE, () -> context -> before.incrementAndGet());
        attack.addStatusEffect(EffectAttachmentNode.HitPhase.AFTER_DAMAGE, () -> context -> after.incrementAndGet());
        var child = attack.copy();
        var context = new ExecutableEffectContext(attack.getOwner());
        child.getTrigger().fire(context, ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE);
        assertEquals(1, before.get());
        assertEquals(0, after.get());
        child.getTrigger().fire(context, ModTriggerTypes.BULLET_HIT_ENTITY_AFTER);
        assertEquals(1, after.get());
    }

    @Test
    @SuppressWarnings("unchecked")
    void colorsKeepEqualPriorityAndAllowHigherPriority() throws Exception {
        IForgeRegistry<BulletColor> registry = RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY);
        if (registry == null) {
            var create = RegistryBuilder.class.getDeclaredMethod("create");
            create.setAccessible(true);
            registry = (IForgeRegistry<BulletColor>) create.invoke(new RegistryBuilder<BulletColor>()
                    .setName(ModBulletColors.BULLET_COLOR_KEY.location()).disableSaving().disableSync());
        }
        var first = ResourceLocation.parse("test:node_first");
        var equal = ResourceLocation.parse("test:node_equal");
        var higher = ResourceLocation.parse("test:node_higher");
        registry.register(first, new BulletColor(0xFF0000, 1, 20));
        registry.register(equal, new BulletColor(0x00FF00, 1, 20));
        registry.register(higher, new BulletColor(0x0000FF, 1, 100));
        var attack = attack();
        attack.offerColor(first);
        attack.offerColor(equal);
        assertEquals(first, attack.getColorRl());
        attack.offerColor(higher);
        assertEquals(higher, attack.getColorRl());
        attack.freeze();
        attack.offerColor(first);
        assertEquals(higher, attack.getColorRl());
    }
}
