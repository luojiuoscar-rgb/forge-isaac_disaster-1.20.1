package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.BlueFlyEntity;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarEntityType;
import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarLimitGroup;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarLimitGroups;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** Exercises granted quota through registered addon types and actual server entities. */
@GameTestHolder("isaac_disaster_blue_fly")
@PrefixGameTestTemplate(false)
public final class FamiliarQuotaGameTests {
    private static final ResourceLocation FAMILIAR_SOURCE = ResourceLocation.parse("isaac_disaster:command");
    private static final ResourceLocation LOW_DROP = id("low_drop");
    private static final ResourceLocation LOW_RETAIN = id("low_retain");
    private static final ResourceLocation HIGH = id("high");
    private static final AtomicInteger NEXT_FIXTURE = new AtomicInteger();

    @Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    // EventBus 6 keys wrappers by simple class and method names; avoid other nested Types.register handlers.
    public static final class FamiliarQuotaRegistration {
        private static final FamiliarLimitGroup GROUP = new FamiliarLimitGroup(2);

        @SubscribeEvent
        public static void registerQuotaTypes(RegisterEvent event) {
            event.register(Registries.ENTITY_TYPE, helper -> {
                IsaacDisaster.LOGGER.info("Registering familiar quota test EntityTypes: {}", List.of(LOW_DROP, LOW_RETAIN, HIGH));
                for (ResourceLocation id : List.of(LOW_DROP, LOW_RETAIN, HIGH)) {
                    helper.register(id, EntityType.Builder.<BlueFlyEntity>of(BlueFlyEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F).clientTrackingRange(8).updateInterval(1).build(id.toString()));
                }
            });
            event.register(ModFamiliarLimitGroups.FAMILIAR_LIMIT_GROUP_KEY, helper -> {
                IsaacDisaster.LOGGER.info("Registering familiar quota test group: {}", id("group"));
                helper.register(id("group"), GROUP);
            });
            event.register(ModFamiliarEntities.FAMILIAR_ENTITY_KEY, helper -> {
                IsaacDisaster.LOGGER.info("Registering familiar quota test descriptors: {}", List.of(LOW_DROP, LOW_RETAIN, HIGH));
                helper.register(LOW_DROP, descriptor(LOW_DROP, 0, false));
                helper.register(LOW_RETAIN, descriptor(LOW_RETAIN, 0, true));
                helper.register(HIGH, descriptor(HIGH, 10, true));
            });
        }

        private static FamiliarEntityType descriptor(ResourceLocation id, int priority, boolean retain) {
            // Resolve only when the descriptor is consumed, regardless of registry event order.
            return new FamiliarEntityType(() -> entityType(id), 4, () -> GROUP, priority, retain);
        }

        @SuppressWarnings("unchecked")
        private static EntityType<? extends BlueFlyEntity> entityType(ResourceLocation id) {
            return (EntityType<? extends BlueFlyEntity>) ForgeRegistries.ENTITY_TYPES.getValue(id);
        }
    }

