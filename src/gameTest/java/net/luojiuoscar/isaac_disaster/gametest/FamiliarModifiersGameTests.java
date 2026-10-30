package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItems;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.AbstractFlyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.BlueFlyEntity;
import net.luojiuoscar.isaac_disaster.event.ForgeEvents;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarEntityType;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.FamiliarModifierEntry;
import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.ModFamiliarModifiers;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifeEntity;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.event.CuriosEventHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@GameTestHolder("isaac_disaster_familiar_modifiers")
@PrefixGameTestTemplate(false)
public final class FamiliarModifiersGameTests {
    private static final ResourceLocation FAMILIAR_SOURCE = ResourceLocation.parse("isaac_disaster:command");
    private static final AtomicInteger NEXT_FIXTURE = new AtomicInteger();
    public static final ResourceLocation EXTRA_ENHANCEMENT = id("extra_enhancement");
    private static final ResourceLocation EXTRA_VISUAL = id("extra_visual");
    private static final ResourceLocation SUPPORT_FLY = id("support_fly");

    @Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class FamiliarModifierTestRegistration {
        @SubscribeEvent
        public static void registerFamiliarModifierTestDefinitions(RegisterEvent event) {
            event.register(ModFamiliarModifiers.FAMILIAR_MODIFIER_KEY, registry -> {
                registry.register(EXTRA_ENHANCEMENT, new FamiliarModifierEntry(
                        FamiliarModifierEntry.Parameter.ENHANCEMENT, 0.5, AbstractIsaacFamiliarEntity.class::isInstance));
                registry.register(EXTRA_VISUAL, new FamiliarModifierEntry(
                        FamiliarModifierEntry.Parameter.VISUAL_SCALE, 0.25, familiar -> true));
            });
            event.register(Registries.ENTITY_TYPE, registry -> registry.register(SUPPORT_FLY,
                    EntityType.Builder.<SupportFly>of(SupportFly::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F).clientTrackingRange(8).updateInterval(1).build(SUPPORT_FLY.toString())));
            event.register(ModFamiliarEntities.FAMILIAR_ENTITY_KEY, registry -> registry.register(SUPPORT_FLY,
                    new FamiliarEntityType(FamiliarModifierTestRegistration::supportType, 1)));
        }

        @SuppressWarnings("unchecked")
        private static EntityType<SupportFly> supportType() {
            return (EntityType<SupportFly>) ForgeRegistries.ENTITY_TYPES.getValue(SUPPORT_FLY);
        }
    }

    public static final class SupportFly extends BlueFlyEntity {
        private double supportCharge;

        public SupportFly(EntityType<? extends SupportFly> type, Level level) {
            super(type, level);
        }

        @Override
        protected float getContactDamage(ServerPlayer owner, LivingEntity target) { return 4; }

        @Override
        protected void onContactHit(ServerPlayer owner, LivingEntity target, Vec3 hitPosition,
                                    float attemptedDamage, boolean damageAccepted) {
            supportCharge += getEnhancement() * 10;
        }
    }

