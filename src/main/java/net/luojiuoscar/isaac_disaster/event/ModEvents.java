package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItem;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.commands.clearPassiveItemsCommand;
import net.luojiuoscar.isaac_disaster.commands.showPassiveItemsCommand;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class ModEvents {
    /**
     * 首次给玩家创建Capability相关数据
     */
    @SubscribeEvent
    public static void onAttachCapabilityPlayer(AttachCapabilitiesEvent<Entity> event){
        if(event.getObject() instanceof Player){
            //passive item
            if(!event.getObject().getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "player_passive_item_cap"), new PlayerPassiveItemProvider());
            }
            //stat manager
            if(!event.getObject().getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "player_stat_manager_cap"), new PlayerStatModifierProvider());
            }
        }
    }


    /**
     * 死后保留
     */
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event){
        if(event.isWasDeath()){
            // 玩家的cap信息会在死亡后删除。需要用到reviveCaps恢复。复制完后再删除
            event.getOriginal().reviveCaps();
            // passive item
            event.getOriginal().getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
            // stat manager
            event.getOriginal().getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore, event.getEntity()); //同时传入新生成的玩家实例
                });
            });
            event.getOriginal().invalidateCaps();
        }
    }


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event){

    }


    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event){
        new showPassiveItemsCommand(event.getDispatcher());
        new clearPassiveItemsCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
}
