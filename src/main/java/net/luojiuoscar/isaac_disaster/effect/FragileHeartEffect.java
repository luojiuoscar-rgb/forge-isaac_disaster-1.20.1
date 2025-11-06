package net.luojiuoscar.isaac_disaster.effect;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.UUID;

public class FragileHeartEffect extends MobEffect {
    private static final UUID FRAGILE_HEART_EFFECT_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:fragile_heart_effect").getBytes());

    protected FragileHeartEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player)){
            entity.removeEffect(this);
            return;
        }

        double healthBonus = StatManager.MAX_HEALTH.getBonus() * (amplifier + 1);

        AttributeInstance instance = entity.getAttribute(Attributes.MAX_HEALTH);
        if (instance == null) return;
        instance.addPermanentModifier(
                new AttributeModifier(
                        FRAGILE_HEART_EFFECT_UUID,
                        "fragile_heart_bonus",
                        healthBonus,
                        AttributeModifier.Operation.ADDITION
                )
        );
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        AttributeInstance instance = entity.getAttribute(Attributes.MAX_HEALTH);
        if (instance == null) return;
        instance.removeModifier(FRAGILE_HEART_EFFECT_UUID);
    }

    @Override
    public java.util.List<ItemStack> getCurativeItems() {
        return java.util.Collections.emptyList();
    }

    public static void onTriggered(LivingHurtEvent event){
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getAmount() > Math.max(1.0f, StatManager.MAX_HEALTH.getBonus() * 0.25f)) return;

        double emptyHealth = player.getMaxHealth() - player.getHealth();
        // 当前骨心中有生命值时不消耗
        if (emptyHealth >= StatManager.MAX_HEALTH.getBonus()){
            EntityHelper.addAmplifier(player, ModEffects.FRAGILE_HEART.get(), -1);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.BONE_HEART.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            event.setAmount(0.0f); // 骨心破碎时不额外扣除生命值
        }
    }
}
