package net.luojiuoscar.isaac_disaster.manager.attack;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public interface IAttackType {
    int getId();

    double getPriority();

    default void performAttack(LivingEntity livingEntity, int colorId, Set<Integer> hitEffects){
        handleAttack(livingEntity, colorId, hitEffects);
        makeSound(livingEntity);
    }

    void handleAttack(LivingEntity livingEntity, int colorId, Set<Integer> hitEffects);

    void makeSound(LivingEntity entity);


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
}
