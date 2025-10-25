package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class TheNecronmicon implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.THE_NECRONMICON.getId();
    }

    @Override
    public SoundEvent getSound() {
        return ModSounds.THE_NECRONMICON_USE.get();
    }

    @Override
    public void onFirstUse(Player player){
        StatManager.modifySetWithId(player, SetId.BOOK.getId(), 1, true);
    }

    @Override
    public void onTriggeredEffect(Player player) {
        damageNearby(player, StatManager.getDamageBonus() * 20);
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        damageNearby(player, StatManager.getDamageBonus() * 40);
    }

    private void damageNearby(Player player, double amount){
        Level level = player.level();

        if (level.isClientSide) return;

        // 设定范围半径
        double radius = StatManager.getNearbyRange();

        List<LivingEntity> entities = LevelHelper.selectBySphere(level, player.getX(), player.getY(), player.getZ(), radius);

        // 遍历并造成伤害
        for (LivingEntity target : entities) {

            if (target == player) continue;

            // 跳过同队玩家
            if (target instanceof Player otherPlayer) {
                if (player.getTeam() != null && player.isAlliedTo(otherPlayer)) {
                    continue;
                }
            }

            // 造成伤害
            if (target.getMobType() == MobType.UNDEAD){
                target.hurt(level.damageSources().playerAttack(player), (float) amount * 2);
            }else {
                target.hurt(level.damageSources().playerAttack(player), (float) amount);
            }

            // 粒子
            damageParticle(target);
        }
    }

    private void damageParticle(LivingEntity entity){
        if (entity.level() instanceof ServerLevel serverLevel) {
            ParticleOptions particleType = ParticleTypes.LARGE_SMOKE; // 黑色烟雾粒子

            double x = entity.getX();
            double y = entity.getY() + entity.getBbHeight() / 2.0;
            double z = entity.getZ();

            // 向上喷射的黑烟
            for (int i = 0; i < 10; i++) {
                serverLevel.sendParticles(
                        particleType,
                        x, y, z,
                        1,      // 一次一个粒子
                        0, 0.5, 0,// 扩散
                        0.1
                );
            }
        }
    }


    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.THE_NECRONMICON.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.the_necronmicon.lore.1"),
                Component.translatable("item.isaac_disaster.the_necronmicon.lore.2")
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        List<Component> description = new ArrayList<>();

        description.addAll(SetManager.getInstance().getSetFromId(SetId.BOOK.getId()).getSynergyDescription());

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.stronger"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }


        return description;
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.addAll(SetManager.getInstance().getSetFromId(SetId.BOOK.getId()).getExplain());

        return description;
    }
}
