package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.helper.CuriosHelper;
import net.luojiuoscar.isaac_disaster.item.ModTrinkets;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DropPerfection implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;

        List<ItemStack> trinkets = CuriosHelper.getEquippedItemsInSlot(player, CuriosHelper.TRINKET);
        if ((trinkets.stream().anyMatch(Trinket::isEnchanted) ? 0.4 : 0.2) < player.getRandom().nextDouble()) return true;

        if (trinkets.stream().anyMatch(stack -> {
            if (!(stack.getItem() instanceof Trinket item)) return false;
            stack.setCount(0); // 从饰品移除
            return item.getTrinketId() == TrinketId.PERFECTION.getId();})){}
        else{
            // 从cap中删除
            player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(
                    playerPassiveItem -> playerPassiveItem.removeFromId(player, TrinketId.PERFECTION.getId())
            );
        }
        spawnFakePaper(player.level(), player);
        return true;
    }

    private static void spawnFakePaper(Level level, Player player) {
        if (level.isClientSide) return;

        // 创建纸的掉落实体
        ItemStack perfection = new ItemStack(ModTrinkets.PERFECTION.get());
        ItemEntity itemEntity = new ItemEntity(level,
                player.getX(), player.getY() + 1.0, player.getZ(), perfection);

        // 设置掉落方向
        Vec3 look = player.getLookAngle();
        double speed = 0.2;
        itemEntity.setDeltaMovement(look.scale(speed).add(0, 0.1, 0));

        itemEntity.setPickUpDelay(-1);
        itemEntity.lifespan = 15;
        level.addFreshEntity(itemEntity);
    }
}
