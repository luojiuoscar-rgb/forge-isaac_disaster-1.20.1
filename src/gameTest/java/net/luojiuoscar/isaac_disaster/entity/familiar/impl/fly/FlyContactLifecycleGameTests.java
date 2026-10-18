package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** Isolates contact accounting from navigation, using actual Forge hurt callbacks and registered quota types. */
@GameTestHolder("isaac_disaster_blue_fly")
@PrefixGameTestTemplate(false)
public final class FlyContactLifecycleGameTests {
    private static final ResourceLocation LOW = ResourceLocation.parse("familiar_quota_test:low_drop");
    private static final ResourceLocation HIGH = ResourceLocation.parse("familiar_quota_test:high");
    private static final AtomicInteger NEXT_FIXTURE = new AtomicInteger();

    @GameTest(template = "trajectory_empty", batch = "fly_contact_lifecycle")
    public static void damageCallbackReplacementDoesNotConsumeTheSurvivingFly(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            fixture.onAttack(event -> {
                fixture.data.setCount(HIGH, 1);
                FamiliarHelper.maintainExistingFamiliars(fixture.owner);
            });
            fixture.contact();
            helper.assertTrue(fixture.data.getCount(LOW) == 1 && fixture.data.getEffectiveCount(LOW) == 1,
                    "Contact and callback must leave one low fly; recorded=" + fixture.data.getCount(LOW));
            helper.assertTrue(!fixture.first.isRemoved() && fixture.data.containsEntity(LOW, fixture.first.getUUID()),
                    "The first fly survives because contact frees a slot before the callback adds the higher priority type");
            helper.assertTrue(fixture.data.getCount(HIGH) == 1 && fixture.data.getEffectiveCount(HIGH) == 1,
                    "The callback receives one shared slot without double consumption");
            fixture.assertFinished();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_contact_lifecycle")
    public static void damageCallbackClearAndAddKeepsTheNewRequirement(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            fixture.onAttack(event -> {
                fixture.data.clearRequirements();
                FamiliarHelper.maintainExistingFamiliars(fixture.owner);
                fixture.data.addCount(LOW, 1);
            });
            fixture.contact();
            helper.assertTrue(fixture.data.getCount(LOW) == 1 && fixture.data.getEffectiveCount(LOW) == 1,
                    "Finishing the old hit must not subtract the newly added requirement");
            helper.assertTrue(fixture.first.isRemoved(), "Only the explicit clear removes the other original fly");
            fixture.assertFinished();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fly_contact_lifecycle")
    public static void throwingDamageCallbackStillConsumesAndDiscardsExactlyOnce(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            RuntimeException expected = new RuntimeException("intentional contact callback failure");
            fixture.onAttack(event -> { throw expected; });
            try {
                fixture.contact();
                throw new AssertionError("The intentional callback exception should propagate");
            } catch (RuntimeException failure) {
                helper.assertTrue(failure == expected, "The original callback failure propagates");
            }
            helper.assertTrue(fixture.data.getCount(LOW) == 1 && fixture.data.getEffectiveCount(LOW) == 1,
                    "Exceptional contact consumes its own requirement once");
            fixture.assertFinished();
        }
        helper.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        private final GameTestHelper helper;
        private final ServerPlayer owner;
        private final PlayerFamiliarData data;
        private final AbstractFlyFamiliarEntity first;
        private final AbstractFlyFamiliarEntity tail;
        private final Zombie target;
        private Consumer<LivingAttackEvent> listener;
        private int callbacks;
        private boolean consumedBeforeCallback;

        private Fixture(GameTestHelper helper) {
            this.helper = helper;
            var level = helper.getLevel();
            owner = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "contact_lifecycle"));
            BlockPos anchor = helper.absolutePos(BlockPos.ZERO);
            owner.setPos(anchor.getX() + 4, 220 + NEXT_FIXTURE.getAndIncrement() * 12, anchor.getZ() + 4);
            owner.setGameMode(GameType.SURVIVAL);
            owner.setInvulnerable(false);
            owner.getAbilities().invulnerable = false;
            owner.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4);
            owner.setHealth(owner.getMaxHealth());
            data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
            data.setCount(LOW, 2);
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "First registered low fly generated");
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner), "Tail registered low fly generated");
            var entry = data.getEntry(LOW).orElseThrow();
            first = (AbstractFlyFamiliarEntity) level.getEntity(entry.getEntityId(0));
            tail = (AbstractFlyFamiliarEntity) level.getEntity(entry.getEntityId(1));
            target = EntityType.ZOMBIE.create(level);
            target.setNoAi(true);
            target.setNoGravity(true);
            target.setInvulnerable(false);
            target.setPos(owner.getX() + 2, owner.getY(), owner.getZ());
            helper.assertTrue(level.addFreshEntity(target), "Actual hostile added for contact");
        }

        private void onAttack(Consumer<LivingAttackEvent> callback) {
            listener = event -> {
                if (event.getEntity() != target || event.getSource().getDirectEntity() != tail) return;
                callbacks++;
                consumedBeforeCallback = !data.containsEntity(LOW, tail.getUUID()) && data.getCount(LOW) == 1
                        && !tail.isRemoved() && event.getSource().getEntity() == owner;
                callback.accept(event);
                tail.tickFamiliar();
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, LivingAttackEvent.class, listener);
        }

        private void contact() {
            try {
                FlyOwnerFrame frame = FlyOwnerFrame.get(owner);
                frame.refresh(owner, helper.getLevel().getGameTime());
                var frameField = AbstractFlyFamiliarEntity.class.getDeclaredField("ownerFrame");
                frameField.setAccessible(true);
                frameField.set(tail, frame);
                tail.setPos(target.getBoundingBox().getCenter().add(0, -tail.getBbHeight() * 0.5, 0));
                var contact = AbstractFlyFamiliarEntity.class.getDeclaredMethod("tryContact",
                        ServerPlayer.class, Vec3.class, Vec3.class);
                contact.setAccessible(true);
                helper.assertTrue((boolean) contact.invoke(tail, owner, tail.position(), tail.position()),
                        "An actual stationary contact is detected");
            } catch (InvocationTargetException failure) {
                if (failure.getCause() instanceof RuntimeException runtime) throw runtime;
                if (failure.getCause() instanceof Error error) throw error;
                throw new AssertionError(failure.getCause());
            } catch (ReflectiveOperationException failure) {
                throw new AssertionError(failure);
            }
        }

        private void assertFinished() {
            helper.assertTrue(callbacks == 1, "Reentering the fly tick cannot dispatch a second contact");
            helper.assertTrue(consumedBeforeCallback, "Ownership is consumed before the event, while the direct entity remains alive");
            helper.assertTrue(tail.isRemoved() && !data.containsEntity(LOW, tail.getUUID()),
                    "Finally discards the direct source and leaves its UUID unlinked");
        }

        @Override
        public void close() {
            if (listener != null) MinecraftForge.EVENT_BUS.unregister(listener);
            target.discard();
            FamiliarHelper.discardAllRuntimeFamiliars(owner);
            tail.discardSilently();
            data.clearRequirements();
            data.pruneEmptyEntries();
            owner.discard();
        }
    }
}
