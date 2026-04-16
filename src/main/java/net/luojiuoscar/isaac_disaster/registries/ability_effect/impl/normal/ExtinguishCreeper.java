package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ExtinguishCreeper implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());
        if (!(entity.level() instanceof ServerLevel level)) return true;

        List<Creeper> creepers = LevelHelper.selectBySquare(
                        entity.level(),
                        pos,
                        StatManager.getNearbyRange(),
                        e -> e instanceof Creeper
                ).stream()
                .map(e -> (Creeper) e)
                .toList();

        for (Creeper creeper : creepers){
            float progress = creeper.getSwelling(0.0F);
            if (progress < 0.8f) continue;

            Vec3 tntPos = creeper.position();
            creeper.discard();

            ItemEntity item = new ItemEntity(
                    level,
                    tntPos.x,
                    tntPos.y,
                    tntPos.z,
                    new ItemStack(Items.TNT)
            );

            item.setDeltaMovement(
                    (level.random.nextDouble() - 0.5) * 0.2,
                    0.2,
                    (level.random.nextDouble() - 0.5) * 0.2
            );
            item.setPickUpDelay(20);
            level.addFreshEntity(item);
        }

        return true;
    }
}
