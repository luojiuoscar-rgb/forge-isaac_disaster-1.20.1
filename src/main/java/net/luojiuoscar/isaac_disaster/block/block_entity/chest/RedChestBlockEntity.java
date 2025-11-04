package net.luojiuoscar.isaac_disaster.block.block_entity.chest;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.EventHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class RedChestBlockEntity extends ItemChestBlockEntity {

    public RedChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RED_CHEST_BLOCK_ENTITY.get(), pos, state);

        this.setItemLootChance(0.4);
    }

    @Override
    public ResourceLocation getLidResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/red_chest_lid");
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.isaac_disaster.red_chest");
    }

    @Override
    public boolean tryLootItem(ServerLevel serverLevel, Player player, BlockPos pos){
        if (isOpened()) return false;
        if (level == null) return false;

        RandomSource rand = player.getRandom();

        if (rand.nextDouble() < 0.5){ // 50%进行其他事件
            setOpened(true);

            EventHelper.triggerWeightedEvent(serverLevel, player, rand,
                    new EventHelper.EventWeight(() ->{
                        EntityHelper.spawnBomb(pos.getCenter().add(0, 1, 0), null, level, Vec3.ZERO, 2);
                    }, 2),
                    new EventHelper.EventWeight(() -> {
                        EntityHelper.spawnBomb(pos.getCenter().add(0, 1, 0), null, level, Vec3.ZERO, 1);
                        EntityHelper.spawnBomb(pos.getCenter().add(0, 1, 0), null, level, Vec3.ZERO, 1);
                    }, 3),
                    new EventHelper.EventWeight(() ->{
                        CaveSpider s = new CaveSpider(EntityType.CAVE_SPIDER, level);
                        s.setPos(pos.getCenter().add(0, 1, 0));
                        level.addFreshEntity(s);

                        CaveSpider s2 = new CaveSpider(EntityType.CAVE_SPIDER, level);
                        s2.setPos(pos.getCenter().add(0, 1, 0));
                        level.addFreshEntity(s2);
                    }, 2),
                    new EventHelper.EventWeight(() ->{
                        Spider s = new Spider(EntityType.SPIDER, level);
                        s.setPos(pos.getCenter().add(0, 1, 0));
                        level.addFreshEntity(s);

                        Spider s2 = new Spider(EntityType.SPIDER, level);
                        s2.setPos(pos.getCenter().add(0, 1, 0));
                        level.addFreshEntity(s2);
                    }, 3)
            );
            return false;
        }
        return super.tryLootItem(serverLevel, player, pos);
    }

}

