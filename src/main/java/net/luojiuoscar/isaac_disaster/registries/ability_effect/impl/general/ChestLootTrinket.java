package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class ChestLootTrinket implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof GeneralLootModifyEvent event)) return false;
        var objectArrayList = event.getObjectArrayList();
        var lootContext = event.getLootContext();

        if (objectArrayList.isEmpty()) return true;

        Vec3 pos = lootContext.getParamOrNull(LootContextParams.ORIGIN);
        if (pos == null) return true;

        ServerLevel level = lootContext.getLevel();
        if (!(level.getBlockEntity(BlockPos.containing(pos)) instanceof RandomizableContainerBlockEntity)) return true;
        if (!(lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player)) return true;

        RandomSource rand = player.getRandom();
        ObjectArrayList<ItemStack> newList = new ObjectArrayList<>(objectArrayList);

        // lucky toe
        if (rand.nextDouble() < PlayerHelper.getValueFromTrinket(0.33, 0.66, TrinketId.LUCKY_TOE.getId(), player)){
            newList.addAll(LootHelper.getLoot(level, LootTableManager.RANDOM_COINS,
                    new LootParams.Builder(level)
                            .withParameter(LootContextParams.THIS_ENTITY, player)
                            .create(LootContextParamSets.EMPTY)));
        }

        // match stick
        if (rand.nextDouble() < PlayerHelper.getValueFromTrinket(0.33, 0.66, TrinketId.MATCH_STICK.getId(), player)){
            newList.addAll(LootHelper.getLoot(level, LootTableManager.RANDOM_BOMBS,
                    new LootParams.Builder(level)
                            .withParameter(LootContextParams.THIS_ENTITY, player)
                            .create(LootContextParamSets.EMPTY)));
        }

        // childs heart
        if (rand.nextDouble() < PlayerHelper.getValueFromTrinket(0.33, 0.66, TrinketId.CHILDS_HEART.getId(), player)){
            newList.addAll(LootHelper.getLoot(level, LootTableManager.RANDOM_HEARTS,
                    new LootParams.Builder(level)
                            .withParameter(LootContextParams.THIS_ENTITY, player)
                            .create(LootContextParamSets.EMPTY)));        }

        // rusted key
        if (rand.nextDouble() < PlayerHelper.getValueFromTrinket(0.33, 0.66, TrinketId.RUSTED_KEY.getId(), player)){
            newList.addAll(LootHelper.getLoot(level, LootTableManager.RANDOM_KEYS,
                    new LootParams.Builder(level)
                            .withParameter(LootContextParams.THIS_ENTITY, player)
                            .create(LootContextParamSets.EMPTY)));        }

        // ace of spades
        if (rand.nextDouble() < PlayerHelper.getValueFromTrinket(0.33, 0.66, TrinketId.ACE_OF_SPADES_TRINKET.getId(), player)){
            newList.addAll(LootHelper.getLoot(level, LootTableManager.RANDOM_CARDS,
                    new LootParams.Builder(level)
                            .withParameter(LootContextParams.THIS_ENTITY, player)
                            .create(LootContextParamSets.EMPTY)));
        }

        // safety pin
        if (rand.nextDouble() < PlayerHelper.getValueFromTrinket(0.33, 0.66, TrinketId.SAFETY_CAP.getId(), player)){
            newList.addAll(LootHelper.getLoot(level, LootTableManager.RANDOM_PILLS,
                    new LootParams.Builder(level)
                            .withParameter(LootContextParams.THIS_ENTITY, player)
                            .create(LootContextParamSets.EMPTY)));
        }

        // poker chip
        if (PlayerHelper.hasTrinket(TrinketId.POKER_CHIP.getId(), player)){
            if (rand.nextDouble() < -1){
                newList.clear();
            }else{
                newList.addAll(new ObjectArrayList<>(newList));
            }
        }

        event.setObjectArrayList(newList);

        return true;
    }
}