    @GameTest(template = "trajectory_empty", batch = "familiar_quota")
    public static void canceledWorldJoinKeepsGrantedReplacementWaitingForRetry(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            fixture.data.setCount(LOW_DROP, FAMILIAR_SOURCE, 2);
            fixture.spawn(LOW_DROP);
            fixture.spawn(LOW_DROP);
            List<UUID> original = fixture.ids(LOW_DROP);
            AbstractIsaacFamiliarEntity first = fixture.entity(original.get(0));
            AbstractIsaacFamiliarEntity tail = fixture.entity(original.get(1));
            fixture.data.queueRecall(tail.getUUID());
            fixture.data.setCount(HIGH, FAMILIAR_SOURCE, 1);
            helper.assertTrue(fixture.data.getCount(LOW_DROP) == 1 && fixture.data.getEffectiveCount(LOW_DROP) == 1
                            && fixture.data.getCount(HIGH) == 1 && fixture.data.getEffectiveCount(HIGH) == 1,
                    "The high-priority requirement immediately replaces one non-retained low-priority slot");
            FamiliarHelper.maintainExistingFamiliars(fixture.owner);
            helper.assertTrue(fixture.ids(LOW_DROP).equals(List.of(first.getUUID())) && !first.isRemoved()
                            && tail.isRemoved() && fixture.data.pollRecall() == null,
                    "Reconciliation removes the displaced tail and its queued recall before the incoming spawn");
            AtomicInteger canceled = new AtomicInteger();
            Consumer<EntityJoinLevelEvent> listener = event -> {
                if (!(event.getEntity() instanceof BlueFlyEntity fly)
                        || fly.getOwner() != fixture.owner || !HIGH.equals(fly.getFamiliarType())) return;
                canceled.incrementAndGet();
                helper.assertTrue(tail.isRemoved() && fixture.data.getEffectiveCount(HIGH) == 1,
                        "The real join callback sees the already committed quota replacement");
                event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, EntityJoinLevelEvent.class, listener);
            try {
                helper.assertTrue(!FamiliarHelper.spawnNextMissingFamiliar(fixture.owner),
                        "A canceled real world join does not create an incoming entity");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
            }
            helper.assertTrue(canceled.get() == 1 && fixture.ids(LOW_DROP).equals(List.of(first.getUUID()))
                            && !first.isRemoved() && tail.isRemoved(),
                    "Canceling the incoming join keeps the completed tail replacement and earliest UUID");
            helper.assertTrue(fixture.data.getCount(LOW_DROP) == 1 && fixture.data.getEffectiveCount(LOW_DROP) == 1
                            && fixture.data.getCount(HIGH) == 1 && fixture.data.getEffectiveCount(HIGH) == 1
                            && fixture.ids(HIGH).isEmpty(),
                    "The canceled join leaves incoming quota waiting and never restores displaced ownership");
            fixture.spawn(HIGH);
            helper.assertTrue(fixture.ids(LOW_DROP).equals(List.of(first.getUUID())) && !first.isRemoved()
                            && tail.isRemoved() && fixture.data.getCount(LOW_DROP) == 1
                            && fixture.data.getEffectiveCount(LOW_DROP) == 1 && fixture.data.pollRecall() == null,
                    "Retry creates the incoming entity with its existing quota and leaves the surviving low familiar alone");
            helper.assertTrue(fixture.data.getEffectiveCount(HIGH) == 1 && fixture.ids(HIGH).size() == 1
                            && !FamiliarHelper.spawnNextMissingFamiliar(fixture.owner),
                    "The successful retry consumes no extra shared slot and creates no duplicate entity");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "familiar_quota")
    public static void replacementUsesLatestEqualPriorityTypeAndRetainedRecordsCanReturn(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            fixture.data.setCount(LOW_DROP, FAMILIAR_SOURCE, 1);
            fixture.spawn(LOW_DROP);
            fixture.data.setCount(LOW_RETAIN, FAMILIAR_SOURCE, 1);
            fixture.spawn(LOW_RETAIN);
            AbstractIsaacFamiliarEntity earlyType = fixture.entity(fixture.ids(LOW_DROP).get(0));
            AbstractIsaacFamiliarEntity latestType = fixture.entity(fixture.ids(LOW_RETAIN).get(0));
            fixture.data.setCount(LOW_RETAIN, FAMILIAR_SOURCE, 8);
            fixture.data.queueRecall(latestType.getUUID());
            fixture.data.setCount(HIGH, FAMILIAR_SOURCE, 1);
            helper.assertTrue(fixture.data.getEffectiveCount(LOW_RETAIN) == 0
                            && fixture.data.getEffectiveCount(LOW_DROP) == 1
                            && fixture.data.getEffectiveCount(HIGH) == 1,
                    "Priority replacement chooses the latest equal-priority type as soon as requirements change");
            fixture.spawn(HIGH);
            helper.assertTrue(latestType.isRemoved() && !earlyType.isRemoved() && fixture.data.pollRecall() == null,
                    "The latest registered lowest-priority type yields first and loses its stale recall");
            helper.assertTrue(fixture.data.getCount(LOW_RETAIN) == 8 && fixture.data.getEffectiveCount(LOW_RETAIN) == 0,
                    "A retained victim keeps recorded overflow after losing its runtime allocation");
            fixture.data.setCount(HIGH, FAMILIAR_SOURCE, 2);
            fixture.spawn(HIGH);
            helper.assertTrue(earlyType.isRemoved() && fixture.data.getCount(LOW_DROP) == 0,
                    "A displaced non-retained type permanently loses its recorded quantity");
            fixture.data.setCount(HIGH, FAMILIAR_SOURCE, 0);
            FamiliarHelper.maintainExistingFamiliars(fixture.owner);
            fixture.spawn(LOW_RETAIN);
            fixture.spawn(LOW_RETAIN);
            helper.assertTrue(fixture.data.getCount(LOW_RETAIN) == 8
                            && fixture.data.getEffectiveCount(LOW_RETAIN) == 2 && fixture.ids(LOW_RETAIN).size() == 2
                            && !FamiliarHelper.spawnNextMissingFamiliar(fixture.owner),
                    "Retained records rebuild real entities up to the shared group capacity");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "familiar_quota")
    public static void allocationsArePerPlayerAndMissingEntitiesReuseTheirGrantedSlot(GameTestHelper helper) {
        try (Fixture first = new Fixture(helper); Fixture second = new Fixture(helper)) {
            for (Fixture fixture : List.of(first, second)) {
                fixture.data.setCount(LOW_RETAIN, FAMILIAR_SOURCE, 8);
                helper.assertTrue(fixture.data.getEffectiveCount(LOW_RETAIN) == 2 && fixture.ids(LOW_RETAIN).isEmpty(),
                        "Each player receives the registered group's full capacity before any entity joins");
                fixture.spawn(LOW_RETAIN);
                fixture.spawn(LOW_RETAIN);
            }
            List<UUID> secondRoster = second.ids(LOW_RETAIN);
            AbstractIsaacFamiliarEntity lost = first.entity(first.ids(LOW_RETAIN).get(0));
            lost.discardSilently();
            FamiliarHelper.maintainExistingFamiliars(first.owner);
            helper.assertTrue(first.ids(LOW_RETAIN).size() == 1 && first.data.getEffectiveCount(LOW_RETAIN) == 2,
                    "A missing runtime entity keeps its existing shared allocation");
            first.spawn(LOW_RETAIN);
            helper.assertTrue(!first.ids(LOW_RETAIN).contains(lost.getUUID())
                            && first.ids(LOW_RETAIN).size() == 2 && first.data.getEffectiveCount(LOW_RETAIN) == 2
                            && first.data.getCount(LOW_RETAIN) == 8
                            && !FamiliarHelper.spawnNextMissingFamiliar(first.owner),
                    "Respawn replaces the missing UUID without charging another slot or consuming retained records");
            helper.assertTrue(second.ids(LOW_RETAIN).equals(secondRoster)
                            && second.data.getEffectiveCount(LOW_RETAIN) == 2
                            && secondRoster.stream().allMatch(id -> !second.entity(id).isRemoved()),
                    "Both players independently own the full capacity of the same registered group");
        }
        helper.succeed();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("familiar_quota_test", path);
    }

