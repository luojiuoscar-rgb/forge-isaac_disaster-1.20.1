package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items;

import net.luojiuoscar.isaac_disaster.registries.ability.pickup.FoodPickupAbility;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class SoulHeart extends FoodPickupAbility {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.ABSORPTION)
    ));

    public SoulHeart() {
        this(TRIGGER);
    }

    protected SoulHeart(CompositeTrigger trigger) {
        super(trigger);
    }

    @Override
    public void onUse(ServerPlayer player, @Nullable ItemStack stack, @Nullable InteractionHand hand) {
        float absorptionBefore = player.getAbsorptionAmount();
        super.onUse(player, stack, hand);
        if (PlayerHelper.hasItem(ModPassiveItems.SOUL_LOCKET.getId(), player)) {
            var context = getCtx(player, stack, hand);
            double halfHearts = Math.max(0, player.getAbsorptionAmount() - absorptionBefore) / (StatManager.MAX_HEALTH.getBonus() * 0.5);
            context.set(ContextKeys.DOUBLE, List.of(halfHearts));
            ModExecutableEffects.SOUL_LOCKET.get().apply(context);
        }
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.SOUL_HEART.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}

