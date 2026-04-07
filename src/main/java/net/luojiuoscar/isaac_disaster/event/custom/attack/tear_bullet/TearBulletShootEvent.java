package net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.List;

@Cancelable
public class TearBulletShootEvent extends IsaacAttackEvent {
    private final TearBullet bullet;

    public TearBulletShootEvent(IBulletObject source, Entity indirectSource, ResourceLocation attackType,
                                List<SimpleTrigger> triggers,
                                TearBullet bullet) {
        super(source, indirectSource, attackType, triggers);
        this.bullet = bullet;
    }

    public TearBullet getBullet() {
        return bullet;
    }
}