    private static final class Fixture implements AutoCloseable {
        private final GameTestHelper helper;
        private final ServerPlayer owner;
        private final PlayerFamiliarData data;

        private Fixture(GameTestHelper helper) {
            this.helper = helper;
            owner = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "quota_test"));
            Vec3 anchor = helper.absoluteVec(new Vec3(4, 3, 4));
            owner.setPos(anchor.x, 120 + NEXT_FIXTURE.getAndIncrement() * 8, anchor.z);
            owner.setHealth(owner.getMaxHealth());
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
        }

        private void spawn(ResourceLocation type) {
            int before = ids(type).size();
            FamiliarEntityType descriptor = FamiliarHelper.getFamiliarType(type);
            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(type);
            helper.assertTrue(descriptor != null,
                    "The addon descriptor is registered: " + type + "; entity registered="
                            + ForgeRegistries.ENTITY_TYPES.containsKey(type));
            helper.assertTrue(ForgeRegistries.ENTITY_TYPES.containsKey(type)
                            && descriptor.getEntityType() == entityType
                            && FamiliarHelper.isRegisteredFamiliarType(type),
                    "The addon descriptor and EntityType IDs match: requested=" + type
                            + ", descriptor entity key=" + ForgeRegistries.ENTITY_TYPES.getKey(descriptor.getEntityType())
                            + ", direct entity key=" + ForgeRegistries.ENTITY_TYPES.getKey(entityType));
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "The real scheduler admits " + type);
            helper.assertTrue(ids(type).size() == before + 1, "The expected type receives one real entity");
            AbstractIsaacFamiliarEntity familiar = entity(ids(type).get(before));
            helper.assertTrue(familiar.isValidFamiliar() && type.equals(familiar.getFamiliarType()),
                    "The accepted entity belongs to the owner and exact registered type");
        }

        private List<UUID> ids(ResourceLocation type) {
            return data.getEntry(type).map(entry -> List.copyOf(entry.getEntityIds())).orElse(List.of());
        }

        private AbstractIsaacFamiliarEntity entity(UUID id) {
            return (AbstractIsaacFamiliarEntity) helper.getLevel().getEntity(id);
        }

        @Override
        public void close() {
            FamiliarHelper.discardAllRuntimeFamiliars(owner);
            data.clearRequirements();
            data.pruneEmptyEntries();
            owner.discard();
        }
    }
}
