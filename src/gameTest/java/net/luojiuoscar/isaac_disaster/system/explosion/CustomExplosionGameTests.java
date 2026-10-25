package net.luojiuoscar.isaac_disaster.system.explosion;

import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder("isaac_disaster_explosion")
@PrefixGameTestTemplate(false)
public final class CustomExplosionGameTests {
    @GameTest(template = "trajectory_empty", batch = "custom_explosion")
    public static void invalidCentersCancelBeforeEventsOrWorldChanges(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Husk target = fixture.husk(fixture.center.add(1, 0, 0));
            BlockPos stone = fixture.place(new BlockPos(2, 0, 0), Blocks.STONE);
            AtomicInteger starts = new AtomicInteger();
            AtomicInteger detonations = new AtomicInteger();
            Consumer<ExplosionEvent.Start> startListener = event -> starts.incrementAndGet();
            Consumer<ExplosionEvent.Detonate> detonateListener = event -> detonations.incrementAndGet();
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, ExplosionEvent.Start.class, startListener);
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, ExplosionEvent.Detonate.class, detonateListener);
            try {
                for (double invalid : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
                    for (Vec3 center : List.of(new Vec3(invalid, fixture.center.y, fixture.center.z),
                            new Vec3(fixture.center.x, invalid, fixture.center.z),
                            new Vec3(fixture.center.x, fixture.center.y, invalid))) {
                        helper.assertTrue(!fixture.explode(center, fixture.source(), ExplosionParameters.standard(4, 50)),
                                "An invalid center cancels the explosion without throwing: " + center);
                    }
                }
                helper.assertTrue(starts.get() == 0 && detonations.get() == 0,
                        "Invalid-center cancellation occurs before Forge explosion events");
                near(helper, target.getHealth(), 200, "Invalid centers cannot damage entities");
                helper.assertTrue(target.getDeltaMovement().equals(Vec3.ZERO), "Invalid centers cannot push entities");
                helper.assertTrue(helper.getLevel().getBlockState(stone).is(Blocks.STONE),
                        "Invalid centers cannot destroy blocks");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(startListener);
                MinecraftForge.EVENT_BUS.unregister(detonateListener);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "custom_explosion")
    public static void arbitraryDirectSourceRetainsOwnerForChainedTnt(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Husk owner = fixture.husk(fixture.center.add(20, 0, 0));
            ItemEntity direct = new ItemEntity(helper.getLevel(), fixture.center.x, fixture.center.y,
                    fixture.center.z, new ItemStack(Items.STONE));
            fixture.place(BlockPos.ZERO, Blocks.TNT);
            try {
                fixture.explode(fixture.center, helper.getLevel().damageSources().explosion(direct, owner),
                        ExplosionParameters.standard(4, 0));
                var chained = helper.getLevel().getEntitiesOfClass(PrimedTnt.class,
                        new AABB(fixture.center, fixture.center).inflate(3));
                helper.assertTrue(chained.size() == 1 && chained.get(0).getOwner() == owner,
                        "An Entity-based carrier retains the responsible owner for chained vanilla TNT");
            } finally {
                for (PrimedTnt tnt : helper.getLevel().getEntitiesOfClass(PrimedTnt.class,
                        new AABB(fixture.center, fixture.center).inflate(3))) tnt.discard();
                direct.discard();
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "custom_explosion")
    public static void legacyHelperUsesOneRequestedDamageAndIncludesItsLivingSource(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Husk source = fixture.husk(fixture.center);
            LevelHelper.explodeCustom(source, fixture.center, 4, 7, false, false);
            near(helper, source.getHealth(), 193, "A living direct source can take exactly the requested center damage");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "custom_explosion")
    public static void friendlyFilterProtectsDamageAndKnockback(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Husk source = fixture.husk(fixture.center);
            fixture.explode(fixture.center, source.damageSources().explosion(source, source),
                    ExplosionParameters.standard(4, 30).withIgnoreFriendly(true));
            near(helper, source.getHealth(), 200, "Friendly direct source takes no damage");
            helper.assertTrue(source.getDeltaMovement().equals(Vec3.ZERO), "Friendly source receives no knockback");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "custom_explosion")
    public static void radiusAndCenterDamageAreIndependentAndAttributionSurvives(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Husk owner = fixture.husk(fixture.center.add(20, 0, 0));
            Husk middle = fixture.husk(fixture.center.add(4, 0, 0));
            Husk outside = fixture.husk(fixture.center.add(8.1, 0, 0));
            fixture.explode(fixture.center, owner.damageSources().explosion(owner, owner),
                    ExplosionParameters.standard(4, 17).withBlockInteraction(Explosion.BlockInteraction.KEEP));
            near(helper, middle.getHealth(), 193, "At half radius the requested center17 yields7 damage");
            near(helper, outside.getHealth(), 200, "A target beyond radius8 is untouched");
            helper.assertTrue(middle.getLastDamageSource().getEntity() == owner
                            && middle.getLastDamageSource().getDirectEntity() == owner,
                    "Independent world explosion retains direct and responsible entities");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "custom_explosion")
    public static void startCancellationPreventsBlocksDamageAndKnockback(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Husk target = fixture.husk(fixture.center.add(1, 0, 0));
            BlockPos stone = fixture.place(new BlockPos(2, 0, 0), Blocks.STONE);
            Consumer<ExplosionEvent.Start> listener = event -> {
                if (event.getExplosion().getPosition().equals(fixture.center)) event.setCanceled(true);
            };
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, ExplosionEvent.Start.class, listener);
            try {
                helper.assertTrue(!fixture.explode(fixture.center, fixture.source(), ExplosionParameters.standard(4, 50)),
                        "Canceled explosion returns false");
                near(helper, target.getHealth(), 200, "Canceled explosion cannot damage");
                helper.assertTrue(target.getDeltaMovement().equals(Vec3.ZERO), "Canceled explosion cannot push");
                helper.assertTrue(helper.getLevel().getBlockState(stone).is(Blocks.STONE), "Canceled explosion cannot break blocks");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "custom_explosion")
    public static void detonateCanRemoveAnEntityFromDamageAndKnockback(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Husk target = fixture.husk(fixture.center.add(1, 0, 0));
            Consumer<ExplosionEvent.Detonate> listener = event -> event.getAffectedEntities().remove(target);
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, ExplosionEvent.Detonate.class, listener);
            try {
                helper.assertTrue(fixture.explode(fixture.center, fixture.source(), ExplosionParameters.standard(4, 50)),
                        "Uncanceled explosion executes");
                near(helper, target.getHealth(), 200, "Detonate removal prevents damage");
                helper.assertTrue(target.getDeltaMovement().equals(Vec3.ZERO), "Detonate removal prevents knockback");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "custom_explosion")
    public static void specialBlockModeProtectsHighResistanceButReachesBehindIt(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            BlockPos shield = fixture.place(BlockPos.ZERO, Blocks.OBSIDIAN);
            BlockPos behind = fixture.place(new BlockPos(1, 0, 0), Blocks.STONE);
            fixture.explode(fixture.center, fixture.source(), ExplosionParameters.standard(4, 0));
            helper.assertTrue(helper.getLevel().getBlockState(behind).is(Blocks.STONE), "Normal energy is absorbed by obsidian");
            var giga = ExplosionParameters.giga(4);
            fixture.explode(fixture.center, fixture.source(), giga);
            helper.assertTrue(helper.getLevel().getBlockState(shield).is(Blocks.OBSIDIAN), "High-resistance block remains");
            helper.assertTrue(helper.getLevel().getBlockState(behind).isAir(), "Ignoring resistance reaches the block behind the shield");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "custom_explosion")
    public static void zeroDamageAndZeroGeometryDisableOnlyTheirOwnAxis(GameTestHelper helper) {
        try (Fixture fixture = new Fixture(helper)) {
            Husk target = fixture.husk(fixture.center.add(1, 0, 0));
            fixture.explode(fixture.center, fixture.source(), ExplosionParameters.standard(4, 0));
            near(helper, target.getHealth(), 200, "Zero center damage never produces the old minimum-one damage");
            target.setDeltaMovement(Vec3.ZERO);
            fixture.explode(fixture.center, fixture.source(), ExplosionParameters.standard(0, 50));
            near(helper, target.getHealth(), 200, "Zero entity radius cannot damage");
            helper.assertTrue(target.getDeltaMovement().equals(Vec3.ZERO), "Zero radius cannot push");
        }
        helper.succeed();
    }

    private static void near(GameTestHelper helper, double actual, double expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < 0.002, message + "; actual=" + actual + ", expected=" + expected);
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper helper;
        final Vec3 center;
        final List<Husk> entities = new ArrayList<>();
        final List<BlockPos> blocks = new ArrayList<>();

        Fixture(GameTestHelper helper) {
            this.helper = helper;
            BlockPos origin = helper.absolutePos(BlockPos.ZERO);
            center = new Vec3(origin.getX() + 3.5, 180.5, origin.getZ() + 3.5);
        }

        Husk husk(Vec3 position) {
            Husk husk = EntityType.HUSK.create(helper.getLevel());
            husk.setNoAi(true);
            husk.setNoGravity(true);
            husk.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
            husk.getAttribute(Attributes.ARMOR).setBaseValue(0);
            husk.setHealth(200);
            husk.setPos(position);
            helper.assertTrue(helper.getLevel().addFreshEntity(husk), "Husk added");
            entities.add(husk);
            return husk;
        }

        BlockPos place(BlockPos offset, Block block) {
            BlockPos pos = BlockPos.containing(center).offset(offset);
            blocks.add(pos);
            helper.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
            return pos;
        }

        DamageSource source() {
            return helper.getLevel().damageSources().explosion(null, null);
        }

        boolean explode(Vec3 position, DamageSource source, ExplosionParameters parameters) {
            return LevelHelper.explodeCustom(helper.getLevel(), position, source, parameters);
        }

        @Override
        public void close() {
            for (Husk husk : entities) husk.discard();
            for (BlockPos pos : blocks) helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }
}
