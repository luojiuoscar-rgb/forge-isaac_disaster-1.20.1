package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItems;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.event.ForgeEvents;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.luojiuoscar.isaac_disaster.manager.MultishotCountManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPlan;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.TheWizAttackPlan;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

import java.util.List;
import java.util.UUID;

@GameTestHolder("isaac_disaster_player_knife")
@PrefixGameTestTemplate(false)
public final class MultishotCountGameTests {
    private static PlayerIsaacItems items(PlayerMomsKnifeGameTests.Fixture f) {
        return f.owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).orElseThrow(IllegalStateException::new);
    }

    private static void assertCount(GameTestHelper helper, PlayerMomsKnifeGameTests.Fixture f, int expected) {
        helper.assertTrue(f.owner.getAttributeValue(ModAttributes.BULLET_COUNT.get()) == expected,
                "The item callbacks update the attribute before an attack reads it");
        int count = ModAttackTypes.BULLET.get().getBulletCount(f.owner);
        helper.assertTrue(count == expected, "Expected multishot " + expected + ", got " + count);
    }

    private static void obtain(PlayerMomsKnifeGameTests.Fixture f, Item item) {
        var stack = new ItemStack(item);
        ((PassiveAbility) ((PassiveItem) item).getAbility()).onObtain(f.owner, stack);
        items(f).addItem(f.owner, stack);
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void combinationsAndBothRemovalOrdersUseAuthoritativeCounts(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(1);
            var first = new ItemStack(ModPassiveItems.PERFECT_VISION.get());
            ((PassiveAbility) ((PassiveItem) first.getItem()).getAbility()).onObtain(f.owner, first);
            assertCount(helper, f, 2);
            helper.assertTrue(items(f).getItemCountFromAll(ModPassiveItems.PERFECT_VISION.getId()) == 0,
                    "Obtain includes the pending copy without recording it early");
            items(f).addItem(f.owner, first);
            obtain(f, ModPassiveItems.PERFECT_VISION.get()); assertCount(helper, f, 2);
            obtain(f, ModPassiveItems.THE_INNER_EYE.get()); assertCount(helper, f, 4);
            obtain(f, ModPassiveItems.MUTANT_SPIDER.get()); assertCount(helper, f, 6);
            items(f).removeFromId(f.owner, ModPassiveItems.THE_INNER_EYE.getId()); assertCount(helper, f, 5);
            items(f).removeFromId(f.owner, ModPassiveItems.PERFECT_VISION.getId()); assertCount(helper, f, 4);
            items(f).removeFromId(f.owner, ModPassiveItems.MUTANT_SPIDER.getId()); assertCount(helper, f, 2);
            items(f).clearPlayerPassiveItems(f.owner); assertCount(helper, f, 1);
            obtain(f, ModPassiveItems.MUTANT_SPIDER.get()); assertCount(helper, f, 4);
            obtain(f, ModPassiveItems.THE_INNER_EYE.get()); assertCount(helper, f, 5);
            items(f).removeFromId(f.owner, ModPassiveItems.MUTANT_SPIDER.getId()); assertCount(helper, f, 3);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void repeatedRefreshesPreserveOtherAttributeSources(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            var attribute = f.owner.getAttribute(ModAttributes.BULLET_COUNT.get());
            attribute.setBaseValue(2);
            UUID other = UUID.randomUUID();
            attribute.addTransientModifier(new AttributeModifier(other, "other source", 3, AttributeModifier.Operation.ADDITION));
            obtain(f, ModPassiveItems.THE_INNER_EYE.get());
            for (int i = 0; i < 4; i++) {
                items(f).refreshItemCountCache();
                MultishotCountManager.update(f.owner);
                assertCount(helper, f, 7);
            }
            items(f).removeFromId(f.owner, ModPassiveItems.THE_INNER_EYE.getId());
            assertCount(helper, f, 5);
            helper.assertTrue(attribute.getModifier(other) != null && attribute.getBaseValue() == 2,
                    "Only the multishot contribution is replaced");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void savedCountsReconcileStaleModifiersAndDoNotStackOnLoginOrRespawn(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            obtain(f, ModPassiveItems.THE_INNER_EYE.get());
            CompoundTag saved = new CompoundTag();
            items(f).saveNBTData(saved);
            StatManager.setModifier(f.owner, MultishotCountManager.MODIFIER_ID, ModAttributes.BULLET_COUNT.get(), 9, null, null, 0);
            items(f).loadNBTData(saved);
            ForgeEvents.onPlayerLoggedIn(new PlayerEvent.PlayerLoggedInEvent(f.owner));
            var stats = f.owner.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).orElseThrow(IllegalStateException::new);
            helper.assertTrue(stats.getStatInstance(MultishotCountManager.MODIFIER_ID).getActualValue() == 2,
                    "Login derives the contribution from saved items instead of trusting a stale modifier");
            ForgeEvents.onPlayerRespawn(new PlayerEvent.PlayerRespawnEvent(f.owner, false));
            ForgeEvents.onPlayerRespawn(new PlayerEvent.PlayerRespawnEvent(f.owner, false));
            helper.assertTrue(stats.getStatInstance(MultishotCountManager.MODIFIER_ID).getActualValue() == 2,
                    "Respawn reconciliation is idempotent");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void realCuriosEquipReplacementUnequipAndReloadReconcileTheSameCounts(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(1);
            f.owner.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(5);
            CurioSlotManager.tick(f.owner);
            var slots = CuriosApi.getCuriosInventory(f.owner).resolve().orElseThrow()
                    .getStacksHandler(CurioSlotManager.PASSIVE_ITEM).orElseThrow().getStacks();
            slots.setStackInSlot(0, new ItemStack(ModPassiveItems.THE_INNER_EYE.get()));
            new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(f.owner));
            CurioSlotManager.tick(f.owner);
            assertCount(helper, f, 3);
            slots.setStackInSlot(0, new ItemStack(ModPassiveItems.MUTANT_SPIDER.get()));
            new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(f.owner));
            CurioSlotManager.tick(f.owner);
            assertCount(helper, f, 4);
            CompoundTag saved = new CompoundTag();
            items(f).saveNBTData(saved);
            items(f).loadNBTData(saved);
            CurioSlotManager.requestReconcile(f.owner);
            CurioSlotManager.tick(f.owner);
            assertCount(helper, f, 4);
            slots.setStackInSlot(0, ItemStack.EMPTY);
            new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(f.owner));
            CurioSlotManager.tick(f.owner);
            assertCount(helper, f, 1);
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void existingAttackTypesReadStableCountsWhileProvidedCountsStayExplicit(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            obtain(f, ModPassiveItems.THE_INNER_EYE.get());
            for (var type : List.of(ModAttackTypes.BULLET.get(), ModAttackTypes.LASER.get(), ModAttackTypes.BRIMSTONE.get(),
                    ModAttackTypes.C_SECTION.get(), ModAttackTypes.CURSED_EYE.get(), ModAttackTypes.MONSTROS_LUNG.get())) {
                helper.assertTrue(type.getBulletCount(f.owner) == 3, "Stable multishot reaches " + type.getId());
                if (type != ModAttackTypes.CURSED_EYE.get()) {
                    helper.assertTrue(type.getAttackContexts(f.owner, 3).size() == 3,
                            "The logical attack count remains three for " + type.getId());
                    helper.assertTrue(type.getAttackContexts(f.owner, 1).size() == 1,
                            "Explicit additional-shot counts do not reapply the item bonus");
                }
            }
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(40);
            helper.assertTrue(ModAttackTypes.BULLET.get().getBulletCount(f.owner) == 17,
                    "The ordinary attack limit is still seventeen");
            helper.assertTrue(HeldMomsKnifeGroup.heldCount(f.owner) == 16, "Only held knives impose a sixteen-knife limit");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "player_knife")
    public static void wizKeepsTheFormerEightContextLimitForMultishotPrimaryAttacks(GameTestHelper helper) {
        try (var f = new PlayerMomsKnifeGameTests.Fixture(helper)) {
            f.owner.getAttribute(ModAttributes.BULLET_COUNT.get()).setBaseValue(40);
            for (var item : List.of(ModPassiveItems.THE_INNER_EYE.get(), ModPassiveItems.MUTANT_SPIDER.get(),
                    ModPassiveItems.PERFECT_VISION.get(), ModPassiveItems.IPECAC.get(), ModPassiveItems.POLYPHEMUS.get())) {
                obtain(f, item);
                for (int amplifier : new int[]{0, 1}) {
                    f.owner.removeEffect(ModEffects.THE_WIZ.get());
                    f.owner.addEffect(new MobEffectInstance(ModEffects.THE_WIZ.get(), 200, amplifier));
                    var event = wizPlan(f, AttackOrigin.PLAYER_PRIMARY);
                    helper.assertTrue(event.getBaseContexts().size() == 8,
                            "Wiz retains eight planned contexts for " + item + ", amplifier=" + amplifier);
                    helper.assertTrue(HeldMomsKnifeGroup.heldCount(f.owner) == 16,
                            "The release-only Wiz limit never changes the held preview");
                }
                var extra = wizPlan(f, AttackOrigin.ABILITY_EXTRA);
                helper.assertTrue(extra.getBaseContexts().size() == 8,
                        "Generated extra attacks share the existing Wiz count-event limit");
                var type = ModAttackTypes.BULLET.get();
                var explicit = AttackRequest.generated(f.owner, type, AttackOrigin.ABILITY_EXTRA, AttackPipelineMode.FULL, false);
                var providedPlan = new AttackPlanEvent(explicit, new AttackPlan(explicit, type.getAttackContexts(f.owner, 17)));
                var context = new ExecutableEffectContext(f.owner);
                context.set(ContextKeys.EVENT, providedPlan);
                helper.assertTrue(new TheWizAttackPlan().applyEffect(context)
                                && providedPlan.getBaseContexts().size() == 18,
                        "Explicit contexts bypass count-event limits and retain their previous Wiz semantics");
                items(f).clearPlayerPassiveItems(f.owner);
            }
            f.owner.removeEffect(ModEffects.THE_WIZ.get());
            f.owner.addEffect(new MobEffectInstance(ModEffects.THE_WIZ.get(), 200));
            helper.assertTrue(wizPlan(f, AttackOrigin.PLAYER_PRIMARY).getBaseContexts().size() == 8,
                    "Wiz alone already limits generated count to eight through the count event");
        }
        helper.succeed();
    }

    private static AttackPlanEvent wizPlan(PlayerMomsKnifeGameTests.Fixture f, AttackOrigin origin) {
        var type = ModAttackTypes.BULLET.get();
        var request = AttackRequest.generated(f.owner, type, origin, AttackPipelineMode.FULL, false);
        var event = new AttackPlanEvent(request, new AttackPlan(request,
                type.getAttackContexts(f.owner, type.getBulletCount(f.owner))));
        var context = new ExecutableEffectContext(f.owner);
        context.set(ContextKeys.EVENT, event);
        if (!new TheWizAttackPlan().applyEffect(context)) throw new IllegalStateException("Invalid Wiz test context");
        return event;
    }
}
