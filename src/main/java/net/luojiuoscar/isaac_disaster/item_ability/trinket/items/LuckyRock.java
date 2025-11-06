package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerSwallowedTrinketsProvider;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITriggerTrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.TrinketId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.TrinketManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class LuckyRock implements ITriggerTrinket {
    @Override
    public int getId() {
        return TrinketId.LUCKY_ROCK.getId();
    }

    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("item.isaac_disaster.lucky_rock.lore.1"));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(Component.translatable("item.isaac_disaster.lucky_rock.enchanted.lore.1")
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
    }

    @Override
    public double getTriggerChance(Player player,  List<ItemStack> stackList) {
        return stackList.stream().anyMatch(Trinket::isEnchanted) ? 0.1 : 0.05;
    }

    @Override
    public void handleTriggerEffect(Player player, List<ItemStack> stackList, Event e){
        if (!(e instanceof BlockEvent.BreakEvent event)) return;
        Vec3 pos = event.getPos().getCenter();
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        LevelHelper.spawnMoney(serverLevel, pos, 1);
    }

    public static void onTriggered(BlockEvent.BreakEvent event){
        Player player = event.getPlayer();

        player.getCapability(PlayerSwallowedTrinketsProvider.PLAYER_SWALLOWED_TRINKETS).ifPresent(
                playerSwallowedTrinkets -> {
                    List<ItemStack> stackList = playerSwallowedTrinkets.getAllTrinketListFromId(player, TrinketId.LUCKY_ROCK.getId());
                    if (!stackList.isEmpty() &&
                            TrinketManager.getInstance().getTrinketFromId(TrinketId.LUCKY_ROCK.getId()) instanceof ITriggerTrinket trinket){
                        trinket.onTrigger(player, stackList, event);
                    }
                });
    }

}
