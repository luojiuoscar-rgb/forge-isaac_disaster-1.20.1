package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.ITarot;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TheSun implements ITarot {
    @Override
    public int getItemId() {
        return PickupId.THE_SUN.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        ServerLevel level = (ServerLevel) player.level();
        level.setDayTime(6000); // 正午
        player.setHealth(player.getMaxHealth()); // 满血
        if (player.hasEffect(MobEffects.BLINDNESS)) player.removeEffect(MobEffects.BLINDNESS);
        if (player.hasEffect(MobEffects.DARKNESS)) player.removeEffect(MobEffects.DARKNESS);

        List<LivingEntity> entities = LevelHelper.selectBySphere(level, player.getX(), player.getY(), player.getZ(), StatManager.getNearbyRange());

        for (LivingEntity entity : entities){
            // 友好、有火抗生物不造成伤害
            if (EntityHelper.isFriendly(entity, player) ||
                    entity.fireImmune()) continue;
            // 生成火焰、造成火伤
            EntityHelper.setFireAtEntity(entity);
            entity.hurt(player.damageSources().inFire(), (float) StatManager.DAMAGE.getBonus() * 64f);

            damageParticle(entity);
        }
    }

    @Override
    public void onUseEffectStronger(Player player, ItemStack stack, InteractionHand hand) {
        // 仅伤害翻倍
        ServerLevel level = (ServerLevel) player.level();
        level.setDayTime(6000); // 正午
        player.setHealth(player.getMaxHealth()); // 满血
        if (player.hasEffect(MobEffects.BLINDNESS)) player.removeEffect(MobEffects.BLINDNESS);
        if (player.hasEffect(MobEffects.DARKNESS)) player.removeEffect(MobEffects.DARKNESS);

        List<LivingEntity> entities = LevelHelper.selectBySphere(level, player.getX(), player.getY(), player.getZ(), StatManager.getNearbyRange());

        for (LivingEntity entity : entities){
            // 友好、有火抗生物不造成伤害
            if (EntityHelper.isFriendly(entity, player) ||
                    entity.fireImmune()) continue;
            // 生成火焰、造成火伤
            EntityHelper.setFireAtEntity(entity);
            entity.hurt(player.damageSources().inFire(), (float) StatManager.DAMAGE.getBonus() * 128f);

            damageParticle(entity);
        }
    }

    private void damageParticle(LivingEntity entity){
        if (entity.level() instanceof ServerLevel serverLevel) {
            ParticleOptions particleType = ParticleTypes.FLAME;

            double x = entity.getX();
            double y = entity.getY() + entity.getBbHeight() / 2.0;
            double z = entity.getZ();

            for (int i = 0; i < 10; i++) {
                serverLevel.sendParticles(
                        particleType,
                        x, y, z,
                        1,
                        0, 0.5, 0,
                        0.1
                );
            }
        }
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);
        player.playSound(SoundEvents.FIRECHARGE_USE);
        player.playSound(ModSounds.THE_SUN.get());
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();
        // 基础效果
        description.addAll(List.of(
                Component.translatable("item.isaac_disaster.the_sun.lore.1"),
                Component.translatable("item.isaac_disaster.the_sun.lore.2"),
                Component.translatable("item.isaac_disaster.the_sun.lore.3"),
                Component.translatable("item.isaac_disaster.the_sun.lore.4"),
                Component.translatable("item.isaac_disaster.action.full_health")
                ));

        // 塔罗牌桌布
        if (ClientDataManager.getInstance().getCountFromId(ItemId.TAROT_CLOTH.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.tarot_cloth").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.stronger"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }
}
