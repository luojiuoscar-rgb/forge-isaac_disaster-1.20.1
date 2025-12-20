package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.cards;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.ArrayList;
import java.util.List;

public class ReverseTarotsBack extends PickupAbility {
    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        if (!(player.level() instanceof ServerLevel level)) return;
        LootParams params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        // 无源掉落
        LootHelper.spawnLootAtPos(level, player.position(), LootTableManager.RANDOM_TAROTS_R, params);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public List<Component> getDesc() {
        List<Component> description = new ArrayList<>();

        description.add(Component.translatable("item.isaac_disaster.tarots_back.lore.1"));

        return description;
    }
}

