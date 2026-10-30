package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import com.mojang.authlib.GameProfile;
import com.google.gson.Gson;
import net.minecraft.core.registries.BuiltInRegistries;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.datapack.GameTestDatapackSettings;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.item.ModActiveItems;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ModActiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_d10")
@PrefixGameTestTemplate(false)
public final class D10GameTests {
    @GameTest(template = "trajectory_empty", batch = "d10")
    public static void mobScopeExcludesArmorStandsWithoutAnExplicitTagEntry(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            LivingEntity stand = f.living(EntityType.ARMOR_STAND, f.origin.add(1, 0, 0));
            Mob cow = f.mob(EntityType.COW, f.origin.add(2, 0, 0));
            helper.assertTrue(!EntityType.ARMOR_STAND.is(TagManager.D10_EXCLUDED_ENTITIES), "Armor stand has no explicit exclusion");
            helper.assertTrue(!D10.isCandidate(EntityType.ARMOR_STAND, f.level), "Non-Mob living types are not random results");
            helper.assertTrue(!D10.eligible(f.owner, stand, f.level), "Non-Mob living targets are excluded by class");
            helper.assertTrue(!D10.replace(f.level, cow, EntityType.ARMOR_STAND) && cow.isAlive(), "Result creation also rejects a non-Mob");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "d10")
    public static void replacementInitializesBeforeScalingAndCopiesOnlyPositionAndHealth(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            helper.assertTrue(D10.isCandidate(EntityType.COW, f.level) && D10.isCandidate(EntityType.ZOMBIE, f.level), "Animals and monsters are candidates");
            helper.assertTrue(!D10.isCandidate(EntityType.PLAYER, f.level) && !D10.isCandidate(EntityType.ITEM, f.level)
                    && !D10.isCandidate(EntityType.FISHING_BOBBER, f.level), "Players, nonliving and nonsummonable types are excluded");
            Mob original = f.mob(EntityType.COW, f.origin.add(1, 0, 0));
            original.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40);
            original.setHealth(10);
            original.setCustomName(Component.literal("old name"));
            original.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200));
            original.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
            original.moveTo(original.getX(), original.getY(), original.getZ(), 37, -16);
            Consumer<MobSpawnEvent.FinalizeSpawn> initialize = event -> {
                if (event.getSpawnType() == MobSpawnType.CONVERSION && event.getEntity().level() == f.level) {
                    event.getEntity().getAttribute(Attributes.MAX_HEALTH).setBaseValue(80);
                }
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, MobSpawnEvent.FinalizeSpawn.class, initialize);
            try {
                helper.assertTrue(D10.replace(f.level, original, EntityType.COW), "Same-type replacement is allowed");
                LivingEntity next = f.lastLiving();
                helper.assertTrue(original.isRemoved(), "Old entity is discarded");
                helper.assertTrue(!next.getUUID().equals(original.getUUID()), "Replacement gets a new UUID");
                helper.assertTrue(next.position().equals(original.position()), "Position is exact");
                helper.assertTrue(next.getYRot() == 37 && next.getXRot() == -16, "Rotation is retained");
                helper.assertTrue(next.getHealth() == 20, "Quarter health uses initialized maximum of 80");
                helper.assertTrue(!next.hasCustomName() && !next.hasEffect(MobEffects.MOVEMENT_SPEED), "Name and effects are not copied");
                helper.assertTrue(next.getItemBySlot(EquipmentSlot.HEAD).isEmpty(), "Equipment is not copied");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(initialize);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "d10")
    public static void shulkerInitializationCannotSnapTheInitialReplacementPositionOrRotation(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            Mob original = f.mob(EntityType.COW, f.origin.add(0.23, 0.37, 0.41));
            original.setYRot(37);
            original.setXRot(-16);
            helper.assertTrue(D10.replace(f.level, original, EntityType.SHULKER), "Shulker is a valid replacement");
            LivingEntity next = f.lastLiving();
            helper.assertTrue(next.position().equals(original.position()), "Native grid snapping does not change the initial position");
            helper.assertTrue(next.getYRot() == 37 && next.getXRot() == -16, "FinalizeSpawn does not overwrite requested rotation");
            helper.assertTrue(Math.abs(next.getBoundingBox().getCenter().x - next.getX()) < 1e-6, "Collision box moves with exact position");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "d10")
    public static void spawnInitializationCannotLeaveUnregisteredPassengersOnTheNewEntity(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            Mob original = f.mob(EntityType.COW, f.origin.add(1, 0, 0));
            var rider = EntityType.CHICKEN.create(f.level);
            Consumer<MobSpawnEvent.FinalizeSpawn> attach = event -> {
                if (event.getEntity().level() == f.level && event.getSpawnType() == MobSpawnType.CONVERSION) {
                    rider.startRiding(event.getEntity(), true);
                }
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, MobSpawnEvent.FinalizeSpawn.class, attach);
            try {
                helper.assertTrue(D10.replace(f.level, original, EntityType.COW), "Root entity is replaced");
                helper.assertTrue(!f.lastLiving().isVehicle() && rider.getVehicle() == null, "No unregistered passenger is retained");
                helper.assertTrue(f.level.getEntity(rider.getUUID()) == null, "Unregistered child is not independently added");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(attach);
                rider.discard();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "d10")
    public static void unloadedTargetsAreRejectedWithoutLoadingTheirChunk(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            Mob detached = EntityType.COW.create(f.level);
            detached.setPos(2_000_000, 64, 2_000_000);
            helper.assertTrue(!f.level.hasChunkAt(detached.blockPosition()), "Far chunk starts unloaded");
            helper.assertTrue(!D10.eligible(f.owner, detached, f.level), "Unloaded target is ineligible");
            helper.assertTrue(!D10.replace(f.level, detached, EntityType.SHEEP) && detached.isAlive(), "Unloaded replacement safely fails");
            helper.assertTrue(!f.level.hasChunkAt(detached.blockPosition()), "Chunk remains unloaded");
            detached.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "d10")
    public static void cancelledDismountKeepsTheOldEntityAndRemovesTheAddedReplacement(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            Mob original = f.mob(EntityType.COW, f.origin.add(1, 0, 0));
            Mob rider = f.mob(EntityType.CHICKEN, original.position());
            rider.startRiding(original, true);
            Consumer<EntityMountEvent> cancel = event -> {
                if (event.isDismounting() && event.getEntityBeingMounted() == original) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, EntityMountEvent.class, cancel);
            try {
                helper.assertTrue(!D10.replace(f.level, original, EntityType.COW), "Cancelled detach safely rejects replacement");
                helper.assertTrue(original.isAlive() && rider.getVehicle() == original, "Original and its blocked relation remain valid");
                helper.assertTrue(f.lastLiving().isRemoved(), "Already-added replacement is discarded on rejection");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(cancel);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "d10")
    public static void failedCreationAndCancelledSpawnsKeepOriginalAndRiders(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            Mob original = f.mob(EntityType.COW, f.origin.add(1, 0, 0));
            Mob rider = f.mob(EntityType.CHICKEN, original.position());
            rider.startRiding(original, true);
            helper.assertTrue(!D10.replace(f.level, original, EntityType.PLAYER) && original.isAlive(), "Vanilla null factory preserves original");
            Consumer<EntityJoinLevelEvent> cancelJoin = event -> {
                if (event.getEntity().getType() == EntityType.SHEEP) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, EntityJoinLevelEvent.class, cancelJoin);
            try {
                helper.assertTrue(!D10.replace(f.level, original, EntityType.SHEEP), "Join cancellation fails replacement");
                helper.assertTrue(original.isAlive() && rider.getVehicle() == original, "Failure preserves original and riding");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(cancelJoin);
            }
            Consumer<MobSpawnEvent.FinalizeSpawn> cancelSpawn = event -> {
                if (event.getSpawnType() == MobSpawnType.CONVERSION) event.setSpawnCancelled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, MobSpawnEvent.FinalizeSpawn.class, cancelSpawn);
            try {
                helper.assertTrue(!D10.replace(f.level, original, EntityType.SHEEP) && original.isAlive(), "Forge spawn cancellation preserves original");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(cancelSpawn);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "d10")
    public static void successfulReplacementAllowsOverlapDetachesRidersAndDoesNotKill(GameTestHelper helper) {
        try (var f = new Fixture(helper)) {
            Mob original = f.mob(EntityType.COW, f.origin.add(1, 0, 0));
            Mob rider = f.mob(EntityType.CHICKEN, original.position());
            rider.startRiding(original, true);
            var block = original.blockPosition();
            var previous = f.level.getBlockState(block);
            int[] deaths = {0};
            Consumer<LivingDeathEvent> death = event -> { if (event.getEntity() == original) deaths[0]++; };
            MinecraftForge.EVENT_BUS.addListener(death);
            try {
                f.level.setBlockAndUpdate(block, Blocks.STONE.defaultBlockState());
                helper.assertTrue(D10.replace(f.level, original, EntityType.COW), "Solid overlap is permitted");
                helper.assertTrue(rider.getVehicle() == null && deaths[0] == 0, "Riding ends without a death event");
                helper.assertTrue(f.level.getEntitiesOfClass(ItemEntity.class, new AABB(block).inflate(1)).isEmpty(), "No death loot is created");
                helper.assertTrue(f.level.getBlockState(block).is(Blocks.STONE), "Terrain is unchanged by replacement");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(death);
                f.level.setBlockAndUpdate(block, previous);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "d10")
    public static void useIsSphericalExcludesFriendsAndPlayersAndProcessesOnlyItsInitialSnapshot(GameTestHelper helper) {
        try (var f = new Fixture(helper); var settings = GameTestDatapackSettings.override(helper)) {
            settings.set(GameplayConfig.NEARBY_RANGE, 5.0);
            Mob animal = f.mob(EntityType.COW, f.origin.add(1, 0, 0));
            Mob monster = f.mob(EntityType.ZOMBIE, f.origin.add(0, 0, 2));
            Mob boundary = f.mob(EntityType.COW, f.origin.add(3, 4, 0));
            Mob outside = f.mob(EntityType.COW, f.origin.add(4, 4, 0));
            Wolf friend = (Wolf) f.mob(EntityType.WOLF, f.origin.add(0, 0, 1));
            friend.tame(f.owner);
            LivingEntity excluded = f.living(EntityType.ARMOR_STAND, f.origin.add(0, 0, 3));
            helper.assertTrue(!D10.eligible(f.owner, f.owner, f.level), "All players are excluded");
            int start = f.replacements.size();
            var context = new ExecutableEffectContext(f.owner);
            context.set(ContextKeys.AMPLIFIER, 2.0);
            helper.assertTrue(new D10().applyEffect(context), "Effective server use completes");
            helper.assertTrue(animal.isRemoved() && monster.isRemoved() && boundary.isRemoved(), "Animals, monsters and sphere boundary reroll");
            helper.assertTrue(outside.isAlive() && friend.isAlive() && excluded.isAlive(), "Outside, friendly and excluded targets remain");
            helper.assertTrue(f.owner.isAlive(), "User is not replaced");
            helper.assertTrue(f.replacements.size() - start == 3, "Car Battery amplifier still produces exactly one result per initial target");
            for (var entity : f.replacements.subList(start, f.replacements.size())) {
                helper.assertTrue(!entity.isRemoved() && !entity.getType().is(TagManager.D10_EXCLUDED_ENTITIES), "New results are not recursively processed or excluded types");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "d10")
    public static void targetEligibilityIsRecheckedAndLaterUsesCanRerollAgain(GameTestHelper helper) {
        try (var f = new Fixture(helper); var settings = GameTestDatapackSettings.override(helper)) {
            settings.set(GameplayConfig.NEARBY_RANGE, 5.0);
            Mob first = f.mob(EntityType.COW, f.origin.add(1, 0, 0));
            Mob second = f.mob(EntityType.COW, f.origin.add(2, 0, 0));
            Consumer<EntityJoinLevelEvent> removeOthers = event -> {
                if (event.getEntity() instanceof LivingEntity && event.getEntity() != f.owner
                        && event.getEntity() != first && event.getEntity() != second) {
                    for (var old : List.of(first, second)) if (!old.isRemoved()) old.discard();
                }
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, EntityJoinLevelEvent.class, removeOthers);
            int start = f.replacements.size();
            try {
                helper.assertTrue(new D10().applyEffect(new ExecutableEffectContext(f.owner)), "Use tolerates target changes during callbacks");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(removeOthers);
            }
            helper.assertTrue(f.replacements.size() == start + 1, "Removed targets are not processed after callbacks");
            LivingEntity next = f.lastLiving();
            f.spawned.stream().filter(entity -> entity != next).forEach(Entity::discard);
            helper.assertTrue(new D10().applyEffect(new ExecutableEffectContext(f.owner)), "Second use succeeds");
            helper.assertTrue(next.isRemoved() && f.replacements.size() == start + 2, "New entity is eligible on a later use");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "d10")
    public static void sixChargeCostAndEmptyUseFollowTheSharedItemFlow(GameTestHelper helper) {
        try (var f = new Fixture(helper); var settings = GameTestDatapackSettings.override(helper)) {
            settings.set(GameplayConfig.NEARBY_RANGE, 0.0);
            ItemStack stack = new ItemStack(ModActiveItems.D10.get());
            f.owner.setItemInHand(InteractionHand.MAIN_HAND, stack);
            helper.assertTrue(stack.getMaxDamage() == 600, "Maximum charge is six bars");
            ModActiveItems.D10.get().use(f.level, f.owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(stack.getDamageValue() == 600, "Empty use consumes all six bars");
            helper.assertTrue(f.owner.getCooldowns().isOnCooldown(stack.getItem()), "Use applies shared cooldown");
            helper.assertTrue(ModActiveAbilities.D10.get().getDesc(stack, f.owner).stream()
                    .allMatch(line -> !line.getString().startsWith("item.isaac_disaster")), "Actual translator resolves all three lore lines");
            f.owner.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).orElseThrow(AssertionError::new)
                    .addItem(f.owner, new ItemStack(ModPassiveItems.CAR_BATTERY.get()));
            settings.set(GameplayConfig.NEARBY_RANGE, 5.0);
            Mob target = f.mob(EntityType.COW, f.origin.add(1, 0, 0));
            int start = f.replacements.size();
            stack.setDamageValue(0);
            f.owner.getCooldowns().removeCooldown(stack.getItem());
            ModActiveItems.D10.get().use(f.level, f.owner, InteractionHand.MAIN_HAND);
            helper.assertTrue(target.isRemoved() && f.replacements.size() == start + 1, "Real Car Battery ownership does not add reroll rounds");
            helper.assertTrue(stack.getDamageValue() == 600, "Car Battery preserves six-bar cost");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "d10_reload", timeoutTicks = 400)
    public static void directAndNestedExclusionsReloadForBothTargetsAndResults(GameTestHelper helper) throws Exception {
        try (var f = new Fixture(helper)) {
            var server = f.level.getServer();
            var directory = server.getWorldPath(LevelResource.ROOT)
                    .resolve("datapacks/isaac_disaster/data/isaac_disaster/tags/entity_types");
            Files.createDirectories(directory);
            var exclusion = directory.resolve("d10_excluded.json");
            var nested = directory.resolve("d10_test_nested.json");
            String original = Files.exists(exclusion) ? Files.readString(exclusion) : null;
            var packs = new ArrayList<>(server.getPackRepository().getSelectedIds());
            try {
                helper.assertTrue(!D10.isCandidate(EntityType.WITHER, f.level) && !D10.isCandidate(EntityType.ENDER_DRAGON, f.level)
                        && !D10.isCandidate(EntityType.ARMOR_STAND, f.level), "Boss tags and non-Mob classes never appear as results");
                Files.writeString(nested, "{\"values\":[\"minecraft:cow\"]}");
                Files.writeString(exclusion, "{\"replace\":false,\"values\":[\"minecraft:pig\",\"#isaac_disaster:d10_test_nested\"]}");
                server.reloadResources(packs).join();
                var cow = f.mob(EntityType.COW, f.origin.add(1, 0, 0));
                var pig = f.mob(EntityType.PIG, f.origin.add(2, 0, 0));
                helper.assertTrue(!D10.isCandidate(EntityType.COW, f.level) && !D10.isCandidate(EntityType.PIG, f.level), "Direct and nested result exclusions are live");
                helper.assertTrue(new D10().applyEffect(new ExecutableEffectContext(f.owner)) && cow.isAlive() && pig.isAlive(), "Reloaded target exclusions are live");
                Files.writeString(exclusion, "{\"values\":[]}");
                server.reloadResources(packs).join();
                helper.assertTrue(D10.isCandidate(EntityType.COW, f.level) && D10.eligible(f.owner, cow, f.level), "Removal of override is visible without stale candidate cache");
                var allTypes = BuiltInRegistries.ENTITY_TYPE.keySet().stream().map(Object::toString).toList();
                Files.writeString(exclusion, new Gson().toJson(Map.of("replace", true, "values", allTypes)));
                server.reloadResources(packs).join();
                helper.assertTrue(!new D10().applyEffect(new ExecutableEffectContext(f.owner)), "An empty candidate pool safely returns false");
                ItemStack stack = new ItemStack(ModActiveItems.D10.get());
                f.owner.setItemInHand(InteractionHand.MAIN_HAND, stack);
                ModActiveItems.D10.get().use(f.level, f.owner, InteractionHand.MAIN_HAND);
                helper.assertTrue(stack.getDamageValue() == 600 && cow.isAlive() && pig.isAlive(), "Even complete exclusion consumes charge without removing targets");
            } finally {
                if (original == null) Files.deleteIfExists(exclusion); else Files.writeString(exclusion, original);
                Files.deleteIfExists(nested);
                server.reloadResources(packs).join();
            }
        }
        helper.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final ServerLevel level;
        final ServerPlayer owner;
        final Vec3 origin;
        final List<Entity> spawned = new ArrayList<>();
        final Set<UUID> conversionIds = new HashSet<>();
        final List<LivingEntity> replacements = new ArrayList<>();
        final Consumer<EntityJoinLevelEvent> tracking;
        final Consumer<MobSpawnEvent.FinalizeSpawn> initialization;

        Fixture(GameTestHelper helper) {
            level = helper.getLevel();
            origin = helper.absoluteVec(new Vec3(3.5, 3, 3.5));
            UUID id = UUID.randomUUID();
            owner = FakePlayerFactory.get(level, new GameProfile(id, "d_" + id.toString().substring(0, 8)));
            owner.setPos(origin);
            level.addNewPlayer(owner);
            initialization = event -> {
                if (event.getEntity().level() == level && event.getSpawnType() == MobSpawnType.CONVERSION) {
                    conversionIds.add(event.getEntity().getUUID());
                }
            };
            tracking = event -> {
                if (event.getLevel() == level && event.getEntity() != owner
                        && event.getEntity().distanceToSqr(origin) < 4096) {
                    spawned.add(event.getEntity());
                    if (event.getEntity() instanceof LivingEntity living && conversionIds.contains(living.getUUID())) {
                        replacements.add(living);
                    }
                }
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, MobSpawnEvent.FinalizeSpawn.class, initialization);
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, EntityJoinLevelEvent.class, tracking);
        }

        LivingEntity living(EntityType<?> type, Vec3 position) {
            LivingEntity entity = (LivingEntity) type.create(level);
            entity.setPos(position);
            entity.setNoGravity(true);
            if (entity instanceof Mob mob) mob.setNoAi(true);
            level.addFreshEntity(entity);
            return entity;
        }

        Mob mob(EntityType<?> type, Vec3 position) {
            return (Mob) living(type, position);
        }

        LivingEntity lastLiving() {
            return replacements.get(replacements.size() - 1);
        }

        @Override
        public void close() {
            MinecraftForge.EVENT_BUS.unregister(tracking);
            MinecraftForge.EVENT_BUS.unregister(initialization);
            spawned.forEach(Entity::discard);
            level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
    }
}
