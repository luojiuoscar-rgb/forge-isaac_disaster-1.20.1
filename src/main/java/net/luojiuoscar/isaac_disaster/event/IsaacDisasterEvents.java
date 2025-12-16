package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerSwallowedTrinketsProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.effect.custom.TheWizEffect;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforeCreateShootEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.*;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.item.pickup.Card;
import net.luojiuoscar.isaac_disaster.item.pickup.Pill;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.TrinketId;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.BingeEater;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.EchoChamber;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.GlitchedCrown;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IsaacDisasterEvents {

    @SubscribeEvent
    public static void onPacManEat(PacManEatEvent event) {
        LivingEntity eatenEntity = event.getEatenEntity();
        Player player = event.getPlayer();
        Level level = player.level();

        if (eatenEntity instanceof Creeper creeper){
            creeper.explodeCreeper();
        }
        if (eatenEntity instanceof EnderMan || eatenEntity instanceof Endermite){
            PlayerHelper.teleportToRandomLocation(player, 10);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    @SubscribeEvent
    public static void onActiveItemUse(ActiveItemUseEvent event){
        Player player = event.getPlayer();

        player.getCapability(PlayerSwallowedTrinketsProvider.PLAYER_SWALLOWED_TRINKETS).ifPresent(
                playerSwallowedTrinkets -> {
                    List<ItemStack> stackList = playerSwallowedTrinkets.getAllTrinkets(player);

                    // 损坏的遥控器
                    if (stackList.stream().anyMatch(stack -> stack.getItem() instanceof Trinket trinket &&
                            trinket.getTrinketId() == TrinketId.BROKEN_REMOTE.getId())){
                        List<ItemStack> s = playerSwallowedTrinkets.getAllTrinketListFromId(player, TrinketId.BROKEN_REMOTE.getId());
                        if (s.stream().anyMatch(Trinket::isEnchanted)){
                            PlayerHelper.teleportToRandomLocation(player, StatManager.getNearbyRange() * 6);
                        }else{
                            PlayerHelper.teleportToRandomLocation(player, StatManager.getNearbyRange() * 3);
                        }
                    }
                });
    }

    @SubscribeEvent
    public static void onItemDisplayAdd(ItemDisplayAddEvent event){
        if (event.getLevel().isClientSide) return;
        ServerPlayer player = (ServerPlayer) event.getPlayer();

        if (PlayerHelper.hasItem(ItemId.GLITCHED_CROWN.getId(), player)){
            GlitchedCrown.addDisplayItem(event);
        }
        if (PlayerHelper.hasItem(ItemId.BINGE_EATER.getId(), player)){
            BingeEater.addDisplayItem(event);
        }

    }

    @SubscribeEvent
    public static void onPickupUse(PickupUseEvent event){
        ItemStack stack = event.getStack();
        Player player = event.getPlayer();
        if (stack == null || player == null || player.level().isClientSide) return;

        if ((stack.getItem() instanceof Pill || stack.getItem() instanceof Card) &&
                PlayerHelper.hasItem(ItemId.ECHO_CHAMBER.getId(), (ServerPlayer) player)){
            EchoChamber.onTriggered(player);
        }
    }

    @SubscribeEvent
    public static void onGetBulletCount(IsaacGetBulletCountEvent event){
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        RandomSource rand = player.getRandom();
        int count = event.getCount();

        if (PlayerHelper.hasSet(ModSetAbility.BOOK.getId(), player) && rand.nextDouble() < 0.25){
            count++;
        }

        if (player.hasEffect(ModEffects.THE_WIZ.get())){
            count = Math.min(count, 8);  // 最大为 8(*2)
        }

        event.setCount(count);
    }

    @SubscribeEvent
    public static void onCreateShootEvent(BeforeCreateShootEvent event){
        if (event.getShooter().hasEffect(ModEffects.THE_WIZ.get())){
            TheWizEffect.onTriggered(event);
        }
    }


}
