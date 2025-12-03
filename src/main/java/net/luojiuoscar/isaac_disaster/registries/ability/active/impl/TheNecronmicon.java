package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.SetId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TheNecronmicon extends ActiveAbility {

    public TheNecronmicon(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstUse(ServerPlayer player, ItemStack stack, @javax.annotation.Nullable InteractionHand hand){
        StatManager.modifySetWithId(player, SetId.BOOK.getId(), 1);
    }

    @Override
    public void onTrigger(ServerPlayer player, ItemStack stack, @javax.annotation.Nullable InteractionHand hand) {
        damageNearby(player, StatManager.DAMAGE.getBonus() * 20);
    }

    @Override
    public void onTriggerStronger(ServerPlayer player, ItemStack stack, @javax.annotation.Nullable InteractionHand hand){
        damageNearby(player, StatManager.DAMAGE.getBonus() * 40);
    }

    @Override
    public void triggerSFX(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), ModSounds.THE_NECRONMICON_USE.get(), SoundSource.PLAYERS);
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
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.the_necronmicon.lore.1"),
                Component.translatable("item.isaac_disaster.the_necronmicon.lore.2")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
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
    public List<Component> getExtraDesc(@Nullable ItemStack stack){
        List<Component> description = new ArrayList<>();

        description.addAll(SetManager.getInstance().getSetFromId(SetId.BOOK.getId()).getExplain());

        return description;
    }
}
