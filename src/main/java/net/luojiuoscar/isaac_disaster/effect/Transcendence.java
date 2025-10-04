package net.luojiuoscar.isaac_disaster.effect;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 一个让玩家在药效期间可以飞行的效果。
 * Forge 1.20.1 适用
 */
public class Transcendence extends MobEffect {
    public Transcendence(MobEffectCategory category, int color) {
        super(category, color);
    }

    /**
     * 每 tick 调用（只在服务端逻辑生效）
     */
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        // 如果玩家还没被允许飞行，启用飞行能力
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    /**
     * 当效果结束（或被移除）时调用
     */
    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);

        if (!(entity instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        // 只有不能飞时才禁用效果
        if (!player.isCreative() && !player.isSpectator() && !PlayerHelper.canFly(player)) {
            player.getAbilities().flying = false;
            player.getAbilities().mayfly = false;
            player.onUpdateAbilities();
        }
    }

    /**
     * 让效果每 tick 都触发（否则默认不调用 applyEffectTick）
     */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
