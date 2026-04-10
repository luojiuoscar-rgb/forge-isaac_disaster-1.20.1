package net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class TearBulletShootEvent extends IsaacAttackEvent {
    private final TearBullet bullet;

    public TearBulletShootEvent(IBulletObject source, Entity indirectSource, ResourceLocation attackType,
                                CompositeTrigger triggers, TearBullet bullet) {
        super(source, indirectSource, attackType, triggers);
        this.bullet = bullet;
    }

    public TearBullet getBullet() {
        return bullet;
    }
}