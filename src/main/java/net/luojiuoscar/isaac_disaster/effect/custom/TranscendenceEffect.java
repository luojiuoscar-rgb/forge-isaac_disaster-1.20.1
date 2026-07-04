package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.helper.FlightHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 一个让玩家在药效期间可以飞行的效果。
 * Forge 1.20.1 适用
 */
public class TranscendenceEffect extends MobEffect {
    public TranscendenceEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /**
     * 每 tick 调用（只在服务端逻辑生效）
     */
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof ServerPlayer player)) return;

        // 如果玩家还没被允许飞行，启用飞行能力
        FlightHelper.grantIsaacFlight(player);
    }

    /**
     * 当效果结束（或被移除）时调用
     */
    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);

        if (!(entity instanceof ServerPlayer player)) return;
        // 只有不能飞时才禁用效果
        FlightHelper.refreshIsaacFlight(player, false);
    }

    /**
     * 让效果每 tick 都触发（否则默认不调用 applyEffectTick）
     */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
