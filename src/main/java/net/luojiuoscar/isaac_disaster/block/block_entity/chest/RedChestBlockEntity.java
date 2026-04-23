package net.luojiuoscar.isaac_disaster.block.block_entity.chest;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.entity.tnt.BombData;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.EventHelper;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
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
    }

    @Override
    public void init(){
        this.setLocked(false);
        this.setItemLootChance(0.1);
        this.setItemLootTable(ModLootTables.RED_CHEST.toString());
    }


    @Override
    public ResourceLocation getPresetLootTable(){
        return ModLootTables.RED_CHEST;
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

        RandomSource rand = serverLevel.getRandom();

        try {
            if (opened) return false;
            setOpened(true);

            if (rand.nextDouble() >= getItemLootChance()){
                if (rand.nextDouble() >= 0.5){
                    openEvent(serverLevel, player, pos);
                    this.clearContent();
                }
                return false;
            }
            boolean s = lootItem(serverLevel, player, pos, ResourceLocation.parse(getItemLootTable()), this::clearContent);
            if (s){
                setDisplayingItem(true);
                return true;
            }
        } catch (Exception e) {
            IsaacDisaster.LOGGER.error("Failed to generate loot for red chests at {} with table {}", worldPosition, getItemLootTable(), e);
            setItemLootTable(DEFAULT_ITEM_LOOT_TABLE);
        }
        return false;
    }

    private void openEvent(ServerLevel serverLevel, Player player, BlockPos pos){
        RandomSource rand = serverLevel.getRandom();
        EventHelper.triggerWeightedEvent(serverLevel, player, rand,
                new EventHelper.EventWeight(() ->{
                    EntityHelper.spawnBomb(pos.getCenter().add(0, 1, 0), null, level, Vec3.ZERO, BombData.MEGA, 80);
                }, 2),
                new EventHelper.EventWeight(() -> {
                    EntityHelper.spawnBomb(pos.getCenter().add(0, 1, 0), null, level, Vec3.ZERO, BombData.NORMAL, 80);
                    EntityHelper.spawnBomb(pos.getCenter().add(0, 1, 0), null, level, Vec3.ZERO, BombData.NORMAL, 80);
                }, 3),
                new EventHelper.EventWeight(() ->{
                    CaveSpider s = new CaveSpider(EntityType.CAVE_SPIDER, serverLevel);
                    s.setPos(pos.getCenter().add(0, 1, 0));
                    serverLevel.addFreshEntity(s);

                    CaveSpider s2 = new CaveSpider(EntityType.CAVE_SPIDER, serverLevel);
                    s2.setPos(pos.getCenter().add(0, 1, 0));
                    serverLevel.addFreshEntity(s2);
                }, 2),
                new EventHelper.EventWeight(() ->{
                    Spider s = new Spider(EntityType.SPIDER, serverLevel);
                    s.setPos(pos.getCenter().add(0, 1, 0));
                    serverLevel.addFreshEntity(s);

                    Spider s2 = new Spider(EntityType.SPIDER, serverLevel);
                    s2.setPos(pos.getCenter().add(0, 1, 0));
                    serverLevel.addFreshEntity(s2);
                }, 3)
        );
    }
}

