package net.luojiuoscar.isaac_disaster.manager.attack;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.event.custom.misc.IsaacGetBulletCountEvent;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

import java.util.Map;
import java.util.Set;

public interface IAttackType {
    int getId();

    double getPriority();

    default void performAttack(LivingEntity livingEntity, AttackContext context){
        handleAttack(livingEntity, context);
        makeSound(livingEntity);
    }

    void handleAttack(LivingEntity livingEntity, AttackContext context);

    void makeSound(LivingEntity entity);

    class AttackContext {
        public int colorId;
        public Set<Integer> hitEffects;
        public Map<Integer, Integer> trajectories;

        public AttackContext(int colorId, Set<Integer> hitEffects, Map<Integer, Integer> trajectories) {
            this.colorId = colorId;
            this.hitEffects = hitEffects;
            this.trajectories = trajectories;
        }
    }


    default boolean isSpectral(LivingEntity entity){
        if (entity instanceof Player player){
            return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                    .map(a -> a.getSpectral() > 0)
                    .orElse(false);
        }
        return false;
    }

    default boolean isHoming(LivingEntity entity){
        if (entity instanceof Player player){
            return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                    .map(a -> a.getHoming() > 0)
                    .orElse(false);
        }
        return false;
    }

    default boolean isPiercing(LivingEntity entity){
        if (entity instanceof Player player){
            return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                    .map(a -> a.getPiercing() > 0)
                    .orElse(false);
        }
        return false;
    }

    default boolean isControllable(LivingEntity entity){
        if (entity instanceof Player player){
            return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                    .map(a -> a.getControllable() > 0)
                    .orElse(false);
        }
        return false;
    }

    default int getBulletCount(Player player){
        AttributeInstance bulletCount = player.getAttribute(ModAttributes.BULLET_COUNT.get());
        int[] count = {bulletCount == null ? 1 : (int) bulletCount.getValue()};

        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(passive -> {
            int innerEye = passive.getItemCountFromAll(player, ItemId.THE_INNER_EYE.getId());
            int mutantSpider = passive.getItemCountFromAll(player, ItemId.MUTANT_SPIDER.getId());
            int perfectVision = passive.getItemCountFromAll(player, ItemId.PERFECT_VISION.getId());

            if (perfectVision >= 1){
                if (innerEye + mutantSpider == 0){
                    count[0] += 1;
                } else {
                    count[0] += perfectVision - 1;
                }
            }

            if (innerEye + mutantSpider > 0){
                count[0] += innerEye + 2 * mutantSpider + 1;
            }
        });

        IsaacGetBulletCountEvent event = new IsaacGetBulletCountEvent(player, count[0]);
        MinecraftForge.EVENT_BUS.post(event);

        return Math.min(event.getCount(), 17);
    }

    default float getDamage(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        return attr != null ? (float) attr.getValue() : 1f;
    }

    default double getBulletSpeed(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(ModAttributes.BULLET_SPEED.get());
        return attr != null ? Math.max(attr.getValue(), 0.1) : 1.0;
    }

    default double getRange(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(ModAttributes.BULLET_RANGE.get());
        return attr != null ? Math.max(Math.min(attr.getValue(), 64), 1) : 18.0;
    }

    default float getBulletScale(LivingEntity entity) {
        double damage = entity.getAttribute(Attributes.ATTACK_DAMAGE) != null ? entity.getAttribute(Attributes.ATTACK_DAMAGE).getValue() : 1.0;
        AttributeInstance extraAttr = entity.getAttribute(ModAttributes.BULLET_SCALE.get());
        float extra = extraAttr != null ? (float) extraAttr.getValue() : 0f;

        float scale = 1.0f;
        if (damage <= 198) {
            scale *= (float) Math.log10(9 + damage / 2);
        } else {
            scale *= Math.min(4f, (float) (2 + Math.log10((damage - 198) / 20)));
        }
        return scale + extra;
    }
}
