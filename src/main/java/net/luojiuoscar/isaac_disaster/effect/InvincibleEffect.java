package net.luojiuoscar.isaac_disaster.effect;


import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InvincibleEffect extends MobEffect {
    protected InvincibleEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public java.util.List<ItemStack> getCurativeItems() {
        return java.util.Collections.emptyList();
    }


    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        // 开启无敌
        if (!player.isInvulnerable()) {
            player.setInvulnerable(true);
        }
    }


    // 当效果结束时触发
    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);

        if (entity instanceof Player player) {
            player.setInvulnerable(false);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

}
