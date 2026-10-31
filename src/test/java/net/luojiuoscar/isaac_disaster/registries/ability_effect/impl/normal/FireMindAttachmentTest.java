package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import com.google.gson.JsonParser;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTriggerView;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment.FireMindAttachment;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColorPrio;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FireMindAttachmentTest {
    @Test
    void tearsAlwaysAttachExactlyTheTwoCollisionEffectsWithoutAnExpiryEffect() throws Exception {
        ensureColors();
        for (double amplifier : new double[]{1, 100}) {
            AttackContext attack = attack(TestAttackTypes.BULLET, ModBulletColors.BASE.getId());
            prepare(attack, amplifier);
            assertEquals(ModBulletColors.FIRE_MIND.getId(), attack.getColorId());
            assertEquals(List.of(ModTriggerTypes.BULLET_HIT_ENTITY_AFTER, ModTriggerTypes.BULLET_HIT_BLOCK),
                    triggers(attack).stream().map(SimpleTrigger::getType).toList());
        }
    }

    @Test
    void effectTierOverridesBaseColorsAndEqualRanksUseRegistryId() throws Exception {
        ensureColors();
        for (var color : List.of(ModBulletColors.HOLY_LIGHT, ModBulletColors.IPECAC)) {
            AttackContext attack = attack(TestAttackTypes.BULLET, color.getId());
            prepare(attack, 1);
            assertEquals(ModBulletColors.FIRE_MIND.getId(), attack.getColorId());
            assertEquals(2, triggers(attack).size());
        }
    }

    @Test
    void lasersKeepTheirOriginalColorButReceiveBothCollisionHooks() throws Exception {
        ensureColors();
        IForgeRegistry<BulletColor> registry = RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY);
        if (!registry.containsKey(ModBulletColors.BLOOD_TEAR.getId())) {
            registry.register(ModBulletColors.BLOOD_TEAR.getId(), BulletColor.builder().color(0xCC171F).priority(BulletColorPrio.BLOOD_TEAR).build());
        }
        for (AttackType type : List.of(TestAttackTypes.LASER, TestAttackTypes.BRIMSTONE)) {
            AttackContext attack = attack(type, ModBulletColors.BLOOD_TEAR.getId());
            prepare(attack, 1);
            assertEquals(ModBulletColors.BLOOD_TEAR.getId(), attack.getColorId());
            assertEquals(2, triggers(attack).size());
        }
    }

    @Test
    void splitCopiesKeepTheAttachmentAndTheirOwnDamage() throws Exception {
        ensureColors();
        AttackContext parent = attack(TestAttackTypes.BULLET, ModBulletColors.BASE.getId());
        prepare(parent, 1);
        AttackContext child = parent.toBuilder().damage(2.0).build();
        assertEquals(2, triggers(child).size());
        assertEquals(ModBulletColors.FIRE_MIND.getId(), child.getColorId());
        assertEquals(2, child.getDamage());
        assertEquals(7, parent.getDamage());
    }

    @Test
    void lorePassesMinecraftTranslationParsingWithoutUnimplementedEffects() throws Exception {
        try (var input = getClass().getResourceAsStream("/assets/isaac_disaster/lang/en_us.json")) {
            assertNotNull(input);
            var lang = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            for (String key : List.of("item.isaac_disaster.fire_mind.lore.1",
                    "item.isaac_disaster.fire_mind.lore.2", "item.isaac_disaster.fire_mind.lore.3",
                    "effect.isaac_disaster.burning.description",
                    "item.isaac_disaster.terra.synergy.fire_mind.1")) {
                String text = lang.get(key).getAsString();
                assertEquals(text, Component.translatableWithFallback("test." + key, text).getString());
                assertFalse(text.contains("爆炸"));
                assertFalse(text.contains("发光"));
            }
        }
    }

    private static void prepare(AttackContext attack, double amplifier) {
        var context = new ExecutableEffectContext(attack.getOwner());
        context.set(ContextKeys.ATTACK_CONTEXT, attack);
        context.set(ContextKeys.EFFECT_ATTACHMENT_NODE, attack);
        context.set(ContextKeys.AMPLIFIER, amplifier);
        new FireMindAttachment().apply(context);
    }

    private static AttackContext attack(AttackType type, ResourceLocation color) throws Exception {
        Class<?> unsafe = Class.forName("sun.misc.Unsafe");
        Field field = unsafe.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        var player = (ServerPlayer) unsafe.getMethod("allocateInstance", Class.class)
                .invoke(field.get(null), ServerPlayer.class);
        return AttackContext.builder(player, player).attackType(type).damage(7.0).bulletScaleModifier(0.0).bulletKnockback(0.2)
                .range(24).speed(1.5).position(Vec3.ZERO).mainAxis(new Vec3(0, 0, 1)).color(color).build();
    }

    @SuppressWarnings("unchecked")
    private static List<SimpleTrigger> triggers(AttackContext context) throws Exception {
        Field field = CompositeTriggerView.class.getDeclaredField("triggers");
        field.setAccessible(true);
        return (List<SimpleTrigger>) field.get(context.copyTrigger().getView());
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
        for (var entry : List.of(ModBulletColors.BASE, ModBulletColors.FIRE_MIND,
                ModBulletColors.HOLY_LIGHT, ModBulletColors.IPECAC)) {
            if (!registry.containsKey(entry.getId())) {
                var priority = entry == ModBulletColors.BASE ? BulletColorPrio.BASE
                        : entry == ModBulletColors.IPECAC ? BulletColorPrio.IPECAC
                        : entry == ModBulletColors.HOLY_LIGHT ? BulletColorPrio.HOLY_LIGHT : BulletColorPrio.FIRE_MIND;
                registry.register(entry.getId(), BulletColor.builder().color(entry == ModBulletColors.FIRE_MIND ? 0xFF9933 : 0xFFFFFF).alpha(1).priority(priority).build());
            }
        }
    }
}
