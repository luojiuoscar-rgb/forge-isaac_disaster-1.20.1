package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTriggerView;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment.UranusAttachment;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.RingAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColorPrio;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.google.gson.JsonParser;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UranusTest {
    @Test
    void attachmentSurvivesSplitsAndDoesNotReplaceOtherMechanisms() throws Exception {
        ensureColors();
        AttackContext parent = attack(TestAttackTypes.BULLET, ModBulletColors.BASE.getId());
        parent.setVisualIds(Set.of(ModBulletVisuals.TERRA_ROCK.getId(), ModBulletVisuals.SINUS_INFECTION_TEAR.getId()));
        prepare(parent);
        AttackContext child = parent.toBuilder().damage(2.0).build();
        assertEquals(List.of(ModTriggerTypes.BULLET_HIT_ENTITY_AFTER, ModTriggerTypes.KILL_ENTITY), types(child));
        assertEquals(ModBulletColors.URANUS.getId(), child.getColorId());
        assertTrue(child.getVisualIds().containsAll(parent.getVisualIds()));
        assertTrue(child.getVisualIds().contains(ModBulletVisuals.URANUS_ICE_TEAR.getId()));
        assertEquals(2, child.getDamage());
        assertEquals(7, parent.getDamage());
        assertEquals(80, UranusSlow.DURATION);
    }

    @Test
    void lasersGetBlueAndTheDeathHookWithoutATearMaterial() throws Exception {
        ensureColors();
        for (AttackType type : List.of(TestAttackTypes.LASER, TestAttackTypes.BRIMSTONE)) {
            AttackContext attack = attack(type, ModBulletColors.BASE.getId());
            prepare(attack);
            assertEquals(ModBulletColors.URANUS.getId(), attack.getColorId());
            assertFalse(attack.getVisualIds().contains(ModBulletVisuals.URANUS_ICE_TEAR.getId()));
            assertEquals(2, types(attack).size());
        }
    }

    @Test
    void iceEffectTierOverridesBaseColor() throws Exception {
        ensureColors();
        for (var color : List.of(ModBulletColors.URANUS, ModBulletColors.IPECAC)) {
            AttackContext attack = attack(TestAttackTypes.BULLET, color.getId());
            prepare(attack);
            assertEquals(ModBulletColors.URANUS.getId(), attack.getColorId());
        }
    }

    @Test
    void ringHasTwelveHorizontalDirectionsAndPreservesExactCenterAndDamage() throws Exception {
        var origin = new Vec3(5.25, 17.4, -9.5);
        var attack = attack(TestAttackTypes.BULLET, ModBulletColors.BASE.getId());
        attack.setPos(origin);
        var ring = new RingAttackPattern().generate(new AttackPatternContext(attack, 12));
        assertEquals(12, ring.size());
        assertEquals(12, ring.stream().map(AttackContext::getMainAxis).distinct().count());
        for (var shard : ring) {
            assertEquals(0, shard.getMainAxis().y, 1e-10);
            assertEquals(1, shard.getMainAxis().length(), 1e-10);
            assertEquals(origin, shard.getPos());
            assertEquals(7, shard.getDamage());
        }
    }

    @Test
    void loreUsesRealTranslationParsingAndShortLines() throws Exception {
        try (var input = getClass().getResourceAsStream("/assets/isaac_disaster/lang/en_us.json")) {
            assertNotNull(input);
            var lang = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            for (var entry : lang.entrySet()) {
                if (!entry.getKey().startsWith("item.isaac_disaster.uranus")) continue;
                String text = entry.getValue().getAsString();
                assertEquals(text, Component.translatableWithFallback("test." + entry.getKey(), text).getString());
                assertTrue(text.length() <= 30, "Static lore line too long: " + text);
            }
        }
    }

    private static void prepare(AttackContext attack) {
        var context = new ExecutableEffectContext(attack.getOwner());
        context.set(ContextKeys.EFFECT_ATTACHMENT_NODE, attack);
        new UranusAttachment().apply(context);
    }

    private static AttackContext attack(AttackType type, ResourceLocation color) throws Exception {
        Class<?> unsafe = Class.forName("sun.misc.Unsafe");
        Field field = unsafe.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        var owner = (ServerPlayer) unsafe.getMethod("allocateInstance", Class.class).invoke(field.get(null), ServerPlayer.class);
        return AttackContext.builder(owner, owner).attackType(type).position(Vec3.ZERO).mainAxis(new Vec3(1, 0, 0))
                .damage(7.0).bulletScaleModifier(0.0).bulletKnockback(0.2).range(24).speed(1).color(color).build();
    }

    @SuppressWarnings("unchecked")
    private static List<Object> types(AttackContext attack) throws Exception {
        Field field = CompositeTriggerView.class.getDeclaredField("triggers");
        field.setAccessible(true);
        return ((List<SimpleTrigger>) field.get(attack.copyTrigger().getView())).stream().map(t -> (Object)t.getType()).toList();
    }

    @SuppressWarnings("unchecked")
    private static void ensureColors() throws Exception {
        IForgeRegistry<BulletColor> registry = RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY);
        if (registry == null) {
            var create = RegistryBuilder.class.getDeclaredMethod("create");
            create.setAccessible(true);
            registry = (IForgeRegistry<BulletColor>) create.invoke(new RegistryBuilder<BulletColor>()
                    .setName(ModBulletColors.BULLET_COLOR_KEY.location()).disableSaving().disableSync());
        }
        for (var entry : List.of(ModBulletColors.BASE, ModBulletColors.URANUS, ModBulletColors.IPECAC)) {
            if (!registry.containsKey(entry.getId())) registry.register(entry.getId(),
                    BulletColor.builder().color(0xA7E8FF).alpha(1).priority(entry == ModBulletColors.BASE ? BulletColorPrio.BASE
                            : entry == ModBulletColors.URANUS ? BulletColorPrio.URANUS : BulletColorPrio.IPECAC).build());
        }
    }
}
