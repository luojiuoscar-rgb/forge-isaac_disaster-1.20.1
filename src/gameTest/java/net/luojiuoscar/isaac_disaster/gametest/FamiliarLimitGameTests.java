package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder("isaac_disaster_blue_fly")
@PrefixGameTestTemplate(false)
public final class FamiliarLimitGameTests {
    private static final ResourceLocation BLUE_FLY = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "blue_fly");
    private static final ResourceLocation MOM_KNIFE = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "mom_knife");

    @GameTest(template = "trajectory_empty", batch = "familiar_limit")
    public static void commandCapsBlueFliesAndSchedulerNeverCreatesA65th(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "fly_limit"));
        player.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(4, 3, 4)));
        var data = player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
        try {
            int result = level.getServer().getCommands().performPrefixedCommand(
                    player.createCommandSourceStack().withPermission(4),
                    "isd familiar add @s isaac_disaster:blue_fly 128");
            helper.assertTrue(result == 1 && data.getCount(BLUE_FLY) == 64,
                    "Command accepts the player but retains only 64 requested blue flies");
            helper.assertTrue(data.getEffectiveCount(BLUE_FLY) == 64,
                    "The accepted requirement immediately receives 64 shared quota slots");
            for (int i = 0; i < 64; i++) {
                helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(player), "Each accepted fly is spawned");
            }
            helper.assertTrue(!FamiliarHelper.spawnNextMissingFamiliar(player), "Scheduler refuses a 65th fly");
            var entry = data.getEntry(BLUE_FLY).orElseThrow();
            helper.assertTrue(entry.getEntityCount() == 64 && data.getEffectiveCount(BLUE_FLY) == 64,
                    "Exactly 64 UUIDs and shared quota slots are accepted");
            var earliest = entry.getEntityId(0);
            data.setCount(BLUE_FLY, 63);
            var last = level.getEntity(entry.getEntityId(63));
            FamiliarHelper.maintainExistingFamiliars(player);
            helper.assertTrue(last != null && last.isRemoved() && entry.getEntityId(0).equals(earliest),
                    "Reconciliation discards the last entity while retaining the earliest order");
            data.addCount(BLUE_FLY, 100);
            helper.assertTrue(data.getCount(BLUE_FLY) == 64, "Further overflow is dropped");
            data.addCount(BLUE_FLY, -1);
            helper.assertTrue(!FamiliarHelper.spawnNextMissingFamiliar(player), "Dropped overflow cannot respawn later");

            var copy = new PlayerFamiliarData();
            copy.copyFrom(data);
            copy.setCount(BLUE_FLY, 128);
            helper.assertTrue(copy.getCount(BLUE_FLY) == 64 && copy.getEffectiveCount(BLUE_FLY) == 64,
                    "Copied requests honor the own limit and reconstruct shared allocations immediately");
            copy.setCount(MOM_KNIFE, 128);
            helper.assertTrue(copy.getCount(MOM_KNIFE) == 27 && copy.getEffectiveCount(MOM_KNIFE) == 27,
                    "Knives have an independent three-layer limit of 27");
            var unknown = ResourceLocation.fromNamespaceAndPath("pending_addon", "familiar");
            copy.setCount(unknown, 128);
            helper.assertTrue(copy.getCount(unknown) == 128 && copy.getEffectiveCount(unknown) == 0,
                    "Unknown addon requirements are retained without granting runtime capacity");
            CompoundTag overLimitEntry = new CompoundTag();
            overLimitEntry.putString("Type", BLUE_FLY.toString());
            overLimitEntry.putInt("Count", 128);
            ListTag savedEntries = new ListTag();
            savedEntries.add(overLimitEntry);
            CompoundTag savedData = new CompoundTag();
            savedData.put("Familiars", savedEntries);
            copy.loadNBTData(savedData);
            helper.assertTrue(copy.getCount(BLUE_FLY) == 64 && copy.getEffectiveCount(BLUE_FLY) == 64,
                    "Loaded saves obey the blue fly own limit and reconstruct shared allocation immediately");
            helper.succeed();
        } finally {
            FamiliarHelper.discardAllRuntimeFamiliars(player);
            data.clearRequirements();
            data.pruneEmptyEntries();
            player.discard();
        }
    }

    @GameTest(template = "trajectory_empty", batch = "familiar_limit")
    public static void commandCapsKnivesAtThreeLayersWithoutBankingOverflow(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "knife_limit"));
        player.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(4, 3, 4)));
        var data = player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
        try {
            int result = level.getServer().getCommands().performPrefixedCommand(
                    player.createCommandSourceStack().withPermission(4),
                    "isd familiar add @s isaac_disaster:mom_knife 128");
            helper.assertTrue(result == 1 && data.getCount(MOM_KNIFE) == 27
                            && data.getEffectiveCount(MOM_KNIFE) == 27,
                    "Only three layers (5 + 9 + 13 knives) are accepted");
            for (int i = 0; i < 27; i++) {
                helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(player), "Each accepted knife is spawned");
            }
            var entry = data.getEntry(MOM_KNIFE).orElseThrow();
            helper.assertTrue(entry.getEntityCount() == 27 && !FamiliarHelper.spawnNextMissingFamiliar(player),
                    "Scheduler refuses a 28th knife");
            var earliest = entry.getEntityId(0);
            var last = level.getEntity(entry.getEntityId(26));
            data.addCount(MOM_KNIFE, 100);
            data.addCount(MOM_KNIFE, -1);
            FamiliarHelper.maintainExistingFamiliars(player);
            helper.assertTrue(data.getCount(MOM_KNIFE) == 26 && entry.getEntityCount() == 26
                            && last != null && last.isRemoved() && entry.getEntityId(0).equals(earliest)
                            && !FamiliarHelper.spawnNextMissingFamiliar(player),
                    "Overflow is discarded; removing one knife preserves order and never replaces it");
            var copy = new PlayerFamiliarData();
            copy.copyFrom(data);
            copy.addCount(MOM_KNIFE, 100);
            CompoundTag savedData = new CompoundTag();
            copy.saveNBTData(savedData);
            var loaded = new PlayerFamiliarData();
            loaded.loadNBTData(savedData);
            helper.assertTrue(copy.getCount(MOM_KNIFE) == 27 && loaded.getCount(MOM_KNIFE) == 27
                            && loaded.getEffectiveCount(MOM_KNIFE) == 27,
                    "Copied and saved requirements keep the same independent knife limit");
            helper.succeed();
        } finally {
            FamiliarHelper.discardAllRuntimeFamiliars(player);
            data.clearRequirements();
            data.pruneEmptyEntries();
            player.discard();
        }
    }
}