    @GameTest(template = "trajectory_empty")
    public static void existingFamiliarsTrackCopiesRemovalAndObserverMetadataWithoutResizing(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var knife = f.spawn("mom_knife");
            var fly = f.spawn("blue_fly");
            var spider = f.spawn("blue_spider");
            var locust = f.spawn("locust_of_death");
            var familiars = List.of(knife, fly, spider, locust);
            var widths = familiars.stream().map(Entity::getBbWidth).toList();
            var heights = familiars.stream().map(Entity::getBbHeight).toList();
            f.grant(ModPassiveItems.BFFS.get());
            f.grant(ModPassiveItems.BFFS.get());
            f.grant(ModPassiveItems.HIVE_MIND.get());
            f.grant(ModPassiveItems.HIVE_MIND.get());
            for (var familiar : familiars) {
                familiar.tick();
                float expected = familiar == knife ? 1.25F : 1.5F;
                helper.assertTrue(familiar.getVisualScale() == expected, "Existing entity updates its visual scale");
                var observer = (AbstractIsaacFamiliarEntity) familiar.getType().create(helper.getLevel());
                observer.getEntityData().assignValues(familiar.getEntityData().getNonDefaultValues());
                helper.assertTrue(observer.getVisualScale() == expected, "A new observer receives scale through entity metadata");
                familiar.getEntityData().packDirty();
                familiar.tick();
                helper.assertTrue(familiar.getEntityData().packDirty() == null,
                        "An unchanged visual scale does not dirty metadata every tick");
            }
            f.items.removeFromIndex(f.owner, 0);
            knife.tick();
            helper.assertTrue(knife.getVisualScale() == 1.25F, "One remaining BFFS keeps its full bonus");
            f.items.removeFromIndex(f.owner, 0);
            knife.tick();
            helper.assertTrue(knife.getVisualScale() == 1, "Removing the last BFFS restores the knife");
            f.items.removeFromIndex(f.owner, 0);
            fly.tick();
            helper.assertTrue(fly.getVisualScale() == 1.5F, "One remaining Hive Mind keeps its full bonus");
            f.items.removeFromIndex(f.owner, 0);
            for (int i = 0; i < familiars.size(); i++) {
                var familiar = familiars.get(i);
                familiar.tick();
                helper.assertTrue(familiar.getVisualScale() == 1 && familiar.getBbWidth() == widths.get(i)
                        && familiar.getBbHeight() == heights.get(i), "Removal restores visuals and never changes collision dimensions");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void newEntitiesAndNewOwnerUseCurrentItemsImmediately(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper); Fixture other = new Fixture(helper)) {
            f.grant(ModPassiveItems.BFFS.get());
            f.grant(ModPassiveItems.HIVE_MIND.get());
            var fly = f.spawn("blue_fly");
            var knife = f.spawn("mom_knife");
            helper.assertTrue(fly.getVisualScale() == 1.5F && knife.getVisualScale() == 1.25F,
                    "Initial metadata includes the active visual bonus before a tick");
            fly.setOwner(other.owner);
            helper.assertTrue(fly.getVisualScale() == 1 && fly.getEnhancement() == 0,
                    "Changing owner cannot retain the previous owner's bonus");
            fly.setOwner(f.owner);
            helper.assertTrue(fly.getVisualScale() == 1.5F, "Returning ownership refreshes the scale");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void knifeRealAttackDoublesOnlyWithBffsAndRetainsOriginalGeometry(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var knife = (MomKnifeEntity) f.spawn("mom_knife");
            Zombie target = EntityType.HUSK.create(helper.getLevel());
            target.setNoAi(true);
            target.setNoGravity(true);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
            target.getAttribute(Attributes.ARMOR).setBaseValue(0);
            target.setPos(f.owner.position().add(2, 0, 0));
            helper.assertTrue(helper.getLevel().addFreshEntity(target), "Knife target joins the world");
            try {
                f.grant(ModPassiveItems.HIVE_MIND.get());
                attack(helper, knife, f.owner, target, 10);
                f.grant(ModPassiveItems.BFFS.get());
                attack(helper, knife, f.owner, target, 20);
                f.grant(ModPassiveItems.BFFS.get());
                attack(helper, knife, f.owner, target, 20);
                f.items.removeFromIndex(f.owner, 1);
                attack(helper, knife, f.owner, target, 20);
                f.items.removeFromIndex(f.owner, 1);
                attack(helper, knife, f.owner, target, 10);
            } finally { target.discard(); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void physicalCuriosReloadAndUnequipKeepBonusesAndSetMembershipSymmetric(GameTestHelper helper) {
        try (Fixture original = new Fixture(helper); Fixture restored = new Fixture(helper)) {
            original.owner.getAttribute(ModAttributes.PASSIVE_ITEM_SLOTS.get()).setBaseValue(5);
            tickCurios(original.owner);
            var slots = CuriosApi.getCuriosInventory(original.owner).resolve().orElseThrow()
                    .getStacksHandler(CurioSlotManager.PASSIVE_ITEM).orElseThrow().getStacks();
            slots.setStackInSlot(0, new ItemStack(ModPassiveItems.HIVE_MIND.get()));
            slots.setStackInSlot(1, new ItemStack(ModPassiveItems.BFFS.get()));
            tickCurios(original.owner);
            helper.assertTrue(original.items.getItemCountFromAll(ModPassiveItems.HIVE_MIND.getId()) == 1
                    && original.items.getSetCountFromId(ModSetAbilities.BEELZEBUB.getId()) == 1,
                    "Physical equip contributes one cached item and one set membership");
            CompoundTag saved = original.owner.saveWithoutId(new CompoundTag());
            restored.owner.load(saved);
            DefaultAttributeManager.apply(restored.owner);
            CurioSlotManager.requestReconcile(restored.owner);
            tickCurios(restored.owner);
            tickCurios(restored.owner);
            var fly = restored.spawn("blue_fly");
            var knife = restored.spawn("mom_knife");
            helper.assertTrue(fly.getVisualScale() == 1.5F && knife.getVisualScale() == 1.25F
                    && restored.items.getSetCountFromId(ModSetAbilities.BEELZEBUB.getId()) == 1,
                    "A full player save/load restores both item effects and does not duplicate set membership");
            var loadedSlots = CuriosApi.getCuriosInventory(restored.owner).resolve().orElseThrow()
                    .getStacksHandler(CurioSlotManager.PASSIVE_ITEM).orElseThrow().getStacks();
            loadedSlots.setStackInSlot(1, ItemStack.EMPTY);
            tickCurios(restored.owner);
            knife.tick();
            helper.assertTrue(knife.getVisualScale() == 1 && fly.getVisualScale() == 1.5F,
                    "Physical BFFS removal does not remove Hive Mind");
            loadedSlots.setStackInSlot(0, ItemStack.EMPTY);
            tickCurios(restored.owner);
            fly.tick();
            helper.assertTrue(fly.getVisualScale() == 1
                    && restored.items.getSetCountFromId(ModSetAbilities.BEELZEBUB.getId()) == 0,
                    "Physical Hive Mind removal clears its bonus and set membership");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void descriptionsResolveCurrentLoreAndKeepItemMetadata(GameTestHelper helper) {
        var bffs = ModPassiveItems.BFFS.get();
        var hive = ModPassiveItems.HIVE_MIND.get();
        for (Item item : List.of(bffs, hive)) {
            var ability = ((PassiveItem) item).getAbility();
            var lore = ability.getDesc(new ItemStack(item), null);
            for (var line : lore) {
                String text = line.getString();
                helper.assertTrue(!text.contains("%s") && !text.contains("%%")
                        && !text.contains("item.isaac_disaster."), "The real translation parser resolves every lore line: " + text);
            }
        }
        helper.assertTrue(ItemId.BFFS.getLevel() == 2 && ItemId.HIVE_MIND.getLevel() == 2
                && ModSetAbilities.BEELZEBUB.get().getRequirementCount() == 3,
                "Both collectibles have quality two and Beelzebub is a three-item set");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void registeredAdditionalSourcesAddAndSurviveIndependentPlayerCloning(GameTestHelper helper) {
        try (Fixture original = new Fixture(helper); Fixture clone = new Fixture(helper)) {
            original.grant(ModPassiveItems.HIVE_MIND.get());
            StatManager.addFamiliarModifier(original.owner, EXTRA_ENHANCEMENT, 1);
            StatManager.addFamiliarModifier(original.owner, EXTRA_VISUAL, 1);
            var originalAbility = original.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).resolve().orElseThrow();
            ForgeEvents.onPlayerCloned(new PlayerEvent.Clone(clone.owner, original.owner, true));
            var fly = clone.spawn("blue_fly");
            helper.assertTrue(fly.getEnhancement() == 1.5 && fly.getVisualScale() == 1.75F,
                    "Independent registered sources add after the real death-copy path");
            StatManager.removeFamiliarModifier(clone.owner, EXTRA_ENHANCEMENT, 1);
            helper.assertTrue(fly.getEnhancement() == 1
                    && originalAbility.getFamiliarModifiers().getCount(EXTRA_ENHANCEMENT) == 1,
                    "Changing the clone's counts cannot change the original");
            original.owner.reviveCaps();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void playerNbtRetainsBuiltInAndAdditionalEntryCounts(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.grant(ModPassiveItems.BFFS.get());
            f.grant(ModPassiveItems.HIVE_MIND.get());
            f.grant(ModPassiveItems.HIVE_MIND.get());
            var ability = f.owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).resolve().orElseThrow();
            StatManager.addFamiliarModifier(f.owner, EXTRA_ENHANCEMENT, 1);
            CompoundTag saved = new CompoundTag();
            ability.saveNBTData(saved);
            int setCount = f.items.getSetCountFromId(ModSetAbilities.BEELZEBUB.getId());
            ability.init();
            ability.loadNBTData(saved);
            var modifiers = ability.getFamiliarModifiers();
            helper.assertTrue(modifiers.getCount(ModFamiliarModifiers.HIVE_MIND_ENHANCEMENT.getId()) == 2
                    && modifiers.getCount(ModFamiliarModifiers.HIVE_MIND_VISUAL_SCALE.getId()) == 2
                    && modifiers.getCount(ModFamiliarModifiers.BFFS_ENHANCEMENT.getId()) == 1
                    && modifiers.getCount(EXTRA_ENHANCEMENT) == 1,
                    "Player NBT restores built-in and additional registered source counts");
            helper.assertTrue(f.items.getSetCountFromId(ModSetAbilities.BEELZEBUB.getId()) == setCount,
                    "Reading modifier data does not replay item or set acquisition effects");
            f.items.removeFromIndex(f.owner, 1);
            helper.assertTrue(modifiers.getCount(ModFamiliarModifiers.HIVE_MIND_ENHANCEMENT.getId()) == 1,
                    "Normal removal still decrements restored source counts");
            var fly = f.spawn("blue_fly");
            helper.assertTrue(fly.getEnhancement() == 1.5, "Registered non-item sources also survive player NBT");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void aFamiliarCanSpendEnhancementOnSupportWithoutMultiplyingContactDamage(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var fly = (SupportFly) f.spawn(SUPPORT_FLY);
            StatManager.addFamiliarModifier(f.owner, EXTRA_ENHANCEMENT, 1);
            Zombie target = EntityType.HUSK.create(helper.getLevel());
            target.setNoAi(true);
            target.setNoGravity(true);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
            target.getAttribute(Attributes.ARMOR).setBaseValue(0);
            target.setHealth(100);
            target.setPos(f.owner.position().add(2, 0, 0));
            helper.assertTrue(helper.getLevel().addFreshEntity(target), "Support familiar target joins");
            try {
                fly.tick();
                fly.setPos(target.getBoundingBox().getCenter().add(0, -fly.getBbHeight() * 0.5, 0));
                var contact = AbstractFlyFamiliarEntity.class.getDeclaredMethod("tryContact", ServerPlayer.class, Vec3.class, Vec3.class);
                contact.setAccessible(true);
                helper.assertTrue((boolean) contact.invoke(fly, f.owner, fly.position(), fly.position()), "Support familiar makes real contact");
                helper.assertTrue(target.getHealth() == 96 && fly.supportCharge == 5,
                        "The familiar spends enhancement on support charge while its own four-point contact stays unchanged");
            } catch (ReflectiveOperationException failure) {
                throw new AssertionError(failure);
            } finally { target.discard(); }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void switchingStackingConfigurationReusesStoredCopiesAndPreservesOrdinaryMultipliers(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.grant(ModPassiveItems.HIVE_MIND.get());
            f.grant(ModPassiveItems.HIVE_MIND.get());
            f.grant(ModPassiveItems.CRICKETS_HEAD.get());
            var fly = f.spawn("blue_fly");
            double damage = f.owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
            GameplayTestSettings.with(helper, json -> json.addProperty("allow_same_familiar_modifier_entry_stacking", false), () -> {
                fly.tick();
                helper.assertTrue(fly.getEnhancement() == 1 && fly.getVisualScale() == 1.5F,
                        "Duplicate contributions are stored but inactive by default");
                GameplayTestSettings.with(helper, json -> json.addProperty("allow_same_familiar_modifier_entry_stacking", true), () -> {
                    fly.tick();
                    helper.assertTrue(fly.getEnhancement() == 2 && fly.getVisualScale() == 2
                            && f.owner.getAttributeValue(Attributes.ATTACK_DAMAGE) == damage,
                            "The familiar switch changes both parameters without touching ordinary damage multipliers");
                });
                fly.tick();
                helper.assertTrue(fly.getEnhancement() == 1 && fly.getVisualScale() == 1.5F,
                        "Turning stacking back off restores one-copy values without losing the other count");
            });
        }
        helper.succeed();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("familiar_modifier_test", path);
    }

    private static void attack(GameTestHelper helper, MomKnifeEntity knife, ServerPlayer owner, Zombie target, float damage) {
        target.setHealth(100);
        target.invulnerableTime = 0;
        try {
            var targetField = MomKnifeEntity.class.getDeclaredField("targetId");
            targetField.setAccessible(true);
            targetField.set(knife, target.getUUID());
            var victimsField = MomKnifeEntity.class.getDeclaredField("hitVictims");
            victimsField.setAccessible(true);
            ((Set<?>) victimsField.get(knife)).clear();
            var attack = MomKnifeEntity.class.getDeclaredMethod("damageTarget", ServerPlayer.class, Vec3.class, Vec3.class);
            attack.setAccessible(true);
            Vec3 anchor = target.getBoundingBox().getCenter().add(0, 0.4, 0);
            attack.invoke(knife, owner, anchor, anchor);
        } catch (InvocationTargetException failure) {
            throw new AssertionError(failure.getCause());
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
        helper.assertTrue(Math.abs(target.getHealth() - (100 - damage)) < 0.001,
                "The real knife attack applies expected damage " + damage + ", actual health=" + target.getHealth());
    }

    private static void tickCurios(ServerPlayer player) {
        CurioSlotManager.tick(player);
        new CuriosEventHandler().tick(new LivingEvent.LivingTickEvent(player));
        CurioSlotManager.tick(player);
    }

    private static final class Fixture implements AutoCloseable {
        private final GameTestHelper helper;
        private final ServerPlayer owner;
        private final PlayerIsaacItems items;
        private final List<AbstractIsaacFamiliarEntity> familiars = new ArrayList<>();

        private Fixture(GameTestHelper helper) {
            this.helper = helper;
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "familiar-buff"));
            owner.setPos(helper.absolutePos(BlockPos.ZERO).getCenter().add(3, 210 + 3 * NEXT_FIXTURE.getAndIncrement(), 3));
            DefaultAttributeManager.apply(owner);
            owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5);
            items = owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).resolve().orElseThrow();
        }

        private void grant(Item item) {
            ItemStack stack = new ItemStack(item);
            ((PassiveAbility) ((PassiveItem) item).getAbility()).onObtain(owner, stack);
            items.addItem(owner, stack);
        }

        private AbstractIsaacFamiliarEntity spawn(String type) {
            return spawn(ResourceLocation.fromNamespaceAndPath("isaac_disaster", type));
        }

        private AbstractIsaacFamiliarEntity spawn(ResourceLocation type) {
            var spawned = FamiliarHelper.spawnFamiliars(owner, type, FAMILIAR_SOURCE, owner.position());
            helper.assertTrue(spawned.size() == 1, "Factory admits familiar " + type);
            familiars.addAll(spawned);
            return spawned.get(0);
        }

        @Override
        public void close() {
            familiars.forEach(AbstractIsaacFamiliarEntity::discardAndUnlink);
            items.clearPlayerPassiveItems(owner);
            owner.discard();
        }
    }
}
