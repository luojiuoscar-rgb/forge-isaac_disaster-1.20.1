package net.luojiuoscar.isaac_disaster.registries.ability.trinket;

import net.luojiuoscar.isaac_disaster.registries.ability.IsaacItemAbility;
import net.minecraft.world.entity.LivingEntity;

public abstract class TrinketAbility extends IsaacItemAbility {

    public TrinketAbility(int id, int level){
        super(id, level);
    }

    public abstract void onFirstEquipped(LivingEntity entity, TrinketAbilityContext ctx);

    public abstract void onEquipped(LivingEntity entity, TrinketAbilityContext ctx);

    public abstract void onUnequipped(LivingEntity entity, TrinketAbilityContext ctx);



}
