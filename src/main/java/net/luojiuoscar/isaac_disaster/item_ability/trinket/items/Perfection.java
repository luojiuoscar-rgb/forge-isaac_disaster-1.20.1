package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerSwallowedTrinketsProvider;
import net.luojiuoscar.isaac_disaster.helper.CuriosHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.IHurtTriggerTrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.TrinketId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class Perfection implements IHurtTriggerTrinket {
    @Override
    public int getId() {
        return TrinketId.PERFECTION.getId();
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.PERFECTION.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("item.isaac_disaster.perfection.lore.1"),
                StatManager.LUCK.description(10));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(Component.translatable("item.isaac_disaster.perfection.enchanted.lore.1")
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
    }

    @Override
    public void onEquipped(LivingEntity entity, boolean isEnchanted, boolean isPermanent){
        if (!(entity instanceof Player player)) return;
        if (isEnchanted) {
            StatManager.LUCK.apply(player, 20);
        } else {
            StatManager.LUCK.apply(player, 10);
        }

    };
    @Override
    public void onUnequipped(LivingEntity entity, boolean isEnchanted, boolean isPermanent){
        if (!(entity instanceof Player player)) return;
        if (isEnchanted) {
            StatManager.LUCK.apply(player, -20);
        } else {
            StatManager.LUCK.apply(player, -10);
        }
    };


    @Override
    public void handleHurtEffect(Player player, Entity attacker, List<ItemStack> stackList, LivingHurtEvent event) {
        List<ItemStack> trinkets = CuriosHelper.getEquippedItemsInSlot(player, CuriosHelper.TRINKET);
        if (trinkets.stream().anyMatch(stack -> {
            if (!(stack.getItem() instanceof Trinket item)) return false;
            stack.setCount(0); // 从饰品移除
            return item.getTrinketId() == 1;})){}
        else{
            // 从cap中删除
            player.getCapability(PlayerSwallowedTrinketsProvider.PLAYER_SWALLOWED_TRINKETS).ifPresent(
                    playerSwallowedTrinkets -> playerSwallowedTrinkets.removeFromId(player, TrinketId.PERFECTION.getId())
            );
        }
        spawnFakePaper(player.level(), player);
    }

    private void spawnFakePaper(Level level, Player player) {
        if (level.isClientSide) return;

        // 创建纸的掉落实体
        ItemStack perfection = new ItemStack(ModItems.PERFECTION.get());
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


    @Override
    public boolean isPunishType() {
        return true;
    }

    @Override
    public double getTriggerChance(Player player, List<ItemStack> stackList) {
        return stackList.stream().anyMatch(Trinket::isEnchanted) ? 0.4 : 0.2;
    }
}
