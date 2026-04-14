package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GoldenEffect extends MobEffect {
    public GoldenEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity pLivingEntity, int pAmplifier){
        if (pLivingEntity.getType().is(TagManager.BOSSES) || pLivingEntity instanceof Player) {
            pLivingEntity.removeEffect(this);
            return;
        }
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (entity instanceof Mob mob){
            mob.setNoAi(true);
            mob.setSilent(true);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (entity instanceof Mob mob){
            mob.setNoAi(false);
            mob.setSilent(false);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

}
