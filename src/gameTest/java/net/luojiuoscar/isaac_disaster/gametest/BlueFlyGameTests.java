package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.BlueFlyEntity;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Separate namespace for the fly's familiar lifecycle, without loading client classes. */
@GameTestHolder("isaac_disaster_blue_fly")
@PrefixGameTestTemplate(false)
public final class BlueFlyGameTests {
    @GameTest(template = "trajectory_empty", batch = "blue_fly")
    public static void flyRemainsRenderableThroughThirtyTwoBlocks(GameTestHelper helper) {
        BlueFlyEntity fly = new BlueFlyEntity(ModEntities.BLUE_FLY.get(), helper.getLevel());
        helper.assertTrue(fly.shouldRenderAtSqrDistance(16 * 16), "A tiny fly remains visible beyond eight blocks");
        helper.assertTrue(fly.shouldRenderAtSqrDistance(32 * 32), "The rendering boundary includes 32 blocks");
        helper.assertTrue(!fly.shouldRenderAtSqrDistance(32.01 * 32.01), "The rendering boundary is still bounded");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "blue_fly", timeoutTicks = 60)
    public static void ownerlessSummonedFlyUsesFamiliarCleanup(GameTestHelper helper) {
        Vec3 position = Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 1, 1)));
        var level = helper.getLevel();
        String command = "summon isaac_disaster:blue_fly "
                + position.x + " " + position.y + " " + position.z;
        int result = level.getServer().getCommands().performPrefixedCommand(
                level.getServer().createCommandSourceStack().withLevel(level).withSuppressedOutput(), command);
        helper.assertTrue(result == 1, "The entity ID remains compatible with /summon");
        var flies = level.getEntitiesOfClass(BlueFlyEntity.class, new AABB(position, position).inflate(0.2));
        helper.assertTrue(flies.size() == 1, "Command creates one blue fly");
        BlueFlyEntity fly = flies.get(0);
        helper.assertTrue(!fly.isValidFamiliar(), "An ownerless fly is not an accepted familiar");
        helper.runAtTickTime(45, () -> {
            helper.assertTrue(fly.isRemoved(), "Ownerless flies are removed by the inherited validity check");
            helper.succeed();
        });
    }

    @GameTest(template = "trajectory_empty", batch = "blue_fly", timeoutTicks = 80)
    public static void familiarCommandCreatesARegisteredBlueFlyAndCanRemoveIt(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "fly-test"));
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 1, 1))));
        var data = player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA)
                .resolve().orElseThrow(() -> new AssertionError("Player familiar capability is attached"));
        var source = player.createCommandSourceStack().withPermission(2).withSuppressedOutput();
        ResourceLocation type = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "blue_fly");
        helper.assertTrue(FamiliarHelper.isRegisteredFamiliarType(type), "Descriptor matches the entity type");
        int result = level.getServer().getCommands().performPrefixedCommand(
                source, "isd familiar @s add isaac_disaster:blue_fly 1");
        helper.assertTrue(result == 1 && data.getCount(type) == 1, "Existing command accepts blue fly requests");
        helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(player), "Existing scheduler creates the fly");
        var entry = data.getEntry(type).orElseThrow();
        BlueFlyEntity fly = (BlueFlyEntity) level.getEntity(entry.getEntityId(0));
        helper.assertTrue(fly != null && fly.getOwner() == player && fly.isValidFamiliar(),
                "Factory sets ownership and records the runtime UUID");
        helper.assertTrue(fly.getBodyColor() == 0x4077E0, "Blue fly keeps its approved color");
        helper.assertTrue(fly.getBbWidth() == 0.125F && fly.getBbHeight() == 0.125F,
                "Body remains two model units wide and high");
        helper.assertTrue(fly.getAddEntityPacket() != null, "Forge client spawn packet is available");
        helper.assertTrue(!fly.shouldBeSaved(), "Runtime familiars are excluded from world entity storage");
        CompoundTag savedRequirements = new CompoundTag();
        data.saveNBTData(savedRequirements);
        PlayerFamiliarData restored = new PlayerFamiliarData();
        restored.loadNBTData(savedRequirements);
        helper.assertTrue(restored.getCount(type) == 1
                        && restored.getEntry(type).orElseThrow().getEntityCount() == 0,
                "Player storage keeps requested counts, while runtime UUIDs are recreated");
        Vec3 spawn = fly.position();
        helper.runAtTickTime(45, () -> {
            helper.assertTrue(!fly.isRemoved() && fly.isValidFamiliar(), "Owned fly survives validity checks");
            helper.assertTrue(fly.isNoGravity() && fly.noPhysics, "Familiar physics rules are inherited");
            helper.assertTrue(fly.position().distanceToSqr(spawn) > 0.25,
                    "An idle fly moves into its orbit instead of staying at the spawn position");
            int removed = level.getServer().getCommands().performPrefixedCommand(
                    source, "isd familiar @s remove isaac_disaster:blue_fly 1");
            helper.assertTrue(removed == 1 && data.getCount(type) == 0 && fly.isRemoved(),
                    "Existing command removes both the requirement and live fly");
            helper.assertTrue(!data.containsEntity(type, fly.getUUID()), "Removal unlinks the runtime UUID");
            player.discard();
            helper.succeed();
        });
    }
}
