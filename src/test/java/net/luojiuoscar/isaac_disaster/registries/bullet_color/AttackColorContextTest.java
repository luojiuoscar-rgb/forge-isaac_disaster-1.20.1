package net.luojiuoscar.isaac_disaster.registries.bullet_color;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AttackColorContextTest {
    private static final ResourceLocation TINT = ResourceLocation.parse("test:context_scoped_tint");
    private static final ResourceLocation TRANSLUCENT = ResourceLocation.parse("test:context_alpha_only");
    private static final ResourceLocation HIGH = ResourceLocation.parse("test:context_high");
    private static final AttackType TEAR = new TestAttack("tear", ModAttackTypes.BULLET.getId(), 0xABCDEF);
    private static final AttackType LASER = new TestAttack("laser", ModAttackTypes.LASER.getId(), 0xFF0000);

    @Test
    void changingAttackTypeInvalidatesRgbAndAlphaResolution() throws Exception {
        var context = context();
        assertEquals(0x123456, context.resolveColor().color());
        assertEquals(0.5F, context.resolveColor().alpha());
        context.setAttackType(LASER);
        assertEquals(0xFF0000, context.resolveColor().color());
        assertEquals(1, context.resolveColor().alpha());
    }

    @Test
    void copiesAndBuilderDerivationsRetainAllCandidatesAndAreIndependent() throws Exception {
        var context = context();
        var copy = context.copy();
        copy.offerColor(HIGH);
        assertEquals(TINT, context.getColorId());
        assertEquals(HIGH, copy.getColorId());
        var derived = context.toBuilder().attackType(LASER).build();
        assertEquals(context.getColorIds(), derived.getColorIds());
        assertEquals(0xFF0000, derived.resolveColor().color());
        assertEquals(1, derived.resolveColor().alpha());
    }

    @Test
    void frozenContextRejectsColorsAndRebindingCreatesIndependentMutableCopy() throws Exception {
        var context = context();
        context.freeze();
        var candidates = context.getColorIds();
        context.offerColor(HIGH);
        context.offerColors(List.of(HIGH));
        assertEquals(candidates, context.getColorIds());
        assertEquals(TINT, context.getColorId());
        var rebound = context.bindAttackTypeOrCopy(LASER);
        assertNotSame(context, rebound);
        assertFalse(rebound.isFrozen());
        assertEquals(candidates, rebound.getColorIds());
        assertEquals(0xFF0000, rebound.resolveColor().color());
        rebound.offerColor(HIGH);
        assertEquals(HIGH, rebound.getColorId());
        assertEquals(TINT, context.getColorId());
    }

    @Test
    void builderSnapshotsTheInputCandidateCollection() throws Exception {
        var source = context();
        var input = new ArrayList<>(source.getColorIds());
        var builder = source.toBuilder().colors(input);
        input.clear();
        assertEquals(source.getColorIds(), builder.build().getColorIds());
    }

    @SuppressWarnings("unchecked")
    private static AttackContext context() throws Exception {
        IForgeRegistry<BulletColor> registry = RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY);
        if (registry == null) {
            var create = RegistryBuilder.class.getDeclaredMethod("create");
            create.setAccessible(true);
            registry = (IForgeRegistry<BulletColor>) create.invoke(new RegistryBuilder<BulletColor>()
                    .setName(ModBulletColors.BULLET_COLOR_KEY.location()).disableSaving().disableSync());
        }
        if (!registry.containsKey(TINT)) {
            registry.register(TINT, BulletColor.builder().color(0x123456).onlyRoots(ModAttackTypes.BULLET).build());
            registry.register(TRANSLUCENT, BulletColor.builder().alpha(0.5F).build());
            registry.register(HIGH, BulletColor.builder().color(0x654321).priority(100, 0).build());
        }
        Class<?> unsafe = Class.forName("sun.misc.Unsafe");
        var field = unsafe.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        var owner = (ServerPlayer) unsafe.getMethod("allocateInstance", Class.class).invoke(field.get(null), ServerPlayer.class);
        return AttackContext.builder(owner, owner).damage(4.0).bulletScaleModifier(0.0).bulletKnockback(0.2)
                .position(Vec3.ZERO).mainAxis(new Vec3(1, 0, 0)).attackType(TEAR).colors(List.of(TINT, TRANSLUCENT)).build();
    }

    private static final class TestAttack extends AttackType {
        private final ResourceLocation id;
        private final ResourceLocation root;
        private final int defaultColor;
        TestAttack(String id, ResourceLocation root, int defaultColor) {
            super(0);
            this.id = ResourceLocation.parse("test:" + id);
            this.root = root;
            this.defaultColor = defaultColor;
        }
        @Override public ResourceLocation getId() { return id; }
        @Override public ResourceLocation getRootId() { return root; }
        @Override public int getDefaultColor() { return defaultColor; }
        @Override public List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount) { return List.of(); }
        @Override public void performAttack(List<AttackContext> contexts) {}
        @Override public void makeSound(LivingEntity entity) {}
        @Override public void shoot(AttackContext context) {}
    }
}
