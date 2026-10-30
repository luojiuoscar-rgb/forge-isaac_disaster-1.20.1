package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider.BlueSpiderEntity;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarLimitGroups;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

@GameTestHolder("isaac_disaster_blue_spider")
@PrefixGameTestTemplate(false)
public final class BlueSpiderGameTests {
    private static final ResourceLocation FAMILIAR_SOURCE = ResourceLocation.parse("isaac_disaster:command");
    private static final ResourceLocation SPIDER=ResourceLocation.parse("isaac_disaster:blue_spider");
    private static final ResourceLocation FLY=ResourceLocation.parse("isaac_disaster:blue_fly");
    @GameTest(template="trajectory_empty",batch="blue_spider",timeoutTicks=70)
    public static void ownerlessSpiderFallsButUsesFamiliarCleanup(GameTestHelper helper) {
        var floor=helper.absolutePos(new BlockPos(1,0,1));
        helper.setBlock(new BlockPos(1,0,1),Blocks.STONE);
        var spider=ModEntities.BLUE_SPIDER.get().create(helper.getLevel());
        spider.setPos(Vec3.atBottomCenterOf(floor.above(2)));
        helper.getLevel().addFreshEntity(spider);
        helper.runAtTickTime(16,()->helper.assertTrue(spider.onGround()&&!spider.isNoGravity()&&!spider.noPhysics,
                "Ownerless spider still falls onto real terrain"));
        helper.runAtTickTime(45,()->{helper.assertTrue(spider.isRemoved(),"Unowned spider uses familiar cleanup");helper.succeed();});
    }
    @GameTest(template="trajectory_empty",batch="blue_spider")
    public static void commandAndIndependentGroupCapsAreRegistered(GameTestHelper helper) {
        var owner=FakePlayerFactory.get(helper.getLevel(),new GameProfile(UUID.randomUUID(),"spider_limit"));
        owner.setPos(helper.absoluteVec(new Vec3(2,2,2)));
        var data=owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
        try {
            int result=helper.getLevel().getServer().getCommands().performPrefixedCommand(owner.createCommandSourceStack().withPermission(4).withSuppressedOutput(),
                    "isd familiar @s add isaac_disaster:blue_spider 128");
            data.setCount(FLY, FAMILIAR_SOURCE,64);
            helper.assertTrue(result==1&&data.getCount(SPIDER)==64&&data.getEffectiveCount(SPIDER)==64,"Spider request capped at64");
            helper.assertTrue(data.getEffectiveCount(FLY)==64,"Fly quota independent");
            helper.assertTrue(ModFamiliarEntities.BLUE_SPIDER.get().getGroup()==ModFamiliarLimitGroups.SPIDERS.get(),"Spider uses shared spiders group");
            helper.assertTrue(ModFamiliarLimitGroups.SPIDERS.get().getLimit()==64,"Shared spider group capped at64");
            for(int i=0;i<128;i++)helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner),"Each independent slot can spawn");
            helper.assertTrue(!FamiliarHelper.spawnNextMissingFamiliar(owner),"No129th familiar can spawn");
            helper.assertTrue(data.getEntry(SPIDER).orElseThrow().getEntityCount()==64
                    && data.getEntry(FLY).orElseThrow().getEntityCount()==64,"Sixty-four spiders and sixty-four flies coexist");
            helper.succeed();
        } finally {FamiliarHelper.discardAllRuntimeFamiliars(owner);data.clearRequirements();owner.discard();}
    }
}
