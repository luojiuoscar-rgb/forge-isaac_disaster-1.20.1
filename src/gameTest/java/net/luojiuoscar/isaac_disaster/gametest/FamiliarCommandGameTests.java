package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder("isaac_disaster_blue_fly")
@PrefixGameTestTemplate(false)
public final class FamiliarCommandGameTests {
    @GameTest(template="trajectory_empty",batch="familiar_command")
    public static void targetSelectorPrecedesAddRemoveAndClear(GameTestHelper helper) {
        var owner=FakePlayerFactory.get(helper.getLevel(),new GameProfile(UUID.randomUUID(),"familiar_command"));
        owner.setPos(helper.absoluteVec(new Vec3(4,3,4)));
        var data=owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElseThrow();
        var source=owner.createCommandSourceStack().withPermission(2).withSuppressedOutput();
        var commands=helper.getLevel().getServer().getCommands();
        var fly=ResourceLocation.parse("isaac_disaster:blue_fly");
        var spider=ResourceLocation.parse("isaac_disaster:blue_spider");
        try {
            helper.assertTrue(commands.performPrefixedCommand(source,"isd familiar @s add isaac_disaster:blue_fly")==1
                    && data.getCount(fly)==1,"Target-first add defaults to one familiar");
            helper.assertTrue(commands.performPrefixedCommand(source,"isd familiar @s add isaac_disaster:blue_fly 3")==1
                    && data.getCount(fly)==4,"Target-first add accepts an explicit amount");
            helper.assertTrue(commands.performPrefixedCommand(source,"isd familiar @s remove isaac_disaster:blue_fly")==1
                    && data.getCount(fly)==3,"Target-first remove defaults to one familiar");
            helper.assertTrue(commands.performPrefixedCommand(source,"isd familiar @s remove isaac_disaster:blue_fly 2")==1
                    && data.getCount(fly)==1,"Target-first remove accepts an explicit amount");
            helper.assertTrue(commands.performPrefixedCommand(source,"isd familiar @s add isaac_disaster:blue_spider")==1
                    && data.getCount(spider)==1,"The command works for the spider type too");
            helper.assertTrue(FamiliarHelper.spawnNextMissingFamiliar(owner)&&FamiliarHelper.spawnNextMissingFamiliar(owner),
                    "Both remaining requirements spawn live familiars");
            var liveFly=helper.getLevel().getEntity(data.getEntry(fly).orElseThrow().getEntityId(0));
            var liveSpider=helper.getLevel().getEntity(data.getEntry(spider).orElseThrow().getEntityId(0));
            helper.assertTrue(commands.performPrefixedCommand(source,"isd familiar @s clear")==1
                    && data.getCount(fly)==0&&data.getCount(spider)==0,"Target-first clear removes all requested types");
            helper.assertTrue(liveFly!=null&&liveFly.isRemoved()&&liveSpider!=null&&liveSpider.isRemoved(),
                    "Clear immediately discards live familiars as well");
            helper.succeed();
        } finally {
            FamiliarHelper.discardAllRuntimeFamiliars(owner);data.clearRequirements();owner.discard();
        }
    }
}
