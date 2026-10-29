package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import com.google.gson.JsonParser;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment.HolyLightAttachment;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.*;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.HolyLight;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.IForgeRegistry;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class HolyLightItemTest {
    @Test
    void collectibleIsQualityThreeAndAppendedWithoutReusingTheWikiId() {
        ItemId item = ItemId.valueOf("HOLY_LIGHT");
        assertEquals(3, item.getLevel());
        assertEquals(ItemId.CRACK_THE_SKY.ordinal() + 1, item.getId());
        assertEquals(item.ordinal(), item.getId());
    }

    @Test
    void chanceUsesLuckStepsAndCapsAtFiftyPercent() {
        assertEquals(1.0 / 11, HolyLight.chance(-1));
        assertEquals(0.1, HolyLight.chance(0));
        assertEquals(0.1, HolyLight.chance(1));
        assertEquals(1.0 / 9, HolyLight.chance(2));
        assertEquals(1.0 / 3, HolyLight.chance(8.88));
        assertEquals(0.5, HolyLight.chance(8.89));
        assertEquals(0.5, HolyLight.chance(1000));
    }

    @Test
    void restrictedHitsUseTheSameSingleLuckRollAsOtherHolyProcs() throws Exception {
        TestPlayer player = player();
        var context = new ExecutableEffectContext(player);
        context.set(ContextKeys.AMPLIFIER, 100.0);
        Field field = CompositeTriggerView.class.getDeclaredField("triggers");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<SimpleTrigger> triggers = (List<SimpleTrigger>) field.get(new HolyLight().getTriggers(context));
        var restricted = triggers.stream().filter(t -> t.getType() == ModTriggerTypes.HIT_ENTITY_RESTRICTED).toList();
        assertEquals(1, restricted.size(), "The owned module must expose exactly one restricted-hit trigger");
        Field predicate = SimpleTrigger.class.getDeclaredField("condition");
        predicate.setAccessible(true);
        @SuppressWarnings("unchecked")
        Predicate<ExecutableEffectContext> chance = (Predicate<ExecutableEffectContext>) predicate.get(restricted.get(0));
        assertTrue(chance.test(context));
        assertFalse(chance.test(context));
        assertEquals(2, player.randomSource.rolls);
    }

    @Test
    void eachTearRollsOnceAndModuleAmplifierDoesNotIncreaseChance() throws Exception {
        ensureColors();
        TestPlayer player = player();
        var effect = new HolyLightAttachment();
        for (int i = 0; i < 8; i++) {
            AttackContext tear = tear(player, TestAttackTypes.BULLET, ModBulletColors.BASE.getId());
            var context = new ExecutableEffectContext(player);
            context.set(ContextKeys.ATTACK_CONTEXT, tear);
            context.set(ContextKeys.EFFECT_ATTACHMENT_NODE, tear);
            context.set(ContextKeys.AMPLIFIER, 100.0);
            effect.apply(context);
            boolean success = i % 2 == 0;
            assertEquals(success ? 2 : 0, triggers(tear).size());
            assertEquals(success ? ModBulletColors.HOLY_LIGHT.getId() : ModBulletColors.BASE.getId(), tear.getColorRl());
            assertEquals(i + 1, player.randomSource.rolls);
            if (success) {
                assertSame(ModTriggerTypes.BULLET_HIT_ENTITY_AFTER, triggers(tear).get(0).getType());
                assertSame(ModTriggerTypes.BULLET_HIT_BLOCK, triggers(tear).get(1).getType());
            }
        }
    }

    @Test
    void equalAndHigherPriorityColorsSurviveWithoutRemovingTheHolyEffect() throws Exception {
        ensureColors();
        var effect = new HolyLightAttachment();
        for (var color : List.of(ModBulletColors.POISON, ModBulletColors.SACRED_HEART, ModBulletColors.IPECAC)) {
            TestPlayer player = player();
            AttackContext tear = tear(player, TestAttackTypes.BULLET, color.getId());
            var context = new ExecutableEffectContext(player);
            context.set(ContextKeys.ATTACK_CONTEXT, tear);
            context.set(ContextKeys.EFFECT_ATTACHMENT_NODE, tear);
            effect.apply(context);
            assertEquals(color.getId(), tear.getColorRl());
            assertEquals(2, triggers(tear).size());
        }
    }

    @Test
    void splitCopyRetainsHolyTriggersAndUsesItsOwnReducedDamage() throws Exception {
        ensureColors();
        TestPlayer player = player();
        AttackContext parent = tear(player, TestAttackTypes.BULLET, ModBulletColors.BASE.getId());
        var context = new ExecutableEffectContext(player);
        context.set(ContextKeys.ATTACK_CONTEXT, parent);
        context.set(ContextKeys.EFFECT_ATTACHMENT_NODE, parent);
        new HolyLightAttachment().apply(context);
        AttackContext child = parent.toBuilder().damage(3.5).build();
        assertEquals(2, triggers(child).size());
        assertEquals(ModBulletColors.HOLY_LIGHT.getId(), child.getColorRl());
        assertEquals(3.5F, child.getDamage());
        assertEquals(7F, parent.getDamage());
        assertEquals(1, player.randomSource.rolls, "Inheritance does not reroll");
    }

    @Test
    void lasersKeepTheirColorAndRollIndependentlyAtHitTime() throws Exception {
        var effect = new HolyLightAttachment();
        for (AttackType type : List.of(TestAttackTypes.LASER, TestAttackTypes.BRIMSTONE)) {
            TestPlayer player = player();
            AttackContext laser = tear(player, type, ModBulletColors.BLOOD_TEAR.getId());
            var context = new ExecutableEffectContext(player);
            context.set(ContextKeys.ATTACK_CONTEXT, laser);
            context.set(ContextKeys.EFFECT_ATTACHMENT_NODE, laser);
            effect.apply(context);
            assertEquals(0, player.randomSource.rolls);
            assertEquals(ModBulletColors.BLOOD_TEAR.getId(), laser.getColorRl());
            assertEquals(1, triggers(laser).size());
            SimpleTrigger trigger = triggers(laser).get(0);
            assertSame(ModTriggerTypes.BULLET_HIT_ENTITY_AFTER, trigger.getType());
            Field field = SimpleTrigger.class.getDeclaredField("condition");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Predicate<ExecutableEffectContext> condition = (Predicate<ExecutableEffectContext>) field.get(trigger);
            assertTrue(condition.test(context));
            assertFalse(condition.test(context));
            assertTrue(condition.test(context));
        }
    }

    @Test
    void percentageLoreUsesMinecraftTranslationParser() throws Exception {
        try (var input = getClass().getResourceAsStream("/assets/isaac_disaster/lang/en_us.json")) {
            assertNotNull(input);
            var lang = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            String format = lang.get("item.isaac_disaster.holy_light.lore.1").getAsString();
            assertEquals("10%概率发射圣光泪弹", Component.translatableWithFallback("test.holy_light.chance", format, "10").getString());
            assertEquals("50%概率发射圣光泪弹", Component.translatableWithFallback("test.holy_light.chance", format, "50").getString());
        }
    }

    private static AttackContext tear(TestPlayer player, AttackType type, ResourceLocation color) {
        return AttackContext.builder(player, player).attackType(type).damage(7.0).bulletScaleModifier(0.0).bulletKnockback(0.2)
                .range(24).speed(1.5).position(Vec3.ZERO).mainAxis(new Vec3(0, 0, 1)).color(color).build();
    }

    @SuppressWarnings("unchecked")
    private static List<SimpleTrigger> triggers(AttackContext context) throws Exception {
        Field field = CompositeTriggerView.class.getDeclaredField("triggers");
        field.setAccessible(true);
        return (List<SimpleTrigger>) field.get(context.copyTrigger().getView());
    }

    private static TestPlayer player() throws Exception {
        Class<?> type = Class.forName("sun.misc.Unsafe");
        Field field = type.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        TestPlayer player = (TestPlayer) type.getMethod("allocateInstance", Class.class).invoke(field.get(null), TestPlayer.class);
        player.randomSource = new AlternatingRandom();
        player.luck = new AttributeInstance(Attributes.LUCK, ignored -> {});
        return player;
    }

    @SuppressWarnings("unchecked")
    private static void ensureColors() throws Exception {
        IForgeRegistry<BulletColor> registry = RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY);
        if (registry == null) {
            var create = RegistryBuilder.class.getDeclaredMethod("create");
            create.setAccessible(true);
            registry = (IForgeRegistry<BulletColor>) create.invoke(
                    new RegistryBuilder<BulletColor>().setName(ModBulletColors.BULLET_COLOR_KEY.location()).disableSaving().disableSync());
        }
        for (var entry : List.of(ModBulletColors.BASE, ModBulletColors.BLOOD_TEAR, ModBulletColors.HOLY_LIGHT, ModBulletColors.POISON,
                ModBulletColors.SACRED_HEART, ModBulletColors.IPECAC)) {
            if (!registry.containsKey(entry.getId())) {
                int priority = entry == ModBulletColors.BASE ? 0 : entry == ModBulletColors.BLOOD_TEAR ? 5
                        : entry == ModBulletColors.SACRED_HEART ? 60 : entry == ModBulletColors.IPECAC ? 100 : 20;
                int rgb = entry == ModBulletColors.BASE ? 0xE7C5E0 : entry == ModBulletColors.BLOOD_TEAR ? 0xCC171F
                        : entry == ModBulletColors.HOLY_LIGHT ? 0x99DDFF : entry == ModBulletColors.SACRED_HEART ? 0xFFFFFF : 0x5CA45C;
                registry.register(entry.getId(), new BulletColor(rgb, 1, priority));
            }
        }
    }

    private static final class AlternatingRandom extends LegacyRandomSource {
        private int rolls;
        private AlternatingRandom() { super(531); }
        @Override public double nextDouble() { return rolls++ % 2 == 0 ? 0.05 : 0.95; }
    }

    private static final class TestPlayer extends ServerPlayer {
        private AlternatingRandom randomSource;
        private AttributeInstance luck;
        private TestPlayer() { super(null, null, null); }
        @Override public AlternatingRandom getRandom() { return randomSource; }
        @Override public AttributeInstance getAttribute(Attribute attribute) { return attribute == Attributes.LUCK ? luck : null; }
    }
}
