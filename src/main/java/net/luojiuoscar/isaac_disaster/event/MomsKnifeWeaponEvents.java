package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.weapon.MomsKnifeWeapon;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.server.ServerStoppingEvent;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public final class MomsKnifeWeaponEvents {
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        for (var level : event.getServer().getAllLevels()) {
            var carried = new ArrayList<PlayerMomsKnifeEntity>();
            for (var entity : level.getAllEntities()) {
                if (entity instanceof PlayerMomsKnifeEntity knife && knife.hasCarriedItem()) carried.add(knife);
            }
            carried.forEach(PlayerMomsKnifeEntity::discard);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMelee(AttackEntityEvent event) {
        if (event.getEntity().getMainHandItem().is(ModItems.MOMS_KNIFE_WEAPON.get())
                && MomsKnifeWeapon.isUnavailable(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onAnvil(AnvilUpdateEvent event) {
        if (event.getLeft().is(ModItems.MOMS_KNIFE_WEAPON.get())
                && !EnchantmentHelper.getEnchantments(event.getRight()).isEmpty()) event.setCanceled(true);
    }
}
