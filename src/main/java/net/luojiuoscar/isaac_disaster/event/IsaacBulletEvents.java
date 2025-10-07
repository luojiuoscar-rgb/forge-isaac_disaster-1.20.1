package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.projectile.IsaacBullet;
import net.luojiuoscar.isaac_disaster.event.custom.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class IsaacBulletEvents {


    @SubscribeEvent
    public static void onBulletShoot(IsaacBulletShootEvent event) {
        LocalPlayer pl = Minecraft.getInstance().player;
        if (pl!= null) pl.sendSystemMessage(Component.literal("shoot"));
    }

    @SubscribeEvent
    public static void onBulletTick(IsaacBulletTickEvent event) {
    }

    @SubscribeEvent
    public static void onBulletHitEntity(IsaacBulletHitEntityEvent event) {
        LocalPlayer pl = Minecraft.getInstance().player;
        if (pl!= null) pl.sendSystemMessage(Component.literal("hit target"));
    }

    @SubscribeEvent
    public static void onBulletDiscard(IsaacBulletDiscardEvent event) {
        LocalPlayer pl = Minecraft.getInstance().player;
        if (pl!= null) pl.sendSystemMessage(Component.literal("discard"));
    }

    @SubscribeEvent
    public static void onBulletHitBlock(IsaacBulletHitBlockEvent event) {
        LocalPlayer pl = Minecraft.getInstance().player;
        if (pl!= null) pl.sendSystemMessage(Component.literal("hit block"));
    }
}
